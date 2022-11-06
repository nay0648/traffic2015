/**
 * Created on: Nov 3, 2014
 */
package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Subframer for the SAS-1 array.
 * 
 * @author nay0648
 */
public class SAS1Subframer extends Subframer
{
private static final long serialVersionUID = 3421277524197640162L;
private SensorArray subarray;//the horizontal subarray

	/**
	 * @param monitor
	 * array reference
	 * @param fi 
	 * working frequency bin index
	 */
	public SAS1Subframer(SAS1Monitor monitor,int fi)
	{
	super(monitor,fi);
	CartesianPoint loc;
		
		/*
		 * construct new subarray element locations
		 */
		subarray=new SensorArray();
		for(int j=0;j<monitor.numColumns();j++) 
		{
			loc=new CartesianPoint(monitor.elementLocation(j*monitor.numRows()));
			//clear the z coordinate
			loc.setZ(0);
			
			subarray.addElement(loc);
		}
	}
	
	public SensorArray subarray() 
	{
		return subarray;
	}
	
	public ComplexVector arrayFrame(ComplexVector arrayin) throws IOException 
	{
		return this.monitorArray().arrayFrame(this.workingFrequencyBin(), arrayin);
	}
}
