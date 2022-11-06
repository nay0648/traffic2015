package cn.ac.ioa.hccl.atm.vehicle;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.event.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * A vehicle detection zone abstraction.
 * @author nay0648
 */
public abstract class VehicleDetectionZone implements Serializable
{
private static final long serialVersionUID = 8779984162999510987L;
private boolean dbspectrum=true;//true to convert to dB spectrum before normalization
private boolean sideatten=false;//true to perform side lanes attenuation
private double sideattenth=0.05;//used to attenuate the response of the neighbor lanes
private AutomaticGainControl agc;//used to normalized the vehicle response
private Lane lane;//lane reference
private int zoneidx;//zone index
private ZoneSensitivity sensitivity;//used to select the zone sensitivity
private boolean isactive=false;//true if the detection zone is occupied
private Set<VehicleDetectionListener> ls=new HashSet<VehicleDetectionListener>();
private PostSmoother smoother=new PostSmoother(Param.VEHICLE_POST_SMOOTHER_ORDER);
private double nres;//normalized zone response
	
	/**
	 * @param elevation
	 * zone elevation
	 * @param agc
	 * used to normalized the vehicle response
	 */
	public VehicleDetectionZone(AutomaticGainControl agc)
	{
		this.agc=agc;
		
		sensitivity=new AvgSensitivity();
	}
	
	/**
	 * get AGC module
	 * @return
	 */
	public AutomaticGainControl agc()
	{
		return agc;
	}
	
	/**
	 * get zone elevation
	 * @return
	 */
	public abstract double getElevation();
	
	/**
	 * set zone elevation
	 * @param elevation
	 * elevation angle (degree)
	 */
	public abstract void setElevation(double elevation);
	
	/**
	 * get zone index
	 * @return
	 */
	public int getZoneIndex()
	{
		return zoneidx;
	}
	
	/**
	 * set zone index
	 * @param zoneidx
	 * zone index
	 */
	public void setZoneIndex(int zoneidx)
	{
		this.zoneidx=zoneidx;
	}
	
	/**
	 * get the lane this detection zone belongs to
	 * @return
	 */
	public Lane getLane()
	{
		return lane;
	}
	
	/**
	 * set the lane this detection zone belongs to
	 * @param lane
	 * the lane
	 */
	public void setLane(Lane lane)
	{
		this.lane=lane;
		addVehicleDetectionListener(lane);
	}
	
	/**
	 * to see if there is a vehicle in the detection zone
	 * @return
	 * true if the detection zone is occupied
	 */
	public boolean isOccupied()
	{
		return isactive;
	}
	
	/**
	 * get the detection zone has the same order with this zone in other lane
	 * @param laneidx
	 * lane index
	 * @return
	 */
	public VehicleDetectionZone sameIndexZone(int laneidx)
	{
		return lane.getVehicleDetector().lane(laneidx).zone(zoneidx);
	}
	
	/**
	 * add listener to handle vehicle entered and leaved events
	 * @param l
	 * a listener
	 */
	public void addVehicleDetectionListener(VehicleDetectionListener l)
	{
		ls.add(l);
	}
	
	/**
	 * update detection zone according to the new lane position
	 */
	public abstract void updateZone();
	
	/**
	 * calculate detection zone response
	 * @return
	 * @throws IOException
	 */
	public abstract double zoneResponse() throws IOException;
	
	/**
	 * get the normalized zone response in [0, 1]
	 * @return
	 */
	public double normalizedZoneResponse()
	{
		return nres;
	}
	
	/**
	 * calculate |h'*C*v|^2
	 * @param h
	 * horizontal beamformer
	 * @param mc
	 * correlation matrix
	 * @param v
	 * vertical beamformer
	 * @return
	 */
	public static double crossResponse(ComplexVector h, ComplexMatrix mc, ComplexVector v)
	{
	double[][] c,d;
	double[] a,b,e,f;
	double tr,ti,real=0,imag=0;
	
		if(h==null||mc==null||v==null) return 0;
		
		a=h.real();
		b=h.imaginary();
		
		c=mc.real();
		d=mc.imaginary();
		
		e=v.real();
		f=v.imaginary();
		
		for(int i=0;i<mc.numRows();i++) 
			for(int j=0;j<mc.numColumns();j++) 
			{
				tr=a[i]*c[i][j]+b[i]*d[i][j];
				ti=a[i]*d[i][j]-b[i]*c[i][j];
								
				real+=tr*e[j]-ti*f[j];
				imag+=tr*f[j]+ti*e[j];
			}

		return real*real+imag*imag;
	}
	
	/**
	 * update traffic information
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
	VehicleDetectionEvent e;
	
		/*
		 * calculate beamformer response
		 */
		nres=zoneResponse();
		
		//attenuating the effect of the neighboring lanes
		if(sideatten)
		{
		double sideres,maxsideres=0;
			
			for(int lidx=0;lidx<lane.getVehicleDetector().numLanes();lidx++) 
			{
				if(lidx==lane.laneIndex()) continue;
				
				sideres=sameIndexZone(lidx).zoneResponse();
				if(sideres>maxsideres) maxsideres=sideres;	
			}
			
			nres-=sideattenth*maxsideres;
			if(nres<0) nres=0;
		}
		
		/*
		 * convert to dB
		 */		
		if(dbspectrum) nres=10*Math.log10(nres);
		if(nres<0) nres=0;
		//smooth the response
		nres=smoother.smoothedZoneResponse(nres);
		
		//vehicle in the detection zone
		if(sensitivity.isActive(nres)) 
		{
			if(!isactive) 
			{
				isactive=true;
				
				e=new VehicleDetectionEvent(this);
				for(VehicleDetectionListener l:ls) l.vehicleEnteredDetectionZone(e);
			}
		}
		//vehicle not in the detection zone
		else 
		{
			if(isactive)
			{
				isactive=false;
				
				e=new VehicleDetectionEvent(this);
				for(VehicleDetectionListener l:ls) l.vehicleLeavedDetectionZone(e);
			}
		}
		
		//normalize to [0, 1]
		nres=agc.normalize(nres);
	}
	
	/**
	 * Used to smooth the zone response.
	 * 
	 * @author nay0648
	 */
	private class PostSmoother implements Serializable
	{
	private static final long serialVersionUID = 7260503760367713138L;
	private double[] buffer;//data buffer
	private int idx=0;
	private double sum=0;
		
		/**
		 * @param order
		 * no. of frames to be averaged
		 */
		public PostSmoother(int order)
		{
			buffer=new double[order];
		}
		
		/**
		 * get the smoothed zone response
		 * @param res
		 * zone response
		 * @return
		 */
		public double smoothedZoneResponse(double res)
		{
			sum-=buffer[idx];
			
			buffer[idx]=res;
			if(++idx>=buffer.length) idx=0;
			sum+=res;
			
			return sum/buffer.length;
		}
	}
}
