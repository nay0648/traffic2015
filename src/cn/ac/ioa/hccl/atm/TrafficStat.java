package cn.ac.ioa.hccl.atm;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.event.*;
import javax.swing.table.*;

import cn.ac.ioa.hccl.atm.array.*;
import cn.ac.ioa.hccl.atm.event.*;
import cn.ac.ioa.hccl.atm.vehicle.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Traffic flow statistical information.
 * @author nay0648
 */
public class TrafficStat implements Serializable, TableModel, VehicleDetectionListener
{
private static final long serialVersionUID = 1103585719955381543L;
private MonitorArray monitor;//monitor reference
private Set<TableModelListener> ls=new HashSet<TableModelListener>();
private Vehicle.Type[] currenttype;//current vehicle type
private double[] currentspeed;//current vehicle speed
private int[] countl;//large vehicle count per lane
private int[] counts;//small vehicle count
private double[] totalspeed;//total vehicle speed
private double[] totaloccupation;//total lane occupation time
private Format numformat=new DecimalFormat("0.00");
private VehicleLogger vlogger=null;//used to generate the vehicle log

	/**
	 * @param monitor
	 * array reference
	 * @param numlanes
	 * number of lanes
	 */
	public TrafficStat(MonitorArray monitor,int numlanes)
	{
		this.monitor=monitor;
		
		currenttype=new Vehicle.Type[numlanes];
		currentspeed=new double[numlanes];
		countl=new int[numlanes];
		counts=new int[numlanes];
		totalspeed=new double[numlanes];
		totaloccupation=new double[numlanes];
		
		try
		{
			vlogger=new FileVehicleLogger();
		}
		catch(IOException e)
		{
			throw new RuntimeException("failed to generate vehicle logger.",e);
		}
	}
	
	/**
	 * get the number of lanes
	 * @return
	 */
	public int numLanes()
	{
		return countl.length;
	}

	public int getRowCount() 
	{
		//plus a name row, a total row
		return numLanes()+2;
	}

	public int getColumnCount() 
	{
		return 8;
	}

	public String getColumnName(int columnIndex) 
	{
		return null;
	}

	public Class<?> getColumnClass(int columnIndex) 
	{
		return String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) 
	{
		return false;
	}
	
	/**
	 * get row name
	 * @param rowidx
	 * row index
	 * @return
	 */
	private String rowName(int rowidx)
	{
		if(rowidx==0) return "";
		else if(rowidx>=1&&rowidx<=numLanes()) return "车道"+rowidx;
//		else if(rowidx>=1&&rowidx<=numLanes()) return "Lane "+rowidx;
		else if(rowidx==numLanes()+1) return "总计";
//		else if(rowidx==numLanes()+1) return "Total";
		else return "";
	}
	
	/**
	 * get column name
	 * @param colidx
	 * column index
	 * @return
	 */
	private String columnName(int colidx)
	{
		switch(colidx)
		{
			case 1: return "当前车型";
			case 2: return "当前速度（km/h）";
			case 3: return "大车流量";
			case 4: return "小车流量";
			case 5: return "车流量";
			case 6: return "平均速度（km/h）";
			case 7: return "车道占有率（%）";
			
//			case 1: return "Vehicle Type";
//			case 2: return "Vehicle Speed (km/h)";
//			case 3: return "Large Vehicle Count";
//			case 4: return "Small Vehicle Count";
//			case 5: return "Vehicle Count";
//			case 6: return "Average Speed (km/h)";
//			case 7: return "Lane Occupancy (%)";
			
			default: return "";
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) 
	{
		/*
		 * row name and column name
		 */
		if(rowIndex==0&&columnIndex==0) return "";
		//row name
		else if(columnIndex==0) return rowName(rowIndex);
		//column name
		else if(rowIndex==0) return columnName(columnIndex);
		
		switch(columnIndex)
		{
			//current vehicle type
			case 1: return currentVehicleType(rowIndex-1);
			//current vehicle speed
			case 2:
			{
				if(rowIndex-1==numLanes()) return "--";
				else return numformat.format(currentSpeed(rowIndex-1));
			}
			//large vehicle count
			case 3: return Integer.valueOf(largeVehicleCount(rowIndex-1)).toString();
			//small vehicle count
			case 4: return Integer.valueOf(smallVehicleCount(rowIndex-1)).toString();
			//total vehicle count
			case 5: return Integer.valueOf(vehicleCount(rowIndex-1)).toString();
			//average speed
			case 6: return numformat.format(averageSpeed(rowIndex-1));
			//averate lane occupation rate
			case 7: return numformat.format(averageLaneOccupationRate(rowIndex-1)*100);
			default: return "";
		}
	}
	
	/**
	 * get current vehicle type
	 * @param laneidx
	 * lane index
	 * @return
	 */
	public String currentVehicleType(int laneidx)
	{
		if(laneidx==numLanes()) return "--";
		else if(currenttype[laneidx]==null) return "";
		else if(currenttype[laneidx]==Vehicle.Type.large) return "大车";
//		else if(currenttype[laneidx]==Vehicle.Type.large) return "Large";
		else if(currenttype[laneidx]==Vehicle.Type.small) return "小车";
//		else if(currenttype[laneidx]==Vehicle.Type.small) return "Small";
		else return "";
	}
	
	/**
	 * get current speed
	 * @param laneidx
	 * lane index
	 * @return
	 */
	public double currentSpeed(int laneidx)
	{
		if(laneidx==numLanes()) return 0;
		else return currentspeed[laneidx];
	}
	
	/**
	 * get large vehicle count
	 * @param laneidx
	 * lane index, numlanes for total
	 * @return
	 */
	public int largeVehicleCount(int laneidx)
	{
		if(laneidx==numLanes()) return BLAS.sum(countl);
		else return countl[laneidx];
	}
	
	/**
	 * get small vehicle count
	 * @param laneidx
	 * lane index, numlanes for total
	 * @return
	 */
	public int smallVehicleCount(int laneidx)
	{
		if(laneidx==numLanes()) return BLAS.sum(counts);
		else return counts[laneidx];
	}
	
	/**
	 * get vehicle count (large+small)
	 * @param laneidx
	 * lane index, numlanes for total
	 * @return
	 */
	public int vehicleCount(int laneidx)
	{
		if(laneidx==numLanes()) return BLAS.sum(countl)+BLAS.sum(counts);
		else return countl[laneidx]+counts[laneidx];		
	}
	
	/**
	 * get average speed (km/h)
	 * @param laneidx
	 * lane index, numlanes for total
	 * @return
	 */
	public double averageSpeed(int laneidx)
	{
	double speed;	
		
		if(laneidx==numLanes()) 
			speed=BLAS.sum(totalspeed)/(BLAS.sum(countl)+BLAS.sum(counts));
		else speed=totalspeed[laneidx]/(countl[laneidx]+counts[laneidx]);
		
		if(Double.isNaN(speed)) return 0;
		else return speed;
	}
	
	/**
	 * get average lane occupation rate
	 * @param laneidx
	 * lane index, numlanes for total
	 * @return
	 */
	public double averageLaneOccupationRate(int laneidx)
	{
	long time;
	double rate;
	
		time=monitor.frameTime();
		
		if(laneidx==numLanes()) rate=BLAS.sum(totaloccupation)/time;
		else rate=totaloccupation[laneidx]/time;
		
		if(Double.isNaN(rate)) return 0;
		else return rate;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) 
	{}

	public void addTableModelListener(TableModelListener l) 
	{
		ls.add(l);
	}

	public void removeTableModelListener(TableModelListener l) 
	{
		ls.remove(l);
	}

	public void vehicleDetected(VehicleDetectionEvent e) 
	{
	Vehicle vehicle;
	int laneidx;
	TableModelEvent tme;
		
		vehicle=e.vehicle();
		laneidx=vehicle.laneIndex();
		
		currentspeed[laneidx]=vehicle.speed();
		
		currenttype[laneidx]=vehicle.type();
		//accumulate vehicle count
		switch(vehicle.type())
		{
			case large:
			{
				countl[laneidx]++;
			}break;
			case small:
			{
				counts[laneidx]++;
			}break;
		}
		
		//accumulate speed
		totalspeed[laneidx]+=Math.abs(currentspeed[laneidx]);
		
		//accumulate occupation time
		totaloccupation[laneidx]+=vehicle.avgPassingTime();
		
		/*
		 * fire table changed event
		 */
		tme=new TableModelEvent(this);
		for(TableModelListener l:ls) l.tableChanged(tme);
		
		//generate the log
		if(vlogger!=null) 
		{
			try
			{
				vlogger.logVehicle(vehicle);
			}
			catch (IOException e1)
			{
				throw new RuntimeException("failed to log vehicle.",e1);
			}
		}
	}

	public void vehicleEnteredDetectionZone(VehicleDetectionEvent e) 
	{}

	public void vehicleLeavedDetectionZone(VehicleDetectionEvent e) 
	{}
	
	public void finalize()
	{
		try
		{
			if(vlogger!=null) vlogger.close();
		}
		catch(IOException e)
		{}
	}
}
