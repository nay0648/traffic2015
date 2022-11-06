package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * Perform istft, and send result signal into stream
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Nov 22, 2012 3:48:28 PM, revision:
 */
public class ISTFTSink extends SignalSink
{
private ShortTimeFourierTransformer stft;//used to perform stft
private boolean complex;//true to output complex signal
private SignalSink out;//underlying output stream
private boolean imagframe=true;//true means a complex frame is outputed
private ComplexVector ifftbuffer;//for the ifft
private ComplexVector overlapbuffer;//for the overlapped part
private int numsegs=0;//number of stft blocks outputed

	/**
	 * @param stft
	 * the stft
	 * @param complex
	 * true to output complex signal
	 * @param out
	 * the underlying output stream
	 */
	public ISTFTSink(ShortTimeFourierTransformer stft,boolean complex,SignalSink out)
	{
	this.stft=stft;
	this.complex=complex;
	this.out=out;
	
		ifftbuffer=new ComplexVector(stft.fftSize());
		overlapbuffer=new ComplexVector(stft.stftOverlap());
	}
	
	public int numChannels()
	{
		return stft.fftSize();
	}
	
	/**
	 * get the number of stft frames outputted
	 * @return
	 */
	public int numSTFTFrames()
	{
		return numsegs;
	}
	
	public void writeFrame(double[] frame) throws IOException
	{
	double[] destbuffer,swindow,real,imag,or,oi;
		
		imagframe=!imagframe;
		
		if(imagframe) destbuffer=ifftbuffer.imaginary();
		else destbuffer=ifftbuffer.real();
	
		//get a stft frame
		if(frame.length==stft.fftSize()) System.arraycopy(frame,0,destbuffer,0,frame.length);	
		else if(frame.length==stft.fftSize()/2+1) 
		{
			for(int f=0;f<frame.length;f++) 
			{
				destbuffer[f]=frame[f];
				
				if(f>0&&f<stft.fftSize()/2) 
				{
					if(imagframe) destbuffer[stft.fftSize()-f]=-frame[f];
					else destbuffer[stft.fftSize()-f]=frame[f];
				}
			}
		}
		else throw new IllegalArgumentException("number of channels not match for input signal: "
				+frame.length+", required: "+stft.fftSize()+", or "+(stft.fftSize()/2+1));
		
		if(!imagframe) return;//waiting for a complete complex frame
		
		//perform ifft
		FourierTransformer.ifft(ifftbuffer);
		
		/*
		 * output data
		 */
		swindow=stft.getSynthesisWindow();
		real=ifftbuffer.real();
		imag=ifftbuffer.imaginary();
		or=overlapbuffer.real();
		oi=overlapbuffer.imaginary();
		
		if(complex)
		{
			if(swindow!=null) 
			{
				BLAS.entryMultiply(swindow,real,real);
				BLAS.entryMultiply(swindow,imag,imag);
			}
			
			//add overlapping samples
			for(int i=0;i<stft.stftOverlap();i++) 
			{	
				real[i]+=or[i];
				imag[i]+=oi[i];
			}
			
			//write to result stream
			out.writeSamples(ifftbuffer,0,stft.stftShift());
				
			//copy overlapping samples for next window
			for(int i=0;i<stft.stftOverlap();i++) 
			{	
				or[i]=real[stft.stftShift()+i];
				oi[i]=imag[stft.stftShift()+i];
			}
		}
		else
		{
			if(swindow!=null) BLAS.entryMultiply(swindow,real,real);
			
			//add overlapping samples
			for(int i=0;i<stft.stftOverlap();i++) real[i]+=or[i];
			
			//write to result stream
			out.writeSamples(real,0,stft.stftShift());
			
			//copy overlapping samples for next window
			for(int i=0;i<stft.stftOverlap();i++) or[i]=real[stft.stftShift()+i];
		}
		
		numsegs++;
	}
	
	public void flush() throws IOException
	{
	double[] temp;
	
		if(!imagframe) 
		{
			temp=new double[stft.fftSize()];
			this.writeFrame(temp);
		}
		
		//write the last part
		if(numsegs>0)
		{
			if(complex) out.writeComplexSamples(overlapbuffer);
			else out.writeSamples(overlapbuffer.real());
		}
		
		out.flush();
	}

	public void close() throws IOException
	{
		out.close();
	}
}
