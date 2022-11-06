/**
 * Created on: 2015Äê4ÔÂ15ÈÕ
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * Used to generate vehicle log.
 * 
 * @author nay0648
 */
public interface VehicleLogger extends Serializable
{
	/**
	 * log the detected vehicle
	 * @param v
	 * a vehicle
	 * @throws IOException
	 */
	public void logVehicle(Vehicle v) throws IOException;
	
	/**
	 * close the logger
	 * @throws IOException
	 */
	public void close() throws IOException;
}
