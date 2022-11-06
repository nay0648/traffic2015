/**
 * Created on: Nov 2, 2014
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
 * Detect vehicles by MVDR beamforming. For any array topology.
 * @author nay0648
 */
public class MVDRZone extends VehicleDetectionZone
{
private static final long serialVersionUID = -7904845627078688301L;
private CorrelationAccumulator corr;//used to calculate the correlation matrices
private double elevation;//beamformer elevation
private double diagload=1e-2;//to prevent singular correlation matrix
private ComplexMatrix mc=null;//the correlation matrix for the total subband
private ComplexMatrix upper=null;//the upper trangular part of the LDL decomposition
private ComplexMatrix imc;//the inverse correlation matrix
private ComplexVector[] w;//the beamformers
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
	public MVDRZone(AutomaticGainControl agc, CorrelationAccumulator corr, double elevation)
	{
	super(agc);
	
		this.corr=corr;
		setElevation(elevation);
	
		/*
		 * correlation matrix and its inverse
		 */
		mc=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
		imc=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
	}
	
	public double getElevation() 
	{
		return elevation;
	}

	public void setElevation(double elevation) 
	{
	MonitorArray monitor;
	Beamformer bfdesign;
	SphericalPoint[] fdir;
		
		this.elevation=elevation;
		
		/*
		 * generate scanning beamformers
		 */
		monitor=corr.monitorArray();
		
		//the same as the lane detector
		fdir=Beamformer.azimuthScan(
				Param.LANE_SCANAZ1, 
				Param.LANE_SCANAZ2, 
				Param.LANE_NUMSCANS);
		for(int i=0;i<fdir.length;i++) fdir[i].setElevation(elevation);

		bfdesign=new Beamformer(
				corr.subarray(),
				//using middle frequency bin
				monitor.binIndex2Frequency(
						(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2),
				Param.soundSpeed());
		
		w=new ComplexVector[fdir.length];
		for(int azidx=0;azidx<w.length;azidx++) 
			w[azidx]=bfdesign.delayAndSumBeamformer(fdir[azidx]);
	}
	
	public void updateZone() 
	{}
	
	/**
	 * calculate the inverse of a Hermitian matrix
	 */
	private void hinv()
	{
	double[][] rr,ri,xr,xi;
			
		//perform LDL decomposition
		upper=BLAS.ldl(mc, upper);
			
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
	
	public double zoneResponse() throws IOException 
	{
	LaneDetector.LanePosition pos;
	long fidx;
	double[][] mcr;
	
		pos=this.getLane().getLanePosition();
		
		if(pos==null) res=0;
		else
		{
			fidx=corr.monitorArray().frameIndex();
			if(fidx!=frameidx) 
			{
				/*
				 * prepare the correlation matrix
				 */
				mc=corr.correlationMatrix(mc);
				mcr=mc.real();
			
//				for(int i=0;i<mc.numRows();i++) if(mcr[i][i]>maxdiag) maxdiag=mcr[i][i];
//				BLAS.scalarMultiply(1.0/maxdiag, mc, mc);
			
				//diagonal loading
				for(int i=0;i<mc.numRows();i++) mcr[i][i]+=diagload;
				
				//calculate the inverse matrix
				hinv();
			
				/*
				 * calculate the MVDR spectrum
				 */
				res=1.0/BLAS.quadraticForm(imc, w[pos.centerIndex()]);
				
				frameidx=fidx;
			}
		}
		
		return res;
	}
}
