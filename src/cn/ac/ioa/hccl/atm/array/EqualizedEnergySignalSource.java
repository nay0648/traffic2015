/**
 * Created on: Nov 4, 2014
 */
package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.sio.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * @author nay0648
 * Equalize the signal energy of all channels to the same level. 
 * This is used for the case that sensor responses are different.
 */
public class EqualizedEnergySignalSource extends SignalSource
{
private SignalSource source;
private double[] tempframe;//used to read underlying data
private double[] amps;//amplitude of all channels
private double maxth;//max value threshold

	/**
	 * @param source
	 * underlying signal source
	 */
	public EqualizedEnergySignalSource(SignalSource source)
	{
		this.source=source;
		
		tempframe=new double[numChannels()];
		amps=new double[numChannels()];
		maxth=Double.MAX_VALUE/(2*numChannels());
	}
	
	public int numChannels() 
	{
		return source.numChannels();
	}
	
	public void close() throws IOException 
	{
		source.close();
	}
	
	public void readFrame(double[] frame) throws IOException, EOFException 
	{
	double mean;
	boolean toolarge=false;
	
		this.checkFrameSize(frame.length);
		
		/*
		 * read underlying data and accumulate amplitude
		 */
		source.readFrame(tempframe);
		
		for(int m=0;m<amps.length;m++) 
		{
			amps[m]+=Math.abs(tempframe[m]);
			if(amps[m]>maxth) toolarge=true;
		}
		//prevent overflow
		if(toolarge) BLAS.scalarMultiply(0.5, amps, amps);
		
		/*
		 * adjust amplitude to the same level
		 */
		mean=BLAS.mean(amps);
		for(int m=0;m<frame.length;m++) frame[m]=tempframe[m]*mean/amps[m];
	}
}
