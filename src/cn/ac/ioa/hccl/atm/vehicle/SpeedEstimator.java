package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;

/**
 * Used to estimate vehicle speed.
 * @author nay0648
 */
public interface SpeedEstimator extends Serializable
{
	/**
	 * estimate vehicle speed
	 * @param vehicle
	 * the vehicle
	 * @return
	 * estimated speed in km/h
	 */
	public double speed(Vehicle vehicle);
}
