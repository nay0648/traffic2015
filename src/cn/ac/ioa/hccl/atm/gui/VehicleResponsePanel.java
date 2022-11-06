package cn.ac.ioa.hccl.atm.gui;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import cn.ac.ioa.hccl.atm.vehicle.*;

/**
 * Show vehicle responses in each lane.
 * @author nay0648
 */
public class VehicleResponsePanel extends JPanel
{
private static final long serialVersionUID = 5660060606608759405L;
private VehicleDetector vdet;//vehicle detector reference
private VehicleResponseDataset[] vdata;

	/**
	 * @param vdet
	 * vehicle detector reference
	 * @param vreslen
	 * length of the cache
	 */
	public VehicleResponsePanel(VehicleDetector vdet,int vreslen)
	{
	CombinedDomainXYPlot cplot;//the combined plot
	XYPlot plot;
	StandardXYItemRenderer renderer;
	JFreeChart chart;
	
		this.vdet=vdet;
		
		/*
		 * the dataset
		 */
		vdata=new VehicleResponseDataset[vdet.numLanes()];
		for(int i=0;i<vdata.length;i++) 
			vdata[i]=new VehicleResponseDataset(vdet.lane(i),vreslen);
		
		/*
		 * the combined plot
		 */
		cplot=new CombinedDomainXYPlot(new NumberAxis());
		cplot.setGap(10);
		cplot.getDomainAxis().setLowerMargin(0);
		cplot.getDomainAxis().setUpperMargin(0);
		
		/*
		 * add each subplot
		 */
		for(int i=0;i<vdet.numLanes();i++)
		{
			renderer=new StandardXYItemRenderer();
					
			for(int zc=0;zc<vdata[i].getSeriesCount();zc++) 
				renderer.setSeriesPaint(zc, DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[zc]);
			if(i!=0) 
				for(int zc=0;zc<vdata[i].getSeriesCount();zc++) 
					renderer.setSeriesVisibleInLegend(zc, false);
				
			//construct the plot
			plot=new XYPlot(
					vdata[i],
					null,
//					new NumberAxis("Lane "+(i+1)),
					new NumberAxis("车道 "+(i+1)),
					renderer);
			plot.getRangeAxis().setRange(0, 1);
			
			cplot.add(plot);
		}
		
		/*
		 * the combined chart
		 */
		chart=new JFreeChart(cplot);
		this.setLayout(new BorderLayout());
		this.add(new ChartPanel(chart),BorderLayout.CENTER);
	}
	
	/**
	 * update traffic information
	 */
	public void updateTrafficInfo()
	{
		for(int i=0;i<vdet.numLanes();i++) vdata[i].updateTrafficInfo();
	}
	
	/**
	 * Dataset for the array response
	 * @author nay0648
	 *
	 */
	private class VehicleResponseDataset implements XYDataset
	{
	private Lane lane;//the lane reference
	private double[][] vres;//vehicle responses, each row for a zone
	private int respointer=0;
	private double[] x;
	private List<DatasetChangeListener> llist=new LinkedList<DatasetChangeListener>();
	
		/**
		 * @param lane
		 * the lane reference
		 * @param size
		 * dataset size
		 */
		public VehicleResponseDataset(Lane lane,int size)
		{
			this.lane=lane;
			vres=new double[lane.numZones()][size];
			
			x=new double[size];
			for(int i=0;i<x.length;i++) x[i]=i;
		}
	
		public int getSeriesCount() 
		{
			return vres.length;
		}

		@SuppressWarnings("rawtypes")
		public Comparable getSeriesKey(int arg0) 
		{
//			if(arg0>=0&&arg0<vres.length) return "Zone "+arg0;
			if(arg0>=0&&arg0<vres.length) return "区域 "+arg0;
			else throw new IndexOutOfBoundsException(arg0+", "+vres.length);
		}

		@SuppressWarnings("rawtypes")
		public int indexOf(Comparable arg0) 
		{
		String[] szone;
			
			szone=arg0.toString().trim().split("\\s+");
			
			if(szone.length!=2) return -1;
//			if("Zone".equals(szone[0])) 
			if("区域".equals(szone[0])) 
			{
				try
				{
					return Integer.parseInt(szone[1]);
				}
				catch(NumberFormatException e)
				{
					return -1;
				}
			}
			else return -1;
		}

		public void addChangeListener(DatasetChangeListener arg0) 
		{
			llist.add(arg0);
		}
		
		public void removeChangeListener(DatasetChangeListener arg0) 
		{
			llist.remove(arg0);
		}

		public DatasetGroup getGroup() 
		{
			return null;
		}

		public void setGroup(DatasetGroup arg0) 
		{}

		public DomainOrder getDomainOrder()
		{
			return DomainOrder.ASCENDING;
		}

		public int getItemCount(int arg0) 
		{
			return x.length;
		}

		public Number getX(int arg0,int arg1)
		{
			return x[arg1];
		}

		public double getXValue(int arg0,int arg1)
		{
			return getX(arg0,arg1).doubleValue();
		}

		public Number getY(int arg0,int arg1)
		{
		int idx;
			
			idx=respointer-1-arg1;
			if(idx<0) idx+=vres[0].length;
			return vres[arg0][idx];
		}

		public double getYValue(int arg0,int arg1)
		{
			return getY(arg0,arg1).doubleValue();
		}
		
		/**
		 * add vehicle responses of a lane to the dataset
		 */
		public void updateTrafficInfo()
		{
		DatasetChangeEvent event;
						
			for(int i=0;i<vres.length;i++) 
				vres[i][respointer]=lane.zone(i).normalizedZoneResponse();
			
			if(++respointer>=vres[0].length) respointer=0;
			
			/*
			 * dataset is changed
			 */
			event=new DatasetChangeEvent(this,this);
			for(DatasetChangeListener l:llist) l.datasetChanged(event);
		}
	}
}
