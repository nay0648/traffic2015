/**
 * Created on: 2015Äê2ÔÂ8ÈÕ
 */
package cn.ac.ioa.hccl.atm.preprocess;
import java.io.*;
import java.util.regex.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * Convert a uff file into wave format.
 * 
 * @author nay0648
 */
public class Uff2Wav implements Serializable
{
private static final long serialVersionUID = 7035073762541506718L;
private static final int DEFAULT_NBITS=16;//number of bits per sample

	/**
	 * convert a single uff file to wav
	 * @param path
	 * uff file path
	 * @param nbits
	 * number of bits per sample
	 * @throws IOException
	 */
	private static void singleUff2Wav(File path,int nbits) throws IOException
	{
	Pattern puff=Pattern.compile("channel\\d+\\.uff");
	Matcher m;
	File fparent;
	int numch=0;
	UFFSource[] s;
	SignalSource ss;
	LightWaveSink x;
	String fname;
	double[][] buffer;
		
		/*
		 * delete old splitted uff files
		 */
		fparent=path.getParentFile();
		for(File f:fparent.listFiles()) 
		{
			m=puff.matcher(f.getName());
			if(m.find()) f.delete();
		}
			
		UFFSource.splitUFF(path);
			
		/*
		 * rearrange
		 */
		for(File f:fparent.listFiles()) 
		{
			m=puff.matcher(f.getName());
			if(m.find()) numch++;
		}
			
		s=new UFFSource[numch];
		for(int i=0;i<s.length;i++) s[i]=new UFFSource(
				new BufferedInputStream(new FileInputStream(new File(fparent,"channel"+i+".uff"))));
		ss=new SignalMixer(s);
		
		fname=path.getName();
		fname=fname.substring(0, fname.length()-4)+".wav";
		x=new LightWaveSink(s[0].sampleRate(),nbits,ss.numChannels(),new File(fparent,fname));
		
		buffer=new double[x.numChannels()][1024];
		for(int c=0;(c=ss.readSamples(buffer))>0;) x.writeSamples(buffer, 0, c);
			
		ss.close();
		x.flush();
		x.close();
			
		/*
		 * delete old splitted uff files
		 */
		fparent=path.getParentFile();
		for(File f:fparent.listFiles()) 
		{
			m=puff.matcher(f.getName());
			if(m.find()) f.delete();
		}
	}
	
	/**
	 * convert uff files in a directory to wave format
	 * @param path
	 * directory path
	 * @param nbits
	 * number of bits per sample
	 * @throws IOException
	 */
	public static void uff2Wav(File path,int nbits) throws IOException
	{
		if(path.isFile()) singleUff2Wav(path,nbits);
		else if(path.isDirectory()) 
		{
			for(File f:path.listFiles()) 
			{
				if(f.getName().toLowerCase().endsWith(".uff")) 
				{
					System.out.println(f.getName());
					singleUff2Wav(f,nbits);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		args=new String[] {"D:/111"};
		
		uff2Wav(new File(args[0]),DEFAULT_NBITS);
	}
}
