/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.corr;
import cn.ac.ioa.hccl.libsp.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * correlator for normal arrays.
 * 
 * @author nay0648
 * 
 */
public class Correlator extends MovingAverageCorrelator
{
private static final long serialVersionUID = 3271735669888636407L;
private Subframer[] subframer;
private CorrelationMatrix[] cov;//for the total subband

	/**
	 * @param array
	 * array reference
	 * @param numframes
	 * number of frames used
	 * @param fi1
	 * starting frequency bin
	 * @param fi2
	 * ending frequency bin
	 */
	public Correlator(STFTArray array,int numframes,int fi1,int fi2)
	{
	super(array,numframes,fi1,fi2);
		
		subframer=new Subframer[this.numFrequencyBins()];
		for(int i=0;i<subframer.length;i++) 
			subframer[i]=new Framer(array,fi1+i);
		
		cov=new CorrelationMatrix[this.numFrequencyBins()];
		for(int i=0;i<cov.length;i++) 
			cov[i]=new CorrelationMatrix(this.subframeSize());
	}

	public Subframer subframer(int fi) 
	{
		return subframer[fi-this.startingFrequencyBinIndex()];
	}
	
	public void update(int fi, ComplexVector moveout, ComplexVector movein) 
	{
	CorrelationMatrix c;
	
		c=cov[fi-this.startingFrequencyBinIndex()];
		c.decumulate(moveout);
		c.accumulate(movein);
	}

	public ComplexMatrix correlationMatrix(int fi) 
	{
		return cov[fi-this.startingFrequencyBinIndex()];
	}
}
