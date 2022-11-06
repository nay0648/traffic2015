/**
 * Created on: Nov 20, 2014
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
 * Mills cross array multiplicative algorithm and least square beamforming 
 * for vehicle detection.
 * 
 * @author nay0648
 */
public class CrossLSZone extends VehicleDetectionZone
{
private static final long serialVersionUID = -6335274294211306759L;
private CrossCorrelator corr;//correlation matrices
private ComplexMatrix mc=null;//the correlation matrix
private double elevation;//zone elevation
private Beamformer vbf;//used to design vertical beamformer
private Beamformer hbf;//used to design horizontal beamformer
/*
 * parameters for beamformer design
 */
private double az1=-90,az2=90;
private int numscan=91;
private SphericalPoint[] scan;
private double[] dres;//desired response
private ComplexVector wh;//beamformer for horizontal subarray
private ComplexVector wv;//beamformer for vertical subarray
private double res=0;
private long frameidx=-1;
	
	/**
	 * @param agc
	 * used to normalize the vehicle response
	 * @param corr
	 * correlation matrix for beamforming
	 * @param elevation
	 * detection zone elevation
	 */
	public CrossLSZone(AutomaticGainControl agc,CrossCorrelator corr,double elevation)
	{
	super(agc);
	CrossMonitor monitor;
	int fi;
			
		this.corr=corr;
		monitor=corr.monitorArray();
		//use the middle frequency bin to approximately represent the entire subband
		fi=(corr.startingFrequencyBinIndex()+corr.endingFrequencyBinIndex())/2;
		
		/*
		 * vertical beamformer
		 */
		vbf=new Beamformer(
				//use the vertical subarray
				monitor.verticalSubarray(),
				monitor.binIndex2Frequency(fi),
				Param.soundSpeed());
		
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
		
		scan=Beamformer.azimuthScan(az1, az2, numscan);
		dres=new double[scan.length];
	}
	
	public double getElevation() 
	{
		return elevation;
	}

	public void setElevation(double elevation) 
	{	
		this.elevation=elevation;
		
		//zero degree elevation
		wv=vbf.delayAndSumBeamformer(new SphericalPoint(0,elevation,1));
		//apply a window to attenuate the sound outside the cross section
		BLAS.entryMultiply(Window.cheb(wv.size()), wv, wv);
	}
	
	/**
	 * get the beamformer
	 * @return
	 */
	public ComplexVector getBeamformer()
	{
		return wh;
	}
		
	/**
	 * set beamformer
	 * @param wh
	 * the beamformer
	 */
	public void setBeamformer(ComplexVector wh)
	{
		this.wh=wh;
	}
	
	public void updateZone() 
	{
	Lane lane;
		
		lane=this.getLane();
	
		if(this.getZoneIndex()==0)
		{
		LaneDetector.LanePosition pos;
		double az;	
			
			pos=lane.getLanePosition();
			if(pos==null) 
			{
				wh=null;
				return;
			}
					
			//generate desired response
			for(int i=0;i<dres.length;i++) 
			{
				az=scan[i].getAzimuth();
				
				if(az>=pos.lowerBoundAzimuth()&&az<=pos.upperBoundAzimuth()) dres[i]=1;
				else dres[i]=0;
			}
			
			wh=hbf.leastSquareBeamformer(scan, dres);
		}
		//the same as the first zone
		else
		{
			//must be the same type of detection zones
			wh=((CrossLSZone)lane.zone(0)).getBeamformer();
		}
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
