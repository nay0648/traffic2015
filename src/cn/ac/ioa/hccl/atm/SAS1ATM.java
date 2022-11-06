/**
 * Created on: Nov 3, 2014
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;

import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.atm.vehicle.*;
import experiment.slicer.DSSlicer;

/**
 * @author nay0648
 * the SAS-1 system simulation.
 */
public class SAS1ATM extends AcousticTrafficMonitor
{
private SAS1Correlator finecorr,coarsecorr;//used to calculate correlation matrix
private double fexpdiff=2500;//the difference of expected frequencies between two detection zones
private LaneDetector ldetector;//used to detect lanes
private VehicleDetector vdetector;//used to watch vehicles

	/**
	 * for experiment
	 * @throws IOException 
	 */
	public SAS1ATM() throws IOException
	{
	this(new SAS1Monitor(
			Param.DEFFS,Param.FRAMESHIFT,Param.FFTSIZE),Param.NUMLANES);
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
	public SAS1ATM(SAS1Monitor monitor,int numlanes) throws IOException
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
		fi1=monitor.frequency2BinIndex(Param.CORR_FEXP_FIRST1-fexpdiff);
		fi2=monitor.frequency2BinIndex(Param.CORR_FEXP_FIRST2-fexpdiff);
		coarsecorr=new SAS1Correlator(monitor,numframes,fi1,fi2);
		
		fi1=monitor.frequency2BinIndex(Param.CORR_FEXP_SECOND1);
		fi2=monitor.frequency2BinIndex(Param.CORR_FEXP_SECOND2);
		finecorr=new SAS1Correlator(monitor,numframes,fi1,fi2);
		
		//lane detector
		ldetector=new PeakValleyLaneDetector(new DSSlicer(finecorr),numlanes);
//		ldetector=new ParzenLaneDetector(new MVDRSlicer(finecorr),numlanes);
		
		/*
		 * construct vehicle detector
		 */
		vagc=new DoubleHeapGainControl(monitor);
		lanes=new Lane[numlanes];
		
		for(int i=0;i<lanes.length;i++) 
			lanes[i]=new Lane(
					i,
					new MVDRZone(vagc,coarsecorr,0),
					new MVDRZone(vagc,finecorr,0));
		
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
		coarsecorr.updateTrafficInfo();
		finecorr.updateTrafficInfo();
	}
	
	public static void main(String[] args) throws IOException
	{
	AcousticTrafficMonitor atm;
		
		atm=new SAS1ATM();
		atm.start();
	}
}
