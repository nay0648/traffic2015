/**
 * Created on: Nov 3, 2014
 */
package cn.ac.ioa.hccl.atm.corr;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.libsp.corr.CorrelationMatrix;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Correlation accumulator for the SAS-1 array.
 * 
 * @author nay0648
 */
public class SAS1Correlator extends CorrelationAccumulator
{
private static final long serialVersionUID = -4239096400588897894L;
private Subframer[] subframer;
private CorrelationMatrix[] cov;//covariance matrix for the total subband

	/**
	 * @param monitor
	 * monitor reference
	 * @param numframes
	 * number of frames used
	 * @param fi1
	 * starting frequency bin
	 * @param fi2
	 * ending frequency bin
	 */
	public SAS1Correlator(SAS1Monitor monitor,int numframes,int fi1,int fi2)
	{
	super(monitor,numframes,fi1,fi2);
	
		subframer=new Subframer[this.numFrequencyBins()];
		for(int i=0;i<subframer.length;i++) 
			subframer[i]=new SAS1Subframer(monitor,fi1+i);
		
		cov=new CorrelationMatrix[this.numFrequencyBins()];
		for(int i=0;i<cov.length;i++) 
			cov[i]=new CorrelationMatrix(this.subframeSize());
	}

	public Subframer subframer(int fi) 
	{
		return subframer[fi-this.startingFrequencyBinIndex()];
	}

	public void updateCorrelation(int fi, ComplexVector moveout, ComplexVector movein) 
	{
		cov[fi-this.startingFrequencyBinIndex()].decumulate(moveout);
		cov[fi-this.startingFrequencyBinIndex()].accumulate(movein);
	}

	public ComplexMatrix correlationMatrix(int fi) 
	{
		return cov[fi-this.startingFrequencyBinIndex()];
	}
}
