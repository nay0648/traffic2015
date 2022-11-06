/**
 * Created on: 2015Äê3ÔÂ25ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;

/**
 * Estimate vehicle speed by angular speed.
 * 
 * @author nay0648
 */
public class DefaultSpeedEstimator implements SpeedEstimator
{
private static final long serialVersionUID = -9120678551104478982L;
/*
 * road parameters (m)
 */
private double monitorheight=5.5;
private double lanewidth=3.75;
private double sepspace=3;
private double basespace=3.25;
	
	/**
	 * get lane center distance from the base structure
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
	double d,r;
		
		d=laneCenterDistance(vehicle.lane().laneIndex());
		//distance from monitor to lane center
		r=Math.sqrt(monitorheight*monitorheight+d*d);
		
		return 3.6*r*vehicle.angularSpeed();
	}
}
