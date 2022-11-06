package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * Abstract signal destination, used to output signals. Both single channel signal 
 * and multichannel signal, both real signal and complex signal are supported.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Mar 8, 2011 1:46:24 PM, revision:
 */
public abstract class SignalSink
{
private int currentch=0;//current channel for read sample
private double[] frame;//underlying frame

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
	 * get number of channels
	 * @return
	 */
	public abstract int numChannels();
	
	/**
	 * get current channel for write sample methods
	 * @return
	 */
	public int getCurrentChannel()
	{
		return currentch;
	}
	
	/**
	 * set channel index for write sample methods
	 * @param ch
	 * channel index
	 */
	public void setCurrentChannel(int ch)
	{
		if(ch<0||ch>=numChannels()) throw new IllegalArgumentException("illegal channel index: "+ch);
		currentch=ch;
	}
	
	/**
	 * write a frame into signal sink
	 * @param frame
	 * signal frame
	 * @throws IOException
	 */
	public abstract void writeFrame(double[] frame) throws IOException;
	
	/**
	 * write a complex frame into signal sink
	 * @param frame
	 * data frame
	 * @throws IOException
	 */
	public void writeFrame(ComplexVector frame) throws IOException
	{
		writeFrame(frame.real());
		writeFrame(frame.imaginary());
	}

	/**
	 * flush the buffered data to the sink
	 * @throws IOException
	 */
	public abstract void flush() throws IOException;
	
	/**
	 * close the signal sink
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
	 * write a sample to stream for current selected channel
	 * @param s
	 * a sample
	 * @throws IOException
	 */
	public void writeSample(double s) throws IOException
	{
		if(frame==null) frame=new double[numChannels()];
		frame[currentch]=s;
		writeFrame(frame);
	}
	
	/**
	 * write samples to stream
	 * @param buffer
	 * buffer contains signal samples
	 * @param offset
	 * the start position
	 * @param len
	 * length of samples need to be written
	 * @throws IOException
	 */
	public void writeSamples(double[] buffer,int offset,int len) throws IOException
	{
		if(frame==null) frame=new double[numChannels()];
		for(int i=offset;i<offset+len;i++) 
		{
			frame[currentch]=buffer[i];
			writeFrame(frame);
		}
	}
	
	/**
	 * write samples into stream
	 * @param buffer
	 * buffer contains signal samples
	 * @throws IOException
	 */
	public void writeSamples(double[] buffer) throws IOException
	{
		writeSamples(buffer,0,buffer.length);
	}
	
	/**
	 * Write samples into stream for multichannel signals, samples are written as: 
	 * s11, s21, ..., sm1, s12, s22, ..., sm2, s1n, s2n, ...smn, where m is channel 
	 * index, n is sample index.
	 * @param buffer
	 * Buffer contains signals, each row for a channel.
	 * @param offset
	 * the start position
	 * @param len
	 * number of frames to be written
	 * @throws IOException
	 */
	public void writeSamples(double[][] buffer,int offset,int len) throws IOException
	{
		checkFrameSize(buffer.length);
		if(frame==null) frame=new double[numChannels()];
		
		for(int n=offset;n<offset+len;n++)
		{
			//construct frame
			for(int m=0;m<buffer.length;m++) frame[m]=buffer[m][n];
			writeFrame(frame);
		}
	}
	
	/**
	 * Write samples into stream for multichannel signals, samples are written as: 
	 * s11, s21, ..., sm1, s12, s22, ..., sm2, s1n, s2n, ...smn, where m is channel 
	 * index, n is sample index.
	 * @param buffer
	 * Buffer contains signals, each row for a channel.
	 * @throws IOException
	 */
	public void writeSamples(double[][] buffer) throws IOException
	{
		writeSamples(buffer,0,buffer[0].length);
	}
	
	/**
	 * write complex samples
	 * @param buffer
	 * data buffer
	 * @param offset
	 * the start position
	 * @param len
	 * length of samples to be written
	 * @throws IOException
	 */
	public void writeSamples(ComplexVector buffer,int offset,int len) throws IOException
	{
		if(frame==null) frame=new double[numChannels()];

		for(int i=offset;i<offset+len;i++) 
		{
			frame[currentch]=buffer.getReal(i);
			writeFrame(frame);
			frame[currentch]=buffer.getImaginary(i);
			writeFrame(frame);
		}
	}
	
	/**
	 * write complex samples to stream
	 * @param buffer
	 * data buffer
	 * @throws IOException
	 */
	public void writeComplexSamples(ComplexVector buffer) throws IOException
	{
		writeSamples(buffer,0,buffer.size());
	}
	
	/**
	 * Write complex multichannel signal into stream
	 * @param buffer
	 * data buffer, each row is a channel
	 * @param offset
	 * the start position
	 * @param len
	 * number of frames need to be written
	 * @throws IOException
	 */
	public void writeSamples(ComplexMatrix buffer,int offset,int len) throws IOException
	{
		checkFrameSize(buffer.numRows());
		if(frame==null) frame=new double[numChannels()];
		
		//write according to column major order
		for(int n=offset;n<offset+len;n++)
		{
			/*
			 * write real part
			 */
			for(int m=0;m<buffer.numRows();m++) frame[m]=buffer.getReal(m,n);
			writeFrame(frame);
			
			/*
			 * write imaginary part
			 */
			for(int m=0;m<buffer.numRows();m++) frame[m]=buffer.getImaginary(m,n);
			writeFrame(frame);
		}
	}
	
	/**
	 * write complex multichannel signal into stream
	 * @param buffer
	 * data buffer
	 * @throws IOException
	 */
	public void writeComplexSamples(ComplexMatrix buffer) throws IOException
	{
		writeSamples(buffer,0,buffer.numColumns());
	}
}
