/**
 * Created on: 2015Äê3ÔÂ31ÈÕ
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
 * Perform frequency bin-wize MVDR.
 * 
 * @author nay0648
 */
public class BinwiseMVDRZone extends VehicleDetectionZone
{
private static final long serialVersionUID = 6632315963302477260L;
private CorrelationAccumulator corr;//used to calculate the correlation matrices
private double diagload=1e-3;//to prevent singular correlation matrix
private double elevation;//beamformer elevation
private BinwiseMVDR[] mvdr=null;//only the first lane have values
private double res=0;
private long frameidx=-1;
	
	/**
	 * @param agc
	 * used to normalize the vehicle response
	 * @param corr
	 * used to calculate the inverse correlation matrix
	 * @param elevation
	 * the elevation angle of the beamformers
	 */
	public BinwiseMVDRZone(AutomaticGainControl agc, CorrelationAccumulator corr, double elevation)
	{
	super(agc);
	
		this.corr=corr;
		setElevation(elevation);
	}
	
	public double getElevation() 
	{
		return elevation;
	}

	public void setElevation(double elevation) 
	{
		this.elevation=elevation;

		if(this.getLane().laneIndex()==0) 
		{
			mvdr=new BinwiseMVDR[corr.numFrequencyBins()];
			for(int i=0;i<mvdr.length;i++) 
				mvdr[i]=new BinwiseMVDR(corr.startingFrequencyBinIndex()+i);
		}
	}
	
	public void updateZone() 
	{}

	/**
	 * get the binwise mvdr
	 * @param fi
	 * frequency bin index
	 * @return
	 */
	private BinwiseMVDR binwiseMVDR(int fi)
	{
		if(this.getLane().laneIndex()==0) return mvdr[fi-corr.startingFrequencyBinIndex()];
		else return ((BinwiseMVDRZone)this.sameIndexZone(0)).binwiseMVDR(fi);
	}
	
	public double zoneResponse() throws IOException 
	{
	LaneDetector.LanePosition pos;
	long fidx;
	int azidx;
	
		pos=this.getLane().getLanePosition();
		
		if(pos==null) res=0;
		else
		{
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				azidx=(pos.lowerBoundIndex()+pos.upperBoundIndex())/2;
				
				res=1;
				for(int fi=corr.startingFrequencyBinIndex();fi<=corr.endingFrequencyBinIndex();fi++) 
					res*=binwiseMVDR(fi).binwiseResponse(azidx);
				res=Math.pow(res, 1.0/corr.numFrequencyBins());
				
				frameidx=fidx;
			}
		}
		
		return res;
	}
	
	/**
	 * MVDR for the single frequency bin.
	 * 
	 * @author nay0648
	 */
	private class BinwiseMVDR implements Serializable
	{
	private static final long serialVersionUID = -4970929317905605133L;
	private int fi;//the frequency bin index
	private ComplexVector[] a;//the steering vectors;
	private ComplexMatrix mc=null;//the correlation matrix
	private ComplexMatrix mcnormalized=null;//the normalized correlation matrix
	private ComplexMatrix upper=null;//the upper trangular part of the LDL decomposition
	private ComplexMatrix imc;//the inverse correlation matrix
	private ComplexVector w=null;//the mvdr beamformer
	
		/**
		 * @param fi
		 * frequency bin index
		 */
		public BinwiseMVDR(int fi)
		{
		Beamformer bfdesign;
		SphericalPoint[] fdir;
		
			this.fi=fi;
			
			/*
			 * generate the steering vectors
			 */
			//the same as the lane detector
			fdir=Beamformer.azimuthScan(
					Param.LANE_SCANAZ1, 
					Param.LANE_SCANAZ2, 
					Param.LANE_NUMSCANS);
			for(int i=0;i<fdir.length;i++) fdir[i].setElevation(elevation);

			bfdesign=new Beamformer(
					corr.subarray(),
					corr.monitorArray().binIndex2Frequency(fi),
					Param.soundSpeed());
			
			a=new ComplexVector[fdir.length];
			for(int azidx=0;azidx<a.length;azidx++) 
				a[azidx]=bfdesign.delayAndSumBeamformer(fdir[azidx]);
			
			/*
			 * correlation matrix and its inverse
			 */
			mcnormalized=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
			imc=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
		}
		
		/**
		 * calculate the inverse of a Hermitian matrix
		 */
		private void hinv()
		{
		double[][] rr,ri,xr,xi;
				
			//perform LDL decomposition
			upper=BLAS.ldl(mcnormalized, upper);
				
			rr=upper.real();
			ri=upper.imaginary();
			xr=imc.real();
			xi=imc.imaginary();
					
			//perform back substitution
			for(int j=imc.numColumns()-1;j>=0;j--) 
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
		
		/**
		 * the binwise response
		 * @param azidx
		 * scanning direction index
		 * @return
		 */
		public double binwiseResponse(int azidx)
		{
			mc=corr.correlationMatrix(fi);
			
			//calculate the inverse correlation
			if(BinwiseMVDRZone.this.getLane().laneIndex()==0) 
			{
			int tr=0;
			
				mcnormalized.copy(mc);
				for(int i=0;i<mcnormalized.numRows();i++) tr+=mcnormalized.getReal(i, i);
				BLAS.scalarMultiply(1.0/tr, mcnormalized, mcnormalized);
				
				//diagonal loading
				for(int i=0;i<mcnormalized.numRows();i++) 
					mcnormalized.real()[i][i]+=diagload;
				
				//calculate the inverse matrix
				hinv();
			}
			
			/*
			 * construct the MVDR beamformer
			 */
			w=BLAS.multiply(imc, a[azidx], w);
			w=BLAS.scalarMultiply(1.0/BLAS.quadraticForm(imc, a[azidx]), w, w);
			
			//the energy spectrum
			return BLAS.quadraticForm(mc, w);
		}
	}
}
