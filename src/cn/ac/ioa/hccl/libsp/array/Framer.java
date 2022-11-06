/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Directly use array data, with no "sub".
 * 
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
	public Framer(STFTArray array,int fi)
	{
		super(array,fi);
	}

	public SensorArray subarray() 
	{
		return this.array();
	}

	public ComplexVector frame(ComplexVector arrayin)
	{
		return this.array().frame(this.workingFrequencyBin(), arrayin);
	}
}
