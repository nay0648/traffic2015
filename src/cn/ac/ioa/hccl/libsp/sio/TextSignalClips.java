package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import java.util.*;

/**
 * Read text signals in a directory as clips.
 * @author nay0648
 */
public class TextSignalClips extends SignalSource
{
private int numch=0;
private boolean loop;//true to looply load clips
private File[] clips;//all clip files
private int clipidx=0;
private SignalSource ss=null;//underlying text signal source

	/**
	 * @param loop
	 * true to looply load signal clips
	 * @param datadir
	 * data directory path
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public TextSignalClips(boolean loop,File datadir) throws FileNotFoundException, IOException
	{
		this(loop,datadir.listFiles());
	}
	
	/**
	 * @param loop
	 * true to looply load signal clips
	 * @param clipfiles
	 * text signal clip files
	 * @throws IOException
	 */
	public TextSignalClips(boolean loop,File... clipfiles) throws IOException
	{
	List<File> datalist;
	TextSignalSource ts=null;
	double[] tf;
	
		this.loop=loop;
		
		/*
		 * check signal data
		 */
		datalist=new LinkedList<File>();
		for(File df:clipfiles) 
		{
			try
			{
				ts=new TextSignalSource(new BufferedInputStream(new FileInputStream(df)));
				tf=new double[ts.numChannels()];
				ts.readFrame(tf);//test whether the text file is a signal file or not
				
				if(numch==0) numch=ts.numChannels();
				else if(ts.numChannels()!=numch) throw new IllegalArgumentException(
						"number of channels not match: "+ts.numChannels()+", "+numch);
				
				datalist.add(df);
			}
			catch(IOException e)
			{}
			catch(NumberFormatException e)
			{}
			finally
			{
				try
				{
					if(ts!=null) ts.close();
				}
				catch(IOException e)
				{}
			}
		}
		
		clips=new File[datalist.size()];
		clips=datalist.toArray(clips);
		
		ss=new TextSignalSource(new BufferedInputStream(new FileInputStream(clips[clipidx++])));
		if(loop&&clipidx>=clips.length) clipidx=0;
	}

	public int numChannels() 
	{
		return numch;
	}
	
	/**
	 * get the number of signal clips
	 * @return
	 */
	public int numClips()
	{
		return clips.length;
	}
	
	/**
	 * get the clip file path
	 * @param idx
	 * clip file index
	 * @return
	 */
	public File clipFile(int idx)
	{
		return clips[idx];
	}
	
	/**
	 * get the index of the current signal clip
	 * @return
	 */
	public int currentClipIndex()
	{
		return clipidx;
	}

	public void close() throws IOException 
	{
		ss.close();	
	}

	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		this.checkFrameSize(frame.length);
		read(frame);
	}
	
	/**
	 * read a frame
	 * @param frame
	 * frame buffer
	 * @throws IOException
	 * @throws EOFException
	 */
	private void read(double[] frame) throws IOException, EOFException
	{
		try
		{
			ss.readFrame(frame);
		}
		catch(EOFException e)
		{
			if(!loop&&clipidx>=clips.length) throw e;
			else 
			{
				ss=new TextSignalSource(new BufferedInputStream(
						new FileInputStream(clips[clipidx++])));
				if(loop&&clipidx>=clips.length) clipidx=0;
				
				read(frame);
			}
		}
	}
	
	public static void main(String[] args) throws IOException
	{
	TextSignalClips ss;
	double[] frame;
	
		ss=new TextSignalClips(true,new File("D:/"));
		frame=new double[ss.numChannels()];
		for(;;)
		{
			try
			{
				ss.readFrame(frame);
				System.out.println(Arrays.toString(frame));
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{}
			}
			catch(EOFException e)
			{
				break;
			}
		}
	}
}
