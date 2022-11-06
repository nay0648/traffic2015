/**
 * Created on: 2015Äê1ÔÂ5ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle.TravelDirection;

/**
 * Check vehicle by entering and leaving time.
 * @author nay0648
 */
public class DefaultIsVehicleClassifier implements IsVehicleClassifier
{
private static final long serialVersionUID = 7528298922374484543L;
private static final double MAX_SPEED=200;//vehicle speed upper bound

	public boolean isVehicle(Vehicle v) 
	{
		/*
		 * check data completeness
		 */
		if(v.travelDirection()==null) return false;
		
		for(int i=0;i<v.numZones();i++) 
		{
			if(v.getTimeEnterZone(i)<0) return false;
			if(v.getTimeLeaveZone(i)<0) return false;
		}
		
		/*
		 * check time order
		 */
		for(int i=0;i<v.numZones();i++) 
			if(v.getTimeLeaveZone(i)<v.getTimeEnterZone(i)) return false;
		
		if(v.travelDirection()==TravelDirection.uproad) 
		{
			for(int i=1;i<v.numZones();i++) 
			{
			long act0,act1;
				
				act0=(v.getTimeEnterZone(i-1)+v.getTimeLeaveZone(i-1))/2;
				act1=(v.getTimeEnterZone(i)+v.getTimeLeaveZone(i))/2;
				
				if(act1<act0) return false;
			}
		}
		else
		{
			for(int i=1;i<v.numZones();i++) 
			{
			long act0,act1;
				
				act0=(v.getTimeEnterZone(i-1)+v.getTimeLeaveZone(i-1))/2;
				act1=(v.getTimeEnterZone(i)+v.getTimeLeaveZone(i))/2;
				
				if(act1>act0) return false;
			}
		}
		
		//check speed
		if(Math.abs(v.speed())>MAX_SPEED) return false;
		
		return true;
	}
}
