/**
 * Created on: 2015Äê4ÔÂ3ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;

import java.io.*;

import cn.ac.ioa.hccl.atm.Param;
import cn.ac.ioa.hccl.atm.agc.AutomaticGainControl;
import cn.ac.ioa.hccl.atm.array.Beamformer;
import cn.ac.ioa.hccl.atm.array.CrossMonitor;
import cn.ac.ioa.hccl.atm.array.SensorArray;
import cn.ac.ioa.hccl.atm.array.SphericalPoint;
import cn.ac.ioa.hccl.atm.corr.CrossCorrelator;
import cn.ac.ioa.hccl.libsp.util.BLAS;
import cn.ac.ioa.hccl.libsp.util.ComplexMatrix;
import cn.ac.ioa.hccl.libsp.util.ComplexVector;
import cn.ac.ioa.hccl.libsp.util.Window;

/**
 * Binwise least-square beamforming approach.
 * @author nay0648
 */
public class BinwiseCrossLSZone extends VehicleDetectionZone
{
private static final long serialVersionUID = -6498621328660580967L;
private CrossCorrelator corr;//correlation matrices
private double elevation;//zone elevation
private ComplexVector[] wv;//beamformer for vertical subarray [bin index]
private double res=0;
private long frameidx=-1;

	/**
	 * @param agc
	 * the AGC module
	 * @param corr
	 * used to calculate the correlation matrix
	 * @param elevation
	 * detection zone elevation
	 */
	public BinwiseCrossLSZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
	{
	super(agc);
	
		this.corr=corr;
		//space for beamformers
		wv=new ComplexVector[corr.numFrequencyBins()];
		//generate vertical beamformers
		setElevation(elevation);
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
		// TODO Auto-generated method stub
		
	}
	
	public double zoneResponse() throws IOException 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Used to construct horizontal beamformers.
	 * @author nay0648
	 */
	private class HorizontalBeamformer implements Serializable
	{
	private static final long serialVersionUID = -8485249285477075405L;
	private ComplexVector[][] ah;//the steering vectors [bin index][az index]
	
		public HorizontalBeamformer()
		{
			//generate steering vectors
			{
			SensorArray array;
			double f,c;
			SphericalPoint[] fdir;
			
				//for the horizontal subarray
				array=corr.monitorArray().horizontalSubarray();
				
				fdir=Beamformer.azimuthScan(
						Param.LANE_SCANAZ1, 
						Param.LANE_SCANAZ2, 
						Param.LANE_NUMSCANS);
				
				ah=new ComplexVector[corr.numFrequencyBins()][fdir.length];
				c=Param.soundSpeed();
				
				for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
				{
					f=corr.monitorArray().binIndex2Frequency(fi);
					
					for(int azidx=0;azidx<fdir.length;azidx++) 
						ah[fi-corr.startingFrequencyBinIndex()][azidx]=
								array.steeringVector(fdir[azidx], f, c);
				}
			}
		}
	}
}
