package cn.ac.ioa.hccl.atm.preprocess;
import java.io.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * Convert multichannel txt to wav format.
 * @author nay0648
 */
public class Txt2Wav
{
	public static void main(String[] args) throws IOException
	{
	File path,parent;
	double fs;
	String name;
	SignalSource sin;
	SignalSink sout;
	double[][] buffer;
	
	
		args=new String[] {
				"D:/nay0648/data/research/beamforming/dataset/2014-11-24/60degree-1m-2.txt",
				"16000"
		};
	
		path=new File(args[0]);
		fs=Double.parseDouble(args[1]);
		
		parent=path.getParentFile();
		name=path.getName();
		System.out.println(name);
			
		sin=new TextSignalSource(new BufferedInputStream(new FileInputStream(path)));
		name=name.substring(0,name.length()-4)+".wav";
		sout=new WaveSink(fs,16,sin.numChannels(),new File(parent,name));
			
		buffer=new double[sin.numChannels()][1024];
		for(int c=0;(c=sin.readSamples(buffer))>0;) 
			sout.writeSamples(buffer, 0, c);
			
		sin.close();
		sout.flush();
		sout.close();
	}
}
