package cn.ac.ioa.hccl.atm.gui;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import org.jfree.chart.plot.*;
import cn.ac.ioa.hccl.atm.lane.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Used to draw acoustic image.
 * @author nay0648
 *
 */
public class AcousticImagingPanel extends JPanel
{
private static final long serialVersionUID = 1118701541946575716L;
private static final Color CENTER_COLOR=Color.ORANGE;//lane center color
private static final double LANE_ALPHA=0.2;//lane transparency
private int[] colormap;//the colormap
private LaneDetector lanedet;//the lane detector
private BufferedImage atibuffer;//the acoustic traffic image buffer, each one for a slice
private int bufferpointer=0;//loop buffer pointer
private int x0,y0;//the image origin

	/**
	 * @param lanedet
	 * lane detector reference
	 * @param atiheight
	 * acoustic image height
	 */
	public AcousticImagingPanel(LaneDetector lanedet,int atiheight)
	{
	Color[] tempmap;
	Graphics g;
	
		this.lanedet=lanedet;
		
		tempmap=ShortTimeFourierTransformer.colormapJet();
		colormap=new int[tempmap.length];
		for(int i=0;i<colormap.length;i++) colormap[i]=tempmap[i].getRGB();
		
		/*
		 * initialize the image buffer
		 */
		atibuffer=new BufferedImage(
				lanedet.scanningAzimuth().length,
				atiheight,
				BufferedImage.TYPE_INT_RGB);
		g=atibuffer.getGraphics();
		g.setColor(tempmap[0]);
		g.fillRect(0, 0, atibuffer.getWidth(), atibuffer.getHeight());
	}
	
	/**
	 * get image width
	 * @return
	 */
	public int imageWidth()
	{
		return atibuffer.getWidth();
	}
	
	/**
	 * get image height
	 * @return
	 */
	public int imageHeight()
	{
		return atibuffer.getHeight();
	}
	
	/**
	 * get x coordinate of the origin
	 * @return
	 */
	public int originX()
	{
		return x0;
	}

	/**
	 * get y coordinate of the origin
	 * @return
	 */
	public int originY()
	{
		return y0;
	}
	
	/**
	 * update traffic information
	 */
	public void updateTrafficInfo()
	{
	LaneDetector.LanePosition lp;
	Color cb,cf;
	double[] response;
	int cidx;
	
		response=lanedet.normalizedSlicedResponse();//get the sliced response
		
		/*
		 * !!!!!!!!!!!!!!!!!!
		 * image stretching required
		 */
		
		//fill the slice
		for(int x=0;x<response.length;x++) 
		{
			/*
			 * convert continuous response [0, 1] to colormap index
			 */
			cidx=(int)Math.round(response[x]*(colormap.length-1));
			if(cidx<0) cidx=0;
			else if(cidx>colormap.length-1) cidx=colormap.length-1;
				
			atibuffer.setRGB(x, bufferpointer, colormap[cidx]);
		}
		
		//draw lanes
		for(int i=0;i<lanedet.numLanes();i++)
		{
			lp=lanedet.lanePosition(i);
			if(lp==null) continue;//lane is not detected
				
			/*
			 * draw lane
			 */
			cf=(Color)DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i+1];
			for(int li=lp.lowerBoundIndex();li<=lp.upperBoundIndex();li++) 
			{
				cb=new Color(atibuffer.getRGB(li, bufferpointer));
				atibuffer.setRGB(
						li, 
						bufferpointer, 
						mergeColor(cf, cb, LANE_ALPHA).getRGB());
			}
			
			/*
			 * draw lane center
			 */
			cf=CENTER_COLOR;
			cb=new Color(atibuffer.getRGB(lp.centerIndex(), bufferpointer));
			atibuffer.setRGB(
					lp.centerIndex(), 
					bufferpointer, 
					mergeColor(cf, cb, LANE_ALPHA).getRGB());
		}
		
		//adjust loop buffer pointer
		if(++bufferpointer>=atibuffer.getHeight()) bufferpointer=0;	
	}
	
	/**
	 * merge two colors
	 * @param fg
	 * foreground color
	 * @param bg
	 * background color
	 * @param alpha
	 * alpha value of the foreground
	 * @return
	 */
	private Color mergeColor(Color fg,Color bg,double alpha)
	{
	int r,g,b;
	
		r=(int)Math.round(fg.getRed()*alpha+bg.getRed()*(1-alpha));
		if(r<0) r=0;
		else if(r>255) r=255;
	
		g=(int)Math.round(fg.getGreen()*alpha+bg.getGreen()*(1-alpha));
		if(g<0) g=0;
		else if(g>255) g=255;
	
		b=(int)Math.round(fg.getBlue()*alpha+bg.getBlue()*(1-alpha));
		if(b<0) b=0;
		else if(b>255) b=255;
		
		return new Color(r,g,b);
	}
	
	public void paintComponent(Graphics g)
	{
	BufferedImage sub;
	
		super.paintComponents(g);
		
		/*
		 * calculate the image origin
		 */
		x0=(this.getWidth()-imageWidth())/2;
		if(x0<0) x0=0;
		y0=(this.getHeight()-imageHeight())/2;
		if(y0<0) y0=0;
	
		/*
		 * draw acoustic image
		 */
		sub=atibuffer.getSubimage(
				0, 
				bufferpointer, 
				atibuffer.getWidth(), 
				atibuffer.getHeight()-bufferpointer);
		g.drawImage(sub,x0,y0,null);
		
		if(bufferpointer!=0)
		{
			sub=atibuffer.getSubimage(
					0, 
					0, 
					atibuffer.getWidth(), 
					bufferpointer);
			g.drawImage(sub,x0,y0+atibuffer.getHeight()-bufferpointer,null);
		}
	}
}
