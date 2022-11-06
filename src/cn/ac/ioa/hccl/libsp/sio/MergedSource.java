/**
 * Created on: 2015Äê3ÔÂ11ÈÕ
 */
package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;

/**
 * Merge multichannel signals into single channel signal source, 
 * multichannel frames will be rearranged as the single channel in order. 
 * Only real signals are supported.
 * 
 * @author nay0648
 */
public class MergedSource extends SignalSource
{
private SignalSource source;//underlying signal source
private double[] uframe;
private int uidx;

	/**
	 * @param source
	 * underlying signal source
	 */
	public MergedSource(SignalSource source)
	{
		this.source=source;
		uframe=new double[source.numChannels()];
		uidx=uframe.length;
	}
	
	public int numChannels() 
	{
		return 1;
	}
	
	public void close() throws IOException 
	{
		source.close();
	}
	
	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		this.checkFrameSize(frame.length);
		
		if(uidx>=uframe.length) 
		{
			source.readFrame(uframe);
			uidx=0;
		}
		
		frame[0]=uframe[uidx++];
	}
}
