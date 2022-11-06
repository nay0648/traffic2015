package cn.ac.ioa.hccl.atm;
import java.io.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.gui.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * The main class of this application.
 * @author nay0648
 */
public abstract class AcousticTrafficMonitor extends Thread
{
private MonitorArray monitor;//array reference
private int numlanes;//number of lanes
private TrafficStat stat;//statistical information
private AcousticTrafficMonitorGUI gui;//the gui
private long sleepms=0;//sleep time (ms) between two iterations
private boolean pause=false;//pause the monitor

	/**
	 * @param monitor
	 * array reference
	 * @param numlanes
	 * number of lanes
	 */
	public AcousticTrafficMonitor(MonitorArray monitor,int numlanes)
	{
		this.monitor=monitor;
		this.numlanes=numlanes;
	}
	
	/**
	 * get the monitor array
	 * @return
	 */
	public MonitorArray monitorArray()
	{
		return monitor;
	}
	
	/**
	 * get the lane detector
	 * @return
	 */
	public abstract LaneDetector laneDetector();
	
	/**
	 * get vehicle detector
	 * @return
	 */
	public abstract VehicleDetector vehicleDetector();
	
	/**
	 * get traffic statistical information
	 * @return
	 */
	public TrafficStat trafficStatistics()
	{
		return stat;
	}
	
	/**
	 * update the correlation accumulator
	 * @throws IOException
	 */
	public abstract void updateCorrelation() throws IOException;
	
	/**
	 * update traffic statistics
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
		//update data correlation which will be used by lane and vehicle detector
		updateCorrelation();
		
		laneDetector().updateTrafficInfo();
		vehicleDetector().updateTrafficInfo();
		gui.updateTrafficInfo();
	}
	
	/**
	 * get sleep time (ms) between two iterations
	 * @return
	 */
	public long getSleepTime()
	{
		return sleepms;
	}
	
	/**
	 * get sleep time (ms) between two iterations
	 * @param sleepms
	 * in milliseconds
	 */
	public void setSleepTime(long sleepms)
	{
		this.sleepms=sleepms;
	}
	
	/**
	 * pause the monitor
	 */
	public void pauseMonitor()
	{
		pause=true;
	}
	
	/**
	 * to see if the monitor is paused
	 * @return
	 */
	public boolean isPaused()
	{
		return pause;
	}
	
	/**
	 * resume the monitor
	 */
	public synchronized void resumeMonitor()
	{
		pause=false;
		this.notify();
	}
	
	public void run()
	{
		stat=new TrafficStat(monitor,numlanes);
		gui=new AcousticTrafficMonitorGUI(this);
		
		/*
		 * add listeners
		 */
		laneDetector().addLaneDetectedListener(vehicleDetector());
		vehicleDetector().addVehicleDetectionListener(stat);
		
		try
		{
			for(;monitorArray().hasNextFrame();) 
			{	
				//pause the monitor
				if(pause) 
				{
					synchronized(this)
					{
						try
						{
							this.wait();
						}
						catch (InterruptedException e)
						{}
					}
				}
				
				updateTrafficInfo();
				
				if(sleepms>0)
				{
					try
					{
						Thread.sleep(sleepms);
					}
					catch(InterruptedException ee)
					{}
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(
					"failed to update traffic information",e);
		}
	}
}
