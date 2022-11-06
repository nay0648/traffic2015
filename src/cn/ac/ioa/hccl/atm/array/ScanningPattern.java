package cn.ac.ioa.hccl.atm.array;
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
