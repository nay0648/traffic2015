/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.corr;
import java.io.*;
import cn.ac.ioa.hccl.libsp.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Used to calculate correlation matrix for a subband.
 * @author nay0648
 */
public abstract class CorrelationAccumulator implements Serializable
{
private static final long serialVersionUID = -5951587800057681952L;
private STFTArray array;//array reference
private int fi1;//starting frequency bin
private int fi2;//ending frequency bin

	/**
	 * @param array
	 * array reference
	 * @param fi1
	 * starting frequency bin
	 * @param fi2
	 * ending frequency bin
	 */
	public CorrelationAccumulator(STFTArray array,int fi1,int fi2)
	{
		this.array=array;
		this.fi1=fi1;
		this.fi2=fi2;
	}
	
	/**
	 * get the array
	 * @return
	 */
	public STFTArray array()
	{
		return array;
	}
	
	/**
	 * get the number of frequency bins used
	 * @return
	 */
	public int numFrequencyBins()
	{
		return fi2-fi1+1;
	}
	
	/**
	 * get the starting frequency bin index
	 * @return
	 */
	public int startingFrequencyBinIndex()
	{
		return fi1;
	}
	
	/**
	 * get the ending frequency bin index
	 * @return
	 */
	public int endingFrequencyBinIndex()
	{
		return fi2;
	}
	
	/**
	 * get the subframer for the specified frequency bin
	 * @param fi
	 * frequency bin index
	 * @return
	 */
	public abstract Subframer subframer(int fi);
	
	/**
	 * get the subframe size
	 * @return
	 */
	public int subframeSize()
	{
		return this.subframer(fi1).subframeSize();
	}
	
	/**
	 * get the subarray corresponding to the accumulated data
	 * @return
	 */
	public SensorArray subarray()
	{
		return this.subframer(fi1).subarray();
	}
	
	/**
	 * accumulate correlation matrix
	 */
	public abstract void update();
	
	/**
	 * get the correlation matrix for the total subband
	 * @param fi
	 * frequency bin index
	 * @return
	 */
	public abstract ComplexMatrix correlationMatrix(int fi);
		
	/**
	 * get the summed correlation matrix in a subband
	 * @param fis
	 * starting frequency bin index
	 * @param fie
	 * ending frequency bin index
	 * @param corr
	 * destination for the correlation, null to allocate new space
	 * @return
	 */
	public ComplexMatrix correlationMatrix(int fis,int fie,ComplexMatrix corr)
	{
	ComplexMatrix c;
		
		c=correlationMatrix(fis);
		if(corr==null) corr=new ComplexMatrix(c.numRows(),c.numColumns());
		else BLAS.checkDestinationSize(corr, c.numRows(), c.numColumns());
		corr.copy(c);
		
		for(int fi=fis+1;fi<fie;fi++) 
			BLAS.add(corr, correlationMatrix(fi), corr);
		
		return corr;
	}
	
	/**
	 * get the summed correlation matrix in the full subband
	 * @param corr
	 * destination for the correlation, null to allocate new space
	 * @return
	 */
	public ComplexMatrix correlationMatrix(ComplexMatrix corr)
	{
		return correlationMatrix(fi1,fi2,corr);
	}
}
