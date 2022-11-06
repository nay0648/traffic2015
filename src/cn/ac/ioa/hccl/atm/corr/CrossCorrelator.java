/**
 * Created on: Nov 20, 2014
 */
package cn.ac.ioa.hccl.atm.corr;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Used to calculate the correlation of the horizontal and vertical subarray data 
 * of the Mills cross array, i.e., xh*xv^H, for the multiplicative beamforming 
 * algorithms.
 * 
 * @author nay0648
 */
public class CrossCorrelator extends CorrelationAccumulator
{
private static final long serialVersionUID = -7212686041271060692L;
private Subframer[] subframer;
private int[] hidx,vidx;
private ComplexMatrix[] corr;//the correlation matrix

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
	public CrossCorrelator(CrossMonitor monitor,int numframes,int fi1,int fi2)
	{
	super(monitor,numframes,fi1,fi2);
	
		subframer=new Subframer[this.numFrequencyBins()];
		for(int i=0;i<subframer.length;i++) 
			subframer[i]=new Framer(monitor,fi1+i);
	
		hidx=monitor.horizontalSubarrayIndices();
		vidx=monitor.verticalSubarrayIndices();
		
		corr=new ComplexMatrix[this.numFrequencyBins()];
		for(int i=0;i<corr.length;i++) 
			corr[i]=new ComplexMatrix(hidx.length,vidx.length);
	}
	
	public CrossMonitor monitorArray()
	{
		return (CrossMonitor)super.monitorArray();
	}
	
	public Subframer subframer(int fi) 
	{
		return subframer[fi-this.startingFrequencyBinIndex()];
	}
	
	public void updateCorrelation(int fi, ComplexVector moveout, ComplexVector movein) 
	{
	ComplexMatrix mcorr;
	double aout,bout,cout,dout;
	double ain,bin,cin,din;
	double[][] real,imag;
	
		mcorr=corr[fi-this.startingFrequencyBinIndex()];
		real=mcorr.real();
		imag=mcorr.imaginary();
		
		for(int i=0;i<mcorr.numRows();i++) 
		{
			aout=moveout.getReal(hidx[i]);
			bout=moveout.getImaginary(hidx[i]);
			
			ain=movein.getReal(hidx[i]);
			bin=movein.getImaginary(hidx[i]);
			
			for(int j=0;j<mcorr.numColumns();j++) 
			{
				cout=moveout.getReal(vidx[j]);
				dout=moveout.getImaginary(vidx[j]);
				
				cin=movein.getReal(vidx[j]);
				din=movein.getImaginary(vidx[j]);
				
				/*
				 * remove old data
				 */
				real[i][j]-=aout*cout+bout*dout;
				imag[i][j]-=bout*cout-aout*dout;
				
				/*
				 * add new data
				 */
				real[i][j]+=ain*cin+bin*din;
				imag[i][j]+=bin*cin-ain*din;
			}
		}
	}
	
	public ComplexMatrix correlationMatrix(int fi) 
	{
		return corr[fi-this.startingFrequencyBinIndex()];
	}
}
