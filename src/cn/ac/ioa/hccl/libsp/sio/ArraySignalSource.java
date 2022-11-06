package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * Encapsulate array into a signal source.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Mar 8, 2011 2:49:04 PM, revision:
 */
public class ArraySignalSource extends SignalSource
{
private SignalSource source;//the underlying source

	/**
	 * <h1>Description</h1>
	 * used to read 1d array
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Aug 29, 2013 7:45:31 PM, revision:
	 */
	private class RealSource1 extends SignalSource
	{
	private double[] data=null;
	private int idx=0;//current index
	
		public RealSource1(double[] data)
		{
			this.data=data;
		}
		
		public int numChannels()
		{
			return 1;
		}

		public void readFrame(double[] frame) throws IOException,EOFException
		{
			this.checkFrameSize(frame.length);
			
			if(idx>=data.length) throw new EOFException();
			frame[0]=data[idx];
			idx++;
		}
		
		public void close() throws IOException
		{}
	}

	/**
	 * <h1>Description</h1>
	 * Used to read double array.
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Aug 29, 2013 7:42:34 PM, revision:
	 */
	private class RealSource2 extends SignalSource
	{
	private double[][] data=null;
	private int idx=0;//current index

		public RealSource2(double[][] data)
		{
			this.data=data;
		}
	
		public int numChannels()
		{
			return data.length;
		}

		public void readFrame(double[] frame) throws IOException,EOFException
		{
			this.checkFrameSize(frame.length);
			
			if(idx>=data[0].length) throw new EOFException();
			for(int i=0;i<frame.length;i++) frame[i]=data[i][idx];
			idx++;
		}
		
		public void close() throws IOException
		{}
	}
	
	/**
	 * <h1>Description</h1>
	 * used to read complex source
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Aug 29, 2013 7:50:10 PM, revision:
	 */
	private class ComplexSource1 extends SignalSource
	{
	private ComplexVector data;
	int idx=0;
	
		public ComplexSource1(ComplexVector data)
		{
			this.data=data;
		}
		
		public int numChannels()
		{
			return 1;
		}

		public void readFrame(double[] frame) throws IOException,EOFException
		{
			this.checkFrameSize(frame.length);
			
			if(idx>=data.size()*2) throw new EOFException();
			
			if(idx%2==0) frame[0]=data.getReal(idx/2);
			else frame[0]=data.getImaginary(idx/2);
			
			idx++;
		}
		
		public void close() throws IOException
		{}
	}
	
	/**
	 * <h1>Description</h1>
	 * used to read complex signals
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Aug 29, 2013 7:55:19 PM, revision:
	 */
	private class ComplexSource2 extends SignalSource
	{
	private ComplexMatrix data;
	int idx=0;
	
		public ComplexSource2(ComplexMatrix data)
		{
			this.data=data;
		}
	
		public int numChannels()
		{
			return data.numRows();
		}
		
		public void readFrame(double[] frame) throws IOException,EOFException
		{
			this.checkFrameSize(frame.length);
			
			if(idx>=data.numColumns()*2) throw new EOFException();
			
			if(idx%2==0) for(int n=0;n<frame.length;n++) frame[n]=data.getReal(n,idx/2);
			else for(int n=0;n<frame.length;n++) frame[n]=data.getImaginary(n,idx/2);
			
			idx++;
		}
	
		public void close() throws IOException
		{}
	}
	
	/**
	 * <h1>Description</h1>
	 * used to read list signals
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Aug 30, 2013 10:45:09 AM, revision:
	 */
	private class RealListSource extends SignalSource
	{
	private int numch;
	private Iterator<double[]> it;
	
		public RealListSource(List<double[]> data)
		{
			numch=data.get(0).length;
			it=data.iterator();
		}
		
		public int numChannels()
		{
			return numch;
		}

		public void readFrame(double[] frame) throws IOException,EOFException
		{
		double[] temp;
		
			this.checkFrameSize(frame.length);
			
			if(!it.hasNext()) throw new EOFException();
			temp=it.next();
			System.arraycopy(temp,0,frame,0,frame.length);
		}
		
		public void close() throws IOException
		{}
	}
		
	/**
	 * for real value single channel signals
	 * @param data
	 * data buffer
	 */
	public ArraySignalSource(double[] data)
	{
		source=new RealSource1(data);
	}

	/**
	 * for real multichannel signals
	 * @param data
	 * data buffer
	 */
	public ArraySignalSource(double[][] data)
	{
		source=new RealSource2(data);
	}
	
	/**
	 * for complex value single channel signals
	 * @param buffer
	 * data buffer
	 */
	public ArraySignalSource(ComplexVector buffer)
	{
		source=new ComplexSource1(buffer);
	}
		
	/**
	 * for complex value multichannel signals
	 * @param buffer
	 * data buffer
	 */
	public ArraySignalSource(ComplexMatrix buffer)
	{
		source=new ComplexSource2(buffer);
	}
	
	/**
	 * @param buffer
	 * a list of data
	 */
	public ArraySignalSource(List<double[]> buffer)
	{
		source=new RealListSource(buffer);
	}

	public int numChannels()
	{
		return source.numChannels();
	}

	public void readFrame(double[] frame) throws IOException,EOFException
	{
		source.readFrame(frame);
	}
	
	public void close() throws IOException
	{
		source.close();
	}
	
	public static void main(String[] args) throws IOException
	{
	double[][] data={{1,2,3,4,5},{6,7,8,9,10}};
	ArraySignalSource source;
	double[] frame;
	
		source=new ArraySignalSource(data);
		frame=new double[source.numChannels()];
		for(;;)
		{
			try
			{
				source.readFrame(frame);
			}
			catch(EOFException e)
			{
				break;
			}
			System.out.println(Arrays.toString(frame));
		}
		
		source=new ArraySignalSource(new ComplexVector(data[0],data[1]));
		System.out.println(Arrays.toString(source.toArray((double[])null)));
	}
}
