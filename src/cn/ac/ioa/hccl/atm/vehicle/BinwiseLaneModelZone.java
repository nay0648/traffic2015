/**
 * Created on: 2015Äê4ÔÂ2ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.corr.CorrelationMatrix;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Binwise maximum directivity beamformer according to the Gaussian lane model.
 * 
 * @author nay0648
 */
public class BinwiseLaneModelZone extends VehicleDetectionZone
{
private static final long serialVersionUID = 6168844864922318420L;
private static final double VTAPER_ATTEN=60;//the attenuation of the vertical beamformer (dB)
private static HorizontalBeamformer WH=null;//used to generate horizontal beamformers
private double diagload=1e-2;//to prevent singular correlation matrix
private double diffusefactor=0;//coefficient of the diffuse noise
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
	public BinwiseLaneModelZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
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
			BLAS.entryMultiply(Window.cheb(wv[i].size(),VTAPER_ATTEN), wv[i], wv[i]);		
		}
	}
	
	/**
	 * get the horizontal beamformer
	 * @return
	 */
	public HorizontalBeamformer horizontalBeamformer()
	{
		if(WH==null) WH=new HorizontalBeamformer();
		return WH;
	}
	
	public void updateZone() 
	{
	int numlanes;
		
		numlanes=this.getLane().getVehicleDetector().numLanes();
		//wait untill the last lane to get the complete lane position information
		if(this.getLane().laneIndex()==numlanes-1&&this.getZoneIndex()==0) 
			horizontalBeamformer().updateLaneModel();
	}
	
	public double zoneResponse() throws IOException 
	{
	long fidx;
	HorizontalBeamformer whdesign;
	
		whdesign=horizontalBeamformer();
		if(whdesign==null) res=0;
		else
		{
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				/*
				 * geometric mean
				 */
				res=0;
				for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
					res+=Math.log(VehicleDetectionZone.crossResponse(
							whdesign.horizontalBeamformer(this.getLane().laneIndex(), fi), 
							corr.correlationMatrix(fi), 
							wv[fi-corr.startingFrequencyBinIndex()]));
				res=Math.exp(res/corr.numFrequencyBins());
				
				/*
				 * arithmetic mean
				 */
//				res=0;
//				for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
//					res+=VehicleDetectionZone.crossResponse(
//							whdesign.horizontalBeamformer(this.getLane().laneIndex(), fi), 
//							corr.correlationMatrix(fi), 
//							wv[fi-corr.startingFrequencyBinIndex()]);
//				res/=corr.numFrequencyBins();
				
				frameidx=fidx;
			}
		}
		
		return res;
	}
	
	/**
	 * Represents horizontal beamformers for each frequency bin.
	 * 
	 * @author nay0648
	 */
	private class HorizontalBeamformer implements Serializable
	{
	private static final long serialVersionUID = 4126964559495465930L;
	private static final double EPS=2.220446049250313e-16;
	private double[] scanaz;//scanning azimuths
	private ComplexVector[][] ah;//the steering vectors [bin index][az index]
	private ComplexMatrix[] imr;//the inverse syhthetic correlation matrix for each bin [bin index]
	private ComplexVector[][] wh;//horizontal beamformers for each lane [lane index][bin index]
	
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
				
				scanaz=new double[fdir.length];
				for(int azidx=0;azidx<scanaz.length;azidx++) scanaz[azidx]=fdir[azidx].getAzimuth();
				
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
			
			imr=new ComplexMatrix[corr.numFrequencyBins()];
			wh=new ComplexVector[BinwiseLaneModelZone.this.getLane()
			                     .getVehicleDetector().numLanes()][corr.numFrequencyBins()];
		}
	
		/**
		 * update the lane model according to the new lane positions
		 */
		public void updateLaneModel()
		{
		double[] g;
		
			//generate the weight
			{
			VehicleDetector vdet;
			double[] mu,sigma;
			LaneDetector.LanePosition pos;
			double nu;
		
				vdet=BinwiseLaneModelZone.this.getLane().getVehicleDetector();
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
		
			//update the inverse correlation
			{
			CorrelationMatrix mr;
			ComplexVector gah;
			int idx;
			double tr;
			
				mr=new CorrelationMatrix(ah[0][0].size());
				gah=new ComplexVector(ah[0][0].size());
			
				for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
				{
					idx=fi-corr.startingFrequencyBinIndex();
					mr.clear();
					
					for(int azidx=0;azidx<ah[0].length;azidx++) 
					{
						if(g[azidx]<EPS) continue;
							
						//the weighted steering vector
						BLAS.scalarMultiply(g[azidx], ah[idx][azidx], gah);
						mr.accumulate(gah);
					}
					
					/*
					 * calculate the inverse
					 */
					tr=0;
					for(int i=0;i<mr.numRows();i++) tr+=mr.getReal(i, i);
					BLAS.scalarMultiply(1.0/tr, mr, mr);
					
					for(int i=0;i<mr.numRows();i++) mr.real()[i][i]+=diagload;
					
					imr[idx]=BLAS.hinv(mr, imr[idx]);
				}
			}
			
			//update the horizontal beamformers
			{
			LaneDetector.LanePosition pos;
			int idx;
			
				for(int laneidx=0;laneidx<wh.length;laneidx++) 
				{
					//get correct lane position
					pos=BinwiseLaneModelZone.this.getLane().
							getVehicleDetector().lane(laneidx).getLanePosition();
					if(pos==null) continue;
					
					for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
					{
						idx=fi-corr.startingFrequencyBinIndex();
						wh[laneidx][idx]=generateHorizontalBeamformer(
								fi, 
								(pos.lowerBoundIndex()+pos.upperBoundIndex())/2, 
								wh[laneidx][idx]);
					}
				}
			}
		}
		
		/**
		 * generate the horizontal beamformer
		 * @param fi
		 * frequency bin index
		 * @param azidx
		 * scanning azimuth index
		 * @param wh
		 * destination space, null to allocate new space
		 * @return
		 */
		private ComplexVector generateHorizontalBeamformer(int fi,int azidx,ComplexVector wh)
		{
		ComplexVector a;
		ComplexMatrix ic;
		
			a=ah[fi-corr.startingFrequencyBinIndex()][azidx];
			ic=imr[fi-corr.startingFrequencyBinIndex()];
			
			if(wh==null) wh=new ComplexVector(a.size());
			else BLAS.checkDestinationSize(wh, a.size());
			
			wh=BLAS.multiply(ic, a, wh);
			wh=BLAS.scalarMultiply(1.0/BLAS.quadraticForm(ic, a), wh, wh);
			
			return wh;
		}
		
		/**
		 * get the horizontal beamformer
		 * @param laneidx
		 * lane index
		 * @param fi
		 * frequency bin index
		 * @return
		 */
		public ComplexVector horizontalBeamformer(int laneidx,int fi)
		{
			return wh[laneidx][fi-corr.startingFrequencyBinIndex()];
		}
	}
}
