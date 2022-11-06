package cn.ac.ioa.hccl.atm.lane;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.agc.*;
import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.event.*;

/**
 * Used to detect lanes.
 * @author nay0648
 *
 */
public abstract class LaneDetector implements Serializable
{
private static final long serialVersionUID = -8658851178316384446L;
private boolean todbspectrum=true;//true to convert to dB spectrum before normalization
private AutomaticGainControl agc;//used to control the beamformer gain
private SliceGenerator slice;//the slice generator
private double[] scanaz;//scanning azimuth (degree)
private double[] sliceres;//the response of the slice
private double[] nsliceres;//normalized slice response
private boolean sliceoccupied=false;//true if the slice is occupied
//only array responses larger than this threshold is valid
private double validth=Param.LANE_SENSITIVITY;
private int numlanes;//number of lanes, both uproad and downroad
//all lane detected listeners
private Set<LaneDetectionListener> listeners=new HashSet<LaneDetectionListener>();
private double maxlaneradius=Param.LANE_MAX_RADIUS_DEGREE;//maximum lane radius
private int rescount=0;//count the added response
//lane detection threshold (every <> times response added for a new detection)
private int detectth=Param.LANE_NUMFRAMES_PER_DETECT;
private LaneDetectionThread detth=null;//lane detection thread

	/**
	 * @param slice
	 * used to generate slice
	 * @param numlanes
	 * number of lanes, both uproad and downroad
	 */
	public LaneDetector(SliceGenerator slice,int numlanes)
	{
		this.slice=slice;
		this.numlanes=numlanes;
		
		agc=new ParzenGainControl(slice.monitorArray());
		((ParzenGainControl)agc).setThresholdDiff(6);
		
		scanaz=slice.scanningAzimuth();
		sliceres=new double[slice.numScans()];//beamformer response
		nsliceres=new double[slice.numScans()];//normalized response
	}
	
	/**
	 * get array reference
	 * @return
	 */
	public MonitorArray monitorArray()
	{
		return slice.monitorArray();
	}
	
	/**
	 * get the slice generator
	 * @return
	 */
	public SliceGenerator sliceGenerator()
	{
		return slice;
	}
	
	/**
	 * get the number of lanes (both directions)
	 * @return
	 */
	public int numLanes()
	{
		return numlanes;
	}
	
	/**
	 * get line position information
	 * @param index
	 * line number index
	 * @return
	 */
	public abstract LanePosition lanePosition(int index);
	
	/**
	 * add lane detected listener
	 * @param l
	 * the listener
	 */
	public void addLaneDetectedListener(LaneDetectionListener l)
	{
		listeners.add(l);
	}
	
	/**
	 * to see if the dB spectrum is used
	 * @return
	 */
	public boolean get2DBSpectrum()
	{
		return todbspectrum;
	}
	
	/**
	 * set to use the dB spectrum or not
	 * @param todbspectrum
	 * true to use dB spectrum
	 */
	public void set2DBSpectrum(boolean todbspectrum)
	{
		this.todbspectrum=todbspectrum;
	}
	
	/**
	 * get the scanning azimuth angles of the lane detector
	 * @return
	 */
	public double[] scanningAzimuth()
	{
		return scanaz;
	}
	
	/**
	 * get the beamformer response of the last time
	 * @return
	 */
	public double[] slicedResponse()
	{
		return sliceres;
	}
	
	/**
	 * get the normalized beamformer response of the last time
	 * @return
	 */
	public double[] normalizedSlicedResponse()
	{
		return nsliceres;
	}
	
	/**
	 * get the average sliced response used for lane detection
	 * @return
	 */
	public abstract double[] laneResponse();
	
	/**
	 * accumulate lane information for lane detection
	 * @param maxidx
	 * max sliced response index
	 */
	public abstract void accumulateLaneInfo(int maxidx);
	
	/**
	 * detect lanes
	 */
	public abstract void detectLanes();
	
	/**
	 * perform lane detection using an additional thread
	 */
	public void threadDetectLanes()
	{
		if(detth==null) 
		{
			detth=new LaneDetectionThread();
			detth.start();
		}
	}
	
	/**
	 * accumulate traffic noise data collected by the monitor, calculate 
	 * beamformer response
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
	int maxidx=0;
	double maxval=0;

		/*
		 * calculate the beamformer response
		 */
		sliceres=slice.slicedResponse(sliceres);
		//convert to dB
		if(todbspectrum) 
			for(int i=0;i<nsliceres.length;i++) nsliceres[i]=10*Math.log10(sliceres[i]+1);
		else System.arraycopy(sliceres, 0, nsliceres, 0, sliceres.length);
		//normalize to [0, 1]
		agc.normalize(nsliceres);
			
		//see if it is a valid response
		for(int i=0;i<nsliceres.length;i++) 
			if(nsliceres[i]>maxval) 
			{
				maxval=nsliceres[i];
				maxidx=i;
			}
		
		if(maxval>=validth) 
		{
			if(!sliceoccupied)
			{	
				sliceoccupied=true;
				
				//accumulate information for lane detection
				accumulateLaneInfo(maxidx);
				
				//detect lanes
				if(++rescount>=detectth) 
				{
					threadDetectLanes();
					rescount=0;
				}
			}
		}
		else
		{
			if(sliceoccupied) sliceoccupied=false;
		}
	}
	
	/**
	 * used to detect lanes
	 * @author nay0648
	 */
	private class LaneDetectionThread extends Thread
	{
		public void run()
		{
		LaneDetectionEvent e;
			
			try
			{
				LaneDetector.this.detectLanes();
			
				/*
				 * fire the lane detected event
				 */
				e=new LaneDetectionEvent(LaneDetector.this);
				for(LaneDetectionListener l:listeners) l.laneDetected(e);
			}
			finally
			{
				detth=null;
			}
		}
	}
	
	/**
	 * Information about lane position and lane bondaries.
	 * @author nay0648
	 *
	 */
	public class LanePosition implements Serializable, Comparable<LanePosition>
	{
	private static final long serialVersionUID = -8366963549141279832L;
	private int posi;//lane position index
	private int bi1,bi2;//lane boundary indices
	private int maxnumw=(int)Math.round(maxlaneradius/Math.abs(scanaz[1]-scanaz[0]));
	
		/**
		 * @param posi
		 * lane position index
		 * @param bi1
		 * lower boundary index
		 * @param bi2
		 * upper boundary index
		 */
		public LanePosition(int posi,int bi1,int bi2)
		{	
			this.posi=posi;
			this.bi1=bi1;
			this.bi2=bi2;
			
			/*
			 * restricted lane width
			 */
			if(posi-bi1>maxnumw) this.bi1=posi-maxnumw;
			if(bi2-posi>maxnumw) this.bi2=posi+maxnumw;
		}
		
		/**
		 * get the lane center index
		 * @return
		 */
		public int centerIndex()
		{
			return posi;
		}
		
		/**
		 * get the lane lower boundary index
		 * @return
		 */
		public int lowerBoundIndex()
		{
			return bi1;
		}
		
		/**
		 * get the lane upper boundary index
		 * @return
		 */
		public int upperBoundIndex()
		{
			return bi2;
		}
		
		/**
		 * get the lane center azimuth angle (angle)
		 * @return
		 */
		public double centerAzimuth()
		{
			return scanaz[posi];
		}
		
		/**
		 * get lane lower bound azimuth (degree)
		 * @return
		 */
		public double lowerBoundAzimuth()
		{
			return scanaz[bi1];
		}
		
		/**
		 * get lane upper bound azimuth (degree)
		 * @return
		 */
		public double upperBoundAzimuth()
		{
			return scanaz[bi2];
		}
		
		public String toString()
		{
//			return posi+": ["+bi1+", "+bi2+"]";
			return centerAzimuth()+": "+"["+lowerBoundAzimuth()+", "+upperBoundAzimuth()+"]";
		}

		public int compareTo(LanePosition o) 
		{
			if(posi<o.posi) return -1;
			else if(posi>o.posi) return 1;
			else return 0;
		}
	}
}
