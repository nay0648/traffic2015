/**
 * Created on: Nov 2, 2014
 */
package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.sio.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * @author nay0648
 * The array used in the SAS-1 system.
 */
public class SAS1Monitor extends MonitorArray
{
private static final long serialVersionUID = 6854455983338179189L;
private static final File ARRAY_GEOM=new File("arraygeometry/sas1.txt");
private int numrows=10;//number of rows in the rectangular array
private int numcolumns=8;//number of columns in the rectangular array
private ShortTimeFourierTransformer stfter;
private ShortTimeFourierTransformer.STFTIterator stftit=null;
private long frameidx=0;//frame index
private ComplexVector[] stftframe;//the stft buffer

	/**
	 * @param fs
	 * sampling rate
	 * @param frameshift
	 * frame shift
	 * @param fftsize
	 * fft size
	 * @throws IOException 
	 */
	public SAS1Monitor(double fs,int frameshift,int fftsize) throws IOException
	{
	super(ARRAY_GEOM,fs,frameshift,fftsize);
		
		stfter=new ShortTimeFourierTransformer(this.frameShift(),this.fftSize());
		stfter.setAnalysisWindow(ShortTimeFourierTransformer.hannWindow(this.fftSize()));
	}
	
	/**
	 * get the row number of the rectangular array
	 * @return
	 */
	public int numRows()
	{
		return numrows;
	}
	
	/**
	 * get the column number of the rectangular array
	 * @return
	 */
	public int numColumns()
	{
		return numcolumns;
	}

	public long frameIndex() 
	{
		return frameidx;
	}
	
	public boolean hasNextFrame() throws IOException 
	{
		//initialization
		if(stftit==null) 
		{	
			//use special signal source to sum vertical subarrays
			stftit=stfter.stftIterator(new SAS1Source(this.getSignalSource()));
			
			stftframe=new ComplexVector[stftit.numChannels()];
			for(int m=0;m<stftframe.length;m++) 
				stftframe[m]=new ComplexVector(stfter.fftSize());
		}
		
		if(stftit.next()) 
		{
			//cache all stft frames
			for(int m=0;m<stftframe.length;m++) 
				//perform stft and copy data into buffer
				stftframe[m].copy(stftit.stftFrame(m));
			
			frameidx++;
			return true;
		}
		else return false;
	}

	public ComplexVector arrayFrame(int fi, ComplexVector arrayin)throws IOException 
	{
		//frame size is the same as the number of columns in the rectangular arrangement
		if(arrayin==null) arrayin=new ComplexVector(numColumns());
		else BLAS.checkDestinationSize(arrayin, numColumns());
		
		//get frame data for a specified frequency bin
		for(int m=0;m<arrayin.size();m++) 
			arrayin.setValue(
					m, 
					stftframe[m].getReal(fi), 
					stftframe[m].getImaginary(fi));
			
		return arrayin;
	}
	
	/**
	 * Used to generate subarray data.
	 * @author nay0648
	 */
	private class SAS1Source extends SignalSource
	{
	private SignalSource source;//underlying signal source
	private double[] taper;//the window	
	private double[] uframe;//used to read underlying data
	
		/**
		 * @param source
		 * underlying signal source
		 */
		public SAS1Source(SignalSource source)
		{
			if(source.numChannels()!=numrows*numcolumns) throw new IllegalArgumentException(
					"number of channels not match: "+source.numChannels()+", "+numrows*numcolumns);
			
			this.source=source;
			
			uframe=new double[numrows*numcolumns];
			taper=Window.cheb(numrows);
		}
		
		public int numChannels() 
		{
			return numcolumns;
		}
		
		public void close() throws IOException 
		{
			source.close();
		}
		
		public void readFrame(double[] frame) throws IOException, EOFException 
		{
			this.checkFrameSize(frame.length);
			
			source.readFrame(uframe);
			Arrays.fill(frame, 0);
			
			//apply windows to attenuate sidelobes in vertical direction
			for(int j=0;j<frame.length;j++) 
				for(int i=0;i<taper.length;i++) 
					frame[j]+=taper[i]*uframe[j*numrows+i];	
		}
	}
}
