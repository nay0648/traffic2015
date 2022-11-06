package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;

/**
 * Return the buffered samples infinitely as a loop.
 * @author nay0648
 *
 */
public class LoopBufferSignalSource extends SignalSource
{
private double[][] buffer;
private int sidx=0;//sample index

	/**
	 * @param buffer
	 * underlying buffer, each row is a channel, data is not copied
	 */
	public LoopBufferSignalSource(double[][] buffer)
	{
		this.buffer=buffer;
	}
	
	/**
	 * @param buffer
	 * underlying single channel buffer, data is not copied
	 */
	public LoopBufferSignalSource(double[] buffer)
	{
		this.buffer=new double[1][];
		this.buffer[0]=buffer;
	}
	
	public int numChannels() 
	{
		return buffer.length;
	}

	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		this.checkFrameSize(frame.length);
		
		for(int n=0;n<frame.length;n++) frame[n]=buffer[n][sidx];
		if(++sidx>=buffer[0].length) sidx=0;
	}
	
	public void close() throws IOException 
	{}
}
