package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Supports instantaneously fractional delay (anticipation).
 * @author nay0648
 *
 */
public class DelayedSignalSource extends SignalSource
{
private SignalSource ss;//underlying signal source
private ShortTimeFourierTransformer stfter;
private ShortTimeFourierTransformer.STFTIterator stftit;
private double[] delay;//number of delayed samples for each channel
private ComplexVector[] df;//the delay filter for each channel
/*
 * for ifft
 */
private ComplexVector[] ifftbuffer;//for the ifft for each channel
private ComplexVector[] overlapbuffer;//for the overlapped part for each channel
/*
 * for the delayed signal source
 */
private int spointer;//signal pointer
private boolean stftframeleft=true;//to identify if there are still stft frames unprocessed

	/**
	 * @param ss
	 * underlying signal source
	 * @param fftsize
	 * fft size, the maximum delay allowed is fftsize/4
	 * @throws IOException 
	 */
	public DelayedSignalSource(SignalSource ss,int fftsize) throws IOException
	{
	double[] w1,w2;
	
		this.ss=ss;
		
		stfter=new ShortTimeFourierTransformer(fftsize/4,fftsize);
		stftit=stfter.stftIterator(ss);
		
		/*
		 * generate and set the window
		 */
		w1=ShortTimeFourierTransformer.hannWindow(fftsize/2);
		w2=new double[fftsize];
		System.arraycopy(w1, 0, w2, fftsize/4, w1.length);
		stfter.setAnalysisWindow(w2);
		
		delay=new double[ss.numChannels()];
		df=new ComplexVector[ss.numChannels()];
		for(int m=0;m<df.length;m++) df[m]=new ComplexVector(fftsize/2+1);
		
		setDelay(0);
		
		/*
		 * initialize ifft buffer
		 */
		ifftbuffer=new ComplexVector[ss.numChannels()];
		overlapbuffer=new ComplexVector[ss.numChannels()];
		for(int n=0;n<ifftbuffer.length;n++) 
		{	
			ifftbuffer[n]=new ComplexVector(stfter.fftSize());
			overlapbuffer[n]=new ComplexVector(stfter.stftOverlap());
		}
		
		spointer=stfter.stftShift();
	}
	
	/**
	 * with the maximum allowed delay set to 128
	 * @param ss
	 * underlying signal source
	 * @throws IOException 
	 */
	public DelayedSignalSource(SignalSource ss) throws IOException
	{
		this(ss,512);
	}
	
	/**
	 * get the maximum delay (no. of samples) allowed
	 * @return
	 */
	public double maxDelay()
	{
		return stfter.fftSize()/4;
	}
	
	/**
	 * get the delayed no. of samples
	 * @param chidx
	 * channel index
	 * @return
	 */
	public double getDelay(int chidx)
	{
		return delay[chidx];
	}
	
	/**
	 * set the delay
	 * @param chidx
	 * channel index
	 * @param delay
	 * no. of samples, negative delay means anticipation
	 */
	public void setDelay(int chidx,double delay)
	{
	double temp;
		
		if(Math.abs(delay)>maxDelay()) throw new IllegalArgumentException(
				"delay out of bounds: "+delay+", [-"+maxDelay()+", "+maxDelay()+
				"], set longer FFT size to support longer delay.");
		
		this.delay[chidx]=delay;
		//generate the delay filter
		for(int fi=0;fi<df[chidx].size();fi++) 
		{
			temp=2.0*Math.PI*fi*delay/stfter.fftSize();	
			df[chidx].setValue(fi, Math.cos(temp), -Math.sin(temp));
		}
	}
	
	/**
	 * set all channel's delay as the same value
	 * @param delay
	 * no. of samples, negative delay means anticipation
	 */
	public void setDelay(double delay)
	{
		for(int m=0;m<this.numChannels();m++) setDelay(m,delay);
	}

	public int numChannels() 
	{
		return ss.numChannels();
	}
	
	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		//no buffered samples available
		if(spointer>=stfter.stftShift()) 
		{
			if(!stftframeleft) throw new EOFException();
			else delay();//delay next frame
		}
		
		this.checkFrameSize(frame.length);
		for(int n=0;n<frame.length;n++) 
			frame[n]=ifftbuffer[n].getReal(spointer);
		spointer++;
	}
	
	/**
	 * perform frequency domain delay
	 * @throws IOException
	 */
	private void delay() throws IOException
	{
	ComplexVector stftframe;
		
		if(stftit.next()) 
		{
			//perform time delay channel by channel
			for(int n=0;n<ss.numChannels();n++) 
			{
				//get stft result
				stftframe=stftit.stftFrame(n);
				
				//perform delay in frequency domain
				for(int fi=0;fi<stfter.fftSize()/2+1;fi++) 
				{
				double a,b,c,d,real,imag;
					
					a=df[n].getReal(fi);
					b=df[n].getImaginary(fi);
					c=stftframe.getReal(fi);
					d=stftframe.getImaginary(fi);
					
					real=a*c-b*d;
					imag=a*d+b*c;
					
					ifftbuffer[n].setValue(fi, real, imag);
					//its complex conjugate counterpart
					if(fi>0&&fi<stfter.fftSize()/2) 
						ifftbuffer[n].setValue(stfter.fftSize()-fi, real, -imag);
				}
				
				//perform istft
				{
				double[] real,or;
				
					//perform ifft
					FourierTransformer.ifft(ifftbuffer[n]);
					
					real=ifftbuffer[n].real();
					or=overlapbuffer[n].real();
					
					if(stfter.getSynthesisWindow()!=null) BLAS.entryMultiply(
							stfter.getSynthesisWindow(),real,real);
					
					//add overlapping samples
					for(int i=0;i<stfter.stftOverlap();i++) real[i]+=or[i];

					//copy overlapping samples for next window
					for(int i=0;i<stfter.stftOverlap();i++) or[i]=real[stfter.stftShift()+i];
				}
			}
			
			stftframeleft=true;
			//reset signal pointer
			spointer=0;
		}
		else stftframeleft=false;
	}
	
	public void close() throws IOException 
	{
		ss.close();	
	}
	
	public static void main(String[] args) throws IOException
	{
	double[] t;
	double[][] s1,s2=null;
	DelayedSignalSource ds;
	
		t=Util.linspace(0, 1, 8001);
		s1=new double[3][t.length];
		for(int i=0;i<s1[0].length;i++) 
		{	
			s1[0][i]=Math.cos(2*Math.PI*10*t[i]);
			s1[1][i]=s1[0][i];
			s1[2][i]=s1[0][i];
		}
		
		ds=new DelayedSignalSource(new ArraySignalSource(s1));
		ds.setDelay(0,0);
		ds.setDelay(1,120.5);
		ds.setDelay(2,-120.5);
		
		s2=ds.toArray(s2);
		ds.close();
		
		Util.plotSignals(s2);
	}
}
