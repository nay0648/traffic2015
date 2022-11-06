package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * Used to test sensor array.
 * @author nay0648
 *
 */
public class ArrayTestSignalSource extends SignalSource
{
private SensorArray array;//sensor array reference
private int randseed=37;//seed for random number generator
private double fs;//signal sampling rate
private DelayedSignalSource[] testss;//the underlying signal source
private SphericalPoint focaldir;//focal direction

private ScanningPattern scan=new HorizontalScan();//used to change the focal direction
private int samplecount=0;
private int scansampleth=1000;//threshold to change the focal direction

	/**
	 * @param array
	 * the sensor array
	 * @param fs
	 * sampling rate
	 * @throws IOException 
	 */
	public ArrayTestSignalSource(SensorArray array,double fs) throws IOException
	{
		this.array=array;
		this.fs=fs;
		
		testss=new DelayedSignalSource[array.numElements()];
		for(int m=0;m<testss.length;m++) 
			testss[m]=new DelayedSignalSource(new GaussianNoiseSource(0,1,randseed));
	}
	
	/**
	 * @param array
	 * the sensor array
	 * @param fs
	 * sampling rate
	 * @param randseed
	 * seed for random number generator
	 * @throws IOException
	 */
	public ArrayTestSignalSource(SensorArray array,double fs,int randseed) throws IOException
	{
		this.array=array;
		this.fs=fs;
		this.randseed=randseed;
		
		testss=new DelayedSignalSource[array.numElements()];
		for(int m=0;m<testss.length;m++) 
			testss[m]=new DelayedSignalSource(new GaussianNoiseSource(0,1,randseed));		
	}
	
	/**
	 * use prerecorded data for the test
	 * @param array
	 * sensor array
	 * @param fs
	 * sampling rate
	 * @param data
	 * prerecorded data, will be looply read
	 * @throws IOException
	 */
	public ArrayTestSignalSource(SensorArray array,double fs,double[] data) throws IOException
	{
		this.array=array;
		this.fs=fs;
		
		testss=new DelayedSignalSource[array.numElements()];
		for(int m=0;m<testss.length;m++) 
			testss[m]=new DelayedSignalSource(new LoopBufferSignalSource(data));
	}
	
	public int numChannels() 
	{
		return array.numElements();
	}
	
	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		this.checkFrameSize(frame.length);

		//change focal direction if need
		if(samplecount++>=scansampleth) 
		{
			if(scan!=null) setFocalDirection(scan.nextDirection());
			samplecount=0;
		}
		
		for(int m=0;m<frame.length;m++) frame[m]=testss[m].readSample();
	}
	
	public void close() throws IOException 
	{}
	
	/**
	 * get signal sampling rate
	 * @return
	 */
	public double sampleRate()
	{
		return fs;
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
		
		for(int m=0;m<array.numElements();m++) 
			testss[m].setDelay(array.timeDelay(m, focaldir, Param.soundSpeed())*fs);
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
	}
	
	/**
	 * get the scanning speed (number of samples to change the focal direction)
	 * @return
	 */
	public int getScanningGap()
	{
		return scansampleth;
	}
	
	/**
	 * set the scanning speed
	 * @param numsamples
	 * number of samples to change the focal direction
	 */
	public void setScanningGap(int numsamples)
	{
		scansampleth=numsamples;
	}
}
