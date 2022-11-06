/**
 * Created on: Oct 31, 2014
 */
package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import java.util.*;

/**
 * @author nay0648
 */
public class CrossArrayConfig implements Serializable
{
private static final long serialVersionUID = 3577967522825821920L;
private static final CrossArrayConfig INSTANCE=new CrossArrayConfig();
private Map<String,int[][]> submap=new HashMap<String,int[][]>();

	private CrossArrayConfig()
	{
	int[][] subidx;
		
		subidx=new int[][]{
				{0,1,2,3,4,5,6,7,8,9,10},
				{11,12,13,14,5,15,16,17,18}};
		submap.put("crossarray8k.txt", subidx);
		
		subidx=new int[][]{
				{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20},
				{21,22,23,24,25,26,27,28,10,29,30,31,32,33,34,35,36}};
		submap.put("crossarray16k.txt", subidx);
	}
	
	/**
	 * get subarray indices
	 * @param name
	 * array name
	 * @return
	 * each row for a subarray, or null if not found
	 */
	public static int[][] subarrayIndices(String name)
	{
		return INSTANCE.submap.get(name);
	}
}
