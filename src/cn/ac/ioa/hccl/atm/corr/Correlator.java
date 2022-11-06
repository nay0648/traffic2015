/**
 * Created on: Oct 30, 2014
 */
package cn.ac.ioa.hccl.atm.corr;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.libsp.corr.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * @author nay0648
 * For normal arrays.
 */
public class Correlator extends CorrelationAccumulator
{
private static final long serialVersionUID = 3271735669888636407L;
private Subframer[] subframer;
private CorrelationMatrix[] corr;//for the total subband

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
	public Correlator(MonitorArray monitor,int numframes,int fi1,int fi2)
	{
	super(monitor,numframes,fi1,fi2);
		
		subframer=new Subframer[this.numFrequencyBins()];
		for(int i=0;i<subframer.length;i++) 
			subframer[i]=new Framer(monitor,fi1+i);
		
		corr=new CorrelationMatrix[this.numFrequencyBins()];
		for(int i=0;i<corr.length;i++) 
			corr[i]=new CorrelationMatrix(this.subframeSize());
	}

	public Subframer subframer(int fi) 
	{
		return subframer[fi-this.startingFrequencyBinIndex()];
	}

	public void updateCorrelation(int fi, ComplexVector moveout, ComplexVector movein) 
	{
	CorrelationMatrix mc;
	
		mc=corr[fi-this.startingFrequencyBinIndex()];
		mc.decumulate(moveout);
		mc.accumulate(movein);
	}
	
	public ComplexMatrix correlationMatrix(int fi) 
	{
		return corr[fi-this.startingFrequencyBinIndex()];
	}
}
