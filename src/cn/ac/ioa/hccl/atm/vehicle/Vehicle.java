package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Represents a vehicle.
 * @author nay0648
 */
public class Vehicle implements Serializable
{
private static final long serialVersionUID = -6709294735417425627L;
//used to estimate vehicle speed
private static final SpeedEstimator VEST=new DefaultSpeedEstimator();
//used to perform vehicle classification
private static final VehicleClassifier CLASSIFIER=new LengthClassifier();
//private static final VehicleClassifier CLASSIFIER=new RandClassifier();
private Type type=null;//vehicle type
private Lane lane;//lane reference
private long[] vehicleenter;//time (ms) of vehicle enter detection zones indexed by zone id
private long[] vehicleleave;//time (ms) of vehicle leave detection zones indexed by zone id
private int triggeredzoneidx=-1;//triggered zone index
private TravelDirection direction=null;//vehicle traveling direction
private VehicleDetectionZone featurezone=null;//detection zone for feature extraction
private List<Double> rawfeature=new LinkedList<Double>();//vehicle feature vector

	/**
	 * Vehicle traveling directions.
	 * 
	 * @author nay0648
	 */
	public enum TravelDirection
	{
		/**
		 * This vehicle is traveling uproad.
		 */
		uproad,
		/**
		 * This vehicle is traveling downroad.
		 */
		downroad;
	}
	
	/**
	 * Vehicle type.
	 * @author nay0648
	 */
	public enum Type
	{
		/**
		 * large vehicle
		 */
		large,
		/**
		 * small vehicle
		 */
		small;
	}
	
	/**
	 * @param lane
	 * the corresponding lane
	 */
	public Vehicle(Lane lane)
	{
		this.lane=lane;
		
		vehicleenter=new long[lane.numZones()];
		vehicleleave=new long[lane.numZones()];
	}
	
	/**
	 * get the lane this vehicle belongs to
	 * @return
	 */
	public Lane lane()
	{
		return lane;
	}
	
	/**
	 * get the lane index of this vehicle
	 * @return
	 */
	public int laneIndex()
	{
		return lane.laneIndex();
	}
	
	/**
	 * get the number of vehicle detection zones in the lane
	 * @return
	 */
	public int numZones()
	{
		return lane.numZones();
	}
	
	/**
	 * get vehicle traveling direction
	 * @return
	 */
	public TravelDirection travelDirection()
	{
		return direction;
	}
	
	/**
	 * trigger a zone to notify this zone's information is set
	 * @param zoneidx
	 * zone index
	 */
	private void triggerZone(int zoneidx)
	{
		//traveling uproad
		if(direction==TravelDirection.uproad&&zoneidx==triggeredzoneidx+1) 
			triggeredzoneidx=zoneidx;
		//traveling downroad
		else if(direction==TravelDirection.downroad&&zoneidx==triggeredzoneidx-1) 
			triggeredzoneidx=zoneidx;
		//initialize uproad
		else if(zoneidx==0) 
		{
			triggeredzoneidx=0;
			direction=TravelDirection.uproad;
		}
		//initialize downroad
		else if(zoneidx==lane.numZones()-1) 
		{
			triggeredzoneidx=lane.numZones()-1;
			direction=TravelDirection.downroad;
		}
		//reset the triggered zones
		else
		{
			triggeredzoneidx=-1;
			direction=null;
		}
	}
	
	/**
	 * to see if the vehicle information need to fill is complete (ready to fire event)
	 * @return
	 */
	public boolean isVehicleInfoComplete()
	{
		if(direction==TravelDirection.uproad) 
		{
			if(triggeredzoneidx==lane.numZones()-1) return true;
			else return false;
		}
		else if(direction==TravelDirection.downroad) 
		{
			if(triggeredzoneidx==0) return true;
			else return false;
		}
		else return false;
	}
	
	/**
	 * get time (ms) which vehicle enter a detection zone
	 * @param zoneidx
	 * zone index
	 * @return
	 */
	public long getTimeEnterZone(int zoneidx)
	{
		return vehicleenter[zoneidx];
	}
	
	/**
	 * set time (ms) which vehicle enter a detection zone
	 * @param zoneidx
	 * zone index
	 * @param timems
	 * time in milliseconds
	 */
	public void setTimeEnterZone(int zoneidx,long timems)
	{
		vehicleenter[zoneidx]=timems;
		//clear the corresponding leaving time
		vehicleleave[zoneidx]=0;
	}
	
	/**
	 * get time (ms) which vehicle leave a detection zone
	 * @param zoneidx
	 * zone index
	 * @return
	 */
	public long getTimeLeaveZone(int zoneidx)
	{
		return vehicleleave[zoneidx];
	}
	
	/**
	 * set time (ms) which vehicle leave a detection zone
	 * @param zoneidx
	 * zone index
	 * @param timems
	 * time in milliseconds
	 */
	public void setTimeLeaveZone(int zoneidx,long timems)
	{
		vehicleleave[zoneidx]=timems;
		
		//this zone's information is complete
		if(vehicleenter[zoneidx]>0&&vehicleleave[zoneidx]>0) 
			triggerZone(zoneidx);
	}
	
	/**
	 * get the time when the vehicle is detected
	 * @return
	 */
	public long timeOfAppearance()
	{
		if(travelDirection()==TravelDirection.uproad) return getTimeLeaveZone(numZones()-1);
		else return getTimeLeaveZone(0);
	}
	
	/**
	 * get detection zone for feature extraction
	 * @return
	 */
	public VehicleDetectionZone getFeatureZone()
	{
		return featurezone;
	}
	
	/**
	 * set detection zone for feature extraction
	 * @param zone
	 * detection zone
	 */
	public void setFeatureZone(VehicleDetectionZone zone)
	{
		featurezone=zone;
		rawfeature.clear();
	}
	
	/**
	 * extract vehicle feature for a frame
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
		//use raw response as the feature
		if(featurezone!=null) rawfeature.add(featurezone.zoneResponse());
	}
	
	/**
	 * get the feature for vehicle classification
	 * @return
	 */
	public List<Double> rawFeature()
	{
		return rawfeature;
	}
	
	/**
	 * get the time (ms) of the vehicle passing a detection zone
	 * @param zoneidx
	 * detection zone index
	 * @return
	 */
	public long passingTime(int zoneidx)
	{
		return getTimeLeaveZone(zoneidx)-getTimeEnterZone(zoneidx);
	}
	
	/**
	 * get the average time (ms) of the vehicle passing all detection zones
	 * @return
	 */
	public long avgPassingTime() 
	{
	long totaltime=0;
	
		for(int i=0;i<numZones();i++) totaltime+=passingTime(i);
		return totaltime/numZones();
	}
		
	/**
	 * get vehicle type
	 * @return
	 */
	public Type type()
	{
		if(type==null) type=CLASSIFIER.type(this);
		return type;
	}
	
	/**
	 * estimate the angular speed (rad/s) by the least square method
	 * @return
	 */
	public double angularSpeed()
	{
	double[][] mtheta;
	double[] time;
	double[] ab;
	
		/*
		 * the coefficient matrix for detection zone elevations
		 */
		mtheta=new double[numZones()][2];
		for(int i=0;i<mtheta.length;i++) 
		{
			mtheta[i][0]=Math.tan(lane.zone(i).getElevation()*Math.PI/180.0);
			mtheta[i][1]=1;
		}
		
		/*
		 * the result vector for zone activation time
		 */
		time=new double[numZones()];
		for(int i=0;i<time.length;i++) 
			//convert to second
			time[i]=(vehicleenter[i]+vehicleleave[i])/2000.0;
		
		//least square solution for the line coefficient
		ab=BLAS.leastSquare(mtheta, time, null);
		
		if(ab==null) return 0;
		else return 1.0/ab[0];
	}
	
	/**
	 * get vehicle speed (km/h)
	 * @return
	 * negative speed for downroad direction
	 */
	public double speed()
	{
		return VEST.speed(this);
	}
	
	/**
	 * get vehicle length (m)
	 * @return
	 */
	public double length()
	{
		return avgPassingTime()*Math.abs(speed())/(3.6*1000);
	}
	
	public String toString()
	{
	StringBuilder s;
	long mintime=Long.MAX_VALUE;
	
		for(long t:vehicleenter) if(t<mintime) mintime=t;
		for(long t:vehicleleave) if(t<mintime) mintime=t;
	
		s=new StringBuilder();
		s.append("lane index: "+lane.laneIndex()+"\n");
		s.append("traveling direction: "+direction+"\n");
		s.append("speed: "+speed()+"\n");
		s.append("type: "+type()+"\n");
		s.append("appearance time: "+mintime+"\n");
		s.append("zone index		enter time		leave time\n");
		for(int i=0;i<vehicleenter.length;i++) 
			s.append(i+"			"+(vehicleenter[i]-mintime)+"			"+(vehicleleave[i]-mintime)+"\n");
		
		return s.toString();
	}
}
