/**
 * Created on: 2015Äê3ÔÂ10ÈÕ
 */
package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import javax.sound.sampled.*;
import cn.ac.ioa.hccl.libsp.agc.*;

/**
 * @author nay0648
 */
public class PlaybackSink extends SignalSink
{
private AudioFormat format;//playback audio format
private SourceDataLine line;//the playback data line
private int bps;//bytes per sample
private int quantization;//max quantized value
private int halfquantization;//used to adjust signed value
private byte[] bframe;//frame in bytes
private AutomaticGainControl agc=new SimpleWaveAGC();//used to normalize the signals

	/**
	 * @param format
	 * playback audio format
	 * @throws LineUnavailableException
	 */
	public PlaybackSink(AudioFormat format) throws LineUnavailableException
	{
		this.format=format;
		
		if(format.getEncoding()!=AudioFormat.Encoding.PCM_SIGNED&&
				format.getEncoding()!=AudioFormat.Encoding.PCM_UNSIGNED) 
			throw new IllegalArgumentException("only PCM encoding is supported");
		bps=format.getSampleSizeInBits()/Byte.SIZE;
		bframe=new byte[bps*this.numChannels()];
		
		quantization=(int)Math.pow(2,format.getSampleSizeInBits())-1;
		halfquantization=(int)Math.pow(2,format.getSampleSizeInBits()-1);
		
		line=AudioSystem.getSourceDataLine(format);
		line.open(format);
		line.start();
	}
	
	/**
	 * get the audio format
	 * @return
	 */
	public AudioFormat audioFormat()
	{
		return format;
	}
	
	public int numChannels() 
	{
		return format.getChannels();
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
	
	/**
	 * quantize a sample according to the audio format
	 * @param sample
	 * a sample
	 * @return
	 */
	private int quantize(double sample)
	{
	int quantized;
	
		//cut to [-1, 1]
		if(sample>1) sample=1;else if(sample<-1) sample=-1;

		quantized=(int)Math.round(((sample+1)*quantization)/2.0);
		if(quantized<0) quantized=0;else if(quantized>quantization) quantized=quantization;
		
		//adjust value for signed format
		if(AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())) quantized-=halfquantization;
		
		return quantized;
	}
	
	public void writeFrame(double[] frame) throws IOException 
	{
	int quantized;	
		
		this.checkFrameSize(frame.length);
		
		//normalize the value to [-1, 1] if needed
		if(agc!=null) agc.normalize(frame);
		
		//convert double frame to byte frame
		for(int m=0;m<frame.length;m++) 
		{
			quantized=quantize(frame[m]);

			if(format.isBigEndian()) 
			{
				for(int i=0;i<bps;i++) 
					bframe[m*bps+i]=(byte)((quantized>>((bps-1-i)*Byte.SIZE))&0x000000ff);
			}
			else 
			{
				for(int i=0;i<bps;i++) 
					bframe[m*bps+i]=(byte)((quantized>>(i*Byte.SIZE))&0x000000ff);
			}
		}
		
		line.write(bframe, 0, bframe.length);
	}
	
	public void flush() throws IOException 
	{
		line.drain();
	}
	
	public void close() throws IOException 
	{
		line.stop();
		line.close();
	}
	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
	WaveSource s1,s2;
	double[][] s;
	AudioFormat fin,fout;
	PlaybackSink sink;
	
		s1=new WaveSource(new File("data/SawadaDataSet/s1.wav"),true);
		s2=new WaveSource(new File("data/SawadaDataSet/s2.wav"),true);
		s=new double[2][];
		s[0]=s1.toArray(s[0]);
		s[1]=s2.toArray(s[1]);
		s1.close();
		s2.close();
		
		fin=s1.audioFormat();
		fout=new AudioFormat(fin.getSampleRate(),fin.getSampleSizeInBits(),2,true,false);
		sink=new PlaybackSink(fout);
		sink.writeSamples(s);
		sink.flush();
		sink.close();
	}
}
