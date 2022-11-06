package cn.ac.ioa.hccl.atm.agc;

/**
 * Use a threshold to control the gain.
 * @author nay0648
 */
public class ThresholdAGC extends AutomaticGainControl
{
private static final long serialVersionUID = -6108650986778328905L;
private double th;

	/**
	 * @param th
	 * threshold
	 */
	public ThresholdAGC(double th)
	{
		this.th=th;
	}

	public double normalize(double gain) 
	{
		if(gain>=th) return 1;
		else return 0;
	}
}
