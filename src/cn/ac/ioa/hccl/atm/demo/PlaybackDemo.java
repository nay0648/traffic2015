/**
 * Created on: 2015Äê4ÔÂ15ÈÕ
 */
package cn.ac.ioa.hccl.atm.demo;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * The demo for real data.
 * 
 * @author nay0648
 */
public class PlaybackDemo extends AcousticTrafficMonitor
{
private static final int NUM_CHANNELS=37;
private static final File ARRAY_GEOM=new File("arraygeometry/crossarray16k.txt");
private static final double FS=32000;
private static final int FFT_SIZE=1024;
private static final int STFT_SHIFT=FFT_SIZE/2;
private static final int NUM_LANES=4;
private static final int CORR_TIME_WINDOW=200;
private static final int CORR_LOWER_FEXP=7000;
private static final int CORR_UPPER_FEXP=7500;
private static final double ZONE_ELEVATION=10;

private CrossCorrelator corr;//used to calculate correlation matrix
private LaneDetector ldetector;//used to detect lanes
private VehicleDetector vdetector;//used to watch vehicles
	
	/**
	 * @param monitor
	 * the array, already set signal source and smaple rate
	 * @param numlanes
	 * number of lanes
	 * @throws IOException
	 */
	public PlaybackDemo(CrossMonitor monitor,int numlanes) throws IOException
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
		
		//lane detector
		ldetector=new ParzenLaneDetector(new Rank1MUSICSlicer(corr),numlanes);
		
		/*
		 * construct vehicle detector
		 */
		vagc=new ParzenGainControl(monitor);
		((ParzenGainControl)vagc).setForegroundOnly(true);
		
		lanes=new Lane[numlanes];
		for(int i=0;i<lanes.length;i++) 
		{
			lanes[i]=new Lane(i);
			
			lanes[i].addZone(new BinwiseLaneModelZone(vagc,corr,-ZONE_ELEVATION));
//			lanes[i].addZone(new BinwiseLaneModelZone(vagc,corr,-ZONE_ELEVATION/2.0));
			lanes[i].addZone(new BinwiseLaneModelZone(vagc,corr,0));
//			lanes[i].addZone(new BinwiseLaneModelZone(vagc,corr,ZONE_ELEVATION/2.0));
			lanes[i].addZone(new BinwiseLaneModelZone(vagc,corr,ZONE_ELEVATION));
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
	CrossMonitor monitor;
	SignalSource source;
	AcousticTrafficMonitor atm;
		
		monitor=new CrossMonitor(
				ARRAY_GEOM,
				FS,
				STFT_SHIFT,
				FFT_SIZE);
		
		source=new RawSignalClips(
				true,
				NUM_CHANNELS,
				new File("D:/data/research/beamforming/dataset/vehiclenoise2015-02-06/20150206-135145.raw"));
		monitor.setSignalSource(source);
		
		atm=new PlaybackDemo(monitor,NUM_LANES);
		atm.start();
	}
}
