package cn.ac.ioa.hccl.atm.agc;
import java.io.*;

/**
 * Control the magnitude of the beamforming response
 * @author nay0648
 */
public abstract class AutomaticGainControl implements Serializable
{
private static final long serialVersionUID = 6156165468545869910L;

	/**
	 * normalize the value to [0, 1]
	 * @param gain
	 * beamforming response
	 * @return
	 */
	public abstract double normalize(double gain);
	
	/**
	 * normalize the value to [0, 1]
	 * @param gain
	 * beamforming response
	 * @return
	 */
	public void normalize(double[] gain)
	{
		for(int i=0;i<gain.length;i++) gain[i]=normalize(gain[i]);
	}
}
