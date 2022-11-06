package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * Simulate multichannel signal sources for Mills cross array using only the 
 * horizontal or vertical part.
 * @author nay0648
 */
public class SemisimulatedSignalSource extends SignalSource
{
private MonitorArray monitor;
private int[] hidx,vidx;//horizontal and vertical subarray indices
private int[] datasubidx;//subarray indices with data
private int[] emptysubidx;//subarray indices without data
private AugmentedSignalSource ass;
private DelayedSignalSource dss;//underlying signal source
private double fs;//sampling rate
private SphericalPoint focaldir;//focal direction
private ScanningPattern scan;
private int pclipidx=0;//previous signal clip index
	
	/**
	 * @param monitor
	 * Mills cross monitor
	 * @param datadir
	 * data directory
	 * @param vertical
	 * true means the data is for the vertical subarray
	 * @param fs
	 * sampling rate;
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public SemisimulatedSignalSource(CrossMonitor monitor,File datadir,
			boolean vertical,double fs) throws FileNotFoundException, IOException
	{
	CartesianPoint refloc;
	int refidx;
	
		this.monitor=monitor;
		
		hidx=monitor.horizontalSubarrayIndices();
		vidx=monitor.verticalSubarrayIndices();
		
		if(vertical)
		{
			datasubidx=vidx;
			emptysubidx=hidx;
		}
		else
		{
			datasubidx=hidx;
			emptysubidx=vidx;
		}
		
		refidx=datasubidx[datasubidx.length/2];
		refloc=monitor.elementLocation(refidx);
		if(refloc.getX()!=0||refloc.getY()!=0||refloc.getZ()!=0) 
			throw new IllegalArgumentException(
					"illegal array geometry, phase center must have zero coordinates: "+refloc);
		
		this.fs=fs;
		ass=new AugmentedSignalSource(datadir);
		dss=new DelayedSignalSource(ass);
	}
	
	/**
	 * get sampling rate
	 * @return
	 */
	public double sampleRate()
	{
		return fs;
	}
	
	public int numChannels() 
	{
		return monitor.numElements();
	}

	public void close() throws IOException 
	{
		dss.close();
	}

	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		dss.readFrame(frame);
		
		if(pclipidx!=ass.currentClipIndex()) 
		{
			pclipidx=ass.currentClipIndex();
			if(scan!=null) setFocalDirection(scan.nextDirection());
		}
	}
	
	/**
	 * get focal direction
	 * @return
	 */
	public SphericalPoint getFocalDirection()
	{
		return focaldir;
	}
	
	/**
	 * set array focal direction
	 * @param focaldir
	 * focal direction
	 */
	public void setFocalDirection(SphericalPoint focaldir)
	{
		this.focaldir=focaldir;
		
		//only the empty half is delayed
		for(int m=0;m<emptysubidx.length;m++) 
			dss.setDelay(
					emptysubidx[m],
					monitor.timeDelay(emptysubidx[m], focaldir, Param.soundSpeed())*fs);
	}
	
	/**
	 * get the scanning pattern used in the test
	 * @return
	 */
	public ScanningPattern getScanningPattern()
	{
		return scan;
	}
	
	/**
	 * set the scanning pattern used in the test
	 * @param scan
	 * null to cancel the scan
	 */
	public void setScanningPattern(ScanningPattern scan)
	{
		this.scan=scan;
		
		if(scan==null) setFocalDirection(new SphericalPoint(0,0,1));
		else setFocalDirection(scan.nextDirection());
	}
	
	/**
	 * To augment half side to full side signals.
	 * @author nay0648
	 */
	private class AugmentedSignalSource extends SignalSource
	{
	private TextSignalClips half;//the underlying half source
	private double[] halfframe;
		
		/**
		 * @param datadir
		 * path for data clips
		 * @throws IOException 
		 */
		public AugmentedSignalSource(File datadir) throws IOException
		{
			half=new TextSignalClips(true,datadir);
			if(half.numChannels()!=datasubidx.length) throw new IllegalArgumentException(
					"number of data subarray channels not match: "
							+half.numChannels()+", "+datasubidx.length);
			
			halfframe=new double[half.numChannels()];
		}
		
		/**
		 * get current signal clip index
		 * @return
		 */
		public int currentClipIndex()
		{
			return half.currentClipIndex();
		}
		
		public int numChannels() 
		{
			return monitor.numElements();
		}

		public void close() throws IOException 
		{
			half.close();
		}

		public void readFrame(double[] frame) throws IOException, EOFException 
		{
		double refdata;	
			
			this.checkFrameSize(frame.length);
			half.readFrame(halfframe);//read underlying data
			
			/*
			 * copy phase center data
			 */
			refdata=halfframe[halfframe.length/2];
			for(int i=0;i<emptysubidx.length;i++) frame[emptysubidx[i]]=refdata;
			
			//copy subarray data from underlying signal source
			for(int i=0;i<datasubidx.length;i++) frame[datasubidx[i]]=halfframe[i];
		}		
	}
}
