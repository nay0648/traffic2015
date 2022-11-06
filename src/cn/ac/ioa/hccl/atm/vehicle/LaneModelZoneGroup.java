/**
 * Created on: 2015Äê4ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.gui.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.corr.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Detection vehicles by cross section responses.
 * 
 * @author nay0648
 */
public class LaneModelZoneGroup implements Serializable
{
private static final long serialVersionUID = 3025899802766120315L;
private static final double EPS=2.220446049250313e-16;
private static final double WV_ATTEN=100;//the attenuation of the vertical beamformer (dB)
/*
 * parameters for synthetic correlation matrix
 */
private double scanaz1=-90;
private double scanaz2=90;
private int numscans=181;

private double diagload=1e-2;//to prevent singular correlation matrix
private double diffusefactor=0;//coefficient of the diffuse noise
private int peakradius=3;//used to detect peaks
private CrossCorrelator corr;//correlation matrices
private AutomaticGainControl agc;//the agc reference
private int numlanes;//number of lanes
private double[] scanaz;//scanning azimuths
private ComplexVector[][] ah;//the steering vectors [bin index][az index]
private ComplexVector[][] wh;//horizontal beamformers [bin index][az index]
private ElevationGroup[] elgroup;//zones in the same elevation

	/**
	 * @param agc
	 * agc reference
	 * @param corr
	 * the correlation matrix accumulator
	 * @param numlanes
	 * number of lanes
	 * @param elevation
	 * elevations of the detection zones
	 */
	public LaneModelZoneGroup(
			AutomaticGainControl agc,CrossCorrelator corr,int numlanes,double... elevation)
	{
		this.agc=agc;
		this.corr=corr;
		this.numlanes=numlanes;
		wh=new ComplexVector[corr.numFrequencyBins()][numscans];
		
		//generate steering vectors
		{
		SensorArray array;
		double f,c;
		SphericalPoint[] fdir;
		
			//for the horizontal subarray
			array=corr.monitorArray().horizontalSubarray();
			
			fdir=Beamformer.azimuthScan(scanaz1, scanaz2, numscans);
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
		
		/*
		 * generate elevation groups
		 */
		elgroup=new ElevationGroup[elevation.length];
		for(int i=0;i<elgroup.length;i++) elgroup[i]=new ElevationGroup(elevation[i]);
	}
	
	/**
	 * get a vehicle detection zone
	 * @param laneidx
	 * lane index
	 * @param elidx
	 * elevation index
	 * @return
	 */
	public VehicleDetectionZone zone(int laneidx, int elidx)
	{
		return elgroup[elidx].zone(laneidx);
	}
	
	/**
	 * update the lane model according to the new lane positions
	 */
	private void updateLaneModel()
	{
	double[] g;
	
		//generate the weight
		{
		double[] mu,sigma;
		VehicleDetector vdet;
		LaneDetector.LanePosition pos;
		double nu;
		
			mu=new double[numlanes];
			sigma=new double[numlanes];
			vdet=zone(0, 0).getLane().getVehicleDetector();
			
			for(int i=0;i<numlanes;i++) 
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
//					g[azidx]+=Math.exp(-nu*nu/(2*sigma[i]*sigma[i]))/(Math.sqrt(2*Math.PI)*sigma[i]);
					g[azidx]+=Math.exp(-nu*nu/(2*sigma[i]*sigma[i]));
				}
		
			//weight
			BLAS.scalarMultiply(1.0/BLAS.sum(g), g, g);
		}
	
		//update the horizontal beamformers
		{
		CorrelationMatrix mr;
		ComplexMatrix imr;
		ComplexVector gah;
		int idx;
		double tr;
		ComplexVector a;
		
			mr=new CorrelationMatrix(ah[0][0].size());
			imr=new ComplexMatrix(ah[0][0].size(),ah[0][0].size());
			gah=new ComplexVector(ah[0][0].size());
		
			for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
			{
				idx=fi-corr.startingFrequencyBinIndex();
				mr.clear();
				
				//accumulate the synthetic correlation
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
				
				imr=BLAS.hinv(mr, imr);
				
				//update beamformers
				for(int azidx=0;azidx<wh[idx].length;azidx++)
				{
					a=ah[idx][azidx];
					
//					wh[idx][azidx]=BLAS.multiply(imr, a, wh[idx][azidx]);
//					wh[idx][azidx]=BLAS.scalarMultiply(
//							1.0/BLAS.quadraticForm(imr, a), 
//							wh[idx][azidx], 
//							wh[idx][azidx]);
					
					
					
					wh[idx][azidx]=a;
					
					
					
				}
			}
		}
	}
	
	/**
	 * Contain detection zones in the same elevation.
	 * @author nay0648
	 */
	private class ElevationGroup implements Serializable
	{
	private static final long serialVersionUID = 7715225841957550756L;
	private double elevation;//the elevation
	private ComplexVector[] wv;//beamformer for vertical subarray [bin index]
	private Zone[] zones;//detection zones in the same elevation
	private double[] sectionres;//cross section response
	private List<Peak> peakval;//the peaks on the response curve
	private long frameidx=-1;//frame index
	
	
	
	private Monitor1D gui1=null;
	private double[] sres;
	DoubleHeapGainControl agc2;
	
	
	
		/**
		 * @param elevation
		 * the elevation
		 */
		public ElevationGroup(double elevation)
		{
		CrossMonitor monitor;
		Beamformer vbf;

			this.elevation=elevation;
			sectionres=new double[numscans];
			peakval=new ArrayList<Peak>(numlanes*3);
			
			/*
			 * generate vertical beamformers
			 */
			wv=new ComplexVector[corr.numFrequencyBins()];
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
				BLAS.entryMultiply(Window.cheb(wv[i].size(), WV_ATTEN), wv[i], wv[i]);		
			}
			
			/*
			 * generate vehicle detection zones
			 */
			zones=new Zone[numlanes];
			for(int i=0;i<zones.length;i++) zones[i]=new Zone(this);
			
			
			
			if(elevation==0) 
			{	
				gui1=new Monitor1D(scanaz);
				sres=new double[sectionres.length];
				agc2=new DoubleHeapGainControl(corr.monitorArray());
				agc2.setForegroundOnly(true);
			}
		}
		
		/**
		 * get the elevation
		 * @return
		 */
		public double elevation()
		{
			return elevation;
		}
		
		/**
		 * get the vehicle detection zone
		 * @param laneidx
		 * lane index
		 * @return
		 */
		public VehicleDetectionZone zone(int laneidx)
		{
			return zones[laneidx];
		}
		
		/**
		 * update the cross section response
		 */
		public void updateCrossSectionResponse()
		{
		long fidx;
		int idx;
		double meanres;
			
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				//calculate the response
				for(int azidx=0;azidx<sectionres.length;azidx++) 
				{
					sectionres[azidx]=0;
					for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
					{
						idx=fi-corr.startingFrequencyBinIndex();
						
						sectionres[azidx]+=Math.log(VehicleDetectionZone.crossResponse(
								wh[idx][azidx], 
								corr.correlationMatrix(fi), 
								wv[idx]));
					}
					sectionres[azidx]=Math.exp(sectionres[azidx]/corr.numFrequencyBins());
				}
				
				/*
				 * remove sidelobes
				 */
				meanres=BLAS.mean(sectionres);
				for(int i=0;i<sectionres.length;i++) 
				{
					sectionres[i]-=meanres;
					if(sectionres[i]<0) sectionres[i]=0;
				}
				
				
				
				if(gui1!=null) 
				{			
					System.arraycopy(sectionres, 0, sres, 0, sres.length);
					agc2.normalize(sres);
					gui1.setSlicedResponse(sres);
				}
				
				
				
				//detect peaks
				{
					peakval.clear();
					
					for(int i=0;i<sectionres.length;i++) 
					{
					int i1,i2;
					double lmax=0;
					int lmaxi=0;
										
						/*
						 * detecting boundaries
						 */
						i1=i-peakradius;
						if(i1<0) i1=0;
						i2=i+peakradius;
						if(i2>sectionres.length-1) i2=sectionres.length-1;
						
						for(int ii=i1;ii<=i2;ii++) 
						{
							//find peaks
							if(sectionres[ii]>lmax) 
							{
								lmax=sectionres[ii];
								lmaxi=ii;
							}	
						}
						
						if(lmaxi==i&&sectionres[i]>0) 
							peakval.add(new Peak(i,sectionres[i]));
					}
					
					Collections.sort(peakval);
				}
				
				frameidx=fidx;
			}
		}
		
		/**
		 * get the detected peaks
		 * @return
		 */
		public List<Peak> peaks()
		{
			return peakval;
		}
	}
	
	/**
	 * Used to detect peaks.
	 * @author nay0648
	 */
	private class Peak implements Comparable<Peak>
	{
	int peakidx;
	double peakval;
	
		private Peak(int peakidx,double peakval)
		{
			this.peakidx=peakidx;
			this.peakval=peakval;
		}
	
		public int compareTo(Peak o) 
		{
			if(peakval>o.peakval) return -1;
			else if(peakval<o.peakval) return 1;
			else return 0;
		}
	}
	
	/**
	 * A vehicle detection zone
	 * @author nay0648
	 */
	private class Zone extends VehicleDetectionZone
	{
	private static final long serialVersionUID = -7875338462973800959L;
	private ElevationGroup elgroup;//get the elevation group
	private long frameidx=-1;
	private double res=0;
	
		/**
		 * @param elgroup
		 * elevation group
		 */
		public Zone(ElevationGroup elgroup)
		{
			super(agc);
			this.elgroup=elgroup;
		}
		
		public double getElevation() 
		{
			return elgroup.elevation();
		}
		
		public void setElevation(double elevation) 
		{
			throw new UnsupportedOperationException("cannot set elevation");
		}
		
		public void updateZone() 
		{
			if(this.getLane().laneIndex()==numlanes-1&&this.getZoneIndex()==0) 
				LaneModelZoneGroup.this.updateLaneModel();
		}
		
		public double zoneResponse() throws IOException 
		{
		long fidx;
		LaneDetector.LanePosition pos;
		List<Peak> peaks;
		Peak peak;
		
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				//update one time for the same elevation
				if(this.getLane().laneIndex()==0) elgroup.updateCrossSectionResponse();
				
				res=0;
				pos=this.getLane().getLanePosition();
				
				if(pos!=null)
				{
					peaks=elgroup.peaks();
					for(int i=0;i<Math.min(numlanes, peaks.size());i++) 
					{
						peak=peaks.get(i);
						if(scanaz[peak.peakidx]>pos.lowerBoundAzimuth()
								&&scanaz[peak.peakidx]<pos.upperBoundAzimuth())
						{
							res=peak.peakval;
							break;
						}
					}
				}
				
				frameidx=fidx;
			}
			
			return res;
		}	
	}
}
