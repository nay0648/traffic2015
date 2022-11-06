/**
 * Created on: Nov 20, 2014
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * Mills cross array, multiplicative algorithm, delay-and-sum cross section, 
 * least square vehicle detection zone.
 * 
 * @author nay0648
 */
public class CrossATM extends AcousticTrafficMonitor
{
private CrossCorrelator corr;//used to calculate correlation matrix
private LaneDetector ldetector;//used to detect lanes
private VehicleDetector vdetector;//used to watch vehicles

	/**
	 * Default constructor for experiments.
	 * @throws IOException
	 */
	public CrossATM() throws IOException
	{
	this(new CrossMonitor(
			Param.DEFARRAYGEOM,Param.DEFFS,Param.FRAMESHIFT,Param.FFTSIZE),Param.NUM_LANES);
	MonitorArray monitor;
	ArrayTestSignalSource x;
		
		monitor=this.monitorArray();
		x=new ArrayTestSignalSource(monitor,monitor.sampleRate());
		x.setScanningPattern(new HighwayScan());
		monitor.setSignalSource(x);
	}
	
	/**
	 * @param monitor
	 * the array, already set signal source and smaple rate
	 * @param numlanes
	 * number of lanes
	 * @throws IOException
	 */
	public CrossATM(CrossMonitor monitor,int numlanes) throws IOException
	{
	super(monitor,numlanes);
	int numframes;
	int fi1,fi2;
	Lane[] lanes;
	AutomaticGainControl vagc;
			
		/*
		 * correlation matrix calculator used by different beamformers
		 */
		numframes=monitor.numFramesInTimeInterval(Param.CORR_TIME_WINDOW);
		fi1=monitor.frequency2BinIndex(Param.CORR_LOWER_FEXP);
		fi2=monitor.frequency2BinIndex(Param.CORR_UPPER_FEXP);
		corr=new CrossCorrelator(monitor,numframes,fi1,fi2);
			
		//lane detector
		ldetector=new PeakValleyLaneDetector(new CrossDSSlicer(corr),numlanes);
		
		/*
		 * construct vehicle detector
		 */
		vagc=new ParzenGainControl(monitor);
		
		lanes=new Lane[numlanes];
		for(int i=0;i<lanes.length;i++) 
		{
			lanes[i]=new Lane(i);
			
			lanes[i].addZone(new CrossLSZone(vagc,corr,-Param.ZONE_ELEVATION));
			lanes[i].addZone(new CrossLSZone(vagc,corr,Param.ZONE_ELEVATION));
		}
		
		vdetector=new VehicleDetector(monitor,lanes);
	}
	
	public LaneDetector laneDetector()
	{
		return ldetector;
	}
	
	public VehicleDetector vehicleDetector()
	{
		return vdetector;
	}
	
	public void updateCorrelation() throws IOException 
	{
		corr.updateTrafficInfo();
	}
			
	public static void main(String[] args) throws IOException
	{
	AcousticTrafficMonitor atm;
			
		atm=new CrossATM();
		atm.start();
	}
}
