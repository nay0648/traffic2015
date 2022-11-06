/**
 * Created on: 2015Äê3ÔÂ10ÈÕ
 */
package cn.ac.ioa.hccl.libsp.agc;

/**
 * Normalize signals to [-1, 1] by its max absolute amplitude for wave audio format.
 * 
 * @author nay0648
 */
public class SimpleWaveAGC extends AutomaticGainControl
{
private static final long serialVersionUID = -7464281433616399833L;
private double maxamp=1;

	public double normalize(double s) 
	{
	double abss;
	
		abss=Math.abs(s);
		if(abss>maxamp) maxamp=abss;
		
		return s/maxamp;
	}
}
