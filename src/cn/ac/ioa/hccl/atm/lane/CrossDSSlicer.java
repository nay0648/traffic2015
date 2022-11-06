/**
 * Created on: Nov 20, 2014
 */
package cn.ac.ioa.hccl.atm.lane;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.gui.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * The cross section is constructed by the delay-and-sum beamforming implemented in 
 * Mills cross multiplicative algorithm.
 * 
 * @author nay0648
 */
public class CrossDSSlicer extends SliceGenerator
{
private static final long serialVersionUID = -7902274458902917135L;
private CrossCorrelator corr;//correlation accumulator
private ComplexMatrix mc=null;//the correlation matrix
private ComplexVector[] wh;//beamformer for each scanning direction
private ComplexVector wv;//beamformer for the vertical subarray
	
	/**
	 * @param corr
	 * the correlation matrix accumulator
	 */
	public CrossDSSlicer(CrossCorrelator corr)
	{
	super(corr.monitorArray());
	CrossMonitor monitor;
	Beamformer bfdesign;
	double[] scanaz;
	SphericalPoint[] fdir;

		this.corr=corr;
		monitor=corr.monitorArray();
	
		/*
		 * construct beamformers
		 */
		scanaz=this.scanningAzimuth();
		fdir=new SphericalPoint[scanaz.length];
		for(int azidx=0;azidx<fdir.length;azidx++) 
			fdir[azidx]=new SphericalPoint(scanaz[azidx],0,1);
		
		bfdesign=new Beamformer(
				//only use the horizontal subarray
				monitor.horizontalSubarray(),
				//using middle frequency bin
				monitor.binIndex2Frequency(
						(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2),
				Param.soundSpeed());	
		wh=new ComplexVector[this.numScans()];
		for(int azidx=0;azidx<scanaz.length;azidx++) 
			wh[azidx]=bfdesign.delayAndSumBeamformer(fdir[azidx]);
		
		/*
		 * beamformer for the vertical subarray
		 */
		bfdesign=new Beamformer(
				//only use the vertical subarray
				monitor.verticalSubarray(),
				//using middle frequency bin
				monitor.binIndex2Frequency(
						(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2),
				Param.soundSpeed());
		
		//zero degree elevation
		wv=bfdesign.delayAndSumBeamformer(new SphericalPoint(0,0,1));
		//apply a window to attenuate the sound outside the cross section
		BLAS.entryMultiply(Window.cheb(wv.size()), wv, wv);
	}
	
	public double[] slicedResponse(double[] response) throws IOException 
	{
	double[][] c,d;
	double[] a,b,e,f;
	double tr,ti,real,imag;
	double resmean;
	
		if(response==null) response=new double[this.numScans()];
		else BLAS.checkDestinationSize(response, this.numScans());
		
		mc=corr.correlationMatrix(mc);
		c=mc.real();
		d=mc.imaginary();
		
		e=wv.real();
		f=wv.imaginary();
		
		//calculate the energy spectrum
		for(int azidx=0;azidx<response.length;azidx++) 
		{
			a=wh[azidx].real();
			b=wh[azidx].imaginary();
			
			/*
			 * calculate wh^H*C*wv
			 */
			real=0;
			imag=0;
			
			for(int i=0;i<mc.numRows();i++) 
				for(int j=0;j<mc.numColumns();j++) 
				{
					tr=a[i]*c[i][j]+b[i]*d[i][j];
					ti=a[i]*d[i][j]-b[i]*c[i][j];
					
					real+=tr*e[j]-ti*f[j];
					imag+=tr*f[j]+ti*e[j];
				}
			
			response[azidx]=real*real+imag*imag;	
		}
		
		/*
		 * remove the response in the entire cross section
		 */
		resmean=BLAS.mean(response);
		for(int azidx=0;azidx<response.length;azidx++) 
		{
			response[azidx]-=resmean;
			if(response[azidx]<0) response[azidx]=0;
		}
		
		return response;
	}
	
	public static void main(String[] args) throws IOException
	{
	CrossMonitor monitor;
	ArrayTestSignalSource x;
	int fi1,fi2;
	CrossCorrelator corr;
	SliceGenerator slice;
	AutomaticGainControl agc;
	Monitor1D gui;
	double[] response=null;
	LaneDetector lanedet;
	Monitor2D gui2;
	
		monitor=new CrossMonitor(
				new File("arraygeometry/crossarray8k.txt"),
				20000,
				256,
				512);
		x=new ArrayTestSignalSource(monitor,20000);
//		x.setScanningPattern(null);
		x.setScanningPattern(new HighwayScan());
//		x.setFocalDirection(new SphericalPoint(-30,0,1));
		monitor.setSignalSource(x);
		
		fi1=monitor.frequency2BinIndex(6500);
		fi2=monitor.frequency2BinIndex(7000);
		System.out.println(monitor.numFramesInTimeInterval(Param.CORR_TIME_WINDOW)+", "+(fi2-fi1+1));
		corr=new CrossCorrelator(
				monitor,
				monitor.numFramesInTimeInterval(Param.CORR_TIME_WINDOW),
				fi1,
				fi2);
		
		slice=new CrossDSSlicer(corr);
		agc=new HeapGainControl(monitor);
		gui=new Monitor1D(slice.scanningAzimuth());	
		
		lanedet=new PeakValleyLaneDetector(slice,3);
		gui2=new Monitor2D(lanedet,400);
		
		for(;monitor.hasNextFrame();) 
		{
			corr.updateTrafficInfo();
			
			response=slice.slicedResponse(response);
			agc.normalize(response);
			gui.setSlicedResponse(response);
			gui.setFrameTime(monitor.frameTime());
			
			lanedet.updateTrafficInfo();
			gui2.updateTrafficInfo();
		}	
	}
}
