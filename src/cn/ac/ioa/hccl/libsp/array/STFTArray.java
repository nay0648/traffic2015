/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.sio.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * An array providing frequency domain data via STFT, 
 * used for frequency domain beamforming.
 * 
 * @author nay0648
 */
public class STFTArray extends SensorArray
{
private static final long serialVersionUID = -3311286003724556084L;
private SignalSource input;//multichannel signal source
private double fs;//sampling frequency (Hz)
private int frameshift;//frame shift
private int fftsize;//fft size
private ShortTimeFourierTransformer stfter;
private ShortTimeFourierTransformer.STFTIterator stftit=null;
private long frameidx=0;//frame index
private ComplexVector[] stftframe;//the stft buffer

	/**
	 * construct an empty array
	 * @param fs
	 * sampling rate (Hz)
	 * @param frameshift
	 * stft frame shift
	 * @param fftsize
	 * fft size
	 * @throws IOException
	 */
	public STFTArray(double fs,int frameshift,int fftsize)
	{
		this.fs=fs;
		this.frameshift=frameshift;
		this.fftsize=fftsize;
		
		stfter=new ShortTimeFourierTransformer(frameshift,fftsize);
	}
	
	/**
	 * @param topology
	 * array topology, each row is a (x, y, z) point
	 * @param fs
	 * sampling rate (Hz)
	 * @param frameshift
	 * stft frame shift
	 * @param fftsize
	 * fft size
	 * @throws IOException
	 */
	public STFTArray(File topology,double fs,int frameshift,int fftsize) throws IOException
	{
		super(topology);
		
		this.fs=fs;
		this.frameshift=frameshift;
		this.fftsize=fftsize;
		
		stfter=new ShortTimeFourierTransformer(frameshift,fftsize);
	}
	
	/**
	 * get signal sampling rate
	 * @return
	 */
	public double sampleRate()
	{
		return fs;
	}
	
	/**
	 * get the short time Fourier transformer
	 * @return
	 */
	public ShortTimeFourierTransformer stfter()
	{
		return stfter;
	}
	
	/**
	 * get frame shift
	 * @return
	 */
	public int frameShift()
	{
		return frameshift;
	}
	
	/**
	 * get fft size
	 * @return
	 */
	public int fftSize()
	{
		return fftsize;
	}
	
	/**
	 * close the array input
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		input.close();
	}
	
	/**
	 * get multichannel array input signal source
	 * @return
	 */
	public SignalSource getSignalSource()
	{
		return input;
	}
	
	/**
	 * set multichannel array input signal source
	 * @param x
	 * signal source
	 */
	public void setSignalSource(SignalSource x)
	{
		if(x.numChannels()!=this.numElements()) throw new IllegalArgumentException(
				"number of channels not match: "+x.numChannels()+", "+this.numElements());
		
		input=x;
	}

	/**
	 * convert frequency bin index to frequency
	 * @param fi
	 * frequency bin index
	 * @return
	 * corresponding frequency (Hz)
	 */
	public double binIndex2Frequency(int fi)
	{
		if(fi<0||fi>fftsize/2) throw new IndexOutOfBoundsException(
				"frequency bin index out of bounds: "+fi+", [0, "+fftsize/2+"]");
		
		return (fs/fftsize)*fi;
	}
	
	/**
	 * convert frequency to the nearest frequency bin index
	 * @param f
	 * frequency (Hz)
	 * @return
	 */
	public int frequency2BinIndex(double f)
	{
		if(f<0||f>fs/2) throw new IllegalArgumentException(
				"frequency out of bounds: "+f+", [0, "+fs/2+"]");
		return (int)Math.round(f/(fs/fftsize));
	}
	
	/**
	 * get the number of frames in a time interval
	 * @param t
	 * time interval (ms)
	 * @return
	 */
	public int numFramesInTimeInterval(long t)
	{
		return (int)Math.ceil(fs*t/(1000*frameshift));
	}
	
	/**
	 * get time (ms) of current frame
	 * @return
	 */
	public long frameTime()
	{
		return (long)(frameidx*frameshift*1000/fs);
	}
	
	/**
	 * get current frame index
	 * @return
	 */
	public long frameIndex() 
	{
		return frameidx;
	}
	
	/**
	 * forward to the next frame
	 * @return
	 * false if no frame is left
	 * @throws IOException 
	 */
	public boolean hasNextFrame() throws IOException
	{
		//initialization
		if(stftit==null) 
		{	
			stftit=stfter.stftIterator(this.getSignalSource());
			
			stftframe=new ComplexVector[stftit.numChannels()];
			for(int m=0;m<stftframe.length;m++) 
				stftframe[m]=new ComplexVector(stfter.fftSize());
		}
		
		if(stftit.next()) 
		{
			//cache all stft frames
			for(int m=0;m<stftframe.length;m++) 
				//perform stft and copy data into buffer
				stftframe[m].copy(stftit.stftFrame(m));
			
			frameidx++;
			return true;
		}
		else return false;
	}
	
	/**
	 * read a frame from sensor array
	 * @param fi
	 * frequency bin index
	 * @param arrayin
	 * data destination, null for allocate new space
	 * @return
	 * @throws IOException 
	 */
	public ComplexVector frame(int fi,ComplexVector arrayin)
	{	
		if(arrayin==null) arrayin=new ComplexVector(this.numElements());
		else BLAS.checkDestinationSize(arrayin, this.numElements());
				
		//get frame data for a specified frequency bin
		for(int m=0;m<arrayin.size();m++) 
			//only get the fi'th frequency bin
			arrayin.setValue(
					m, 
					stftframe[m].getReal(fi), 
					stftframe[m].getImaginary(fi));
		
		return arrayin;
	}
}
