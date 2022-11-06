package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Generate subarray frame from the original array data for next uses.
 * @author nay0648
 */
public abstract class Subframer implements Serializable
{
private static final long serialVersionUID = -6807372206696985093L;
private MonitorArray monitor;//the monitor reference
private int fi;//working frequency bin index
private ComplexVector bufferedframe=null;//buffered array frame
private long bufferedframeidx=-1;//buffered array frame index

	/**
	 * @param monitor
	 * array reference
	 * @param fi 
	 * working frequency bin index
	 */
	public Subframer(MonitorArray monitor,int fi)
	{
		this.monitor=monitor;
		this.fi=fi;
	}
	
	/**
	 * get the array reference
	 * @return
	 */
	public MonitorArray monitorArray()
	{
		return monitor;
	}
	
	/**
	 * get the working frequency bin of the subframer
	 * @return
	 */
	public int workingFrequencyBin()
	{
		return fi;
	}
	
	/**
	 * get the size of the generated subframe
	 * @return
	 */
	public int subframeSize()
	{
		return subarray().numElements();
	}
	
	/**
	 * get the corresponding subarray
	 * @return
	 */
	public abstract SensorArray subarray();
	
	/**
	 * calculate the subframe
	 * @param arrayin
	 * space for the data, null to allocate new space
	 * @return
	 * @throws IOException
	 */
	public abstract ComplexVector arrayFrame(ComplexVector arrayin) throws IOException;
	
	/**
	 * get the buffered frame, the data will change when the 
	 * array's hasNextFrame() method is called
	 * @return
	 * data is not copied
	 * @throws IOException
	 */
	public ComplexVector arrayFrame() throws IOException
	{
		if(bufferedframeidx!=monitor.frameIndex()) 
		{
			bufferedframe=arrayFrame(bufferedframe);
			bufferedframeidx=monitor.frameIndex();
		}
			
		return bufferedframe;
	}
}
