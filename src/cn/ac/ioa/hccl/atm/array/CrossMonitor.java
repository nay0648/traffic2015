/**
 * Created on: Oct 31, 2014
 */
package cn.ac.ioa.hccl.atm.array;
import java.io.*;

/**
 * @author nay0648
 * Cross array with horizontal and vertical subarrays.
 */
public class CrossMonitor extends DefaultMonitor
{
private static final long serialVersionUID = 6712029449844905333L;
private int[] hidx;//horizontal subarray indices
private int[] vidx;//vertical subarray indices

	/**
	 * @param arraygeom
	 * array layout
	 * @param fs
	 * sampling rate
	 * @param frameshift
	 * frame shift
	 * @param fftsize
	 * fft size
	 * @throws IOException
	 */
	public CrossMonitor(File arraygeom,double fs,int frameshift,int fftsize) throws IOException
	{
		super(arraygeom,fs,frameshift,fftsize);
		
		/*
		 * subarray indices
		 */
		int[][] subidx=CrossArrayConfig.subarrayIndices(this.name());
		if(subidx==null) throw new IllegalArgumentException(
				"subarrays are not supported for this array: "+this.name());
		hidx=subidx[0];
		vidx=subidx[1];
	}
	
	/**
	 * get the horizontal subarray indices
	 * @return
	 */
	public int[] horizontalSubarrayIndices()
	{
		return hidx;
	}
	
	/**
	 * get the vertical subarray indices
	 * @return
	 */
	public int[] verticalSubarrayIndices()
	{
		return vidx;
	}
	
	/**
	 * get the horizontal subarray
	 * @return
	 */
	public SensorArray horizontalSubarray()
	{
		return this.subarray(hidx);
	}
	
	/**
	 * get the vertical subarray
	 * @return
	 */
	public SensorArray verticalSubarray()
	{
		return this.subarray(vidx);
	}
}
