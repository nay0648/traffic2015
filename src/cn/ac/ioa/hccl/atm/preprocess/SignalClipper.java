package cn.ac.ioa.hccl.atm.preprocess;
import java.io.*;
import java.util.regex.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * Used to clip multichannel text signals into short clips.
 * 
 * fs=44100
 * 
 * file=Recording-1.txt
 * 0:12:000 0:14.011
 * 0:20:000 0:25.092
 * ...
 * 
 * file=Recording-2.txt
 * ...
 * 
 * @author nay0648
 */
public class SignalClipper
{
private static final Pattern PTIME=Pattern.compile("^(\\d+):(\\d+)\\.(\\d+)$");
private File parent;//working directory
private double fs;//sampling rate
private String currentfile;//current file name
private SignalSource currentss;//current signal source
private long frameidx=0;//current frame index
private double[] frame=null;

	/**
	 * @param task
	 * task instruction path
	 * @throws IOException 
	 */
	public SignalClipper(File task) throws IOException
	{
		parent=task.getParentFile();
		parseTasks(task);
	}
	
	/**
	 * parse and commence tasks
	 * @param task
	 * task file
	 * @throws IOException
	 */
	private void parseTasks(File task) throws IOException
	{
	BufferedReader taskin;	
	
		taskin=new BufferedReader(new InputStreamReader(new FileInputStream(task)));
		
		for(String ts=null;(ts=taskin.readLine())!=null;) 
		{
			ts=ts.trim();
			if(ts.length()==0) continue;
			//sampling rate
			else if(ts.startsWith("fs=")) fs=Double.parseDouble(ts.substring(3));
			//new file starts
			else if(ts.startsWith("file=")) 
			{
				try
				{
					currentfile=ts.substring(5);
					System.out.println("cut "+currentfile+"...");
				
					if(currentss!=null) currentss.close();
					currentss=new TextSignalSource(new BufferedInputStream(
							new FileInputStream(new File(parent,currentfile))));
//					currentss=new WaveSource(new File(parent,currentfile),true);
					
					frameidx=0;
					frame=new double[currentss.numChannels()];
				}
				catch(Exception e)
				{
					currentss=null;
					e.printStackTrace();
				}
			}
			//task
			else 
			{
				try
				{
					cutSignals(ts);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		taskin.close();
	}
	
	/**
	 * parse 0:00.000 format into frame index
	 * @param time
	 * time format
	 * @return
	 */
	private long parseTime(String time)
	{
	Matcher ma;
	int m,s,ms;
	
		ma=PTIME.matcher(time);
		if(!ma.find()) throw new IllegalArgumentException(
				"unknown time format: "+time);
		
		m=Integer.parseInt(ma.group(1));
		s=Integer.parseInt(ma.group(2));
		ms=Integer.parseInt(ma.group(3));
		
		return (long)((m*60+s+ms*0.001)*fs);
	}
	
	/**
	 * cut signals
	 * @param duration
	 * 0:12:000 0:14.011
	 * @throws IOException
	 */
	private void cutSignals(String duration) throws IOException
	{
	String[] tt;
	long sidx,eidx;
	String fout;
	SignalSink sout;
	
		if(currentss==null) return;
		
		tt=duration.trim().split("\\s+");
		tt[0]=tt[0].trim();
		tt[1]=tt[1].trim();
		sidx=parseTime(tt[0]);
		eidx=parseTime(tt[1]);

		if(sidx<frameidx) throw new IllegalArgumentException(
				"signals already read: "+sidx+", "+frameidx);
		
		tt[0]=tt[0].replace(':', '.');
		tt[1]=tt[1].replace(':', '.');
		fout=currentfile.substring(0,currentfile.length()-4)
				+" "+tt[0]+" "+tt[1]+".txt";
		System.out.println(fout);
		sout=new TextSignalSink(new BufferedOutputStream(
				new FileOutputStream(new File(parent,fout))),frame.length);
//		sout=new WaveSink(fs,16,frame.length,new File(parent,fout));
		
		//skip frames
		for(;frameidx<sidx;frameidx++) currentss.readFrame(frame);

		//cut data
		for(;frameidx<=eidx;frameidx++) 
		{
			currentss.readFrame(frame);
			sout.writeFrame(frame);
		}
		
		sout.flush();
		sout.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		args=new String[] {"D:/nay0648/data/research/beamforming/dataset/synthetic_and_vehicle_noise2014-08-18/clippertask.txt"};
		new SignalClipper(new File(args[0]));
	}
}
