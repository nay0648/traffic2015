package cn.ac.ioa.hccl.atm.gui;
import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import cn.ac.ioa.hccl.atm.event.*;
import cn.ac.ioa.hccl.atm.lane.*;

/**
 * Used to visualize lane responses and peaks and valleys.
 * @author nay0648
 */
public class LaneDetectionPanel extends JPanel
{
private static final long serialVersionUID = -4144775874795670776L;
private static final Color CENTER_COLOR=Color.DARK_GRAY;//lane center color
private static final float LANE_ALPHA=0.4f;//lane transparency
private static final float CENTER_ALPHA=1f;//lane center alpha
private LaneDetector lanedet;
private SlicedResponseDataset dataset;
private XYPlot plot;

	/**
	 * @param lanedet
	 * lane detector reference
	 */
	public LaneDetectionPanel(LaneDetector lanedet)
	{
	DefaultXYItemRenderer renderer;
	JFreeChart chart;
	ChartPanel cp;
	
		this.lanedet=lanedet;
		dataset=new SlicedResponseDataset();
		lanedet.addLaneDetectedListener(dataset);
		
		renderer=new DefaultXYItemRenderer();
		renderer.setBaseShapesVisible(false);
			
		//construct the plot
		plot=new XYPlot(
				dataset,
//				new NumberAxis("Scanning Azimuth (degree)"),
				new NumberAxis("方位角 (度)"),
//				new NumberAxis("Probability Density"),
				new NumberAxis("概率密度"),
				renderer);
		plot.getDomainAxis().setLowerMargin(0);
		plot.getDomainAxis().setUpperMargin(0);
//		plot.getRangeAxis().setRange(0, 0.01);
				
		/*
		 * the combined chart
		 */
		chart=new JFreeChart(plot);
		chart.removeLegend();
		cp=new ChartPanel(chart);
		cp.setPreferredSize(new Dimension(300,200));
		this.setLayout(new BorderLayout());
		this.add(cp,BorderLayout.CENTER);		
	}
	
	/**
	 * For the lane detection response.
	 * @author nay0648
	 */
	private class SlicedResponseDataset implements XYDataset, LaneDetectionListener
	{
	private Set<DatasetChangeListener> listeners=new HashSet<DatasetChangeListener>();
	
		public int getSeriesCount() 
		{
			return 1;
		}
		
		@SuppressWarnings("rawtypes")
		public Comparable getSeriesKey(int arg0) 
		{
			if(arg0==0) return "Sliced Response";
			else throw new IndexOutOfBoundsException(0+", "+arg0);
		}

		@SuppressWarnings("rawtypes")
		public int indexOf(Comparable arg0) 
		{
			if("Sliced Response".equals(arg0)) return 0;
			else return -1;
		}

		public void addChangeListener(DatasetChangeListener arg0) 
		{
			listeners.add(arg0);
		}
		
		public void removeChangeListener(DatasetChangeListener arg0) 
		{
			listeners.remove(arg0);
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
			return lanedet.scanningAzimuth().length;
		}
		
		public Number getX(int arg0, int arg1) 
		{
			if(arg0==0) return lanedet.scanningAzimuth()[arg1];
			else throw new IndexOutOfBoundsException(0+", "+arg0);
		}
		
		public double getXValue(int arg0, int arg1) 
		{
			return getX(arg0,arg1).doubleValue();
		}
		
		public Number getY(int arg0, int arg1) 
		{
			if(arg0==0) return lanedet.laneResponse()[arg1];
			else throw new IndexOutOfBoundsException(0+", "+arg0);
		}
		
		public double getYValue(int arg0, int arg1) 
		{
			return getY(arg0,arg1).doubleValue();
		}

		public void laneDetected(LaneDetectionEvent e) 
		{
		DatasetChangeEvent ce;
		LaneDetector.LanePosition pos;
		IntervalMarker lm;
		ValueMarker cm;
		
			/*
			 * select the detected lanes
			 */
			plot.clearDomainMarkers();
			for(int lidx=0;lidx<lanedet.numLanes();lidx++) 
			{
				pos=lanedet.lanePosition(lidx);
				if(pos==null) continue;
				
				lm=new IntervalMarker(pos.lowerBoundAzimuth(),pos.upperBoundAzimuth());
				lm.setPaint(DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[lidx+1]);
				lm.setAlpha(LANE_ALPHA);
				plot.addDomainMarker(lm);
				
				cm=new ValueMarker(pos.centerAzimuth());
				cm.setPaint(CENTER_COLOR);
				cm.setAlpha(CENTER_ALPHA);
				plot.addDomainMarker(cm);
			}
			
			/*
			 * fire the event
			 */
			ce=new DatasetChangeEvent(this,this);
			for(DatasetChangeListener l:listeners) l.datasetChanged(ce);
		}
	}
}
