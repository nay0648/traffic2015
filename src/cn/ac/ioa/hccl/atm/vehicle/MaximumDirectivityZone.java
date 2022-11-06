/**
 * Created on: 2015Äê3ÔÂ19ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.corr.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Generate vehicle detection zones by maximum directivity beamforming. 
 * MVDR beamforming with fixed noise model.
 * 
 * @author nay0648
 */
public class MaximumDirectivityZone extends VehicleDetectionZone
{
private static final long serialVersionUID = 3394762944867636670L;
private static HorizontalSteeringVector AH=null;//the horizontal steering vectors
private static MaximumDirectivityBeamformer WH=null;//the horizontal beamformer
private double diagload=1e-6;//to prevent singular correlation matrix
private double diffusefactor=0.1;//coefficient of the diffuse noise
private CrossCorrelator corr;//correlation matrices
private ComplexMatrix mc=null;//the correlation matrix
private double elevation;//zone elevation
private ComplexVector wv;//beamformer for vertical subarray
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
	public MaximumDirectivityZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
	{
	super(agc);
	
		this.corr=corr;
		//generate vertical beamformer
		setElevation(elevation);
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
	int numlanes;
		
		numlanes=this.getLane().getVehicleDetector().numLanes();
		//wait untill the last lane to get the complete lane position information
		if(this.getLane().laneIndex()==numlanes-1&&this.getZoneIndex()==0) 
			WH=new MaximumDirectivityBeamformer();
	}
	
	public double zoneResponse() throws IOException 
	{
	LaneDetector.LanePosition pos;
	long fidx;
		
		pos=this.getLane().getLanePosition();
		
		if(pos==null||WH==null) res=0;
		else
		{
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				mc=corr.correlationMatrix(mc);
				res=VehicleDetectionZone.crossResponse(
						WH.wh[(pos.lowerBoundIndex()+pos.upperBoundIndex())/2], mc, wv);
				
				frameidx=fidx;
			}
		}
		
		return res;
	}
	
	/**
	 * get the horizontal steering vectors
	 * @return
	 */
	private HorizontalSteeringVector horizontalSteeringVectors()
	{
		if(AH==null) AH=new HorizontalSteeringVector();
		return AH;
	}
	
	/**
	 * Contains steering vectors from the horizontal subarray.
	 * 
	 * @author nay0648
	 */
	public class HorizontalSteeringVector implements Serializable
	{
	private static final long serialVersionUID = 1175192329555579311L;
	double[] scanaz;//scanning azimuths
	ComplexVector[] ah;//the steering vectors
	
		public HorizontalSteeringVector()
		{
		SensorArray array;
		double f,c;
		SphericalPoint[] fdir;
			
			//for the horizontal subarray
			array=corr.monitorArray().horizontalSubarray();
			f=corr.monitorArray().binIndex2Frequency(
					(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2);
			c=Param.soundSpeed();
			
			fdir=Beamformer.azimuthScan(
					Param.LANE_SCANAZ1, 
					Param.LANE_SCANAZ2, 
					Param.LANE_NUMSCANS);
			
			scanaz=new double[fdir.length];
			ah=new ComplexVector[fdir.length];
			for(int azidx=0;azidx<ah.length;azidx++) 
			{
				scanaz[azidx]=fdir[azidx].getAzimuth();
				ah[azidx]=array.steeringVector(fdir[azidx], f, c);
			}
		}
	}
	
	/**
	 * The maximum directivity beamformer.
	 * 
	 * @author nay0648
	 */
	private class MaximumDirectivityBeamformer implements Serializable
	{
	private static final long serialVersionUID = 2292666689648908569L;
	private static final double EPS=2.220446049250313e-16;
	ComplexVector[] wh;//the horizontal beamformers

		public MaximumDirectivityBeamformer()
		{
		ComplexVector[] ah;
		double[] scanaz;
		double[] g;
		ComplexMatrix imr;
		
			//generate scanning steering vectors
			{
			HorizontalSteeringVector hsv;
			
				hsv=horizontalSteeringVectors();
				ah=hsv.ah;
				scanaz=hsv.scanaz;
			}
		
			//generate the weight
			{
			VehicleDetector vdet;
			double[] mu,sigma;
			LaneDetector.LanePosition pos;
			double nu;
			
				vdet=MaximumDirectivityZone.this.getLane().getVehicleDetector();
				mu=new double[vdet.numLanes()];
				sigma=new double[vdet.numLanes()];
				
				for(int i=0;i<vdet.numLanes();i++) 
				{
					pos=vdet.lane(i).getLanePosition();
					if(pos==null) continue;
					
					mu[i]=pos.centerAzimuth();
					sigma[i]=Math.min(
							pos.centerAzimuth()-pos.lowerBoundAzimuth(), 
							pos.upperBoundAzimuth()-pos.centerAzimuth())/3;
				}
				
				/*
				 * the Gaussian weight
				 */
				g=new double[scanaz.length];
				Arrays.fill(g, diffusefactor);
				
				for(int azidx=0;azidx<g.length;azidx++) 
					for(int i=0;i<mu.length;i++) 
					{
						if(sigma[i]==0) continue;
						
						nu=scanaz[azidx]-mu[i];
						g[azidx]+=Math.exp(-nu*nu/(2*sigma[i]*sigma[i]));
					}
				
				//weight
				BLAS.scalarMultiply(1.0/BLAS.sum(g), g, g);
			}
			
			//accumulate the correlation matrix
			{
			CorrelationMatrix mr;
			ComplexVector gah;
			
				mr=new CorrelationMatrix(ah[0].size());
				gah=new ComplexVector(ah[0].size());
				
				for(int azidx=0;azidx<ah.length;azidx++) 
				{
					if(g[azidx]<EPS) continue;
					
					//the weighted steering vector
					BLAS.scalarMultiply(g[azidx], ah[azidx], gah);
					mr.accumulate(gah);
				}
								
				/*
				 * calculate the inverse
				 */
				for(int i=0;i<mr.numRows();i++) mr.real()[i][i]+=diagload;
				imr=BLAS.hinv(mr, null);
			}
			
			//construct the horizontal beamformers
			{
				wh=new ComplexVector[ah.length];
				
				for(int azidx=0;azidx<wh.length;azidx++) 
				{
					wh[azidx]=BLAS.multiply(imr, ah[azidx], wh[azidx]);
					BLAS.scalarMultiply(1.0/BLAS.quadraticForm(imr, ah[azidx]), wh[azidx], wh[azidx]);
				}	
			}
		}
	}
}
