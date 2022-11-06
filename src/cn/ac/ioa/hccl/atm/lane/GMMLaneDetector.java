package cn.ac.ioa.hccl.atm.lane;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Detect lanes by Gaussian mixture model.
 * @author nay0648
 */
public class GMMLaneDetector extends LaneDetector
{
private static final long serialVersionUID = 8912448992735425608L;
private static final int NUM_POSDATA=1000;//number of position data points
private static final int NUM_RETRY=10;//number of gmm retries
//number of initial points skipped to make the AGC stable
private static final int NUM_SKIP=10;
private LanePosition[] lanes;//the detected lanes
private double[] laneres;//lane response
private int[] posdata;//vehicle position data
private int posidx=0;//vehicle position index
private int poscount=0;//number of data
private int skipcount=0;//count the skipped data
private int dfradius=2;//detector filter radius

	/**
	 * @param slicer
	 * slice generator reference
	 * @param numlanes
	 * number of lanes
	 */
	public GMMLaneDetector(SliceGenerator slicer,int numlanes)
	{
	super(slicer,numlanes);
	
		lanes=new LanePosition[this.numLanes()];
		laneres=new double[slicer.numScans()];
		posdata=new int[NUM_POSDATA];
	}
	
	public LanePosition lanePosition(int index) 
	{
		return lanes[index];
	}

	public double[] laneResponse() 
	{
		return laneres;
	}
	
	public void accumulateLaneInfo(int maxidx) 
	{
		//skip data to wait AGC become stable
		if(skipcount<NUM_SKIP) skipcount++; 
		else
		{
			posdata[posidx++]=maxidx;
			if(posidx>=posdata.length) posidx=0;
		
			if(poscount<posdata.length) poscount++;
		}
	}
	
	public void detectLanes()
	{
	int[] dataset;
	GaussianMixtureModel1D gmm;
	int lanedetected;
		
		//copy data
		if(poscount<posdata.length) 
		{
		int i2;
		
			dataset=new int[poscount];
			for(int i=0;i<dataset.length;i++) 
			{
				i2=posidx-poscount;
				if(i2<0) i2+=posdata.length;
				
				i2+=i;
				if(i2>=posdata.length) i2-=posdata.length;
				
				dataset[i]=posdata[i2];
			}
		}
		else dataset=posdata;
			
		//generate lane response curve by gmm
		for(int retrycount=0;retrycount<NUM_RETRY;retrycount++) 
		{
			gmm=new GaussianMixtureModel1D(this.numLanes());
			gmm.trainModel(dataset, true);
			for(int azidx=0;azidx<laneres.length;azidx++) 
				laneres[azidx]=gmm.probabilityDensity(azidx);
			
//			Util.plotSignals(laneres);
			
			//detect lanes from the probability density curve
			detectLanesByPeakValley();
			
			/*
			 * see if retry is needed
			 */
			lanedetected=0;
			for(int i=0;i<lanes.length;i++) if(lanes[i]!=null) lanedetected++;
			if(lanedetected>=lanes.length) break;
		}
	}
	
	/**
	 * Used to detect lanes.
	 * @author nay0648
	 *
	 */
	private class LanePeak implements Comparable<LanePeak>
	{
	int peakidx;
	double peakval;
	
		private LanePeak(int peakidx,double peakval)
		{
			this.peakidx=peakidx;
			this.peakval=peakval;
		}
	
		public int compareTo(LanePeak o) 
		{
			if(peakval>o.peakval) return -1;
			else if(peakval<o.peakval) return 1;
			else return 0;
		}
	}
		
	/**
	 * detect lanes by peak-valley method
	 */
	private void detectLanesByPeakValley() 
	{
	double[] peakres,valleyres;
	List<LanePeak> peakval;
	List<LanePosition> templanes;
	double[] scanaz;
					
		/*
		 * detect peaks and valleys
		 */
		peakres=new double[laneres.length];
		valleyres=new double[laneres.length];
					
		for(int i=0;i<laneres.length;i++) 
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
			if(i2>laneres.length-1) i2=laneres.length-1;
						
			for(int ii=i1;ii<=i2;ii++) 
			{
				//find peaks
				if(laneres[ii]>lmax) 
				{
					lmax=laneres[ii];
					lmaxi=ii;
				}	
				//find valleys
				if(laneres[ii]<lmin)
				{
					lmin=laneres[ii];
					lmini=ii;
				}
			}
						
			if(lmaxi==i) peakres[i]=laneres[i];
			if(lmini==i) valleyres[i]=laneres[i];
		}
		
		/*
		 * find lanes
		 */
		/*
		 * select numLanes() of largest lanes
		 */
		peakval=new ArrayList<LanePeak>(numLanes()*2);
		for(int i=0;i<peakres.length;i++) 
			if(peakres[i]>0) peakval.add(new LanePeak(i,peakres[i]));
		Collections.sort(peakval);
					
		templanes=new ArrayList<LanePosition>(numLanes());
		scanaz=this.scanningAzimuth();
		for(int i=0;i<Math.min(numLanes(), peakval.size());i++) 
		{
		int posi,bi1,bi2;
				
			posi=peakval.get(i).peakidx;
			if(posi<0) posi=0;
			else if(posi>scanaz.length-1) posi=scanaz.length-1;
						
			for(bi1=posi;bi1>=0;bi1--) if(valleyres[bi1]>0) break;
			if(bi1<0) bi1=0;
			else if(bi1>scanaz.length-1) bi1=scanaz.length-1;
						
			for(bi2=posi;bi2<valleyres.length;bi2++) if(valleyres[bi2]>0) break;
			if(bi2<0) bi2=0;
			else if(bi2>scanaz.length-1) bi2=scanaz.length-1;
						
			templanes.add(new LaneDetector.LanePosition(posi,bi1,bi2));
		}
					
		/*
		 * sort lanes according to position
		 */
		Collections.sort(templanes);
		Arrays.fill(lanes, null);
		for(int i=0;i<Math.min(numLanes(), templanes.size());i++) 
			lanes[i]=templanes.get(i);
	}
}
