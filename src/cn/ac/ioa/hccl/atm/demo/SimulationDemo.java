/**
 * Created on: 2015Äê6ÔÂ15ÈÕ
 */
package cn.ac.ioa.hccl.atm.demo;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * @author nay0648
 */
public class SimulationDemo extends AcousticTrafficMonitor
{
private static final File ARRAY_GEOM=new File("arraygeometry/crossarray16k.txt");
private static final double FS=32000;
private static final int FFT_SIZE=512;
private static final int STFT_SHIFT=FFT_SIZE/2;
private static final int NUM_LANES=4;
private static final int CORR_TIME_WINDOW=200;
private static final int CORR_LOWER_FEXP=6500;
private static final int CORR_UPPER_FEXP=7000;
private static final int CORR_LOWER_FEXP2=12000;
private static final int CORR_UPPER_FEXP2=12500;
private static final int ZONE_ELEVATION=3;

private CrossCorrelator corr,corr2;//used to calculate correlation matrix
private LaneDetector ldetector;//used to detect lanes
private VehicleDetector vdetector;//used to watch vehicles
	
	/**
	 * Default constructor for experiments.
	 * @throws IOException
	 */
	public SimulationDemo() throws IOException
	{
	this(
			new CrossMonitor(ARRAY_GEOM,FS,STFT_SHIFT,FFT_SIZE),
			NUM_LANES);
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
	public SimulationDemo(CrossMonitor monitor,int numlanes) throws IOException
	{
	super(monitor,numlanes);
	int numframes;
	int fi1,fi2;
	Lane[] lanes;
	AutomaticGainControl vagc;
			
		/*
		 * correlation matrix calculator used by different beamformers
		 */
		numframes=monitor.numFramesInTimeInterval(CORR_TIME_WINDOW);
		fi1=monitor.frequency2BinIndex(CORR_LOWER_FEXP);
		fi2=monitor.frequency2BinIndex(CORR_UPPER_FEXP);
		corr=new CrossCorrelator(monitor,numframes,fi1,fi2);
		fi1=monitor.frequency2BinIndex(CORR_LOWER_FEXP2);
		fi2=monitor.frequency2BinIndex(CORR_UPPER_FEXP2);
		corr2=new CrossCorrelator(monitor,numframes,fi1,fi2);
		
		//lane detector
		ldetector=new ParzenLaneDetector(new Rank1MUSICSlicer(corr),numlanes);
		
		/*
		 * construct vehicle detector
		 */
		vagc=new ParzenGainControl(monitor);
//		((ParzenGainControl)vagc).setForegroundOnly(true);
		
		lanes=new Lane[numlanes];
		for(int i=0;i<lanes.length;i++) 
		{
			lanes[i]=new Lane(i);
			
			lanes[i].addZone(new CrossDSZone(vagc,corr2,-ZONE_ELEVATION));
			lanes[i].addZone(new CrossDSZone(vagc,corr2,0));
			lanes[i].addZone(new CrossDSZone(vagc,corr2,ZONE_ELEVATION));
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
		corr2.updateTrafficInfo();
	}
	
	public static void main(String[] args) throws IOException
	{
	AcousticTrafficMonitor atm;
			
		atm=new SimulationDemo();
		atm.start();
	}
}
