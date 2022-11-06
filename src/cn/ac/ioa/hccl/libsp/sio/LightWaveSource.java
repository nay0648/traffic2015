package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * Used to read wave files without using javax.sound API, for Android systems.
 * The *.wav header format is referenced from: https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Nov 6, 2013 3:28:16 PM, revision:
 */
public class LightWaveSource extends SignalSource
{
private InputStream in;//the underlying input stream
private boolean normalize;//true to normalize data to [-1, 1]
private float fs;//sampling rate
private int bitps;//sample size in bits
private int byteps;//bytes per sample
private int numch;//number of channels
private int framesize;//frame size in bytes
private long maxval;//max unsigned value
private byte[] bframe;//bytes data for a frame

	/**
	 * @param in
	 * underlying input stream
	 * @param normalize
	 * true to normalize signal samples to [-1, 1]
	 * @throws IOException 
	 */
	public LightWaveSource(InputStream in,boolean normalize) throws IOException
	{
		this.in=new BufferedInputStream(in);
		this.normalize=normalize;
		
		readHeader();
		
		byteps=bitps/Byte.SIZE;
		maxval=(long)Math.pow(2,bitps);
		bframe=new byte[framesize];
	}
	
	/**
	 * get sampling rate in Hertz
	 * @return
	 */
	public float samplingRate()
	{
		return fs;
	}
	
	/**
	 * get sample size in bits
	 * @return
	 */
	public int sampleSizeInBits()
	{
		return bitps;
	}

	public int numChannels()
	{
		return numch;
	}
	
	/**
	 * get frame size in bytes
	 * @return
	 */
	public int frameSize()
	{
		return framesize;
	}
	
	/**
	 * read little endian number
	 * @return
	 * @throws IOException
	 */
	private int readLittleEndianInt() throws IOException
	{
	int b0,b1,b2,b3;
	
		b0=in.read();
		b1=in.read();
		b2=in.read();
		b3=in.read();
	
		b0|=b1<<8;
		b0|=b2<<16;
		b0|=b3<<24;
	
		return b0;
	}
	
	/**
	 * read little endian number
	 * @return
	 * @throws IOException
	 */
	private int readLittleEndianShort() throws IOException
	{
	int b0,b1;
	
		b0=in.read();
		b1=in.read();
		
		b0|=b1<<8;
		return b0;
	}
	
	/**
	 * read wave file header
	 * @throws IOException
	 */
	private void readHeader() throws IOException
	{
	StringBuilder sriff,swave,sfmt,sdata;
	int audioformat;
		
		/*
		 * read RIFF
		 */
		sriff=new StringBuilder();
		sriff.append((char)in.read());
		sriff.append((char)in.read());
		sriff.append((char)in.read());
		sriff.append((char)in.read());
		if(!"RIFF".equals(sriff.toString())) throw new IllegalStateException(
				"wrong wave header format");
			
		//chunk size
		readLittleEndianInt();
			
		/*
		 * read WAVE
		 */
		swave=new StringBuilder();
		swave.append((char)in.read());
		swave.append((char)in.read());
		swave.append((char)in.read());
		swave.append((char)in.read());
		if(!"WAVE".equals(swave.toString())) throw new IllegalStateException(
				"wrong wave header format");
		
		/*
		 * read fmt 
		 */
		sfmt=new StringBuilder();
		sfmt.append((char)in.read());
		sfmt.append((char)in.read());
		sfmt.append((char)in.read());
		sfmt.append((char)in.read());
		if(!"fmt ".equals(sfmt.toString())) throw new IllegalStateException(
				"wrong wave header format");
		
		//subchunk1size
		readLittleEndianInt();
		
		/*
		 * audio format
		 */
		audioformat=readLittleEndianShort();
		if(audioformat!=1) throw new IllegalStateException(
				"only PCM audio format is supported");
		
		//number of channels
		numch=readLittleEndianShort();
		
		//sampling rate
		fs=readLittleEndianInt();
		
		//byte rate = SampleRate * NumChannels * BitsPerSample/8
		readLittleEndianInt();
		
		//block align = NumChannels * BitsPerSample/8
		framesize=readLittleEndianShort();
		
		//bits per sample
		bitps=readLittleEndianShort();
		
		/*
		 * Subchunk2ID
		 */
		sdata=new StringBuilder();
		sdata.append((char)in.read());
		sdata.append((char)in.read());
		sdata.append((char)in.read());
		sdata.append((char)in.read());
		if(!"data".equals(sdata.toString())) throw new IllegalStateException(
				"wrong wave header format");
		
		//subchunk2size = NumSamples * NumChannels * BitsPerSample/8
		readLittleEndianInt();
	}
	
	public void readFrame(double[] frame) throws IOException,EOFException
	{
	int offset=0,count=0;
	long sample;
			
		this.checkFrameSize(frame.length);
			
		//read a frame in byte form		
		for(;offset<bframe.length;)
		{
			count=in.read(bframe,offset,bframe.length-offset);
			if(count==-1) throw new EOFException();
			offset+=count;
		}
			
		//parse frame
		for(int i=0;i<frame.length;i++)
		{
			sample=0;
			
			//the higher digits are stored at higher address
			for(int j=0;j<byteps;j++) sample|=(bframe[i*byteps+j]&0x000000ff)<<(j*Byte.SIZE);	
			//adjuse value for signed signal
			if((bframe[i*byteps+byteps-1]&0x80)!=0) sample-=maxval;
			frame[i]=sample;
				
			//normalize sample value to [-1, 1] if needed
			if(normalize)
			{
				frame[i]/=maxval/2;
				if(frame[i]<-1) frame[i]=-1;else if(frame[i]>1) frame[i]=1;
			}
		}
	}
	
	public void close() throws IOException
	{
		in.close();
	}
	
	public static void main(String[] args) throws IOException
	{
	LightWaveSource source;
	double[][] data=null;
	
		source=new LightWaveSource(new FileInputStream("data/SawadaDataset/s1.wav"),true);
		data=source.toArray(data);
		source.close();
		
		Util.plotSignals(source.samplingRate(),data);
		Util.playAsAudio(data[0],source.samplingRate());
	}
}
