package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * This represents an abstract signal source, samples can be readed from it. Both single 
 * channel signal and multichannel signal, both real signal and complex signal are supported. 
 * Not safe for multithread access.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Mar 8, 2011 11:15:44 AM, revision:
 */
public abstract class SignalSource
{
private int currentch=0;//current channel for single channel signal input
private ComplexVector tempframe=null;//a frame for real and complex signals
private double[] tempframer;//for real valued signals
private double[] tempframei;//for complex valued signals

	/**
	 * throw execpeion if size not match the channel size
	 * @param size
	 * channel size
	 */
	protected void checkFrameSize(int size)
	{
		if(size!=numChannels()) throw new IllegalArgumentException(
				"number of channels not match: "+size+", required: "+numChannels());
	}

	/**
	 * get number of channels of this signal source
	 * @return
	 */
	public abstract int numChannels();
	
	/**
	 * get current channel for single channel signal input
	 * @return
	 */
	public int getCurrentChannel()
	{
		return currentch;
	}
	
	/**
	 * set channel index for single channel signal input
	 * @param ch
	 * channel index
	 */
	public void setCurrentChannel(int ch)
	{
		if(ch<0||ch>=numChannels()) throw new IndexOutOfBoundsException(
				"channel index out of bounds: "+ch+", "+numChannels());
		currentch=ch;
	}
	
	/**
	 * close the signal source
	 * @throws IOException
	 */
	public abstract void close() throws IOException;
	
	public void finalize()
	{
		try
		{
			close();
		}
		catch(IOException e)
		{}
	}
	
	/**
	 * read a frame from signal source
	 * @param frame
	 * buffer for frame
	 * @throws IOException
	 * @throws EOFException
	 */
	public abstract void readFrame(double[] frame) throws IOException, EOFException;
	
	/**
	 * read a complex frame from signal source
	 * @param framer
	 * real part
	 * @param framei
	 * imaginary part
	 * @throws IOException
	 * @throws EOFException
	 */
	public void readFrame(ComplexVector frame) throws IOException, EOFException
	{
		//read real part
		readFrame(frame.real());
		
		//read imaginary part
		try
		{
			readFrame(frame.imaginary());
		}
		catch(EOFException e)
		{
			Arrays.fill(frame.imaginary(),0);
		}
		
		/*
		 * the next time will cause EOF exception
		 */
	}
	
	/**
	 * read a real frame into buffer
	 * @throws EOFException
	 * @throws IOException
	 */
	private void readFrame() throws EOFException, IOException
	{
		if(tempframe==null) 
		{	
			tempframe=new ComplexVector(numChannels());
			tempframer=tempframe.real();
			tempframei=tempframe.imaginary();
		}

		readFrame(tempframer);
	}
	
	/**
	 * read a complex frame into buffer
	 * @throws IOException
	 * @throws EOFException
	 */
	private void readComplexFrame() throws IOException, EOFException
	{
		if(tempframe==null) 
		{	
			tempframe=new ComplexVector(numChannels());
			tempframer=tempframe.real();
			tempframei=tempframe.imaginary();
		}
		
		readFrame(tempframe);
	}
		
	/**
	 * read a sample of current channel from signal source
	 * @return
	 * @throws IOException
	 * @throws EOFException
	 */
	public double readSample() throws IOException, EOFException
	{
		readFrame();
		return tempframer[currentch];
	}
	
	/**
	 * read samples from signal source
	 * @param buffer
	 * destination buffer used to store loaded data
	 * @param offset
	 * the start offset of the data
	 * @param len
	 * the number of samples to read
	 * @return
	 * Number of actually loaded samples, or -1 if get an EOF.
	 * @throws IOException
	 */
	public int readSamples(double[] buffer,int offset,int len) throws IOException
	{
	int count=0;
	
		for(int tau=offset;tau<offset+len;tau++)
		{
			try
			{
				readFrame();
			}
			catch(EOFException e)
			{
				if(count>0) return count;else return -1;
			}
			
			buffer[tau]=tempframer[currentch];
			count++;
		}
		
		return count;
	}
	
	/**
	 * read samples from signal source
	 * @param buffer
	 * space used to store samples
	 * @return
	 * Number of actually loaded samples, or -1 if get an EOF.
	 * @throws IOException
	 */
	public int readSamples(double[] buffer) throws IOException
	{
		return readSamples(buffer,0,buffer.length);
	}
	
	/**
	 * read samples for mulitchannel signal
	 * @param buffer
	 * each row for a channel
	 * @return
	 * Number of loaded samples of each channel, or -1 if get an EOF.
	 * @throws IOException
	 */
	public int readSamples(double[][] buffer,int offset,int len) throws IOException
	{
	int count=0;
	
		checkFrameSize(buffer.length);
		
		for(int tau=offset;tau<offset+len;tau++)
		{
			try
			{
				readFrame();
			}
			catch(EOFException e)
			{
				if(count>0) return count;else return -1;
			}
			
			for(int n=0;n<buffer.length;n++) buffer[n][tau]=tempframer[n];
			count++;
		}
		
		return count;
	}
	
	/**
	 * read multichannel signals
	 * @param buffer
	 * signal buffer, each row for a channel
	 * @return
	 * number of samples read, or -1 if get an eof
	 * @throws IOException
	 */
	public int readSamples(double[][] buffer) throws IOException
	{
		return readSamples(buffer,0,buffer[0].length);
	}

	/**
	 * read complex samples from signal source
	 * @param buffer
	 * data buffer
	 * @param offset
	 * the start position for loaded samples
	 * @param len
	 * length expected to be loaded
	 * @return
	 * Actual number of loaded samples, or -1 if meet an EOF.
	 * @throws IOException
	 */
	public int readSamples(ComplexVector buffer,int offset,int len) throws IOException
	{
	int count=0;
		
		for(int tau=offset;tau<offset+len;tau++)
		{
			try
			{
				readComplexFrame();
			}
			catch(EOFException e)
			{
				if(count>0) return count;else return -1;
			}
			
			buffer.setValue(tau,tempframer[currentch],tempframei[currentch]);
			count++;
		}
		
		return count;		
	}
	
	/**
	 * read complex samples from signal source
	 * @param buffer
	 * data buffer
	 * @return
	 * Actual number of loaded samples, or -1 if meet an EOF.
	 * @throws IOException
	 */
	public int readSamples(ComplexVector buffer) throws IOException
	{
		return readSamples(buffer,0,buffer.size());
	}

	/**
	 * read multichannel complex signals
	 * @param buffer
	 * data buffer, each row for a channel
	 * @param offset
	 * the start position
	 * @param len
	 * number of frames want to read
	 * @return
	 * actual loaded frames, or -1 if get an EOF
	 * @throws IOException
	 */
	public int readSamples(ComplexMatrix buffer,int offset,int len) throws IOException
	{
	int count=0;
		
		checkFrameSize(buffer.numRows());
		
		for(int tau=offset;tau<offset+len;tau++)
		{
			try
			{
				readComplexFrame();
			}
			catch(EOFException e)
			{
				if(count>0) return count;else return -1;
			}
			
			for(int n=0;n<buffer.numRows();n++) buffer.setValue(n,tau,tempframer[n],tempframei[n]);
			count++;
		}
		
		return count;		
	}
	
	/**
	 * read multichannel complex signals
	 * @param buffer
	 * data buffer
	 * @return
	 * actual loaded frames, or -1 if get an EOF
	 * @throws IOException
	 */
	public int readSamples(ComplexMatrix buffer) throws IOException
	{
		return readSamples(buffer,0,buffer.numColumns());
	}
	
	/**
	 * cache current channel of signal data into buffer
	 * @param buffer
	 * buffer used to cache data, null to allocate new space
	 * @return
	 * @throws IOException
	 */
	public double[] toArray(double[] buffer) throws IOException
	{
		if(buffer==null)
		{
		List<Double> buff;
		int idx=0;
		
			buff=new LinkedList<Double>();
			for(;;)
			{
				try
				{
					readFrame();
				}
				catch(EOFException e)
				{
					break;
				}
				
				buff.add(tempframer[currentch]);
			}
			
			buffer=new double[buff.size()];
			for(Double s:buff) buffer[idx++]=s;
		}
		else
		{
			for(int tau=0;tau<buffer.length;tau++)
			{
				try
				{
					readFrame();
				}
				catch(EOFException e)
				{
					break;
				}
				
				buffer[tau]=tempframer[currentch];
			}
		}
		
		return buffer;
	}
	
	/**
	 * cache signals into array
	 * @param buffer
	 * buffer for signal data, each row for a channel, null to allocate new space
	 * @return
	 * @throws IOException
	 */
	public double[][] toArray(double[][] buffer) throws IOException
	{
		if(buffer==null)
		{
		List<double[]> buff;
		double[] temp;
		int idx=0;
		
			buff=new LinkedList<double[]>();
			
			for(;;)
			{
				try
				{
					temp=new double[numChannels()];
					readFrame(temp);
				}
				catch(EOFException e)
				{
					break;
				}
				
				buff.add(temp);
			}
			
			buffer=new double[numChannels()][buff.size()];
			for(double[] temp2:buff)
			{
				for(int n=0;n<buffer.length;n++) buffer[n][idx]=temp2[n];
				idx++;
			}
		}
		else
		{
			checkFrameSize(buffer.length);
			
			for(int tau=0;tau<buffer[0].length;tau++)
			{
				try
				{
					readFrame();
				}
				catch(EOFException e)
				{
					break;
				}
				
				for(int n=0;n<buffer.length;n++) buffer[n][tau]=tempframer[n];
			}
		}
		
		return buffer;
	}
	
	/**
	 * cache current channel of signal data into buffer
	 * @param buffer
	 * buffer used to cache data, null to allocate new space
	 * @return
	 * @throws IOException
	 */
	public ComplexVector toArray(ComplexVector buffer) throws IOException
	{	
		if(buffer==null)
		{
		List<Double> buffr,buffi;
		Iterator<Double> itr,iti;
		int idx=0;
		
			buffr=new LinkedList<Double>();
			buffi=new LinkedList<Double>();
			
			for(;;)
			{
				try
				{
					readComplexFrame();
				}
				catch(EOFException e)
				{
					break;
				}
				
				buffr.add(tempframer[currentch]);
				buffi.add(tempframei[currentch]);
			}
			
			buffer=new ComplexVector(buffr.size());
			itr=buffr.iterator();
			iti=buffi.iterator();
			for(;itr.hasNext()&&iti.hasNext();) buffer.setValue(idx++,itr.next(),iti.next());
		}
		else
		{
			for(int tau=0;tau<buffer.size();tau++)
			{
				try
				{
					readComplexFrame();
				}
				catch(EOFException e)
				{
					break;
				}
				
				buffer.setValue(tau,tempframer[currentch],tempframei[currentch]);
			}
		}
		
		return buffer;
	}
	
	/**
	 * cache complex signals into array
	 * @param buffer
	 * buffer for signal data, null to allocate new space
	 * @return
	 * @throws IOException
	 */
	public ComplexMatrix toArray(ComplexMatrix buffer) throws IOException
	{
		if(buffer==null)
		{
		List<ComplexVector> buff;
		ComplexVector frame;
		int idx=0;
		
			buff=new LinkedList<ComplexVector>();
			
			for(;;)
			{
				try
				{
					frame=new ComplexVector(numChannels());
					readFrame(frame);
				}
				catch(EOFException e)
				{
					break;
				}
				
				buff.add(frame);
			}
			
			buffer=new ComplexMatrix(numChannels(),buff.size());
			for(ComplexVector frame2:buff)
			{
				for(int n=0;n<frame2.size();n++) 
					buffer.setValue(n,idx,frame2.getReal(n),frame2.getImaginary(n));
				
				idx++;
			}
		}
		else
		{
			checkFrameSize(buffer.numRows());
			
			for(int tau=0;tau<buffer.numColumns();tau++)
			{
				try
				{
					readComplexFrame();
				}
				catch(EOFException e)
				{
					break;
				}
				
				for(int n=0;n<buffer.numRows();n++) buffer.setValue(n,tau,tempframer[n],tempframei[n]);
			}
		}
		
		return buffer;
	}
}
