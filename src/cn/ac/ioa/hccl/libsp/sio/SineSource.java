package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;

/**
 * <h1>Description</h1>
 * Generate a sine wave.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Jul 1, 2011 3:01:00 PM, revision:
 */
public class SineSource extends SignalSource
{
private long len;//length of the sequence
private long count=0;//samples already read
private double amp;//amplitude
private double f;//frequency of the sine
private double phi;//phase
private double fs;//sample rate

	/**
	 * @param len
	 * length of the sequence, 0 for infinite length
	 * @param amp
	 * amplitude of the sine wave
	 * @param f
	 * sine frequency
	 * @param phi
	 * phase
	 * @param fs
	 * sample rate
	 */
	public SineSource(long len,double amp,double f,double phi,double fs)
	{
		this.len=len;
		this.amp=amp;
		this.f=f;
		this.phi=phi;
		this.fs=fs;
	}
	
	/**
	 * get sine wave amplitude
	 * @return
	 */
	public double getAmplitude()
	{
		return amp;
	}
	
	/**
	 * set amplitude
	 * @param amp
	 * new amplitude
	 */
	public void setAmplitude(double amp)
	{
		this.amp=amp;
	}
	
	/**
	 * get sine frequency
	 * @return
	 */
	public double getFrequency()
	{
		return f;
	}
	
	/**
	 * set frequency
	 * @param f
	 * new frequency
	 */
	public void setFrequency(double f)
	{
		this.f=f;
	}
	
	/**
	 * get sine wave phase
	 * @return
	 */
	public double getPhase()
	{
		return phi;
	}
	
	/**
	 * set phase
	 * @param phi
	 * new phase
	 */
	public void setPhase(double phi)
	{
		this.phi=phi;
	}
	
	/**
	 * get sample rate
	 * @return
	 */
	public double getSamplingRate()
	{
		return fs;
	}
	
	/**
	 * set sampling rate
	 * @param fs
	 * new sampling rate
	 */
	public void setSamplingRate(double fs)
	{
		this.fs=fs;
	}
	
	/**
	 * get the sine value
	 * @param n
	 * sequence index
	 * @return
	 */
	public double sin(long n)
	{
		return amp*Math.sin(2*Math.PI*f*n/fs+phi);
	}

	public int numChannels()
	{
		return 1;
	}

	public void readFrame(double[] frame) throws IOException,EOFException
	{
		this.checkFrameSize(frame.length);
		if(len>0&&count>=len) throw new EOFException();
		frame[0]=sin(count++);
	}

	public void close() throws IOException
	{}
}
