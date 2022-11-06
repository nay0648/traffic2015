/**
 * Created on: 2015Äê1ÔÂ5ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;

/**
 * To see if a reported result is a vehicle or just noise.
 * 
 * @author nay0648
 */
public interface IsVehicleClassifier extends Serializable
{
	/**
	 * to see if a reported result is a vehicle or just noise
	 * @param v
	 * a reported vehicle
	 * @return
	 */
	public boolean isVehicle(Vehicle v);
}
