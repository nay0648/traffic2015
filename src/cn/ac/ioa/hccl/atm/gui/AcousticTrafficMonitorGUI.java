package cn.ac.ioa.hccl.atm.gui;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import cn.ac.ioa.hccl.atm.*;

/**
 * The GUI.
 * @author nay0648
 */
public class AcousticTrafficMonitorGUI extends JFrame
{
private static final long serialVersionUID = -5996145760337854536L;
private AcousticTrafficMonitor traffic;//the total statistics
private int atiheight;//acoustic traffic imaging height
private AcousticImagingPanel pati;//used to show acoustic traffic imaging
private LaneDetectionPanel planedet;//used to show lane detection results
private int vreslen;//number of cached vehicle responses
private VehicleResponsePanel pvres;//used to show vehicle response
private JLabel ltime;//label to show current time
private JTextField ftoffset;//time offset text
private long toffset=0;//time offset (ms)
private Format hmsformat=new DecimalFormat("00");
private Format msformat=new DecimalFormat("000");
private JButton bpause;//the pause/resume button

	/**
	 * @param traffic
	 * the total statistics reference
	 */
	public AcousticTrafficMonitorGUI(AcousticTrafficMonitor traffic)
	{
		super("Acoustic Traffic Monitor");
		
		this.traffic=traffic;
		
		atiheight=traffic.monitorArray().numFramesInTimeInterval(Param.GUI_TIMEWINDOWSIZE);
		vreslen=atiheight;
		
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
		
		//statistical data
		{
			JPanel pstat=new JPanel();
//			pstat.setBorder(new TitledBorder("Traffic Statistics"));
			pstat.setBorder(new TitledBorder("交通信息统计"));
			pstat.setLayout(new BorderLayout(5,5));
			this.getContentPane().add(pstat,BorderLayout.NORTH);
			
			/*
			 * statistical table
			 */
			JTable tstat=new JTable(traffic.trafficStatistics());
			pstat.add(tstat,BorderLayout.CENTER);
			
			/*
			 * time control
			 */
			JPanel ptctil=new JPanel();
			ptctil.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
			pstat.add(ptctil,BorderLayout.SOUTH);
			
//			ltime=new JLabel(" Time: 00:00:00.000");
			ltime=new JLabel(" 时间: 00:00:00.000");
			ptctil.add(ltime);
			
//			ptctil.add(new JLabel("Offset (ms): "));
			ptctil.add(new JLabel("提前量 (ms): "));
			ftoffset=new JTextField(8);
			ftoffset.setText("0");
			ptctil.add(ftoffset);
		}
		
		/*
		 * panel for visualization
		 */
		JPanel pvis=new JPanel();
		pvis.setLayout(new BoxLayout(pvis,BoxLayout.X_AXIS));
//		pvis.setLayout(new GridLayout(1,2,5,5));
		this.getContentPane().add(pvis,BorderLayout.CENTER);
		
		//acoustic imaging
		{
			JPanel pai=new JPanel();
//			pai.setBorder(new TitledBorder("Acoustic Traffic Imaging"));
			pai.setBorder(new TitledBorder("车辆噪点成像"));
			pai.setLayout(new BorderLayout());
			pvis.add(pai);
			
			pati=new AcousticImagingPanel(traffic.laneDetector(),atiheight);
			pai.add(pati,BorderLayout.CENTER);
			
			JLabel lpad=new JLabel("                                                                   ");
			pai.add(lpad,BorderLayout.SOUTH);
		}
		
		JPanel pplot=new JPanel();
		pplot.setLayout(new BoxLayout(pplot,BoxLayout.Y_AXIS));
		pvis.add(pplot);
		
		//lane detection
		{
			JPanel plane=new JPanel();
//			plane.setBorder(new TitledBorder("Lane Detection"));
			plane.setBorder(new TitledBorder("车道定位"));
			plane.setLayout(new BorderLayout());
			pplot.add(plane);
			
			planedet=new LaneDetectionPanel(traffic.laneDetector());
			plane.add(planedet, BorderLayout.CENTER);
		}
		
		//vehicle response
		{
			JPanel pvr=new JPanel();
//			pvr.setBorder(new TitledBorder("Vehicle Response"));
			pvr.setBorder(new TitledBorder("车辆检测响应"));
			pvr.setLayout(new BorderLayout());
			pplot.add(pvr);
			
			pvres=new VehicleResponsePanel(traffic.vehicleDetector(),vreslen);
			pvr.add(pvres, BorderLayout.CENTER);
		}
		
		//control panel
		{
			JPanel pct=new JPanel();
			pct.setLayout(new FlowLayout(FlowLayout.LEADING,5,5));
			this.getContentPane().add(pct,BorderLayout.SOUTH);
			
			/*
			 * lane detection button
			 */
//			JButton blanedet=new JButton("Detect Lanes");
			JButton blanedet=new JButton("定位车道");
			blanedet.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					traffic.laneDetector().threadDetectLanes();
				}
			});
			pct.add(blanedet);
			
			/*
			 * pause and resume button
			 */
//			bpause=new JButton("Pause");
			bpause=new JButton("暂停");
			bpause.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) 
				{
					if(traffic.isPaused()) AcousticTrafficMonitorGUI.this.resumeUpdate();
					else AcousticTrafficMonitorGUI.this.pauseUpdate();
				}
			});
			pct.add(bpause);
			
			/*
			 * single step button
			 */
//			JButton bsingle=new JButton("Single Step");
			JButton bsingle=new JButton("单步");
			bsingle.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) 
				{
					if(!traffic.isPaused()) AcousticTrafficMonitorGUI.this.pauseUpdate();
					else
					{
						AcousticTrafficMonitorGUI.this.updateTimeOffset();
						
						synchronized(traffic)
						{
							traffic.notify();
						}
					}
				}
			});
			pct.add(bsingle);
			
			/*
			 * sleep time
			 */
//			pct.add(new JLabel("Sleep Time (ms): "));
			pct.add(new JLabel("等待时间 (ms): "));
			JSpinner ssleep=new JSpinner(new SpinnerNumberModel(0,0,10,1));
			ssleep.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) 
				{
				JSpinner sp;
				
					sp=(JSpinner)e.getSource();
					traffic.setSleepTime((Integer)sp.getValue());
				}
			});
			pct.add(ssleep);
		}
		
		/*
		 * show the GUI
		 */
		this.pack();
		DisplayMode dmode=GraphicsEnvironment.getLocalGraphicsEnvironment().
		getDefaultScreenDevice().getDisplayMode();
		this.setLocation(
				Math.max((dmode.getWidth()-this.getWidth())/2,0),
				Math.max((dmode.getHeight()-this.getHeight())/2,0));
		this.setVisible(true);
	}
	
	/**
	 * update the time offset for experiments, offset will be set to 0 if 
	 * get a NumberFormatException
	 */
	private void updateTimeOffset()
	{
		try
		{
			toffset=Long.parseLong(ftoffset.getText());
		}
		catch(NumberFormatException e)
		{
			toffset=0;
			ftoffset.setText("0");
		}
	}
	
	/**
	 * pause the monitor
	 */
	private void pauseUpdate()
	{
		traffic.pauseMonitor();
//		bpause.setText("Resume");
		bpause.setText("继续");
	}
	
	/**
	 * resume the monitor update
	 */
	private void resumeUpdate()
	{
		updateTimeOffset();
		
		traffic.resumeMonitor();
//		bpause.setText("Pause");
		bpause.setText("暂停");
	}
	
	/**
	 * update current frame time
	 */
	private void updateTime()
	{
	long t,h,m,s;
		
		t=traffic.monitorArray().frameTime();
		t+=toffset;//apply the offset
		
		h=t/3600000;
		t%=3600000;
		m=t/60000;
		t%=60000;
		s=t/1000;
		t%=1000;
		
//		ltime.setText(" Time: "+
		ltime.setText(" 时间: "+
				hmsformat.format(h)+":"+
				hmsformat.format(m)+":"+
				hmsformat.format(s)+"."+
				msformat.format(t));
	}
	
	/**
	 * update traffic information
	 */
	public void updateTrafficInfo()
	{
		updateTime();
		
		pati.updateTrafficInfo();
		pati.getParent().repaint();
		
		pvres.updateTrafficInfo();
	}
}
