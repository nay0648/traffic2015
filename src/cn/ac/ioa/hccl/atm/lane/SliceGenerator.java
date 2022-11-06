package cn.ac.ioa.hccl.atm.lane;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.array.*;

/**
 * Used to formulate the sliced detecting zone using array signals.
 * @author nay0648
 */
public abstract class SliceGenerator implements Serializable
{
private static final long serialVersionUID = 6337213031890419769L;
private MonitorArray monitor;//array reference
private double scanaz1=Param.LANE_SCANAZ1;//left close interval
private double scanaz2=Param.LANE_SCANAZ2;//right close interval
private int numscans=Param.LANE_NUMSCANS;//number of the scanning angle
	
	/**
	 * @param monitor
	 * array reference
	 */
	public SliceGenerator(MonitorArray monitor)
	{
		this.monitor=monitor;
	}
	
	/**
	 * get array reference
	 * @return
	 */
	public MonitorArray monitorArray()
	{
		return monitor;
	}

	/**
	 * get the number of scanning azimuth angles
	 * @return
	 */
	public int numScans()
	{
		return numscans;
	}
	
	/**
	 * get the scanning azimuth (degree)
	 * @return
	 */
	public double[] scanningAzimuth()
	{
	double[] scanaz;
	double delta;
	
		//increment
		delta=(scanaz2-scanaz1)/(numscans-1);
		
		scanaz=new double[numscans];
		for(int i=0;i<scanaz.length;i++) scanaz[i]=scanaz1+delta*i;
		scanaz[scanaz.length-1]=scanaz2;//cancel the residual
		
		return scanaz;
	}
	
	/**
	 * generate the sliced response
	 * @param response
	 * data space, null to allocate new space
	 * @return
	 * @throws IOException
	 */
	public abstract double[] slicedResponse(double[] response) throws IOException;
}
