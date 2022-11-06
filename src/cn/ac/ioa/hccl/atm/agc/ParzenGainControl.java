/**
 * Created on: 2014Äê12ÔÂ26ÈÕ
 */
package cn.ac.ioa.hccl.atm.agc;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Utilizing Parzen window technique to estimate the PDF of vehicle presents and absences, 
 * then using the PDF to detect the normalization thresholds.
 * 
 * @author nay0648
 */
public class ParzenGainControl extends AutomaticGainControl
{
private static final long serialVersionUID = 4657152503234343128L;
private static final int PDF_SIZE=200;//from 0 to 149 dB, with 1 dB increment
private static final double DEFAULT_SIGMA=2;//standard deviation of the Gaussian
private static final double DEFAULT_UPDATE_WEIGHT=0.001;//default PDF updating weight
private static final double DEFAULT_UNIMODAL_THDIFF=6;//default thdiff for unimodal PDF when thdiff is not set
private static final double MIN_FOREGROUND_THRESHOLD=3;//min fgth (dB)
private MonitorArray monitor;//monitor reference
private long localms;//local interval size (ms)
private double localmax;//local max value
private int localreached=0;//number of added local max
private long locallimit;//time limit to find a new local max
private double[] gaussian;//the Gaussian function
private double[] pdf;//the probability density function
private double updatew;//pdf will be updated as (1-w)*oldpdf+w*newpdf
private boolean fgonly=false;//true means only use foreground threshold for the normalization
private double fgth=0;//the threshold for agc
private double bgth=0;//the threshold for denoise
private double thdiff=0;//the difference between fgth and bgth, 0 to auto set
private int thdetectduration=1;//local max count for threshold detection
private int dfradius=3;//detector filter radius
	
	/**
	 * use default parameters
	 * @param monitor
	 * array reference
	 */
	public ParzenGainControl(MonitorArray monitor)
	{
		this(monitor,Param.AGC_LOCALINTERVAL,DEFAULT_SIGMA,DEFAULT_UPDATE_WEIGHT);
	}
	
	/**
	 * @param monitor
	 * array reference
	 * @param localms
	 * local interval size (ms)
	 * @param sigma
	 * the standard deviation of the Gaussian window
	 * @param updatew
	 * update weight w, the PDF will be updated as: (1-w)*oldpdf+w*newpdf
	 */
	public ParzenGainControl(MonitorArray monitor,long localms,double sigma,double updatew)
	{
		this.monitor=monitor;
		this.localms=localms;
		this.updatew=updatew;
		
		localmax=0;
		locallimit=monitor.frameTime()+localms;
			
		gaussian=gaussian(sigma);
		pdf=new double[PDF_SIZE];
	}

	/**
	 * generate a 1D Gaussian function with zero means
	 * @param sigma
	 * standard deviation
	 * @return
	 */
	private double[] gaussian(double sigma)
	{
	double[] g;
	double temp;
	
		g=new double[2*(int)Math.ceil(3*sigma)+1];
		
		for(int i=0;i<g.length;i++) 
		{
			temp=(i-g.length/2)/sigma;		
			g[i]=Math.exp(-temp*temp/2.0)/(Math.sqrt(2.0*Math.PI)*sigma);
		}
		
		return g;
	}

	/**
	 * to see if only use the foreground threshold for the normalization
	 * @return
	 */
	public boolean getForegroundOnly()
	{
		return fgonly;
	}
	
	/**
	 * set if only use the foreground threshold for the normalization
	 * @param fgonly
	 * true to use the foreground threshold only
	 */
	public void setForegroundOnly(boolean fgonly)
	{
		this.fgonly=fgonly;
	}
	
	/**
	 * get the difference requirement between the foreground 
	 * threshold and the background threshold
	 * @return
	 * 0 to auto detect
	 */
	public double getThresholdDiff()
	{
		return thdiff;
	}
	
	/**
	 * set the difference requirement between the foreground 
	 * threshold and the background threshold
	 * @param thdiff
	 * difference (dB), 0 to auto detect
	 */
	public void setThresholdDiff(double thdiff)
	{
		this.thdiff=thdiff;
	}

	public double normalize(double gain) 
	{
	long t;
		
		//values smaller than 0 are discarded
		if(gain<=0) return 0;
		t=monitor.frameTime();
		
		//update local max
		if(gain>localmax) localmax=gain;
		
		//add a new local maximum
		if(t>=locallimit) 
		{
		int radius,idx,i1,i2;
		
			/*
			 * accumulate new data
			 */
			radius=gaussian.length/2;
			
			idx=(int)Math.round(localmax);
			i1=idx-radius;
			if(i1<0) i1=0;
			i2=idx+radius;
			if(i2>pdf.length-1) i2=pdf.length-1;
			
			BLAS.scalarMultiply(1-updatew, pdf, pdf);
			for(int i=i1;i<=i2;i++) pdf[i]+=updatew*gaussian[i-idx+radius];
			
			/*
			 * clear current local peak
			 */
			localmax=0;
			locallimit=t+localms;
			
			/*
			 * select the threshold
			 */
			localreached++;
			
			if(localreached>=thdetectduration) 
			{
				updateThreshold();
				localreached=0;
				
				//perform more threshod selection at the beginning
				if(thdetectduration<Param.AGC_UPDATE_THRESHOLD)
				{
					thdetectduration*=2;
					if(thdetectduration>Param.AGC_UPDATE_THRESHOLD) 
						thdetectduration=Param.AGC_UPDATE_THRESHOLD;	
				}
			}
		}
		
		//perform normalization
		{
		double fth,bth;
		
			if(localmax>fgth) fth=localmax;
			else fth=fgth;
			
			if(thdiff<=0) bth=bgth;
			else 
			{	
				bth=fth-thdiff;
				if(bth<0) bth=0;
			}
			
			if(fgonly) gain=gain/fth;
			else gain=(gain-bth)/(fth-bth);
			
			if(gain<0) gain=0;
			else if(gain>1) gain=1;
			
			if(Double.isNaN(gain)) return 0;
			else return gain;
		}
	}
	
	/**
	 * update the fg and bg thresholds
	 */
	private void updateThreshold()
	{
	double[] peakres,valleyres;
	List<PDFPeak> peakval;
		
		/*
		 * detect peaks and valleys
		 */
		peakres=new double[pdf.length];
		valleyres=new double[pdf.length];
									
		for(int i=0;i<pdf.length;i++) 
		{
		int i1,i2;
		double lmax=0,lmin=Double.MAX_VALUE;
		int lmaxi=0,lmini=0;
							
			/*
			 * detecting boundaries
			 */
			i1=i-dfradius;
			if(i1<0) i1=0;
			i2=i+dfradius;
			if(i2>pdf.length-1) i2=pdf.length-1;
			
			for(int ii=i1;ii<=i2;ii++) 
			{
				//find peaks
				if(pdf[ii]>lmax) 
				{
					lmax=pdf[ii];
					lmaxi=ii;
				}	
				//find valleys
				if(pdf[ii]<lmin)
				{
					lmin=pdf[ii];
					lmini=ii;
				}
			}
								
			if(lmaxi==i) peakres[i]=pdf[i];
			if(lmini==i) valleyres[i]=pdf[i];
		}
		
		/*
		 * select useful peaks
		 */
		peakval=new ArrayList<PDFPeak>(20);
		for(int i=0;i<peakres.length;i++) 
			if(peakres[i]>0) peakval.add(new PDFPeak(i,peakres[i]));
		Collections.sort(peakval);
		
		//select the threshold
		if(peakval.isEmpty()) 
		{
			fgth=0;
			bgth=0;
		}
		//unimodal PDF
		else if(peakval.size()==1) unimodalApproach(peakval.get(0).peakidx);
		//bimodal PDF
		else bimodalApproach(peakval.get(0), peakval.get(1), valleyres);
	}
	
	/**
	 * unimodal threshold selection policy
	 * @param peakidx
	 * unimodal peak index
	 */
	private void unimodalApproach(int peakidx)
	{
		fgth=peakidx;
		if(fgth<MIN_FOREGROUND_THRESHOLD) fgth=MIN_FOREGROUND_THRESHOLD;
		
		if(thdiff<=0) bgth=fgth-DEFAULT_UNIMODAL_THDIFF;
		else bgth=fgth-thdiff;
		
		if(bgth<0) bgth=0;
	}
	
	/**
	 * bimodal threshold selection policy
	 * @param p0
	 * the largest peak
	 * @param p1
	 * the second largest peak
	 * @param valleyres
	 * the valleys
	 */
	private void bimodalApproach(PDFPeak p0, PDFPeak p1, double[] valleyres)
	{
	PDFPeak pfg,pbg;	
		
		if(p0.peakidx>=p1.peakidx) 
		{
			pfg=p0;
			pbg=p1;
		}
		else 
		{
			pfg=p1;
			pbg=p0;
		}
		
		//refuse too small peaks, still considered as the unimodal
		if(pfg.peakval<=0.1*pbg.peakval) unimodalApproach(pbg.peakidx);
		else
		{
			fgth=pfg.peakidx;
			if(fgth<MIN_FOREGROUND_THRESHOLD) fgth=MIN_FOREGROUND_THRESHOLD;
			
			//automatically find the background threshold
			if(thdiff<=0) 
			{
				for(int idx=pbg.peakidx;idx<valleyres.length;idx++) 
					if(valleyres[idx]>0) 
					{	
						bgth=idx;
						break;
					}
				
				//not allowed
				if(bgth>=fgth) unimodalApproach(pfg.peakidx);
			}
			//use difference to find background threshold
			else 
			{
				bgth=fgth-thdiff;
				if(bgth<0) bgth=0;
			}
		}
	}
	
	/**
	 * plot the estimated PDF for experiments.
	 */
	public void plotPDF()
	{
		Util.plotSignals(pdf);
	}
	
	/**
	 * get foreground threshold
	 * @return
	 */
	public double foregroundThreshold()
	{
		return fgth;
	}
	
	/**
	 * get background threshold
	 * @return
	 */
	public double backgroundThreshold()
	{
		return bgth;
	}
	
	/**
	 * Used to detect peaks in PDF.
	 * @author nay0648
	 */
	private class PDFPeak implements Comparable<PDFPeak>
	{
	int peakidx;
	double peakval;
	
		private PDFPeak(int peakidx,double peakval)
		{
			this.peakidx=peakidx;
			this.peakval=peakval;
		}
	
		public int compareTo(PDFPeak o) 
		{
			if(peakval>o.peakval) return -1;
			else if(peakval<o.peakval) return 1;
			else return 0;
		}
	}
}
