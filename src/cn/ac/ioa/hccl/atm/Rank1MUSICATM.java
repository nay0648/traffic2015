/**
 * Created on: Nov 25, 2014
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * Mills cross array, multiplicative algorithm, rank-1 MUSIC cross section, 
 * least square vehicle detection zone.
 * 
 * @author nay0648
 */
public class Rank1MUSICATM extends AcousticTrafficMonitor
{
private CrossCorrelator corr;//used to calculate correlation matrix
private LaneDetector ldetector;//used to detect lanes
private VehicleDetector vdetector;//used to watch vehicles
	
	/**
	 * Default constructor for experiments.
	 * @throws IOException
	 */
	public Rank1MUSICATM() throws IOException
	{
	this(new CrossMonitor(
			Param.DEFARRAYGEOM,Param.DEFFS,Param.FRAMESHIFT,Param.FFTSIZE),Param.NUM_LANES);
	MonitorArray monitor;
	ArrayTestSignalSource x;
		
		monitor=this.monitorArray();
		x=new ArrayTestSignalSource(monitor,monitor.sampleRate());
		x.setScanningPattern(new HighwayScan());
//		x.setScanningPattern(new ElevationScan(0));
		monitor.setSignalSource(x);
	}

	/**
	 * @param monitor
	 * the array, already set signal source and smaple rate
	 * @param numlanes
	 * number of lanes
	 * @throws IOException
	 */
	public Rank1MUSICATM(CrossMonitor monitor,int numlanes) throws IOException
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
		ldetector=new ParzenLaneDetector(new Rank1MUSICSlicer(corr),numlanes);
		
		/*
		 * construct vehicle detector
		 */
		vagc=new ParzenGainControl(monitor);
		
		lanes=new Lane[numlanes];
		for(int i=0;i<lanes.length;i++) 
		{
			lanes[i]=new Lane(i);
			
			lanes[i].addZone(new CrossLSZone(vagc,corr,-Param.ZONE_ELEVATION));
//			lanes[i].addZone(new MultiplicativeLSZone(vagc,corr,-Param.ZONE_ELEVATION/2.0));
			lanes[i].addZone(new CrossLSZone(vagc,corr,0));
//			lanes[i].addZone(new MultiplicativeLSZone(vagc,corr,Param.ZONE_ELEVATION/2.0));
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
			
		atm=new Rank1MUSICATM();
		atm.start();
	}
}
