package cn.ac.ioa.hccl.atm.corr;
import java.io.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Used to calculate correlation matrix for a subband.
 * @author nay0648
 */
public abstract class CorrelationAccumulator implements Serializable
{
private static final long serialVersionUID = -5951587800057681952L;
private MonitorArray monitor;//monitor reference
private long frameidx=-1;
private int numframes;//number of frames used
private int fi1;//starting frequency bin
private int fi2;//ending frequency bin
private ComplexVector[][] buffer=null;//data buffer, each row for a frequency bin
private int bufferidx=0;
private ComplexVector newframe=null;

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
	public CorrelationAccumulator(MonitorArray monitor,int numframes,int fi1,int fi2)
	{
		this.monitor=monitor;
		this.numframes=numframes;
		this.fi1=fi1;
		this.fi2=fi2;
	}
	
	/**
	 * initialize data buffers
	 */
	private void initialize()
	{
		//data buffer for moving average
		buffer=new ComplexVector[numFrequencyBins()][numFrames()];
		for(int i=0;i<buffer.length;i++) 
			for(int j=0;j<buffer[i].length;j++) 
				buffer[i][j]=new ComplexVector(subframeSize());
	}
	
	/**
	 * get monitor array
	 * @return
	 */
	public MonitorArray monitorArray()
	{
		return monitor;
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
	 * get the number of stft frames used in the correlation
	 * @return
	 */
	public int numFrames()
	{
		return numframes;
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
	 * this method will be called when the data is updated
	 * @param fi
	 * the updating frequency bin index
	 * @param moveout
	 * the frame which is moved out from the sliding window
	 * @param movein
	 * the new added frame
	 */
	public abstract void updateCorrelation(int fi,ComplexVector moveout,ComplexVector movein);
	
	/**
	 * accumulate correlation matrix
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
	long fidx;
	ComplexVector oldframe;
	
		if(buffer==null) initialize();
		
		fidx=monitor.frameIndex();
		if(fidx==frameidx) return;
		
		for(int i=0;i<buffer.length;i++) 
		{
			//obsolete data need to be moved out
			oldframe=buffer[i][bufferidx];
			//new data should be added in
			newframe=subframer(fi1+i).arrayFrame(newframe);
			
			//update subclasses
			updateCorrelation(fi1+i, oldframe, newframe);
			
			//copy new data
			oldframe.copy(newframe);
		}
		//adjust loop buffer index
		if(++bufferidx>=buffer[0].length) bufferidx=0;
				
		frameidx=fidx;
	}
	
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
