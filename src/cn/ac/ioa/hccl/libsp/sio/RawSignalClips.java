/**
 * Created on: 2014Äê12ÔÂ22ÈÕ
 */
package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;

/**
 * Read signals in raw data format in a directory as clips.
 * 
 * @author nay0648
 */
public class RawSignalClips extends SignalSource
{
private int numch;
private boolean loop;//true to looply load clips
private File[] clips;//all clip files
private int clipidx=0;
private SignalSource ss=null;//underlying text signal source

	/**
	 * @param loop
	 * true to looply load signal clips
	 * @param numch
	 * number of channels
	 * @param clipfiles
	 * text signal clip files
	 * @throws IOException
	 */
	public RawSignalClips(boolean loop,int numch,File... clipfiles) throws IOException
	{
		this.loop=loop;
		this.numch=numch;
		clips=clipfiles;
		
		ss=new RawSignalSource(
				new BufferedInputStream(new FileInputStream(clips[clipidx++])),
				numch);
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
				ss=new RawSignalSource(new BufferedInputStream(
						new FileInputStream(clips[clipidx++])),numch);
				if(loop&&clipidx>=clips.length) clipidx=0;
				
				read(frame);
			}
		}
	}
}
