package cn.ac.ioa.hccl.atm.event;

/**
 * Handle vehicle detection event.
 * @author nay0648
 */
public interface VehicleDetectionListener
{	
	/**
	 * this will be called when a vehicle is detected
	 * @param e
	 * the event object
	 */
	public void vehicleDetected(VehicleDetectionEvent e);
	
	/**
	 * vehicle entered
	 * @param e
	 * the event object
	 */
	public void vehicleEnteredDetectionZone(VehicleDetectionEvent e);
	
	/**
	 * vehicle leaved
	 * @param e
	 * the event object
	 */
	public void vehicleLeavedDetectionZone(VehicleDetectionEvent e);
}
