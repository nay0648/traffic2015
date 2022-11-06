/**
 * Created on: 2015Äê3ÔÂ16ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Vehicle detection zone via the delay-and-sum beamforming, looking at the lane center.
 * 
 * @author nay0648
 */
public class CrossDSZone extends VehicleDetectionZone
{
private static final long serialVersionUID = 2538916930598170843L;
private CrossCorrelator corr;//correlation matrices
private ComplexMatrix mc=null;//the correlation matrix
private double elevation;//zone elevation
private Beamformer hbf;//used to generate beamformers for the horizontal subarray
private ComplexVector wh;//beamformer for horizontal subarray
private ComplexVector wv;//beamformer for vertical subarray
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
	public CrossDSZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
	{
	super(agc);
	CrossMonitor monitor;
	int fi;
				
		this.corr=corr;
		monitor=corr.monitorArray();
		//use the middle frequency bin to approximately represent the entire subband
		fi=(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2;
		
		//generate vertical beamformer
		setElevation(elevation);
			
		/*
		 * for horizontal beamformer
		 */
		hbf=new Beamformer(
				//use the horizontal subarray
				monitor.horizontalSubarray(),
				monitor.binIndex2Frequency(fi),
				Param.soundSpeed());
	}
	
	public double getElevation() 
	{
		return elevation;
	}
	
	public void setElevation(double elevation) 
	{
	CrossMonitor monitor;
	int fi;
	Beamformer bfdesign;
	
		this.elevation=elevation;
		
		monitor=corr.monitorArray();
		fi=(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2;
		
		bfdesign=new Beamformer(
				//use the vertical subarray
				monitor.verticalSubarray(),
				monitor.binIndex2Frequency(fi),
				Param.soundSpeed());
		
		wv=bfdesign.delayAndSumBeamformer(new SphericalPoint(0,elevation,1));
		//apply a window to attenuate the sound outside the cross section
		BLAS.entryMultiply(Window.cheb(wv.size()), wv, wv);
	}
	
	public void updateZone() 
	{
	LaneDetector.LanePosition pos;
	
		pos=this.getLane().getLanePosition();
		if(pos==null) 
		{
			wh=null;
			return;
		}
		
		wh=hbf.delayAndSumBeamformer(new SphericalPoint(
				//use the average lane width as the lane center, not the center azimuth
				(pos.lowerBoundAzimuth()+pos.upperBoundAzimuth())/2,elevation,1));
//		BLAS.entryMultiply(Window.cheb(wh.size(), 30), wh, wh);
	}
	
	public double zoneResponse() throws IOException 
	{
	long fidx;
	double[][] c,d;
	double[] a,b,e,f;
	double tr,ti,real=0,imag=0;
			
		if(wh==null) res=0;
		else
		{
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				/*
				 * calculate wh'*C*wv
				 */
				a=wh.real();
				b=wh.imaginary();
					
				mc=corr.correlationMatrix(mc);
				c=mc.real();
				d=mc.imaginary();
					
				e=wv.real();
				f=wv.imaginary();
					
				for(int i=0;i<mc.numRows();i++) 
					for(int j=0;j<mc.numColumns();j++) 
					{
						tr=a[i]*c[i][j]+b[i]*d[i][j];
						ti=a[i]*d[i][j]-b[i]*c[i][j];
							
						real+=tr*e[j]-ti*f[j];
						imag+=tr*f[j]+ti*e[j];
					}

				res=real*real+imag*imag;				
				frameidx=fidx;
			}
		}
			
		return res;
	}
}
