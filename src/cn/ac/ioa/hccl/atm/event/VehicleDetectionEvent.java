package cn.ac.ioa.hccl.atm.event;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * Vehicle detection event.
 * @author nay0648
 */
public class VehicleDetectionEvent
{
private Vehicle vehicle=null;
private VehicleDetectionZone zone=null;

	/**
	 * @param vehicle
	 * detected vehicle reference
	 */
	public VehicleDetectionEvent(Vehicle vehicle)
	{
		this.vehicle=vehicle;
	}
	
	/**
	 * @param zone
	 * the detection zone
	 */
	public VehicleDetectionEvent(VehicleDetectionZone zone)
	{
		this.zone=zone;
	}
	
	/**
	 * get the detected vehicle
	 * @return
	 */
	public Vehicle vehicle()
	{
		return vehicle;
	}
	
	/**
	 * get the triggered detection zone
	 * @return
	 */
	public VehicleDetectionZone detectionZone()
	{
		return zone;
	}
}
