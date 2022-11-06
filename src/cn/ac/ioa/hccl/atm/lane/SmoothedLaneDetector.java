package cn.ac.ioa.hccl.atm.lane;
import java.util.*;

/**
 * Detect lanes by smoothing the vehicle position counts.
 * @author nay0648
 */
public class SmoothedLaneDetector extends LaneDetector
{
private static final long serialVersionUID = -3654919661716257799L;
private static final int NUM_POSDATA=1000;//number of position data points
//number of initial points skipped to make the AGC stable
private static final int NUM_SKIP=10;
private LanePosition[] lanes;//the detected lanes
private double[] laneres;//lane response
private int[] posdata;//vehicle position data
private int posidx=0;//vehicle position index
private int skipcount=0;//count the skipped data
private int[] vehiclecount;//count the vehicle in different positions
private int dfradius=2;//detector filter radius
private int smoothradius=5;//radius used to smooth the count

	/**
	 * @param slicer
	 * slice generator reference
	 * @param numlanes
	 * number of lanes
	 */
	public SmoothedLaneDetector(SliceGenerator slicer,int numlanes)
	{
	super(slicer,numlanes);
		
		lanes=new LanePosition[this.numLanes()];
		laneres=new double[slicer.numScans()];
		
		posdata=new int[NUM_POSDATA];
		Arrays.fill(posdata, -1);
		
		vehiclecount=new int[slicer.numScans()];
	}
	
	public LanePosition lanePosition(int index) 
	{
		return lanes[index];
	}
	
	public double[] laneResponse() 
	{
		return laneres;
	}

	public void accumulateLaneInfo(int maxidx) 
	{
		if(skipcount<NUM_SKIP) skipcount++;
		else
		{
			//dequeue old data
			if(posdata[posidx]>=0) vehiclecount[posdata[posidx]]--;
		
			posdata[posidx]=maxidx;
			vehiclecount[maxidx]++;
		
			if(++posidx>=posdata.length) posidx=0;
		}
	}

	public void detectLanes() 
	{
	int i1,i2,numsmooth;
		
		//generates smoothed probability densities
		for(int i=0;i<laneres.length;i++) 
		{
			i1=i-smoothradius;
			if(i1<0) i1=0;
			i2=i+smoothradius;
			if(i2>laneres.length-1) i2=laneres.length-1;
			
			/*
			 * smooth the count
			 */
			laneres[i]=0;
			numsmooth=0;
			for(int ii=i1;ii<=i2;ii++) 
			{
				laneres[i]+=vehiclecount[ii];
				numsmooth++;
			}
			laneres[i]/=numsmooth;
		}
		
		//detect lanes by finding peaks and valleys
		detectLanesByPeakValley();
	}
	
	/**
	 * Used to detect lanes.
	 * @author nay0648
	 *
	 */
	private class LanePeak implements Comparable<LanePeak>
	{
	int peakidx;
	double peakval;
	
		private LanePeak(int peakidx,double peakval)
		{
			this.peakidx=peakidx;
			this.peakval=peakval;
		}
	
		public int compareTo(LanePeak o) 
		{
			if(peakval>o.peakval) return -1;
			else if(peakval<o.peakval) return 1;
			else return 0;
		}
	}
	
	/**
	 * detect lanes by peak-valley method
	 */
	private void detectLanesByPeakValley() 
	{
	double[] peakres,valleyres;
	List<LanePeak> peakval;
	List<LanePosition> templanes;
	double[] scanaz;
					
		/*
		 * detect peaks and valleys
		 */
		peakres=new double[laneres.length];
		valleyres=new double[laneres.length];
					
		for(int i=0;i<laneres.length;i++) 
		{
		int i1,i2;
		double lmax=0,lmin=Double.MAX_VALUE;
		int lmaxi=0,lmini=0;
					
			/*
			 * detecting boundaries
			 */
			i1=i-dfradius;
			if(i1<0) i1=0;
			i2=i+dfradius;
			if(i2>laneres.length-1) i2=laneres.length-1;
						
			for(int ii=i1;ii<=i2;ii++) 
			{
				//find peaks
				if(laneres[ii]>lmax) 
				{
					lmax=laneres[ii];
					lmaxi=ii;
				}	
				//find valleys
				if(laneres[ii]<lmin)
				{
					lmin=laneres[ii];
					lmini=ii;
				}
			}
						
			if(lmaxi==i) peakres[i]=laneres[i];
			if(lmini==i) valleyres[i]=laneres[i];
		}
		
		/*
		 * find lanes
		 */
		/*
		 * select numLanes() of largest lanes
		 */
		peakval=new ArrayList<LanePeak>(numLanes()*2);
		for(int i=0;i<peakres.length;i++) 
			if(peakres[i]>0) peakval.add(new LanePeak(i,peakres[i]));
		Collections.sort(peakval);
					
		templanes=new ArrayList<LanePosition>(numLanes());
		scanaz=this.scanningAzimuth();
		for(int i=0;i<Math.min(numLanes(), peakval.size());i++) 
		{
		int posi,bi1,bi2;
				
			posi=peakval.get(i).peakidx;
			if(posi<0) posi=0;
			else if(posi>scanaz.length-1) posi=scanaz.length-1;
						
			for(bi1=posi;bi1>=0;bi1--) if(valleyres[bi1]>0) break;
			if(bi1<0) bi1=0;
			else if(bi1>scanaz.length-1) bi1=scanaz.length-1;
						
			for(bi2=posi;bi2<valleyres.length;bi2++) if(valleyres[bi2]>0) break;
			if(bi2<0) bi2=0;
			else if(bi2>scanaz.length-1) bi2=scanaz.length-1;
						
			templanes.add(new LaneDetector.LanePosition(posi,bi1,bi2));
		}
					
		/*
		 * sort lanes according to position
		 */
		Collections.sort(templanes);
		Arrays.fill(lanes, null);
		for(int i=0;i<Math.min(numLanes(), templanes.size());i++) 
			lanes[i]=templanes.get(i);
	}
}
