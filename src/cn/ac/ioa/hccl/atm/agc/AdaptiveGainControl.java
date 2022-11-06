package cn.ac.ioa.hccl.atm.agc;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.array.*;

/**
 * Perform gain control adaptively.
 * @author nay0648
 */
public class AdaptiveGainControl extends AutomaticGainControl
{
private static final long serialVersionUID = -1465340639626086067L;
private long localinterval=Param.AGC_LOCALINTERVAL;//local interval (ms) to find local max
private long globalinterval=Param.AGC_GLOBALINTERVAL;//global interval (ms) for normalization
private MonitorArray monitor;//monitor reference
/*
 * used to find local max
 */
private double localmax=0;//local max
private long lt2;//time limit to find a new local max
/*
 * used to find global max
 */
private double[] lmaxbuffer;
private int lmaxidx=0;
private double globalmax=0;

	/**
	 * @param monitor
	 * monitor reference
	 */
	public AdaptiveGainControl(MonitorArray monitor)
	{
		this.monitor=monitor;
		lt2=monitor.frameTime()+localinterval;
		lmaxbuffer=new double[(int)(globalinterval/localinterval)];
	}
	
	public double normalize(double gain) 
	{
	long t;
	
		t=monitor.frameTime();
		//add a new local maximum
		if(t>=lt2) 
		{
			//moved out
			if(globalmax==lmaxbuffer[lmaxidx]) globalmax=0;
			
			/*
			 * move in a new peak
			 */
			lmaxbuffer[lmaxidx]=localmax;
			if(++lmaxidx>=lmaxbuffer.length) lmaxidx=0;
			
			//find global max
			if(globalmax<=0) 
				for(double m:lmaxbuffer) if(m>globalmax) globalmax=m;
			
			/*
			 * clear current local peak
			 */
			localmax=0;
			lt2=t+localinterval;
		}
		
		//update local max
		if(gain>localmax) localmax=gain;
		//adjust local max if needed
		if(localmax>globalmax) globalmax=localmax;
		
		//perform normalization
		if(globalmax==0) return 0;
		else return gain/globalmax;
	}
}
