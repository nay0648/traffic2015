package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import javax.sound.sampled.*;
import cn.ac.ioa.hccl.libsp.agc.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * <h1>Description</h1>
 * Output signals as wav data. See: https://ccrma.stanford.edu/courses/422/projects/WaveFormat/ 
 * for detail wave file header information. The java's AudioFormat is not used, for Android devices.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: May 18, 2013 4:51:34 PM, revision:
 */
public class LightWaveSink extends SignalSink
{
private double fs;//sampling rate, 0 to output as PCM format
private int nbits;//bits per sample
private int byteps;//bytes per sample
private int numch;//number of channels
private File path;//wav file path
private BufferedOutputStream out=null;
private int quantization;//max quantized value
private int halfquantization;//used to adjust signed value
private AutomaticGainControl agc=new SimpleWaveAGC();//used to normalize the signals

	/**
	 * @param fs
	 * sample rate in Hertz, 0 to output as PCM format
	 * @param nbits
	 * sample size in bits
	 * @param numch
	 * number of channels
	 * @param path
	 * wave file path
	 * @throws IOException
	 */
	public LightWaveSink(double fs,int nbits,int numch,File path) throws IOException
	{
		this.fs=fs;
		this.nbits=nbits;
		if(nbits!=8&&nbits!=16&&nbits!=24) throw new IllegalArgumentException(
				"only 8, 16, 24 bits of sample size are supported");
		byteps=nbits/Byte.SIZE;
		this.numch=numch;
		this.path=path;
		
		quantization=(int)Math.pow(2,nbits)-1;
		halfquantization=(int)Math.pow(2,nbits-1);
		
		out=new BufferedOutputStream(new FileOutputStream(path));
		if(fs>0) writeHeader();
	}
	
	/**
	 * get the adopted AGC
	 * @return
	 */
	public AutomaticGainControl getAGC()
	{
		return agc;
	}
	
	/**
	 * set the AGC for the wave signal normalization
	 * @param agc
	 * null allowed
	 */
	public void setAGC(AutomaticGainControl agc)
	{
		this.agc=agc;
	}
	
	public void flush() throws IOException
	{
		out.flush();
	}
	
	/**
	 * get the wave header length
	 * @return
	 */
	public static int waveHeaderLength()
	{
		return 44;
	}
	
	public void close() throws IOException
	{
	RandomAccessFile randout;
	int value;
	
		out.close();
		
		//write data size
		if(fs>0) 
		{
			randout=new RandomAccessFile(path,"rw");
			
			/*
			 * ChunkSize
			 */
			randout.seek(4);
			value=(int)randout.length()-8;
			for(int i=0;i<Integer.SIZE/Byte.SIZE;i++)
			{
				randout.write((byte)(value&0x000000ff));
				value>>=Byte.SIZE;
			}
		
			/*
			 * Subchunk2Size
			 */
			randout.seek(40);
			value=(int)randout.length()-44;
			for(int i=0;i<Integer.SIZE/Byte.SIZE;i++)
			{
				randout.write((byte)(value&0x000000ff));
				value>>=Byte.SIZE;
			}
			
			randout.close();
		}
	}
	
	/**
	 * get wave file path
	 * @return
	 */
	public File path()
	{
		return path;
	}

	/**
	 * get sampling rate
	 * @return
	 * 0 to output as PCM format
	 */
	public double sampleRate()
	{
		return fs;
	}
	
	/**
	 * get sample size in bits
	 * @return
	 */
	public int sampleSizeInBits()
	{
		return nbits;
	}
	
	public int numChannels()
	{
		return numch;
	}
	
	/**
	 * quantize a sample according to the audio format
	 * @param sample
	 * a sample
	 * @return
	 */
	public int quantize(double sample)
	{
	int quantized;
	
		//cut to [-1, 1]
		if(sample>1) sample=1;else if(sample<-1) sample=-1;

		quantized=(int)Math.round(((sample+1)*quantization)/2.0);
		if(quantized<0) quantized=0;else if(quantized>quantization) quantized=quantization;
		
		//adjust value for signed format
		quantized-=halfquantization;
		
		return quantized;
	}
	
	/**
	 * write a 4 byte integer in little endian form
	 * @param value
	 * a integer
	 * @throws IOException
	 */
	private void writeLittleEndianInt(int value) throws IOException
	{
		for(int i=0;i<Integer.SIZE/Byte.SIZE;i++)
		{
			out.write((byte)(value&0x000000ff));
			value>>=Byte.SIZE;
		}
	}
	
	/**
	 * write a 2 byte short in little endian form
	 * @param value
	 * a short integer
	 * @throws IOException
	 */
	private void writeLittleEndianShort(short value) throws IOException
	{
		for(int i=0;i<Short.SIZE/Byte.SIZE;i++)
		{
			out.write((byte)(value&0x000000ff));
			value>>=Byte.SIZE;
		}
	}
	
	/**
	 * write audio format header into output stream
	 * @throws IOException
	 */
	private void writeHeader() throws IOException
	{		
		/*
		 * ChunkID
		 */
		out.write('R');
		out.write('I');
		out.write('F');
		out.write('F');
		//ChunkSize, not specified yet
		writeLittleEndianInt(0);
		/*
		 * Format
		 */
		out.write('W');
		out.write('A');
		out.write('V');
		out.write('E');
		
		/*
		 * Subchunk1ID
		 */
		out.write('f');
		out.write('m');
		out.write('t');
		out.write(' ');
		//Subchunk1Size
		writeLittleEndianInt(16);
		//AudioFormat
		writeLittleEndianShort((short)1);
		//NumChannels
		writeLittleEndianShort((short)numch);
		//SampleRate
		writeLittleEndianInt((int)fs);
		//ByteRate, SampleRate * NumChannels * BitsPerSample/8
		writeLittleEndianInt((int)(fs*numch*byteps));
		//BlockAlign, NumChannels * BitsPerSample/8
		writeLittleEndianShort((short)(numch*byteps));
		//BitsPerSample
		writeLittleEndianShort((short)(byteps*Byte.SIZE));
		
		/*
		 * Subchunk2ID
		 */
		out.write('d');
		out.write('a');
		out.write('t');
		out.write('a');
		//Subchunk2Size, NumSamples * NumChannels * BitsPerSample/8, not specified yet
		writeLittleEndianInt(0);
	}
	
	/**
	 * output a sample without seek the file pointer
	 * @param sample
	 * a sample
	 * @throws IOException
	 */
	private void outputSample(double sample) throws IOException
	{
	int quantized;
	
		quantized=quantize(sample);
		for(int i=0;i<byteps;i++) 
			out.write((byte)((quantized>>(i*Byte.SIZE))&0x000000ff));
	}

	public void writeFrame(double[] frame) throws IOException
	{
		this.checkFrameSize(frame.length);
		
		//normalize the value to [-1, 1] if needed
		if(agc!=null) agc.normalize(frame);
		
		for(double sample:frame) outputSample(sample);
	}
	
	/**
	 * write raw sample data directly
	 * @param buffer
	 * data buffer
	 * @param offset
	 * buffer offset
	 * @param len
	 * data length
	 * @throws IOException
	 */
	public void writeRawData(byte[] buffer,int offset,int len) throws IOException
	{
		out.write(buffer, offset, len);
	}
	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException
	{
	LightWaveSource source;
	double[][] data=null;
	LightWaveSink sink;
	
		data=new double[4][];
		
		source=new LightWaveSource(new FileInputStream("data/SawadaDataset/s1.wav"),true);
		data[0]=source.toArray(data[0]);
		source.close();
		
		source=new LightWaveSource(new FileInputStream("data/SawadaDataset/s2.wav"),true);
		data[1]=source.toArray(data[1]);
		source.close();
		
		source=new LightWaveSource(new FileInputStream("data/SawadaDataset/s3.wav"),true);
		data[2]=source.toArray(data[2]);
		source.close();
		
		source=new LightWaveSource(new FileInputStream("data/SawadaDataset/s4.wav"),true);
		data[3]=source.toArray(data[3]);
		source.close();
		
		sink=new LightWaveSink(8000,16,data.length,new File("d:/sink.wav"));
		sink.writeSamples(data);
		sink.flush();
		sink.close();
		
		source=new LightWaveSource(new FileInputStream("d:/sink.wav"),true);
		data=source.toArray(data);
		source.close();
		Util.plotSignals(data);
	}
}
