package cn.ac.ioa.hccl.atm.gui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import cn.ac.ioa.hccl.atm.lane.*;

/**
 * The GUI of the acoustic traffic monitor, can visualize acoustic image.
 * @author nay0648
 *
 */
public class Monitor2D extends JFrame
{
private static final long serialVersionUID = 3748322777720457019L;
private LaneDetector lanedet;//lane detector
private int atiheight;//image height
private AcousticImagingPanel pati;//used to draw acoustic imaging
private JLabel ltime;//used to show time
private Format hmsformat=new DecimalFormat("00");
private Format msformat=new DecimalFormat("000");

	/**
	 * @param lanedet
	 * the lane detector
	 * @param atiheight
	 * image height
	 */
	public Monitor2D(LaneDetector lanedet,int atiheight)
	{
		super("Acoustic Traffic Monitor 2D");
		
		this.lanedet=lanedet;
		this.atiheight=atiheight;
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
		
		/*
		 * image panel
		 */
		pati=new AcousticImagingPanel(lanedet,atiheight);
		this.getContentPane().add(pati,BorderLayout.CENTER);
		
		/*
		 * time label
		 */
		ltime=new JLabel(" Time: ");
		this.getContentPane().add(ltime,BorderLayout.SOUTH);
		
		/*
		 * show the GUI
		 */
		this.setSize(pati.imageWidth()+40,pati.imageHeight()+80);
		DisplayMode dmode=GraphicsEnvironment.getLocalGraphicsEnvironment().
		getDefaultScreenDevice().getDisplayMode();
		this.setLocation(
				Math.max((dmode.getWidth()-this.getWidth())/2,0),
				Math.max((dmode.getHeight()-this.getHeight())/2,0));
		this.setVisible(true);
	}
	
	/**
	 * set current frame time
	 * @param t
	 * time
	 */
	private void setFrameTime(long t)
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
	
	/**
	 * update traffic information
	 * @throws IOException 
	 */
	public void updateTrafficInfo() throws IOException
	{
		lanedet.updateTrafficInfo();
		
		pati.updateTrafficInfo();
		pati.getParent().repaint();
		
		setFrameTime(lanedet.monitorArray().frameTime());
	}
}
