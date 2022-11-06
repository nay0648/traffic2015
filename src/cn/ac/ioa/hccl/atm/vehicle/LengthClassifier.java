/**
 * Created on: 2015Äê4ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle.Type;

/**
 * Perform classification according to vehicle length. 
 * Vehicles longer than 6m is large vehicle.
 * 
 * @author nay0648
 */
public class LengthClassifier implements VehicleClassifier
{
private static final long serialVersionUID = -8150453954365882243L;
private static final double LENGTH_THRESHOLD=20;	

	public Type type(Vehicle v) 
	{
		if(v.length()>=LENGTH_THRESHOLD) return Vehicle.Type.large;
		else return Vehicle.Type.small;
	}
}
