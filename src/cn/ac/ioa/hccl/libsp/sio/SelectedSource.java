/**
 * Created on: 2015Äê2ÔÂ22ÈÕ
 */
package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;

/**
 * Select and rearrange channels from the underlying signal source.
 * 
 * @author nay0648
 */
public class SelectedSource extends SignalSource
{
private SignalSource source;//underlying signal source
private int[] chidx;//selected channel indices
private double[] uframe;//used to read the underlying signal source

	/**
	 * @param source
	 * underlying signal source
	 * @param chidx
	 * selected channel indices, null means select all
	 */
	public SelectedSource(SignalSource source, int... chidx)
	{
		this.source=source;
		this.chidx=chidx;
		if(chidx==null) 
		{
			this.chidx=new int[source.numChannels()];
			for(int m=0;m<this.chidx.length;m++) this.chidx[m]=m;
		}
		
		uframe=new double[source.numChannels()];
	}
	
	public int numChannels() 
	{
		return chidx.length;
	}

	public void close() throws IOException 
	{
		source.close();
	}

	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		this.checkFrameSize(frame.length);
		source.readFrame(uframe);
		for(int m=0;m<chidx.length;m++) frame[m]=uframe[chidx[m]];	
	}
}
