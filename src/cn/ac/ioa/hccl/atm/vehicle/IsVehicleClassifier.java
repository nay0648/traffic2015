/**
 * Created on: 2015��1��5��
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
