package cn.ac.ioa.hccl.libsp.util;
import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.image.*;

import javax.sound.sampled.*;

import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * <h1>Description</h1>
 * This is used to calculate the short time Fourier transform of a signal.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Jan 18, 2011 9:36:34 PM, revision:
 */
public class ShortTimeFourierTransformer implements Serializable
{
private static final long serialVersionUID=-7691710855523349087L;
private int stftshift;//stft frame shift
private int fftsize;//fft size
private double[] awindow=null;//analysis window
private double[] swindow=null;//synthesis window

	/**
	 * hanning window is used as the default analysis window
	 * @param stftshift
	 * stft frame shift (in no. of samples)
	 * @param fftsize
	 * fft block size
	 */
	public ShortTimeFourierTransformer(int stftshift,int fftsize)
	{
		if(!FourierTransformer.isPowerOf2(fftsize)) throw new IllegalArgumentException(
				"fft size must powers of 2: "+fftsize);
		this.fftsize=fftsize;
		
		if(stftshift<1||stftshift>fftsize) throw new IllegalArgumentException(
				"illegal stft shift: "+stftshift+", should be in [1, "+fftsize+"]");
		this.stftshift=stftshift;
		
		awindow=hannWindow(fftsize);//default analysis window
		BLAS.scalarMultiply(2.0*stftshift/fftsize,awindow,awindow);
	}
	
	/**
	 * hanning window is used as the default analysis window
	 * @param stftsize
	 * actual hanning window size
	 * @param stftshift
	 * stft block shift
	 * @param fftsize
	 * fft block size
	 */
	public ShortTimeFourierTransformer(int stftsize,int stftshift,int fftsize)
	{
		this(stftshift,fftsize);
		
		double[] h=hannWindow(stftsize);
		double[] w=new double[fftsize];
		System.arraycopy(h,0,w,0,h.length);
		BLAS.scalarMultiply(2.0*stftshift/stftsize,w,w);
		
		this.setAnalysisWindow(w);
	}

	/**
	 * generate a n-point Hann (Hanning) window as: 0.5*(1-cos((2*pi*x)/(n-1)))
	 * @param n
	 * number of points
	 * @return
	 */
	public static double[] hannWindow(int n)
	{
	double[] w;
	int half;
	double temp;
	
		w=new double[n];
		half=w.length/2;
	
		//generate window
		for(int i=0;i<w.length;i++) 
			w[i]=0.5*(1-Math.cos((2*Math.PI*i)/(w.length-1)));
		
		//make it symmetric
		for(int i=0;i<half;i++) 
		{
			temp=(w[i]+w[w.length-i-1])/2;
			w[i]=temp;
			w[w.length-i-1]=temp;
		}
				
		/*
		 * pad the residual to have the overlapped window sum equals to 1
		 */
		for(int i=0;i<half;i++) 
		{
			temp=(1-(w[i]+w[half+i]))/2;
			w[i]+=temp;
			w[half+i]+=temp;
		}
		
		if(w.length%2!=0) w[half]+=1-w[half];
		
		/*
		 * normalize to have sum(w)=n/2
		 */
		temp=w.length/(2.0*BLAS.sum(w));
		BLAS.scalarMultiply(temp,w,w);
		
		return w;
	}
	
	/**
	 * generate a rectangular window
	 * @param n
	 * window length
	 * @return
	 */
	public static double[] rectWindow(int n)
	{
	double[] w;
	
		w=new double[n];
		Arrays.fill(w,1);
		return w;
	}
	
	/**
	 * get analysis window
	 * @return
	 */
	public double[] getAnalysisWindow()
	{
		return awindow;
	}
	
	/**
	 * set analysis window
	 * @param awindow
	 * analysis window, null for no window
	 */
	public void setAnalysisWindow(double[] awindow)
	{
		if(awindow!=null&&awindow.length!=fftsize) throw new IllegalArgumentException(
				"window size not match: "+awindow.length+", "+fftsize);
		this.awindow=awindow;
	}
	
	/**
	 * get synthesis window
	 * @return
	 */
	public double[] getSynthesisWindow()
	{
		return swindow;
	}
	
	/**
	 * set synthesis window
	 * @param swindow
	 * new synthesis window, null for no window
	 */
	public void setSynthesisWindow(double[] swindow)
	{
		if(swindow!=null&&swindow.length!=fftsize) throw new IllegalArgumentException(
				"window size not match: "+swindow.length+", "+fftsize);
		this.swindow=swindow;
	}
	
	/**
	 * get stft block overlap in taps fftsize-stftshift
	 * @return
	 */
	public int stftOverlap()
	{
		return fftsize-stftshift;
	}
	
	/**
	 * get number of samples each stft block shifts: stftsize-stftoverlap
	 * @return
	 */
	public int stftShift()
	{
		return stftshift;
	}
	
	/**
	 * get fft block size
	 * @return
	 */
	public int fftSize()
	{
		return fftsize;
	}
	
	/**
	 * perform short time Fourier transform
	 * @param in
	 * signal source
	 * @param out
	 * output stream for transformed signals
	 * @return
	 * number of segments
	 * @throws IOException
	 */
	public int stft(SignalSource in,SignalSink out) throws IOException
	{
	STFTIterator it;
	ComplexVector stftframe;

		for(it=stftIterator(in);it.next();)
		{
			stftframe=it.stftFrame(in.getCurrentChannel());
			out.writeFrame(stftframe);
		}

		return it.numSTFTFrames();
	}
	
	/**
	 * perform stft for mulitchannel signals
	 * @param in
	 * multichannel signal source
	 * @param out
	 * each one for a channel
	 * @return
	 * @throws IOException
	 */
	public int stft(SignalSource in,SignalSink[] out) throws IOException
	{
	STFTIterator it;
	ComplexVector stftframe;
		
		if(in.numChannels()!=out.length) throw new IllegalArgumentException(
				"number of channels not match: "+in.numChannels()+", "+out.length);

		for(it=stftIterator(in);it.next();)
			for(int chidx=0;chidx<out.length;chidx++) 
			{
				stftframe=it.stftFrame(chidx);
				out[chidx].writeFrame(stftframe);
			}

		return it.numSTFTFrames();
	}
	
	/**
	 * perform short time Fourier transform
	 * @param in
	 * signal source
	 * @return
	 * The time-frequency data, real and imaginary part
	 * @throws IOException
	 */
	public ComplexMatrix stft(SignalSource in) throws IOException
	{
	ArraySignalSink out=null;
	ComplexMatrix data=null;
		
		try
		{
			out=new ArraySignalSink(fftSize());
			stft(in,out);
				
			out.flush();
			data=out.toArray(data);
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

		return data;		
	}
	
	/**
	 * perform STFT experiment
	 * @param signal
	 * a signal sequence
	 * @return
	 */
	public ComplexMatrix stft(double[] signal)
	{
	ArraySignalSource in=null;
	ComplexMatrix stftdata;
			
		try
		{
			in=new ArraySignalSource(signal);
			stftdata=stft(in);
		}
		catch(IOException e)
		{
			throw new RuntimeException("failed to perform STFT",e);
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
		
		return stftdata;
	}
		
	/**
	 * perform inverse short time Fourier transform
	 * @param in
	 * stft results of a signal, should be a multichannel complex signal
	 * @param out
	 * the istft result
	 * @param complex
	 * true to output complex signals, false to output real signals
	 * @return
	 * number of segments processed
	 * @throws IOException
	 */
	public int istft(SignalSource in,SignalSink out,boolean complex) throws IOException
	{
	ComplexVector stftframe;
	ISTFTSink sink;
	
		stftframe=new ComplexVector(in.numChannels());
		sink=new ISTFTSink(this,complex,out);
		
		for(;;)
		{
			try
			{
				in.readFrame(stftframe);
			}
			catch(EOFException e)
			{
				break;
			}
			
			sink.writeFrame(stftframe);
		}
		
		sink.flush();
		return sink.numSTFTFrames();
	}
	
	/**
	 * perform inverse stft in memory
	 * @param stftdata
	 * the stft data
	 * @return
	 */
	public double[] istft(ComplexMatrix stftdata)
	{
	ArraySignalSink out;
	ISTFTSink sink=null;
	ComplexVector frame=null;
	double[] data=null;
	
		try
		{
			out=new ArraySignalSink(1);
			sink=new ISTFTSink(this,false,out);
			
			for(int tau=0;tau<stftdata.numColumns();tau++) 
			{
				frame=stftdata.getColumn(tau,frame);
				sink.writeFrame(frame);
			}
			
			sink.flush();
			data=out.toArray(data);
		}
		catch(IOException e)
		{
			throw new RuntimeException("failed to perform stft in memory: ",e);
		}
		finally
		{
			try
			{
				if(sink!=null) sink.close();
			}
			catch(IOException e)
			{}
		}
		
		return data;
	}
	
	/**
	 * convert magnitude to dB
	 * @param p1
	 * usually sound pressure in Pa
	 * @param p0
	 * reference sound pressure, usually equals to 2e-5 Pa
	 * @return
	 */
	public static double magnitude2dB(double p1,double p0)
	{
		return 10*Math.log10((p1*p1)/(p0*p0));
	}
	
	/**
	 * convert magnitude to dB, suitable for spectrograms
	 * @param p1
	 * the magnitude
	 * @return
	 */
	public static double magnitude2dB(double p1)
	{
		return 10*Math.log10(p1*p1+1);
	}
	
	/**
	 * generate colormap for grayscale color
	 * @return
	 */
	public static Color[] colormapGray()
	{
	Color[] colormap;
	
		colormap=new Color[256];
		for(int i=0;i<colormap.length;i++) colormap[i]=new Color(i,i,i);
		return colormap;
	}
	
	/**
	 * used to draw for paper
	 * @return
	 */
	public static Color[] colormapAntigray()
	{
	Color[] colormap;
		
		colormap=new Color[256];
		for(int i=0;i<colormap.length;i++) colormap[i]=new Color(255-i,255-i,255-i);
		return colormap;
	}
	
	/**
	 * generate colormap for cold and warm color
	 * @return
	 */
	public static Color[] colormapJet()
	{
	Color[] colormap;
	double[] hsv,rgb;
	
		hsv=new double[3];
		rgb=new double[3];
		hsv[1]=1;
		colormap=new Color[240];//from blue to read
		for(int i=0;i<colormap.length;i++)
		{
			hsv[0]=(colormap.length-1-i)/360.0;//hue
			hsv[2]=0.1+0.9*(double)i/(colormap.length-1);//intensity
			cn.ac.ioa.hccl.libsp.util.ColorSpace.hsv2RGB(hsv,rgb);
			colormap[i]=new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);
		}
		return colormap;
	}
	
	/**
	 * visualize stft results as an image
	 * @param stftdata
	 * the stft data
	 * @param colormap
	 * adopted colormap
	 * @return
	 */
	public BufferedImage spectrogram(ComplexMatrix stftdata,Color[] colormap)
	{
	double[][] stftr,stfti,amp;
	double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
	BufferedImage stftimg;
	int index;
			
		if(stftdata.numRows()!=fftSize()&&stftdata.numRows()!=(fftSize()/2+1)) 
			throw new IllegalArgumentException("number of channels not match: "
					+stftdata.numRows()+", required: "+fftSize()+", or "+(fftSize()/2+1));

		/*
		 * calculate magnitude and convert to DB, and find max and min magnitude
		 */
		//just need half of segments, and one more row for 0 DC component
		amp=new double[fftSize()/2+1][stftdata.numColumns()];
		stftr=stftdata.real();
		stfti=stftdata.imaginary();
			
		if(stftr.length==fftSize())
		{
			for(int i=0;i<stftr.length/2+1;i++)
				for(int j=0;j<stftr[i].length;j++)
				{
					//including two half frequencies
					amp[amp.length-1-i][j]=magnitude2dB(2*SPComplex.abs(stftr[i][j],stfti[i][j]));
					if(amp[i][j]<min) min=amp[i][j];
					if(amp[i][j]>max) max=amp[i][j];
				}
		}
		else
		{
			for(int i=0;i<stftr.length;i++)
				for(int j=0;j<stftr[i].length;j++)
				{
					//including two half frequencies
					amp[amp.length-1-i][j]=magnitude2dB(2*SPComplex.abs(stftr[i][j],stfti[i][j]));
					if(amp[i][j]<min) min=amp[i][j];
					if(amp[i][j]>max) max=amp[i][j];
				}
		}

		/*
		 * visualize as image
		 */
		stftimg=new BufferedImage(amp[0].length,amp.length,BufferedImage.TYPE_INT_RGB);
		max=(colormap.length-1.0)/(max-min);
		for(int y=0;y<stftimg.getHeight();y++)
			for(int x=0;x<stftimg.getWidth();x++)
			{
				/*
				 * quantize to fit for colormap
				 */
				index=(int)Math.round((amp[y][x]-min)*max);
				if(index<0) index=0;
				else if(index>colormap.length-1) index=colormap.length-1;
				stftimg.setRGB(x,y,colormap[index].getRGB());
			}
		return stftimg;		
	}
	
	/**
	 * visualize stft results as an image with default colormap
	 * @param stftdata
	 * the stft data
	 * @return
	 */
	public BufferedImage spectrogram(ComplexMatrix stftdata)
	{
		return spectrogram(stftdata,colormapJet());
	}
	
	/**
	 * visualize stft results as an image
	 * @param in
	 * signal source
	 * @return
	 */
	public BufferedImage spectrogram(SignalSource in) throws IOException
	{
	ArraySignalSink out;
	ComplexMatrix stftdata=null;//stft result
	
		/*
		 * perform stft and cache results into memory
		 */
		out=new ArraySignalSink(fftSize());
		stft(in,out);
		stftdata=out.toArray(stftdata);
		return spectrogram(stftdata);
	}
	
	/**
	 * get an iterator to perform stft iteratively
	 * @param in
	 * signal source
	 * @return
	 * @throws IOException
	 */
	public STFTIterator stftIterator(SignalSource in) throws IOException
	{
		return new STFTIterator(in);
	}
	
	/**
	 * <h1>Description</h1>
	 * Perform stft iteratively for multichannel signals. Not safe for multithread access.
	 * <h1>abstract</h1>
	 * <h1>keywords</h1>
	 * @author nay0648<br>
	 * if you have any questions, advices, suggests, or find any bugs, 
	 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
	 * @version created on: Mar 20, 2012 9:15:21 PM, revision:
	 */
	public class STFTIterator implements Serializable
	{
	private static final long serialVersionUID=7417925499447350389L;
	private SignalSource in;//signal source
	private double[][] buffer;//buffer used to load samples, each row for a channel
	private ComplexVector fdbuffer;//buffer for transformed signals
	private int numsamples=0;//number of samples already processed
	private int numframes=0;//number of frames already processed
	
		/**
		 * @param in
		 * signal source
		 */
		public STFTIterator(SignalSource in) throws IOException
		{
		int count;
			
			this.in=in;
			buffer=new double[in.numChannels()][fftSize()];//used to read samples
			fdbuffer=new ComplexVector(fftSize());//used to perform fft
			
			/*
			 * read overlapping samples for the first segment
			 */
			count=in.readSamples(buffer,stftShift(),stftOverlap());
			if(count>0) numsamples+=count;
		}
		
		/**
		 * forward the iterator to the next frame
		 * @return
		 * true if there is a frame
		 * @throws IOException 
		 */
		public boolean next() throws IOException
		{
		int count;

			if(numsamples<=0) return false;
			
			//produce overlapping with next segment
			for(int chidx=0;chidx<buffer.length;chidx++) 
				for(int t=0;t<stftOverlap();t++) 
					buffer[chidx][t]=buffer[chidx][stftShift()+t];
			
			/*
			 * read samples
			 */
			count=in.readSamples(buffer,stftOverlap(),stftShift());
			if(count<=0) return false;
			else numsamples+=count;//accumulate sample index
			
			//pad with 0
			for(int t=stftOverlap()+count;t<buffer[0].length;t++) 
				for(int chidx=0;chidx<buffer.length;chidx++) 
					buffer[chidx][t]=0;
			
			numframes++;
			return true;
		}
		
		/**
		 * get current stft frame for a specified channel
		 * @param chidx
		 * channel index
		 * @return
		 * real and imaginary part of the stft frame, please notice that the underlying 
		 * space is not copied
		 */
		public ComplexVector stftFrame(int chidx)
		{
		double[][] data;
			
			data=fdbuffer.data();
			
			//modulate by window function
			for(int t=0;t<awindow.length;t++) data[0][t]=awindow[t]*buffer[chidx][t];
			Arrays.fill(data[1],0);
			
			//perform fft
			FourierTransformer.fft(fdbuffer);
			
			return fdbuffer;
		}
		
		/**
		 * get the number of channels of the input signal
		 * @return
		 */
		public int numChannels()
		{
			return in.numChannels();
		}
		
		/**
		 * get the number of samples already processed
		 * @return
		 */
		public int numSamples()
		{
			return numsamples;
		}
		
		/**
		 * get the number of stft frames already processed
		 * @return
		 */
		public int numSTFTFrames()
		{
			return numframes;
		}
	}
	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException
	{
	WaveSource in;
	double[] s=null;
	ShortTimeFourierTransformer stft;
	ComplexMatrix stftdata;
	double[] s2,err;
	
		in=new WaveSource(new File("data/SawadaDataset/s2.wav"),true);
		s=in.toArray(s);
		in.close();
	
		stft=new ShortTimeFourierTransformer(1024/16,1024);
//		stft=new ShortTimeFourierTransformer(256,256/4,1024);
		stftdata=stft.stft(s);
		Util.imshow(stft.spectrogram(stftdata));
		
		s2=stft.istft(stftdata);
		
		err=new double[Math.min(s.length,s2.length)];
		for(int i=0;i<err.length;i++) err[i]=s[i]-s2[i];
		Util.plotSignals(s,s2,err);
		
		Util.playAsAudio(s2,in.audioFormat().getSampleRate());
	}
}
