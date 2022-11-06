package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.event.*;
import cn.ac.ioa.hccl.atm.lane.*;

/**
 * Represents a lane.
 * 
 * @author nay0648
 */
public class Lane implements Serializable, VehicleDetectionListener
{
private static final long serialVersionUID = 1673319009496761180L;
private VehicleDetector vdet;//vehicle detector reference
private int laneidx;//lane index
private LaneDetector.LanePosition pos;//lane position
//contains all vehicle detection zones in this lane
private List<VehicleDetectionZone> zonelist=new ArrayList<VehicleDetectionZone>(21);
private Vehicle vehicle=null;//detected vehicle
//used to check the vehicle data
private IsVehicleClassifier isvehicle=new DefaultIsVehicleClassifier();

	/**
	 * @param laneidx
	 * lane index
	 * @param direction
	 * vehicle traveling direction
	 */
	public Lane(int laneidx)
	{
		this.laneidx=laneidx;
	}
	
	/**
	 * get the vehicle detector reference
	 * @return
	 */
	public VehicleDetector getVehicleDetector()
	{
		return vdet;
	}
	
	/**
	 * set vehicle detector
	 * @param vdet
	 * vehicle detector
	 */
	public void setVehicleDetector(VehicleDetector vdet)
	{
		this.vdet=vdet;
	}
	
	/**
	 * get lane index
	 * @return
	 */
	public int laneIndex()
	{
		return laneidx;
	}
	
	/**
	 * get the number of vehicle detection zones in this lane
	 * @return
	 */
	public int numZones()
	{
		return zonelist.size();
	}
	
	/**
	 * get a vehicle detection zone
	 * @param zoneidx
	 * zone index
	 * @return
	 */
	public VehicleDetectionZone zone(int zoneidx)
	{
		return zonelist.get(zoneidx);
	}
	
	/**
	 * add a vehicle detection zone into this lane
	 * @param zone
	 * vehicle detection zone
	 */
	public void addZone(VehicleDetectionZone zone)
	{
		zone.setLane(this);
		zone.setZoneIndex(numZones());
		zonelist.add(zone);
	}

	/**
	 * get lane position
	 * @return
	 */
	public LaneDetector.LanePosition getLanePosition()
	{
		return pos;
	}
	
	/**
	 * set lane position and update beamformer
	 * @param pos
	 * new lane position
	 */
	public void setLanePosition(LaneDetector.LanePosition pos)
	{	
		this.pos=pos;
		
		//update the beamformer in this lane according to the new position
		for(VehicleDetectionZone zone:zonelist) 
			zone.updateZone();
	}
	
	/**
	 * update traffic information
	 * @throws IOException
	 */
	public void updateTrafficInfo() throws IOException
	{
		for(VehicleDetectionZone zone:zonelist) 
			zone.updateTrafficInfo();
		
		//extract feature for vehicle classification
		if(vehicle!=null) vehicle.updateTrafficInfo();
	}
	
	public void vehicleDetected(VehicleDetectionEvent e) 
	{}
	
	public void vehicleEnteredDetectionZone(VehicleDetectionEvent e) 
	{
	VehicleDetectionZone zone;
	
		zone=e.detectionZone();
		
		if(vehicle==null) vehicle=new Vehicle(this);
		//set time
		vehicle.setTimeEnterZone(zone.getZoneIndex(), vdet.monitorArray().frameTime());
		//use the middle zone to extract feature
		if(zone.getZoneIndex()==numZones()/2) vehicle.setFeatureZone(zone);
	}

	public void vehicleLeavedDetectionZone(VehicleDetectionEvent e) 
	{
	VehicleDetectionZone zone;
		
		if(vehicle==null) return;
		zone=e.detectionZone();
		
		vehicle.setTimeLeaveZone(zone.getZoneIndex(), vdet.monitorArray().frameTime());
		
		//vehicle information complete
		if(vehicle.isVehicleInfoComplete()) 
		{
			if(isvehicle.isVehicle(vehicle)) 
				vdet.fireVehicleDetected(vehicle);
			vehicle=null;
		}
	}
}
