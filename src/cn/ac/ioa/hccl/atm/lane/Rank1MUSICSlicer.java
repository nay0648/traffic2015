/**
 * Created on: Nov 25, 2014
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
 * Cross array, multiplicative algorithm, use the data in the cross section to 
 * perform single source DOA by the rank one MUSIC algorithm.
 * 
 * @author nay0648
 */
public class Rank1MUSICSlicer extends SliceGenerator
{
private static final long serialVersionUID = -1374619075273464260L;
private double MAX_FREQUENCY_DIFF=1000;//frequency difference (Hz)
private double musicpenalty=0;//to prevent zero denominator
private CrossCorrelator corr;//used to calculate the correlation
private int fi2;//the upper bound frequency bin index
private ComplexMatrix mc=null;//the correlation matrix
private ComplexVector wv;//beamformer for the vertical subarray
private ComplexVector[] wh;//beamformer for each scanning direction
private ComplexVector ev1;//the first eigenvector spans the signal subspace
private ComplexVector[] noisebasis;//basis vectors span the noise subspace
private ComplexVector nbasis;//normalized noise base

	/**
	 * @param corr
	 * the correlation matrix accumulator
	 */
	public Rank1MUSICSlicer(CrossCorrelator corr)
	{
	super(corr.monitorArray());
	CrossMonitor monitor;
	Beamformer bfdesign;
	double[] scanaz;
	SphericalPoint[] fdir;
	
		this.corr=corr;
		monitor=corr.monitorArray();
		
		//select the working frequency bins
		{
		double f1,f2;
		
			f1=monitor.binIndex2Frequency(corr.startingFrequencyBinIndex());
			f2=monitor.binIndex2Frequency(corr.endingFrequencyBinIndex());
			
			if(f2-f1<=MAX_FREQUENCY_DIFF) fi2=corr.endingFrequencyBinIndex();
			else fi2=monitor.frequency2BinIndex(f1+MAX_FREQUENCY_DIFF);		
		}
		
		/*
		 * construct beamformers
		 */
		scanaz=this.scanningAzimuth();
		fdir=new SphericalPoint[scanaz.length];
		for(int azidx=0;azidx<fdir.length;azidx++) 
			fdir[azidx]=new SphericalPoint(scanaz[azidx],0,1);
		
		bfdesign=new Beamformer(
				//only use the horizontal subarray
				monitor.horizontalSubarray(),
				//using middle frequency bin
				monitor.binIndex2Frequency(
						(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2),
				Param.soundSpeed());	
		wh=new ComplexVector[this.numScans()];
		for(int azidx=0;azidx<scanaz.length;azidx++) 
			wh[azidx]=bfdesign.delayAndSumBeamformer(fdir[azidx]);
	
		/*
		 * beamformer for the vertical subarray
		 */
		bfdesign=new Beamformer(
				//only use the vertical subarray
				monitor.verticalSubarray(),
				//using middle frequency bin
				monitor.binIndex2Frequency(
						(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2),
				Param.soundSpeed());
	
		//zero degree elevation
		wv=bfdesign.delayAndSumBeamformer(new SphericalPoint(0,0,1));
		//apply a window to attenuate the sound outside the cross section
		BLAS.entryMultiply(Window.cheb(wv.size()), wv, wv);
		
		/*
		 * initial value for the MUSIC algorithm
		 */
		ev1=new ComplexVector(wh[0].size());
		
		noisebasis=new ComplexVector[ev1.size()-1];
		for(int i=0;i<noisebasis.length;i++) 
		{
			noisebasis[i]=new ComplexVector(ev1.size());
			//free variables
			noisebasis[i].setReal(i+1, -1);
		}
		nbasis=new ComplexVector(ev1.size());
	}
	
	/**
	 * calculate the noise basis
	 */
	private void noiseBasis()
	{
	double a,b,c,d,temp;
	
		a=ev1.getReal(0);
		b=ev1.getImaginary(0);
		
		for(int i=0;i<noisebasis.length;i++) 
		{
			c=ev1.getReal(i+1);
			d=ev1.getImaginary(i+1);
			
			temp=a*a+b*b;
			noisebasis[i].setValue(
					0, 
					(a*c+b*d)/temp, 
					(b*c-a*d)/temp);
		}
	}
	
	public double[] slicedResponse(double[] response) throws IOException 
	{
	double sqrted1;
	double resmean;
			
		/*
		 * calculate the leading eigenvalue and the corresponding eigenvector
		 */
		mc=corr.correlationMatrix(corr.startingFrequencyBinIndex(),fi2,mc);
		//rank 1 matrix's eigenvector is itself
		ev1=BLAS.multiply(mc, wv, ev1);
		sqrted1=BLAS.norm2(ev1);
		ev1=BLAS.scalarMultiply(1.0/sqrted1, ev1, ev1);
		
		//the noise subspace as the orthogonal complement of the first eigenvector
		noiseBasis();

		/*
		 * calculate the response
		 */
		if(response==null) response=new double[this.numScans()];
		else BLAS.checkDestinationSize(response, this.numScans());
		
		for(int azidx=0;azidx<response.length;azidx++) 
		{
			response[azidx]=0;
			
			for(int i=0;i<noisebasis.length;i++) 
			{	
				nbasis.copy(noisebasis[i]);
				BLAS.normalize(nbasis);
				response[azidx]+=BLAS.innerProductAbsSquare(nbasis, wh[azidx]);
			}
			
			response[azidx]=sqrted1/(response[azidx]+musicpenalty);
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
	int fi1,fi2;
	CrossCorrelator corr;
	SliceGenerator slice;
	AutomaticGainControl agc;
	Monitor1D gui;
	double[] response=null;
	LaneDetector lanedet;
	Monitor2D gui2;
	
		monitor=new CrossMonitor(
				new File("arraygeometry/crossarray8k.txt"),
				20000,
				256,
				512);
		x=new ArrayTestSignalSource(monitor,20000);
//		x.setScanningPattern(null);
		x.setScanningPattern(new HighwayScan());
//		x.setFocalDirection(new SphericalPoint(-85,0,1));
		monitor.setSignalSource(x);
		
		fi1=monitor.frequency2BinIndex(6500);
		fi2=monitor.frequency2BinIndex(7000);
		System.out.println(monitor.numFramesInTimeInterval(Param.CORR_TIME_WINDOW)+", "+(fi2-fi1+1));
		corr=new CrossCorrelator(
				monitor,
				monitor.numFramesInTimeInterval(Param.CORR_TIME_WINDOW),
				fi1,
				fi2);
		
		slice=new Rank1MUSICSlicer(corr);
		agc=new HeapGainControl(monitor);
		gui=new Monitor1D(slice.scanningAzimuth());	
		
		lanedet=new ParzenLaneDetector(slice,4);
		gui2=new Monitor2D(lanedet,500);
		
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
