/**
 * Created on: 2015Äê4ÔÂ15ÈÕ
 */
package cn.ac.ioa.hccl.atm;
import java.io.*;
import java.text.*;
import java.util.*;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle;

/**
 * Vehicle logs.
 * @author nay0648
 */
public class VehicleLog implements Serializable, Iterable<VehicleLog.Record>
{
private static final long serialVersionUID = -7982097568900083987L;
private static Format SPEED_FMT=new DecimalFormat("0.00");
private static final String LOG_SKIP="#";
private static final String TYPE_LARGE="l";
private List<Record> records;

	/**
	 * @param path
	 * log file path
	 * @throws IOException
	 */
	public VehicleLog(File path) throws IOException
	{
	BufferedReader in=null;
	String[] ss;
	String time;
	int laneno;
	Vehicle.Type type;
	int occtime;
	double speed=0;
	
		records=new LinkedList<Record>();
		
		try
		{
			in=new BufferedReader(new InputStreamReader(new FileInputStream(path)));	
				
			for(String ts=null;(ts=in.readLine())!=null;) 
			{
				ts=ts.trim();
				if(ts.length()<=0) continue;
				if(ts.startsWith(LOG_SKIP)) continue;
					
				ss=ts.split("\\s+");
				try
				{
					time=ss[0];
					laneno=Integer.parseInt(ss[1]);
					if(TYPE_LARGE.equals(ss[2])) type=Vehicle.Type.large;
					else type=Vehicle.Type.small;
					occtime=Integer.parseInt(ss[3]);
					speed=Double.parseDouble(ss[4]);
						
					records.add(new Record(time,laneno,type,occtime,speed));
				}
				catch(NumberFormatException e)
				{}
			}
		}
		finally
		{
			try
			{
				if(in!=null) in.close();
			}
			catch(IOException e)
			{}
		}	
	}
	
	/**
	 * get the vehicle count
	 * @param laneidx
	 * lane index, -1 for all lanes
	 * @return
	 */
	public int vehicleCount(int laneidx)
	{
	int count=0;
	
		if(laneidx<0) return records.size();
		for(Record r:records) if(r.laneIndex()==laneidx) count++;
		return count;
	}
	
	/**
	 * count the vehicle for a certain type
	 * @param laneidx
	 * lane index, -1 for all lanes
	 * @param type
	 * vehicle type
	 * @return
	 */
	public int vehicleCount(int laneidx,Vehicle.Type type)
	{
	int count=0;
		
		for(Record r:records) 
		{	
			if(laneidx>=0&&r.laneIndex()!=laneidx) continue;
			if(r.type!=type) continue;
			
			count++;
		}
		
		return count;
	}
	
	/**
	 * get the lane occupancy (%)
	 * @param laneidx
	 * lane index, -1 for all lanes
	 * @return
	 */
	public double laneOccupancy(int laneidx)
	{
	long obtime1=Long.MAX_VALUE,obtime2=0;;
	double occtime=0;
	
		for(Record r:this)
		{
			if(r.timeOfAppearance()<obtime1) obtime1=r.timeOfAppearance();
			if(r.timeOfAppearance()>obtime2) obtime2=r.timeOfAppearance();
			
			if(laneidx>=0&&r.laneIndex()!=laneidx) continue;
			occtime+=r.occupancyTime();
		}
		
		return 100*occtime/(obtime2-obtime1);
	}
	
	/**
	 * get the average speed
	 * @param laneidx
	 * lane index, -1 for all lanes
	 * @return
	 */
	public double avgSpeed(int laneidx)
	{
	double speed=0;
	int count=0;
	
		for(Record r:this)
		{
			if(laneidx>=0&&r.laneIndex()!=laneidx) continue;
			speed+=r.vehicleSpeed();
			count++;
		}
		
		return speed/count;
	}
	
	public String toString()
	{
	StringBuilder s;
		
		s=new StringBuilder();
		for(Record r:records) s.append(r.toString());
		return s.toString();		
	}
	
	public Iterator<Record> iterator() 
	{
		return records.iterator();
	}
	
	/**
	 * A vehicle record.
	 * @author nay0648
	 */
	public class Record implements Serializable
	{
	private static final long serialVersionUID = 4398524554134950212L;
	private String time;//time of appearance
	private int laneno;//lane number
	private Vehicle.Type type;//type
	private int occtime;//occupancy time
	private double speed;//speed
	
		/**
		 * @param time
		 * time of appearance
		 * @param laneno
		 * lane number (starts from 1)
		 * @param type
		 * vehicle type
		 * @param occtime
		 * occupancy time (ms)
		 * @param speed
		 * speed (km/h)
		 */
		public Record(String time,int laneno,Vehicle.Type type,int occtime,double speed)
		{
			this.time=time;
			this.laneno=laneno;
			this.type=type;
			this.occtime=occtime;
			this.speed=speed;
		}
		
		/**
		 * get vehicle appearance time as a string
		 * @return
		 */
		public String timeOfAppearanceString()
		{
			return time;
		}
		
		/**
		 * get the appearance time of the vehicle (ms)
		 * @return
		 */
		public long timeOfAppearance()
		{
			return Long.parseLong(time);
		}
			
		/**
		 * get the lane index
		 * @return
		 */
		public int laneIndex()
		{
			return laneno-1;
		}
		
		/**
		 * get vehicle type
		 * @return
		 */
		public Vehicle.Type vehicleType()
		{
			return type;
		}
		
		/**
		 * get the occupancy time (ms)
		 * @return
		 */
		public int occupancyTime()
		{
			return occtime;
		}
		
		/**
		 * get vehicle speed (km/h)
		 * @return
		 */
		public double vehicleSpeed()
		{
			return speed;
		}
		
		public String toString()
		{
		StringBuilder s;
		
			s=new StringBuilder();
			s.append(time+"\t\t");
			s.append(laneno+"\t\t");
			s.append(type+"\t\t");
			s.append(occtime+"\t\t");
			s.append(SPEED_FMT.format(speed)+"\r\n");
			
			return s.toString();
		}
	}
	
	public static void main(String[] args) throws IOException
	{
	VehicleLog vlog;
	
		vlog=new VehicleLog(new File("groundtruth/10-40min.txt"));
//		System.out.println(vlog);
		
		System.out.println("vehicle count:");
		System.out.println(vlog.vehicleCount(0));
		System.out.println(vlog.vehicleCount(1));
		System.out.println(vlog.vehicleCount(2));
		System.out.println();
		
		System.out.println("lane occupancy:");
		System.out.println(vlog.laneOccupancy(0));
		System.out.println(vlog.laneOccupancy(1));
		System.out.println(vlog.laneOccupancy(2));
		System.out.println();
		
		System.out.println("average speed:");
		System.out.println(vlog.avgSpeed(0));
		System.out.println(vlog.avgSpeed(1));
		System.out.println(vlog.avgSpeed(2));
	}
}
