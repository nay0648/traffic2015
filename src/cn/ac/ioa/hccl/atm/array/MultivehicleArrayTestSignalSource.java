package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.sio.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Simulates multivehicle.
 * @author nay0648
 *
 */
public class MultivehicleArrayTestSignalSource extends SignalSource
{
private int[] randseed={2,3,7,11,13,17,19,23,31,37};
private ArrayTestSignalSource[] ss;//underlying signal source
private double[] tempf;//temp frame

	/**
	 * @param array
	 * sensor array
	 * @param fs
	 * sampling rate
	 * @param azimuth
	 * vehicle azimuth (degree)
	 * @throws IOException 
	 */
	public MultivehicleArrayTestSignalSource(SensorArray array,double fs,double... azimuth) throws IOException
	{
		if(azimuth.length>randseed.length) throw new IllegalArgumentException(
				"too many vehicles: "+azimuth.length+", at most "+randseed.length+" are supported");
		
		ss=new ArrayTestSignalSource[azimuth.length];
		for(int vi=0;vi<ss.length;vi++) 
		{
			ss[vi]=new ArrayTestSignalSource(array,fs,randseed[vi]);
			ss[vi].setScanningPattern(new ElevationScan(azimuth[vi]));
			
			/*
			 * just different azimuth angles
			 */
			ss[vi].setScanningPattern(null);
			ss[vi].setFocalDirection(new SphericalPoint(azimuth[vi],0,1));
		}
		
		//used to read underlying ArrayTestSignalSource
		tempf=new double[array.numElements()];
	}

	/**
	 * with default vehicles at: [-60, -30, 0, 30, 60]
	 * @param array
	 * sensor array
	 * @param fs
	 * sampling rate
	 * @throws IOException 
	 */
	public MultivehicleArrayTestSignalSource(SensorArray array,double fs) throws IOException
	{
		this(array,fs,-60,-30,0,30,60);
	}
	
	/**
	 * use pre recorded data for the test
	 * @param array
	 * sensor array
	 * @param fs
	 * sampling rate
	 * @param data
	 * each row for a signal source
	 * @throws IOException
	 */
	public MultivehicleArrayTestSignalSource(SensorArray array,double fs,double[]... data) 
			throws IOException
	{
		ss=new ArrayTestSignalSource[data.length];
		for(int vi=0;vi<ss.length;vi++) 
			ss[vi]=new ArrayTestSignalSource(array,fs,data[vi]);
		
		//used to read underlying ArrayTestSignalSource
		tempf=new double[array.numElements()];
	}
	
	/**
	 * get signal sampling rate
	 * @return
	 */
	public double sampleRate()
	{
		return ss[0].sampleRate();
	}
	
	public int numChannels() 
	{
		return ss[0].numChannels();
	}
	
	/**
	 * return 
	 * @return
	 */
	public int numVehicles()
	{
		return ss.length;
	}
	
	/**
	 * get focal direction of a vehicle
	 * @param index
	 * vehicle index
	 * @return
	 */
	public SphericalPoint getFocalDirection(int index)
	{
		return ss[index].getFocalDirection();
	}
	
	/**
	 * set focal direction of a vehicle
	 * @param index
	 * vehicle index
	 * @param focaldir
	 * the direction
	 */
	public void setFocalDirection(int index,SphericalPoint focaldir)
	{
		ss[index].setFocalDirection(focaldir);
	}
	
	/**
	 * get the scanning pattern of a vehicle
	 * @param index
	 * vehicle index
	 * @return
	 */
	public ScanningPattern getScanningPattern(int index)
	{
		return ss[index].getScanningPattern();
	}
	
	/**
	 * set the scanning pattern of a vehicle
	 * @param index
	 * vehicle index
	 * @param scan
	 * scanning pattern
	 */
	public void setScanningPattern(int index,ScanningPattern scan)
	{
		ss[index].setScanningPattern(scan);
	}
	
	public void readFrame(double[] frame) throws IOException, EOFException 
	{
		this.checkFrameSize(frame.length);
		
		Arrays.fill(frame, 0);
		//just add the signals from different vehicles together
		for(int vi=0;vi<ss.length;vi++) 
		{	
			ss[vi].readFrame(tempf);
			BLAS.add(tempf, frame, frame);
		}
		
		//normalization
		BLAS.scalarMultiply(1.0/numVehicles(), frame, frame);
	}

	public void close() throws IOException 
	{
		for(SignalSource temp:ss) temp.close();
	}
	
	/**
	 * get the scanning speed (number of samples to change the focal direction)
	 * @param index
	 * source index
	 * @return
	 */
	public int getScanningGap(int index)
	{
		return ss[index].getScanningGap();
	}
	
	/**
	 * set the scanning speed
	 * @param index
	 * source index
	 * @param numsamples
	 * number of samples to change the focal direction
	 */
	public void setScanningGap(int index,int numsamples)
	{
		ss[index].setScanningGap(numsamples);
	}
}
