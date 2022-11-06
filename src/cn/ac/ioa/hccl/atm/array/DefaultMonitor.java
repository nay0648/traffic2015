package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Perform multichannel stft for the array output.
 * @author nay0648
 */
public class DefaultMonitor extends MonitorArray
{
private static final long serialVersionUID = -1582639816045753944L;
private ShortTimeFourierTransformer stfter;
private ShortTimeFourierTransformer.STFTIterator stftit=null;
private long frameidx=0;//frame index
/*
 * the stft buffer
 */
private ComplexVector[] stftframe;
private double[] stftnorm;//norm of the each stft buffer
private double avgstftnorm;//average norm
/*
 * used to calculate average energy
 */
private int ebufftime=Param.CORR_TIME_WINDOW*2;//buffer size for averaging energy (ms)
private double[] ebuffer;
private int eidx=0;//loop buffer index
private double sume=0;//partial sum energy

	/**
	 * construct test array
	 * @throws IOException
	 */
	public DefaultMonitor() throws IOException
	{
		this(Param.DEFARRAYGEOM,Param.DEFFS,Param.FRAMESHIFT,Param.FFTSIZE);
	}
		
	/**
	 * @param arraygeom
	 * array layout
	 * @param fs
	 * sampling rate
	 * @param frameshift
	 * frame shift
	 * @param fftsize
	 * fft size
	 * @throws IOException
	 */
	public DefaultMonitor(File arraygeom,double fs,int frameshift,int fftsize) throws IOException
	{
		super(arraygeom,fs,frameshift,fftsize);
		
		stfter=new ShortTimeFourierTransformer(this.frameShift(),this.fftSize());
		stfter.setAnalysisWindow(ShortTimeFourierTransformer.hannWindow(this.fftSize()));
		
		ebuffer=new double[this.numFramesInTimeInterval(ebufftime)];
	}
	
	public long frameIndex() 
	{
		return frameidx;
	}
	
	public boolean hasNextFrame() throws IOException
	{
		//initialization
		if(stftit==null) 
		{	
			stftit=stfter.stftIterator(this.getSignalSource());
			
			stftframe=new ComplexVector[stftit.numChannels()];
			for(int m=0;m<stftframe.length;m++) 
				stftframe[m]=new ComplexVector(stfter.fftSize());
			
			stftnorm=new double[stftit.numChannels()];
		}
		
		if(stftit.next()) 
		{
		double[] real,imag;
		double temp;
		
			/*
			 * cache all stft frames
			 */
			avgstftnorm=0;
			for(int m=0;m<stftframe.length;m++) 
			{
				//perform stft and copy data into buffer
				stftframe[m].copy(stftit.stftFrame(m));
				
				/*
				 * calculate norm
				 */
				real=stftframe[m].real();
				imag=stftframe[m].imaginary();
				stftnorm[m]=0;
				
				for(int fi=0;fi<real.length/2+1;fi++) 
				{
					temp=real[fi]*real[fi]+imag[fi]*imag[fi];
					if(fi>0&&fi<real.length/2) stftnorm[m]+=2*temp;
					else stftnorm[m]+=temp;
				}
				
				stftnorm[m]=Math.sqrt(stftnorm[m]);//norm of stft frame at channel m
				avgstftnorm+=stftnorm[m];
			}
			
			avgstftnorm/=stftnorm.length;//average stft frame norm of all channels
			
			frameidx++;
			return true;
		}
		else return false;
	}
	
	public ComplexVector arrayFrame(int fi,ComplexVector arrayin)
	{
	ComplexVector tempframe;
	double stnorm;
	
		if(arrayin==null) arrayin=new ComplexVector(this.numElements());
		else BLAS.checkDestinationSize(arrayin, this.numElements());
				
		//get frame data for a specified frequency bin
		for(int m=0;m<arrayin.size();m++) 
		{
			//get the stft frame for the m'th sensor
			tempframe=stftframe[m];
			
			/*
			 * only get the fi'th frequency bin
			 */
			//used to adjust short time energy of all channel to the same level
//			stnorm=avgstftnorm/stftnorm[m];
			
			
			
			
			
			
			
			
			
			
			
			/*
			 * not compatible with LS beamforming!!!
			 */
			stnorm=1;
			
			
			
			
			
			
			
			
			
			
			
			arrayin.setValue(
					m, 
					tempframe.getReal(fi)*stnorm, 
					tempframe.getImaginary(fi)*stnorm);
		}
		
		/*
		 * adjust frame energy
		 */
		//average norm of current stft frames
		sume-=ebuffer[eidx];
		sume+=avgstftnorm;
		ebuffer[eidx]=avgstftnorm;
		if(++eidx>=ebuffer.length) eidx=0;
		
//		BLAS.scalarMultiply(
				//fft size is used for energy conservation between time and frequency domain
//				(sume/(ebuffer.length*Math.sqrt(this.fftSize())))/BLAS.norm2(arrayin), 
//				arrayin, 
//				arrayin);
		
		return arrayin;
	}
}
