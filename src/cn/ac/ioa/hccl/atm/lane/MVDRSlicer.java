/**
 * Created on: Oct 31, 2014
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
 * Generate the cross section by the MVDR beamformer.
 * 
 * @author nay0648
 */
public class MVDRSlicer extends SliceGenerator
{
private static final long serialVersionUID = 8542068087218910736L;
private CorrelationAccumulator corr;//correlation accumulator
private ComplexVector[] a;//steering vectors for each scanning direction
private double diagload=1e-2;//to prevent singular correlation matrix
private ComplexMatrix mc=null;//the correlation matrix for the total subband
private ComplexMatrix mcnormalized=null;//the normalized correlation matrix
private ComplexMatrix upper=null;//the upper trangular part of the LDL decomposition
private ComplexMatrix icov;//the inverse correlation matrix
private ComplexVector w=null;//the mvdr beamformer;

	/**
	 * @param corr
	 * the correlation matrix accumulator
	 */
	public MVDRSlicer(CorrelationAccumulator corr)
	{
	super(corr.monitorArray());
	MonitorArray monitor;
	Beamformer bfdesign;
	double[] scanaz;
	SphericalPoint[] fdir;

		this.corr=corr;
		monitor=this.monitorArray();
		
		/*
		 * construct beamformers
		 */
		scanaz=this.scanningAzimuth();
		fdir=new SphericalPoint[scanaz.length];
		for(int azidx=0;azidx<fdir.length;azidx++) 
			fdir[azidx]=new SphericalPoint(scanaz[azidx],0,1);
			
		bfdesign=new Beamformer(
				corr.subarray(),
				//using middle frequency bin
				monitor.binIndex2Frequency((corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2),
				Param.soundSpeed());
		
		a=new ComplexVector[this.numScans()];
		for(int azidx=0;azidx<scanaz.length;azidx++) 
			a[azidx]=bfdesign.delayAndSumBeamformer(fdir[azidx]);
		
		/*
		 * correlation matrix and its inverse
		 */
		mc=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
		mcnormalized=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
		icov=new ComplexMatrix(corr.subframeSize(),corr.subframeSize());
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
		xr=icov.real();
		xi=icov.imaginary();
				
		//perform back substitution
		for(int j=icov.numColumns()-1;j>=0;j--) 
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
	
	public double[] slicedResponse(double[] response) throws IOException 
	{
	double tr=0;
	double resmean;
	
		if(response==null) response=new double[this.numScans()];
		else BLAS.checkDestinationSize(response, this.numScans());
		
		/*
		 * prepare the correlation matrix
		 */
		mc=corr.correlationMatrix(mc);
		
		mcnormalized.copy(mc);
		for(int i=0;i<mcnormalized.numRows();i++) tr+=mcnormalized.getReal(i, i);
		BLAS.scalarMultiply(1.0/tr, mcnormalized, mcnormalized);
		
		//diagonal loading
		for(int i=0;i<mcnormalized.numRows();i++) 
			mcnormalized.real()[i][i]+=diagload;
		
		//calculate the inverse matrix
		hinv();
		
		for(int azidx=0;azidx<response.length;azidx++) 
		{
			/*
			 * the mvdr beamformer
			 */
			w=BLAS.multiply(icov, a[azidx], w);
			w=BLAS.scalarMultiply(1.0/BLAS.quadraticForm(icov, a[azidx]), w, w);
			
			//the energy spectrum
			response[azidx]=BLAS.quadraticForm(mc, w);
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
	int tms=500,fi1,fi2;
	Correlator corr;
	SliceGenerator slice;
	AutomaticGainControl agc;
	Monitor1D gui;
	double[] response=null;
	Monitor2D gui2;
	LaneDetector lanedet;
	
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
		System.out.println(monitor.numFramesInTimeInterval(tms)+", "+(fi2-fi1+1));
		corr=new Correlator(monitor,monitor.numFramesInTimeInterval(tms),fi1,fi2);
		
		slice=new MVDRSlicer(corr);
		agc=new HeapGainControl(monitor);
		gui=new Monitor1D(slice.scanningAzimuth());
		
		lanedet=new ParzenLaneDetector(slice,4);
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
