/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.corr;
import cn.ac.ioa.hccl.libsp.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Calculate the correlation by moving average.
 * 
 * @author nay0648
 */
public abstract class MovingAverageCorrelator extends CorrelationAccumulator
{
private static final long serialVersionUID = -1514913948836929300L;
private long frameidx=-1;
private int numframes;//number of frames used
private ComplexVector[][] buffer=null;//data buffer, each row for a frequency bin
private int bufferidx=0;
private ComplexVector newframe=null;

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
	public MovingAverageCorrelator(STFTArray array,int numframes,int fi1,int fi2)
	{
	super(array,fi1,fi2);
	
		this.numframes=numframes;
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
	 * get the number of stft frames used in the correlation
	 * @return
	 */
	public int numFrames()
	{
		return numframes;
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
	public abstract void update(int fi,ComplexVector moveout,ComplexVector movein);
	
	public void update()
	{
	long fidx;
	ComplexVector oldframe;
	int fi1;
	
		if(buffer==null) initialize();
		
		fidx=this.array().frameIndex();
		if(fidx==frameidx) return;
		
		fi1=this.startingFrequencyBinIndex();
		for(int i=0;i<buffer.length;i++) 
		{
			//obsolete data need to be moved out
			oldframe=buffer[i][bufferidx];
			//new data should be added in
			newframe=subframer(fi1+i).frame(newframe);
			
			//update subclasses
			update(fi1+i, oldframe, newframe);
			
			//copy new data
			oldframe.copy(newframe);
		}
		//adjust loop buffer index
		if(++bufferidx>=buffer[0].length) bufferidx=0;
				
		frameidx=fidx;
	}
}
