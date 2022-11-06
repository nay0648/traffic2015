package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.event.*;

/**
 * Used to detect vehicles.
 * @author nay0648
 */
public class VehicleDetector implements Serializable, LaneDetectionListener
{
private static final long serialVersionUID = -8390040504495152091L;
private MonitorArray monitor;//the array
private Lane[] lanes;//all lanes
private Set<VehicleDetectionListener> ls=new HashSet<VehicleDetectionListener>();
//private BufferedWriter zoneresout=null;//used to output zone responses
	
	/**
	 * @param monitor
	 * the array
	 * @param lanes
	 * lanes
	 */
	public VehicleDetector(MonitorArray monitor,Lane... lanes)
	{
		this.monitor=monitor;
		
		this.lanes=lanes;
		for(int i=0;i<lanes.length;i++) lanes[i].setVehicleDetector(this);
		
		
		
//		try
//		{
//			zoneresout=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
//					"D:/zoneresout.txt")));
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
		
		
		
	}
	
	/**
	 * get the array reference
	 * @return
	 */
	public MonitorArray monitorArray()
	{
		return monitor;
	}
	
	/**
	 * get the number of lanes
	 * @return
	 */
	public int numLanes()
	{
		return lanes.length;
	}
	
	/**
	 * get a lane
	 * @param index
	 * lane index
	 * @return
	 */
	public Lane lane(int index)
	{
		return lanes[index];
	}
	
	public void laneDetected(LaneDetectionEvent e) 
	{
		for(int i=0;i<lanes.length;i++) 
			lanes[i].setLanePosition(e.lanePosition(i));
	}
	
	/**
	 * update traffic acoustic information and detect vehicles
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
		for(Lane tl:lanes) tl.updateTrafficInfo();
		
		
		
//		try
//		{
//			if(monitor.frameTime()>180000)
//			{
//				for(Lane tl:lanes) 
//					for(int z=0;z<tl.numZones();z++) 
//						zoneresout.write(tl.zone(z).zoneResponse()+" ");
			
//				zoneresout.write("\n");
//				zoneresout.flush();
//			}
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
		
		
		
		
	}
	
	/**
	 * add vehicle detection listener
	 * @param l
	 * the listener
	 */
	public void addVehicleDetectionListener(VehicleDetectionListener l)
	{
		ls.add(l);
	}
	
	/**
	 * fire the vehicle detection event
	 * @param vehicle
	 * the detected vehicle
	 */
	public void fireVehicleDetected(Vehicle vehicle)
	{
	VehicleDetectionEvent e;

		e=new VehicleDetectionEvent(vehicle);
		for(VehicleDetectionListener l:ls) l.vehicleDetected(e);
	}
}
