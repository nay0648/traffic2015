package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * With no "sub".
 * @author nay0648
 */
public class Framer extends Subframer
{
private static final long serialVersionUID = -3905897938953703989L;

	/**
	 * @param monitor
	 * array reference
	 * @param fi working frequency bin index
	 */
	public Framer(MonitorArray monitor,int fi)
	{
		super(monitor,fi);
	}

	public SensorArray subarray() 
	{
		return this.monitorArray();
	}

	public ComplexVector arrayFrame(ComplexVector arrayin) throws IOException 
	{
		return this.monitorArray().arrayFrame(this.workingFrequencyBin(), arrayin);
	}
}
