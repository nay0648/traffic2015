package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.sio.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Use to collect traffic noise.
 * @author nay0648
 */
public abstract class MonitorArray extends SensorArray
{
private static final long serialVersionUID = 6192066600779200388L;
private String name;//array name
private SignalSource input;//multichannel signal source
private double fs;//sampling frequency (Hz)
private int frameshift;//frame shift
private int fftsize;//fft size
	
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
	public MonitorArray(File arraygeom,double fs,int frameshift,int fftsize) throws IOException
	{
		super(arraygeom);
		name=arraygeom.getName();
		
		this.fs=fs;
		this.frameshift=frameshift;
		this.fftsize=fftsize;
	}
	
	/**
	 * get array name
	 * @return
	 */
	public String name()
	{
		return name;
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
		return (long)(frameIndex()*frameshift*1000/fs);
	}
	
	/**
	 * get current frame index
	 * @return
	 */
	public abstract long frameIndex();
	
	/**
	 * forward to the next frame
	 * @return
	 * false if no frame is left
	 * @throws IOException 
	 */
	public abstract boolean hasNextFrame() throws IOException;
	
	/**
	 * read a frame from sensor array
	 * @param fi
	 * frequency bin index
	 * @param arrayin
	 * data destination, null for allocate new space
	 * @return
	 * @throws IOException 
	 */
	public abstract ComplexVector arrayFrame(int fi,ComplexVector arrayin) throws IOException;
}
