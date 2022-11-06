/**
 * Created on: Nov 2, 2014
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;

import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;
import experiment.corr.InverseCorrelator;

/**
 * Detect vehicles by MVDR beamforming.
 * @author nay0648
 */
public class MVDRATM extends AcousticTrafficMonitor
{
private InverseCorrelator corr;//used to calculate correlation matrix and its inverse
private LaneDetector ldetector;
private VehicleDetector vdetector;
	
	/**
	 * Default constructor for experiments.
	 * @throws IOException
	 */
	public MVDRATM() throws IOException
	{
	this(new CrossMonitor(
			Param.DEFARRAYGEOM,Param.DEFFS,Param.FRAMESHIFT,Param.FFTSIZE),Param.NUMLANES);
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
	public MVDRATM(CrossMonitor monitor,int numlanes) throws IOException
	{
	super(monitor,numlanes);
	int numframes;
	int fi1,fi2;
	Lane[] lanes;
	AutomaticGainControl vagc;
		
		/*
		 * the correlator
		 */
		numframes=monitor.numFramesInTimeInterval(Param.CORR_TIME_WINDOW);
		fi1=monitor.frequency2BinIndex(Param.CORR_FEXP_FIRST1);
		fi2=monitor.frequency2BinIndex(Param.CORR_FEXP_FIRST2);
		corr=new InverseCorrelator(monitor,numframes,fi1,fi2);
		
		//lane detector
		ldetector=new ParzenLaneDetector(new MVDRSlicer(corr),numlanes);
		
		/*
		 * construct vehicle detector
		 */
		lanes=new Lane[numlanes];
		vagc=new DoubleHeapGainControl(monitor);
		
		for(int i=0;i<lanes.length;i++) 
			lanes[i]=new Lane(
					i,
					new MVDRZone(vagc,corr,-Param.ZONE_ELEVATION),
					new MVDRZone(vagc,corr,Param.ZONE_ELEVATION));
	
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
			
		atm=new MVDRATM();
		atm.start();
	}
}
