/**
 * Created on: 2015Äê6ÔÂ15ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle.Type;

/**
 * For demo.
 * @author nay0648
 */
public class RandClassifier implements VehicleClassifier
{
private static final long serialVersionUID = -7688740460244210081L;

	public Type type(Vehicle v) 
	{
		if(Math.random()>=0.85) return Type.large;
		else return Type.small;
	}
}
