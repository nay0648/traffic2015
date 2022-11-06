/**
 * Created on: 2015��2��27��
 */
package cn.ac.ioa.hccl.libsp.array;
import java.io.*;

/**
 * Represents direction changing pattern per sample.
 * @author nay0648
 *
 */
public interface ScanningPattern extends Serializable
{
	/**
	 * get next scanning direction
	 * @return
	 */
	public SphericalPoint nextDirection();
}
