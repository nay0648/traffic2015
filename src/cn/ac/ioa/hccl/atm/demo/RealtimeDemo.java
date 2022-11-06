/**
 * Created on: 2015Äê5ÔÂ20ÈÕ
 */
package cn.ac.ioa.hccl.atm.demo;
import java.io.*;
import java.net.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * For real time data.
 * 
 * @author nay0648
 */
public class RealtimeDemo extends AcousticTrafficMonitor
{
private static final int NUM_CHANNELS=37;
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
private static final double ZONE_ELEVATION=3;

private CrossCorrelator corr,corr2;//used to calculate correlation matrix
private LaneDetector ldetector;//used to detect lanes
private VehicleDetector vdetector;//used to watch vehicles
	
	/**
	 * @param monitor
	 * the array, already set signal source and smaple rate
	 * @param numlanes
	 * number of lanes
	 * @throws IOException
	 */
	public RealtimeDemo(CrossMonitor monitor,int numlanes) throws IOException
	{
	super(monitor,numlanes);
	int numframes;
	int fi1,fi2;
	Lane[] lanes;
	AutomaticGainControl vagc;
	LaneModelZoneGroup zones;
			
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
		((ParzenGainControl)vagc).setForegroundOnly(true);
		
		lanes=new Lane[numlanes];
		zones=new LaneModelZoneGroup(vagc,corr2,numlanes,-ZONE_ELEVATION,0,ZONE_ELEVATION);
		
		for(int i=0;i<lanes.length;i++) 
		{
			lanes[i]=new Lane(i);
			
			lanes[i].addZone(zones.zone(i, 0));
			lanes[i].addZone(zones.zone(i, 1));
			lanes[i].addZone(zones.zone(i, 2));
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
	CrossMonitor monitor;
	Socket socket=null;
	RawSignalSource rs;
	AcousticTrafficMonitor atm;
			
		try
		{
			monitor=new CrossMonitor(
					ARRAY_GEOM,
					FS,
					STFT_SHIFT,
					FFT_SIZE);
				
			socket=new Socket("localhost",4499);
			rs=new RawSignalSource(new BufferedInputStream(socket.getInputStream()),NUM_CHANNELS);
			monitor.setSignalSource(rs);
		
			atm=new RealtimeDemo(monitor,NUM_LANES);
			atm.start();
		}
		catch(IOException e)
		{
			try
			{
				if(socket!=null) socket.close();
			}
			catch(IOException ee)
			{}
		}
	}
}
