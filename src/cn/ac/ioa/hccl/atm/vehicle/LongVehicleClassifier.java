/**
 * Created on: 2015Äê3ÔÂ26ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle.Type;

/**
 * Perform classification according to the standard GB/T 26771-2011: passing time longer 
 * than 2.5 times of the average passing time is classified as long vehicles.
 * 
 * @author nay0648
 */
public class LongVehicleClassifier implements VehicleClassifier
{
private static final long serialVersionUID = -2276853712912674864L;
private static final double LONG_COEFFICIENT=2.5;
private double ptotal=0;//total passing time
private int count=0;//vehicle count
	
	public Type type(Vehicle v) 
	{
	long p;
	
		p=v.avgPassingTime();
		ptotal+=p;
		count++;
		
		if(p>LONG_COEFFICIENT*(ptotal/count)) return Type.large;
		else return Type.small;
	}
}
