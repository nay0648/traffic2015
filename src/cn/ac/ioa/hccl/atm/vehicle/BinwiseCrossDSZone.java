/**
 * Created on: 2015Äê3ÔÂ17ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Calculate the bin-wise delay-and-sum beamforming.
 * 
 * @author nay0648
 */
public class BinwiseCrossDSZone extends VehicleDetectionZone
{
private static final long serialVersionUID = 8904168927795651839L;
private CrossCorrelator corr;//correlation matrices
private double elevation;//zone elevation
private Beamformer[] hbf;//used to generate beamformers for the horizontal subarray
private ComplexVector[] wh;//beamformer for horizontal subarray
private ComplexVector[] wv;//beamformer for vertical subarray
private double res=0;
private long frameidx=-1;
	
	/**
	 * @param agc
	 * the AGC regerence
	 * @param corr
	 * used to calculate the correlation matrix
	 * @param elevation
	 * beamformer elevation
	 */
	public BinwiseCrossDSZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
	{
	super(agc);
	CrossMonitor monitor;
					
		this.corr=corr;
		monitor=corr.monitorArray();
		
		/*
		 * space for beamformers
		 */
		wh=new ComplexVector[corr.numFrequencyBins()];
		wv=new ComplexVector[corr.numFrequencyBins()];
		
		//generate vertical beamformer
		setElevation(elevation);
		
		/*
		 * for horizontal beamformer
		 */
		hbf=new Beamformer[corr.numFrequencyBins()];
		for(int i=0;i<hbf.length;i++) 
			hbf[i]=new Beamformer(
					//use the horizontal subarray
					monitor.horizontalSubarray(),
					monitor.binIndex2Frequency(corr.startingFrequencyBinIndex()+i),
					Param.soundSpeed());
	}
	
	public double getElevation() 
	{
		return elevation;
	}
	
	public void setElevation(double elevation) 
	{
	CrossMonitor monitor;
	Beamformer vbf;
		
		this.elevation=elevation;
		
		monitor=corr.monitorArray();
		for(int i=0;i<wv.length;i++) 
		{
			vbf=new Beamformer(
					//use the vertical subarray
					monitor.verticalSubarray(),
					monitor.binIndex2Frequency(corr.startingFrequencyBinIndex()+i),
					Param.soundSpeed());
			
			wv[i]=vbf.delayAndSumBeamformer(new SphericalPoint(0,elevation,1));
			//apply a window to attenuate the sound outside the cross section
			BLAS.entryMultiply(Window.cheb(wv[i].size()), wv[i], wv[i]);		
		}
	}
	
	public void updateZone() 
	{
	LaneDetector.LanePosition pos;
//	double[] win=null;
		
		pos=this.getLane().getLanePosition();
		if(pos==null) 
		{
			Arrays.fill(wh, null);
			return;
		}
		
		for(int i=0;i<wh.length;i++) 
		{
			wh[i]=hbf[i].delayAndSumBeamformer(new SphericalPoint(
					//use the average lane width as the lane center, not the center azimuth
					(pos.lowerBoundAzimuth()+pos.upperBoundAzimuth())/2,elevation,1));
			
//			if(win==null) win=Window.cheb(wh[i].size(), 45);
//			BLAS.entryMultiply(win, wh[i], wh[i]);
		}
	}
	
	public double zoneResponse() throws IOException 
	{
	long fidx;
	
		fidx=corr.monitorArray().frameIndex();
		if(fidx!=frameidx) 
		{
			/*
			 * geometric mean
			 */
			res=0;
			for(int i=0;i<corr.numFrequencyBins();i++) 
				res+=Math.log(VehicleDetectionZone.crossResponse(
						wh[i], 
						corr.correlationMatrix(corr.startingFrequencyBinIndex()+i), 
						wv[i]));
			res=Math.exp(res/corr.numFrequencyBins());
			
			/*
			 * arithmetic mean
			 */
//			res=0;
//			for(int i=0;i<corr.numFrequencyBins();i++) 
//				res+=VehicleDetectionZone.crossResponse(
//						wh[i], 
//						corr.correlationMatrix(corr.startingFrequencyBinIndex()+i), 
//						wv[i]);
//			res/=corr.numFrequencyBins();
			
			frameidx=fidx;
		}
		
		return res;
	}
}
