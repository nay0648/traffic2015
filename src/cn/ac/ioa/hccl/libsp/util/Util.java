package cn.ac.ioa.hccl.libsp.util;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.awt.*;
import java.awt.image.*;
import javax.sound.sampled.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * <h1>Description</h1>
 * Some utility methods.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version last modified: Dec 9, 2009
 */
public class Util implements Serializable
{
private static final long serialVersionUID=-5925645488248582955L;
	
	/**
	 * convert an arbitrary 2-dimension array to an image
	 * @param data
	 * input data
	 * @return
	 * A RGB image, but its content is still gray.
	 */
	public static BufferedImage toImage(double[][] data)
	{
	BufferedImage img;
	double max=Double.MIN_VALUE,min=Double.MAX_VALUE;
	double factor;
	int temp,rgb;
		
		//find the max and the min value of the input
		for(int y=0;y<data.length;y++)
			for(int x=0;x<data[y].length;x++)
			{
				if(data[y][x]>max) max=data[y][x];
				if(data[y][x]<min) min=data[y][x];
			}
		/*
		 * Use color image to prevent different gray level model transformation, 
		 * here a pixel with the same R, G, B value stands for a gray color.
		 */
		img=new BufferedImage(data[0].length,data.length,BufferedImage.TYPE_INT_RGB);
		/*
		 * normalize value to 0~255
		 */
		if(min<0)
		{
			factor=255/(max-min);//compensate min value to zero
			for(int y=0;y<data.length;y++)
				for(int x=0;x<data[y].length;x++)
				{
					temp=(int)((data[y][x]-min)*factor);
					rgb=temp;
					rgb|=temp<<8;
					rgb|=temp<<16;
					img.setRGB(x,y,rgb);
				}
		}
		else
		{
			factor=255/max;
			for(int y=0;y<data.length;y++)
				for(int x=0;x<data[y].length;x++)
				{
					temp=(int)(data[y][x]*factor);
					rgb=temp;
					rgb|=temp<<8;
					rgb|=temp<<16;
					img.setRGB(x,y,rgb);
				}
		}
		return img;
	}
	
	/**
	 * convert a map matrix to an image
	 * @param map
	 * Its values range from 0 to 1, values smaller than 0 or larger than 1 
	 * will be turncated.
	 * @return
	 */
	public static BufferedImage mapToImage(double[][] map)
	{
	BufferedImage img;
	int temp,rgb;
		
		img=new BufferedImage(map[0].length,map.length,BufferedImage.TYPE_INT_RGB);
		for(int y=0;y<map.length;y++)
			for(int x=0;x<map[y].length;x++)
			{
				temp=(int)(map[y][x]*255.0);
				//turncate
				if(temp<0) temp=0;
				else if(temp>255) temp=255;
				rgb=temp;
				rgb|=temp<<8;
				rgb|=temp<<16;
				img.setRGB(x,y,rgb);
			}
		return img;
	}
	
	/**
	 * Convert data into image, data will be turncated to the range of 0..255 
	 * if data value exceeds this range.
	 * @param data
	 * input data
	 * @return
	 */
	public static BufferedImage turncatedToImage(double[][] data)
	{
	BufferedImage img;
	int temp,rgb;
	
		img=new BufferedImage(data[0].length,data.length,BufferedImage.TYPE_INT_RGB);
		for(int y=0;y<data.length;y++)
			for(int x=0;x<data[y].length;x++)
			{
				temp=(int)data[y][x];
				//turncate
				if(temp<0) temp=0;
				else if(temp>255) temp=255;
				rgb=temp;
				rgb|=temp<<8;
				rgb|=temp<<16;
				img.setRGB(x,y,rgb);
			}
		return img;
	}
	
	/**
	 * convert an image to a 2D array
	 * @param image
	 * an image
	 * @return
	 * each entry value belongs to [0, 255], represents the grayscale value
	 */
	public static double[][] image2GrayscaleMap(BufferedImage image)
	{
	WritableRaster raster;
	double[] pixel;
	double[][] map;
	
		raster=image.getRaster();
		pixel=new double[raster.getNumBands()];
		map=new double[image.getHeight()][image.getWidth()];
		for(int y=0;y<raster.getHeight();y++)
			for(int x=0;x<raster.getWidth();x++)
			{
				raster.getPixel(x,y,pixel);
				map[y][x]=BLAS.mean(pixel);
			}
		return map;
	}
	
	/**
	 * draw original image and result image into one image
	 * @param origin
	 * original image
	 * @param result
	 * result image
	 * @return
	 */
	public static BufferedImage drawResult(BufferedImage origin,BufferedImage result)
	{
	BufferedImage img;
	Graphics g;
	
		img=new BufferedImage(
				//with one pixel gap
				origin.getWidth()+result.getWidth()+1,
				Math.max(origin.getHeight(),result.getHeight()),
				result.getType());
		g=img.getGraphics();
		g.drawImage(origin,0,0,null);
		g.drawImage(result,origin.getWidth()+1,0,null);
		return img;
	}
	
	/**
	 * draw several image into one large image to show result
	 * @param row, column
	 * image matrix size
	 * @param padding
	 * padding pixels between images
	 * @param images
	 * several images
	 * @return
	 */
	public static BufferedImage drawResult(
			int row,int column,int padding,BufferedImage... images)
	{
	int maxw=0,maxh=0;
	BufferedImage result,subimg;
	Graphics g;
	int index=0;
	int x0,y0;
	
		if(images.length>row*column) throw new IllegalArgumentException(
				"illgal number of images: "+images.length+
				", required: less than or equal to "+(row*column));
		//find the max image width and height
		for(BufferedImage img:images)
		{
			if(img.getWidth()>maxw) maxw=img.getWidth();
			if(img.getHeight()>maxh) maxh=img.getHeight();
		}
		//the result image
		result=new BufferedImage(
				maxw*column+padding*(column-1),
				maxh*row+padding*(row-1),
				BufferedImage.TYPE_INT_ARGB);
		/*
		 * fill background transparent
		 */
		g=result.getGraphics();
		g.setColor(new Color(0,0,0,0));
		g.fillRect(0,0,result.getWidth(),result.getHeight());
		/*
		 * draw images
		 */
		y0=0;
draw:	for(int i=0;i<row;i++)
		{
			x0=0;
			for(int j=0;j<column;j++)
			{
				subimg=images[index++];
				g.drawImage(subimg,x0,y0,null);
				x0+=maxw+padding;
				if(index>=images.length) break draw;
			}
			y0+=maxh+padding;
		}
		return result;
	}

	/**
	 * show experiment result
	 * @param img
	 * result image
	 */
	public static void imshow(BufferedImage img)
	{
		new PPImageObserver(img);
	}
	
	/**
	 * show multiple images
	 * @param row
	 * number of rows
	 * @param column
	 * number of columns
	 * @param padding
	 * padding pixels
	 * @param images
	 * images
	 */
	public static void imshow(int row,int column,int padding,BufferedImage... images)
	{
		imshow(drawResult(row,column,padding,images));
	}
	
	/**
	 * <h1>Description</h1>
	 * Used to indicate the channel dimension.
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Jan 4, 2011 2:15:00 PM, revision:
	 */
	public enum Dimension
	{
		/**
		 * Each row of the data file is a observed channel.
		 */
		ROW,
		/**
		 * Each column of the data file is a observed channel.
		 */
		COLUMN
	}
	
	/**
	 * load a singal channel's signal from file
	 * @param path
	 * file path
	 * @param d
	 * indicate the channel dimension
	 * @return
	 * @throws IOExceptoin
	 */
	public static double[] loadSignal(File path,Dimension d) throws IOException
	{
	BufferedReader in=null;
	
		try
		{
			in=new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			switch(d)
			{
				case ROW:
				{
				String[] sentry;	
				double[] sig;	
				
					for(String ts=null;(ts=in.readLine())!=null;)
					{
						ts=ts.trim();
						if(ts.length()==0) continue;
						sentry=ts.split("\\s+");
						sig=new double[sentry.length];
						for(int i=0;i<sig.length;i++) sig[i]=Double.parseDouble(sentry[i]);
						return sig;
					}
				}break;
				case COLUMN:
				{
				List<Double> sigl;
				double[] sig;
				int idx=0;
				
					sigl=new LinkedList<Double>();
					for(String ts=null;(ts=in.readLine())!=null;)
					{
						ts=ts.trim();
						if(ts.length()==0) continue;
						sigl.add(Double.parseDouble(ts));
					}
					sig=new double[sigl.size()];
					for(double s:sigl) sig[idx++]=s;
					return sig;
				}
				default: throw new IllegalArgumentException("unknown dimension: "+d);
			}
			return null;
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
	 * load all observed signals from file
	 * @param path
	 * file path
	 * @param d
	 * indicate the channel dimension
	 * @return
	 * @throws IOException
	 * each row of the array is a observed channel
	 */
	public static double[][] loadSignals(File path,Dimension d) throws IOException
	{
	BufferedReader in=null;

		try
		{
			in=new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			switch(d)
			{
				case ROW:
				{
				List<double[]> sigl;
				String[] sentry;
				double[] sig;
				double[][] sigs;
				int idx=0;
					
					/*
					 * load data from file
					 */
					sigl=new LinkedList<double[]>();
					for(String ts=null;(ts=in.readLine())!=null;)
					{
						ts=ts.trim();
						if(ts.length()==0) continue;
						sentry=ts.split("\\s+");
						sig=new double[sentry.length];
						for(int i=0;i<sig.length;i++) sig[i]=Double.parseDouble(sentry[i]);
						sigl.add(sig);
					}
					/*
					 * convert from list to array
					 */
					sigs=new double[sigl.size()][];
					for(double[] ch:sigl) sigs[idx++]=ch;
					return sigs;
				}
				case COLUMN:
				{
				List<String[]> sigl;
				double[][] sigs;
				int j=0;
					
					/*
					 * load data from file 
					 */
					sigl=new LinkedList<String[]>();
					for(String ts=null;(ts=in.readLine())!=null;)
					{
						ts=ts.trim();
						if(ts.length()==0) continue;
						sigl.add(ts.split("\\s+"));
					}
					/*
					 * convert to array
					 */
					sigs=new double[sigl.get(0).length][sigl.size()];
					for(String[] sig:sigl) 
					{
						for(int i=0;i<sig.length;i++) sigs[i][j]=Double.parseDouble(sig[i]);
						j++;
					}
					return sigs;	
				}
				default: throw new IllegalArgumentException("unknown dimension: "+d);
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
	 * save a signal sequence into file
	 * @param sig
	 * a signal sequence
	 * @param path
	 * destination file path
	 * @param d
	 * indicate the saved signal's dimension
	 * @throws IOException
	 */
	public static void saveSignal(double[] sig,File path,Dimension d) throws IOException
	{
	BufferedWriter out=null;
	
		try
		{
			out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
			switch(d)
			{
				case ROW:
				{
					for(double s:sig) out.write(s+" ");
					out.write("\n");
					out.flush();
				}break;
				case COLUMN:
				{
					for(double s:sig) out.write(s+"\n");
					out.flush();
				}break;
				default: throw new IllegalArgumentException("unknown dimension: "+d);
			}
		}
		finally
		{
			try
			{
				if(out!=null) out.close();
			}
			catch(IOException e)
			{}
		}
	}
	
	/**
	 * save signal of all channels into file
	 * @param sigs
	 * signal of all channels, each row is from a channel
	 * @param path
	 * destination file path
	 * @param d
	 * indicate the saved signal dimension
	 * @throws IOException
	 */
	public static void saveSignals(double[][] sigs,File path,Dimension d) throws IOException
	{
	BufferedWriter out=null;
	
		try
		{
			out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
			switch(d)
			{
				case ROW:
				{
					for(int i=0;i<sigs.length;i++)
					{
						for(int j=0;j<sigs[i].length;j++) out.write(sigs[i][j]+" ");
						out.write("\n");
					}
					out.flush();
				}break;
				case COLUMN:
				{
					for(int j=0;j<sigs[0].length;j++)
					{
						for(int i=0;i<sigs.length;i++) out.write(sigs[i][j]+" ");
						out.write("\n");
					}
					out.flush();
				}break;
				default: throw new IllegalArgumentException("unknown dimension: "+d);
			}
		}
		finally
		{
			try
			{
				if(out!=null) out.close();
			}
			catch(IOException e)
			{}
		}
	}
	
	/**
	 * plot all channel's signals
	 * @param fs
	 * sample rate
	 * @param sigs
	 * each row is a channel
	 */
	public static void plotSignals(double fs,double[]... sigs)
	{
	SignalViewer viewer;
		
		viewer=new SignalViewer(fs,sigs);
		viewer.visualize();
	}
	
	/**
	 * plot all channel's signals
	 * @param sigs
	 * each row is a channel
	 */
	public static void plotSignals(double[]... sigs)
	{
	SignalViewer viewer;
		
		viewer=new SignalViewer(sigs);
		viewer.visualize();
	}
	
	/**
	 * load an audio file as signal
	 * @param path
	 * audio file path
	 * @return
	 * @throws IOException
	 */
	public static double[] loadAudio(File path) throws IOException
	{
	WaveSource ws=null;
	double[] data=null;
	
		try
		{
			ws=new WaveSource(path,true);
			data=ws.toArray(data);
		}
		catch(UnsupportedAudioFileException e)
		{
			throw new IOException("failed to load audio",e);
		}
		finally
		{
			try
			{
				if(ws!=null) ws.close();
			}
			catch(IOException e)
			{}
		}
		
		return data;
	}
	
	/**
	 * play a signal as audio
	 * @param sig
	 * a signal
	 * @param fs
	 * sample rate
	 */
	public static void playAsAudio(double[] sig,double fs)
	{
	double min=Double.MAX_VALUE,max=Double.MIN_VALUE;
	double mean=0,amp;
	byte[] audio;
	int temp;
	AudioFormat format;
	Clip clip;
		
		/*
		 * normalize to 16 bits format
		 */
		for(double s:sig)
		{
			mean+=s;
			if(s<min) min=s;
			if(s>max) max=s;
		}
		mean/=sig.length;
		amp=Math.max(Math.abs(min-mean),Math.abs(max-mean));
		
		audio=new byte[sig.length*2];
		for(int i=0;i<sig.length;i++)
		{
			temp=(int)Math.round((sig[i]-mean)*32768.0/amp);
			if(temp<-32768) temp=-32768;
			else if(temp>32767) temp=32767;

			/*
			 * little endian
			 */
			audio[2*i]=(byte)(temp&0x000000ff);
			audio[2*i+1]=(byte)((temp>>>8)&0x000000ff);
		}
		/*
		 * play back as sound
		 */
		//construct corresponding audio format
		format=new AudioFormat((float)fs,16,1,true,false);
		try
		{
			clip=AudioSystem.getClip();
			clip.open(format,audio,0,audio.length);
			clip.start();
			Thread.sleep((long)((1.0/fs)*sig.length*1000));
			clip.close();
		}
		catch(LineUnavailableException e)
		{
			throw new RuntimeException("failed to play signal as audio",e);
		}
		catch(InterruptedException e)
		{}
	}
	
	/**
	 * save dataset into file
	 * @param dataset
	 * each element is a sample
	 * @param destpath
	 * destination file path, each sample is saved as a row
	 * @throws IOException
	 */
	public static void saveDataset(List<double[]> dataset,File destpath) throws IOException
	{
	BufferedWriter out=null;
	
		try
		{
			out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destpath)));
			
			for(double[] sample:dataset) 
			{
				for(double s:sample) out.write(s+" ");
				out.write("\n");
			}
			
			out.flush();
		}
		finally
		{
			try
			{
				if(out!=null) out.close();
			}
			catch(IOException e)
			{}
		}
	}
	
	/**
	 * Generate linearly spaced vectors.
	 * @param a
	 * left interval
	 * @param b
	 * right interval
	 * @param n
	 * number of points
	 * @return
	 */
	public static double[] linspace(double a,double b,int n) 
	{
	double[] s;
	double delta;
	
		delta=(b-a)/(n-1);
		
		s=new double[n];
		for(int i=0;i<s.length;i++) s[i]=a+delta*i;
		s[s.length-1]=b;
		
		return s;
	}
}
