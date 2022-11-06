package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;

/**
 * <h1>Description</h1>
 * Used to read signal from text streams.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Mar 8, 2011 11:46:18 AM, revision:
 */
public class TextSignalSource extends SignalSource
{
private BufferedReader textin=null;//nuderlying reader
private int numch;//number of channels
private String[] sframe=null;

	/**
	 * @param in
	 * underlying input stream
	 * @throws IOException
	 */
	public TextSignalSource(InputStream in) throws IOException
	{
		textin=new BufferedReader(new InputStreamReader(in));
		sframe=nextFrame();
		numch=sframe.length;
	}
	
	public void close() throws IOException
	{
		textin.close();
	}

	public int numChannels()
	{
		return numch;
	}
	
	/**
	 * read next frame
	 * @return
	 * return null if get an eof
	 * @throws IOException
	 */
	private String[] nextFrame() throws IOException
	{
	String ts;
	
		for(ts=null;(ts=textin.readLine())!=null;)
		{
			ts=ts.trim();
			if(ts.length()==0) continue;
			return ts.split("\\s+");
		}
		return null;
	}
	
	public void readFrame(double[] frame) throws IOException,EOFException
	{
		this.checkFrameSize(frame.length);
		if(sframe==null) throw new EOFException();
		this.checkFrameSize(sframe.length);
		
		for(int i=0;i<frame.length;i++) frame[i]=Double.parseDouble(sframe[i]);
		sframe=nextFrame();
	}
}
