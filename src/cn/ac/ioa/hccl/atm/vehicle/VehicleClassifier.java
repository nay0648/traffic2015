package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle.Type;

/**
 * Used to perform vehicle classification
 * @author nay0648
 */
public interface VehicleClassifier extends Serializable
{
	/**
	 * perform vehicle classification
	 * @param v
	 * a vehicle
	 * @return
	 */
	public Type type(Vehicle v);
}
