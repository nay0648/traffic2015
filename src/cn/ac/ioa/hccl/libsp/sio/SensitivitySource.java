/**
 * Created on: 2015Äê3ÔÂ12ÈÕ
 */
package cn.ac.ioa.hccl.libsp.sio;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Used to read NI's sound pressure data with sensitivity parameters.
 * 
 * @author nay0648
 */
public class SensitivitySource extends SignalSource
{
private SignalSource source;//underlying signal source
private double[] sensitivity;//the sensitivity parameters

	/**
	 * with default sensitivity equals to 1
	 * @param source
	 * underlying signal source
	 */
	public SensitivitySource(SignalSource source)
	{
		this.source=source;
		
		sensitivity=new double[source.numChannels()];
		Arrays.fill(sensitivity, 1);
	}
	
	/**
	 * @param source
	 * underlying signal source
	 * @param sensitivity
	 * sensitivity parameters (mV/Pa) for each channel, data is not copied
	 */
	public SensitivitySource(SignalSource source, double[] sensitivity)
	{
		this.source=source;
		
		if(sensitivity.length!=source.numChannels()) throw new IllegalArgumentException(
				"number of channels not match: "+sensitivity.length+", "+source.numChannels());
		this.sensitivity=sensitivity;
	}
	
	/**
	 * get sensitivity of a channel
	 * @param chidx
	 * channel index
	 * @return
	 * (mV/Pa)
	 */
	public double getSensitivity(int chidx)
	{
		return sensitivity[chidx];
	}
	
	/**
	 * set sensitivity of a channel
	 * @param chidx
	 * channel index
	 * @param value
	 * sensitivity value (mV/Pa)
	 */
	public void setSensitivity(int chidx, double value)
	{
		sensitivity[chidx]=value;
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
		source.readFrame(frame);
		BLAS.entryMultiply(sensitivity, frame, frame);
	}
}
