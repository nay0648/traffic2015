/**
 * Created on: 2015Äê3ÔÂ26ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.corr.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.corr.CorrelationMatrix;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Generate the ideal correlation matrix by the weighted results of DS beamforming.
 * 
 * @author nay0648
 */
public class WeightedMVDRZone extends VehicleDetectionZone
{
private static final long serialVersionUID = 4188892920270901799L;
private double diagload=1e-3;//to prevent singular correlation matrix
private CrossCorrelator corr;//correlation matrices
private ComplexMatrix mc=null;//the correlation matrix
private double elevation;//zone elevation
private ComplexVector wv;//beamformer for vertical subarray
private HorizontalBeamformer wh=null;//horizontal beamformer
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
	public WeightedMVDRZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
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
	Beamformer vbf;
		
		this.elevation=elevation;
		
		monitor=corr.monitorArray();
		fi=(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2;
			
		vbf=new Beamformer(
				//use the vertical subarray
				monitor.verticalSubarray(),
				monitor.binIndex2Frequency(fi),
				Param.soundSpeed());
			
		wv=vbf.delayAndSumBeamformer(new SphericalPoint(0,elevation,1));
		//apply a window to attenuate the sound outside the cross section
		BLAS.entryMultiply(Window.cheb(wv.size()), wv, wv);
	}
	
	public void updateZone() 
	{}
	
	public double zoneResponse() throws IOException 
	{
	LaneDetector.LanePosition pos;
	long fidx;
	HorizontalBeamformer wh;
			
		pos=this.getLane().getLanePosition();
			
		if(pos==null) res=0;
		else
		{
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				wh=horizontalBeamformer();
				//adaptive beamformer
				if(this.getLane().laneIndex()==0) wh.updateBeamformer();
				
				mc=corr.correlationMatrix(mc);
				res=VehicleDetectionZone.crossResponse(wh.horizontalBeamformer(
						//use the center of the detected lane
						(pos.lowerBoundIndex()+pos.upperBoundIndex())/2), mc, wv);
				
//				res=0;
				//leave out lane boundaries
//				for(int b=pos.lowerBoundIndex()+1;b<=pos.upperBoundIndex()-1;b++) 
//					res+=VehicleDetectionZone.crossResponse(wh.horizontalBeamformer(b), mc, wv);
//				res/=pos.upperBoundIndex()-pos.lowerBoundIndex()-1;
				
				frameidx=fidx;
			}
		}
		
		return res;
	}
	
	/**
	 * get the horizontal beamformer
	 * @return
	 */
	private HorizontalBeamformer horizontalBeamformer()
	{
		if(this.getLane().laneIndex()==0) 
		{
			if(wh==null) wh=new HorizontalBeamformer();
			return wh;
		}
		else return ((WeightedMVDRZone)this.sameIndexZone(0)).horizontalBeamformer();
	}
	
	/**
	 * Represents horizontal beamformer.
	 * 
	 * @author nay0648
	 */
	private class HorizontalBeamformer implements Serializable
	{
	private static final long serialVersionUID = 7553031038982204677L;
	private static final double EPS=2.220446049250313e-16;
//	private double sideattenth=0.1;//used to attentunate the side lobe
	private double[] scanaz;//scanning azimuths
	private ComplexVector[] ah;//horizontal steering vectors
	private ComplexVector[] wh;//the horizontal beamformers
	private ComplexMatrix mc=null;//the correlation matrix
	private double[] weight;//the weight
	private CorrelationMatrix mch;//the horizontal correlation matrix
	private ComplexVector wah;//weighted horizontal steering vector
	private ComplexMatrix upper=null;//the upper trangular part of the LDL decomposition
	private ComplexMatrix imch;//the inverse correlation matrix
	
		public HorizontalBeamformer()
		{
		SensorArray harray;
		double f,c;
		SphericalPoint[] fdir;
				
			//for the horizontal subarray
			harray=corr.monitorArray().horizontalSubarray();
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
				ah[azidx]=harray.steeringVector(fdir[azidx], f, c);
			}
			
			wh=new ComplexVector[ah.length];
			weight=new double[ah.length];
			mch=new CorrelationMatrix(ah[0].size());
			imch=new ComplexMatrix(ah[0].size(),ah[0].size());
		}
		
		/**
		 * get the horizontal beamformer
		 * @param azidx
		 * scanning azimuth index
		 * @return
		 */
		public ComplexVector horizontalBeamformer(int azidx)
		{
			return wh[azidx];
		}
		
		/**
		 * update the beamformer
		 */
		public void updateBeamformer()
		{
		double maxw=0;
		double tr=0;
		
			/*
			 * generate the weight
			 */
			mc=corr.correlationMatrix(mc);
			for(int azidx=0;azidx<weight.length;azidx++) 
			{
				weight[azidx]=crossResponse(ah[azidx],mc,wv);
				if(weight[azidx]>maxw) maxw=weight[azidx];
			}
			
			//attenuate the side lobe
//			for(int azidx=0;azidx<weight.length;azidx++) 
//				if(weight[azidx]<sideattenth*maxw) weight[azidx]=0;
			
			BLAS.scalarMultiply(1.0/BLAS.sum(weight), weight, weight);
			
			/*
			 * generate the synthetic correlation matrix for the horizontal subarray
			 */
			BLAS.fill(mch, 0, 0);
			
			for(int azidx=0;azidx<ah.length;azidx++) 
			{
				if(weight[azidx]<EPS) continue;
					
				//the weighted steering vector
				wah=BLAS.scalarMultiply(weight[azidx], ah[azidx], wah);
				mch.accumulate(wah);
			}
			
			for(int m=0;m<mch.numRows();m++) tr+=mch.getReal(m, m);
			BLAS.scalarMultiply(1.0/tr, mch, mch);
						
			/*
			 * calculate the inverse
			 */
			for(int m=0;m<mch.numRows();m++) mch.real()[m][m]+=diagload;
			hinv();
			
			//generate the horizontal beamformers
			for(int azidx=0;azidx<wh.length;azidx++) 
			{
				wh[azidx]=BLAS.multiply(imch, ah[azidx], wh[azidx]);
				BLAS.scalarMultiply(1.0/BLAS.quadraticForm(imch, ah[azidx]), wh[azidx], wh[azidx]);
			}
		}
		
		/**
		 * calculate the inverse of a Hermitian matrix
		 */
		private void hinv()
		{
		double[][] rr,ri,xr,xi;
			
			//perform LDL decomposition
			upper=BLAS.ldl(mch, upper);
				
			rr=upper.real();
			ri=upper.imaginary();
			xr=imch.real();
			xi=imch.imaginary();
					
			//perform back substitution
			for(int j=imch.numColumns()-1;j>=0;j--) 
				for(int i=j;i>=0;i--) 
				{
					if(i==j) 
					{
						xr[i][i]=1.0/rr[i][i];
						xi[i][i]=0;
								
						for(int k=i+1;k<upper.numColumns();k++) 
							xr[i][j]-=rr[i][k]*xr[k][j]-ri[i][k]*xi[k][j];
					}
					else
					{
						xr[i][j]=0;
						xi[i][j]=0;
								
						for(int k=i+1;k<upper.numColumns();k++) 
						{
							xr[i][j]-=rr[i][k]*xr[k][j]-ri[i][k]*xi[k][j];
							xi[i][j]-=rr[i][k]*xi[k][j]+ri[i][k]*xr[k][j];
						}
								
						/*
						 * Hermitian
						 */
						xr[j][i]=xr[i][j];
						xi[j][i]=-xi[i][j];
					}
				}
		}
	}
}
