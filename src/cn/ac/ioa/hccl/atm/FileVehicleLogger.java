/**
 * Created on: 2015Äê4ÔÂ15ÈÕ
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;
import java.text.*;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle;

/**
 * Save the log into file.
 * 
 * @author nay0648
 */
public class FileVehicleLogger implements VehicleLogger
{
private static final long serialVersionUID = 3347782360224209040L;
private static final Format SPEED_FMT=new DecimalFormat("0.00");
private File logpath=new File("./vehiclelog.txt");
private BufferedWriter out;

	public FileVehicleLogger() throws IOException
	{
		out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logpath)));
		out.write("Time		");
		out.write("Lane		");
		out.write("Type		");
		out.write("OccupancyTime	");
		out.write("Speed\r\n");
		out.flush();
	}

	public void logVehicle(Vehicle v) throws IOException 
	{
		out.write(v.timeOfAppearance()+"\t\t");
		out.write((v.laneIndex()+1)+"\t\t");
		if(Vehicle.Type.large==v.type()) out.write("l\t\t");
		else out.write("s\t\t");
		out.write(v.avgPassingTime()+"\t\t");
		out.write(SPEED_FMT.format(v.speed())+"\r\n");
		
		out.flush();
	}
	
	public void close() throws IOException 
	{
		out.close();
	}
}
