package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * Used to write signal into arrays.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Mar 8, 2011 3:27:32 PM, revision:
 */
public class ArraySignalSink extends SignalSink
{
private int numch;//number of channels
private List<double[]> data;//underlying data

	/**
	 * @param numch
	 * number of channels
	 */
	public ArraySignalSink(int numch)
	{
		this.numch=numch;
		data=new LinkedList<double[]>();
	}

	public int numChannels()
	{
		return numch;
	}

	public void writeFrame(double[] frame) throws IOException
	{
	double[] temp;
	
		this.checkFrameSize(frame.length);
		
		temp=new double[frame.length];
		System.arraycopy(frame,0,temp,0,temp.length);
		
		data.add(temp);
	}
	
	public void flush() throws IOException
	{}
	
	public void close() throws IOException
	{}
	
	/**
	 * get the underlying data as frame list
	 * @return
	 * each element in the list is a frame
	 */
	public List<double[]> data()
	{
		return data;
	}
	
	/**
	 * get single channel real valued signal
	 * @param buffer
	 * data buffer, null to allocate new space
	 * @return
	 */
	public double[] toArray(double[] buffer)
	{
		try
		{
			return (new ArraySignalSource(data)).toArray(buffer);
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	/**
	 * get multichannel channel real valued signal
	 * @param buffer
	 * data buffer, null to allocate new space
	 * @return
	 */
	public double[][] toArray(double[][] buffer)
	{
		try
		{
			return (new ArraySignalSource(data)).toArray(buffer);
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	/**
	 * get single channel complex valued signal
	 * @param buffer
	 * data buffer, null to allocate new space
	 * @return
	 */
	public ComplexVector toArray(ComplexVector buffer)
	{
		try
		{
			return (new ArraySignalSource(data)).toArray(buffer);
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	/**
	 * get multi channel complex valued signal
	 * @param buffer
	 * data buffer, null to allocate new space
	 * @return
	 */
	public ComplexMatrix toArray(ComplexMatrix buffer)
	{
		try
		{
			return (new ArraySignalSource(data)).toArray(buffer);
		}
		catch(IOException e)
		{
			return null;
		}
	}
}
