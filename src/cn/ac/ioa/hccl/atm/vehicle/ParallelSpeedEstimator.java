package cn.ac.ioa.hccl.atm.vehicle;

/**
 * For parallel detection policy.
 * @author nay0648
 */
public class ParallelSpeedEstimator implements SpeedEstimator
{
private static final long serialVersionUID = 8859501428261663910L;
private double elevation;//detection zone elevation
/*
 * road parameters
 */
private double monitorheight=10;
private double lanewidth=3.75;
private double sepspace=3;
private double basespace=3.25;

	/**
	 * @param elevation
	 * detection zone elevation (degree)
	 */
	public ParallelSpeedEstimator(double elevation)
	{
		this.elevation=elevation;
	}

	/**
	 * get lane center distance
	 * @param laneidx
	 * lane index
	 * @return
	 */
	private double laneCenterDistance(int laneidx)
	{
		switch(laneidx)
		{
			case 0: return basespace+lanewidth/2;
			case 1: return basespace+lanewidth+lanewidth/2;
			case 2: return basespace+lanewidth+lanewidth+sepspace+lanewidth/2;
			case 3:	return basespace+lanewidth+lanewidth+sepspace+lanewidth+lanewidth/2;
			default: throw new IllegalArgumentException(
					"unsupported lane index: "+laneidx);
		}
	}
	
	public double speed(Vehicle vehicle) 
	{
	double w,l,d;
	double time;
	
		w=laneCenterDistance(vehicle.lane().laneIndex());
		l=Math.sqrt(monitorheight*monitorheight+w*w);
		d=2*l*Math.tan(elevation*Math.PI/180);
		
		time=(vehicle.getTimeEnterZone(vehicle.numZones()-1)-vehicle.getTimeEnterZone(0)+
				vehicle.getTimeLeaveZone(vehicle.numZones()-1)-vehicle.getTimeLeaveZone(0))/2.0;
		
		return 3.6*1000*d/time;
	}
}
