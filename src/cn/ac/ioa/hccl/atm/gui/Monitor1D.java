package cn.ac.ioa.hccl.atm.gui;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.plot.*;

/**
 * The monitor GUI.
 * @author nay0648
 *
 */
public class Monitor1D extends JFrame
{
private static final long serialVersionUID = -5064765767691914869L;
private double[] scanaz;//scanning azimuth (degree)
private ArrayResponseDataset dataset;//the dataset
private JFreeChart chart;//the chart
private ChartPanel chartpanel;//the panel contain the chart
private JLabel ltime;//used to show time
private Format hmsformat=new DecimalFormat("00");
private Format msformat=new DecimalFormat("000");

	/**
	 * @param scanaz
	 * scanning azimuth (degree)
	 */
	public Monitor1D(double[] scanaz)
	{
		super("Acoustic Traffic Monitor 1D");
		this.scanaz=scanaz;
		
		initGUI();
	}
	
	/**
	 * initialize GUI
	 */
	private void initGUI()
	{
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	
		this.getContentPane().setLayout(new BorderLayout(5,5));
		
		{
		XYPlot plot;
		
			dataset=new ArrayResponseDataset();
			
			//construct the plot
			plot=new XYPlot(
					dataset,
					new NumberAxis("Scanning Azimuth (degree)"),
					new NumberAxis("Amplitude"),
					new StandardXYItemRenderer());
			
			plot.getRangeAxis().setRange(0, 1.1);
			
			/*
			 * the combined chart
			 */
			chart=new JFreeChart(plot);
			chart.removeLegend();
			chartpanel=new ChartPanel(chart);
			
			this.getContentPane().add(chartpanel,BorderLayout.CENTER);
		}
		
		/*
		 * time label
		 */
		ltime=new JLabel(" Time: ");
		this.getContentPane().add(ltime,BorderLayout.SOUTH);
		
		this.pack();
		DisplayMode dmode=GraphicsEnvironment.getLocalGraphicsEnvironment().
		getDefaultScreenDevice().getDisplayMode();
		this.setLocation(
				Math.max((dmode.getWidth()-this.getWidth())/2,0),
				Math.max((dmode.getHeight()-this.getHeight())/2,0));
		this.setVisible(true);
	}
		
	/**
	 * Dataset for the array response
	 * @author nay0648
	 *
	 */
	private class ArrayResponseDataset implements XYDataset
	{
	private List<DatasetChangeListener> llist=new LinkedList<DatasetChangeListener>();	
	private double[] response;//array response
	
		public ArrayResponseDataset()
		{
			response=new double[scanaz.length];
		}
	
		public int getSeriesCount() 
		{
			return 1;
		}

		@SuppressWarnings("rawtypes")
		public Comparable getSeriesKey(int arg0) 
		{
			if(arg0==0) return "Array Response";
			else throw new IndexOutOfBoundsException(1+", "+arg0);
		}

		@SuppressWarnings("rawtypes")
		public int indexOf(Comparable arg0) 
		{
			if("Array Response".equals(arg0)) return 0;
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
			return scanaz.length;
		}

		public Number getX(int arg0,int arg1)
		{
			if(arg0==0) return scanaz[arg1];
			else throw new IndexOutOfBoundsException(1+", "+arg0);
		}

		public double getXValue(int arg0,int arg1)
		{
			return getX(arg0,arg1).doubleValue();
		}

		public Number getY(int arg0,int arg1)
		{
			if(arg0==0) return response[arg1];
			else throw new IndexOutOfBoundsException(1+", "+arg0);
		}

		public double getYValue(int arg0,int arg1)
		{
			return getY(arg0,arg1).doubleValue();
		}
		
		/**
		 * set array response
		 * @param response
		 * array response
		 */
		public void setSlicedResponse(double[] response)
		{
		DatasetChangeEvent event;
			
			if(response.length!=scanaz.length) throw new IllegalArgumentException(
					"illegal response size: "+response+", required: "+scanaz.length);
			
			System.arraycopy(response, 0, this.response, 0, response.length);
			
			/*
			 * notify dataset changed
			 */
			event=new DatasetChangeEvent(this,this);
			for(DatasetChangeListener l:llist) l.datasetChanged(event);
		}
	}
	
	/**
	 * update array response
	 * @param response
	 */
	public void setSlicedResponse(double[] response)
	{		
		dataset.setSlicedResponse(response);
	}
	
	/**
	 * set current frame time
	 * @param t
	 * time
	 */
	public void setFrameTime(long t)
	{
	long h,m,s;
		
		h=t/3600000;
		t%=3600000;
		m=t/60000;
		t%=60000;
		s=t/1000;
		t%=1000;
		
		ltime.setText(" Time: "+
				hmsformat.format(h)+":"+
				hmsformat.format(m)+":"+
				hmsformat.format(s)+"."+
				msformat.format(t));		
	}
}
