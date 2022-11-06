/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Generate subarray frame from the original array data for next uses.
 * @author nay0648
 */
public abstract class Subframer implements Serializable
{
private static final long serialVersionUID = -6807372206696985093L;
private STFTArray array;//the monitor reference
private int fi;//working frequency bin index
private ComplexVector bufferedframe=null;//buffered array frame
private long bufferedframeidx=-1;//buffered array frame index

	/**
	 * @param array
	 * array reference
	 * @param fi 
	 * working frequency bin index
	 */
	public Subframer(STFTArray array,int fi)
	{
		this.array=array;
		this.fi=fi;
	}
	
	/**
	 * get the array reference
	 * @return
	 */
	public STFTArray array()
	{
		return array;
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
	 */
	public abstract ComplexVector frame(ComplexVector arrayin);
	
	/**
	 * get the buffered frame, the data will change when the 
	 * array's hasNextFrame() method is called
	 * @return
	 * data is not copied
	 */
	public ComplexVector frame()
	{
		if(bufferedframeidx!=array.frameIndex()) 
		{
			bufferedframe=frame(bufferedframe);
			bufferedframeidx=array.frameIndex();
		}
			
		return bufferedframe;
	}
}
