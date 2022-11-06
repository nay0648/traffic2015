package cn.ac.ioa.hccl.libsp.util;
import java.io.*;

/**
 * <h1>Description</h1>
 * Methods for spectrum analysis.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Jun 16, 2011 5:20:00 PM, revision:
 */
public class FourierTransformer implements Serializable
{
private static final long serialVersionUID=-8229357512218936962L;
	
	/**
	 * to see if a number is powers of 2
	 * @param p
	 * a number
	 * @return
	 */
	public static boolean isPowerOf2(int p)
	{
		return p==nextPowerOf2(p);
	}
	
	/**
	 * calculate the next power of 2
	 * @param len
	 * segment length
	 * @return
	 */
	public static int nextPowerOf2(int len)
	{
		return (int)Math.pow(2,Math.ceil(Math.log(len)/Math.log(2)));
	}
	
	/**
	 * perform fft
	 * @param data
	 * two rows, for real and imaginary part of the data, 
	 * data length must be powers of 2
	 */
	public static void fft(double[][] data)
	{
		org.apache.commons.math3.transform.FastFourierTransformer.transformInPlace(
				data,
				org.apache.commons.math3.transform.DftNormalization.STANDARD,
				org.apache.commons.math3.transform.TransformType.FORWARD);
	}
	
	/**
	 * perform inverse data
	 * @param data
	 * two rows, for real and imaginary part of the data, 
	 * data length must be powers of 2
	 */
	public static void ifft(double[][] data)
	{
		org.apache.commons.math3.transform.FastFourierTransformer.transformInPlace(
				data,
				org.apache.commons.math3.transform.DftNormalization.STANDARD,
				org.apache.commons.math3.transform.TransformType.INVERSE);
	}
	
	/**
	 * perform fft
	 * @param data
	 * two rows, for real and imaginary part of the data, 
	 * data length must be powers of 2
	 */
	public static void fft(ComplexVector data)
	{
		fft(data.data());
	}
	
	/**
	 * perform inverse data
	 * @param data
	 * two rows, for real and imaginary part of the data, 
	 * data length must be powers of 2
	 */
	public static void ifft(ComplexVector data)
	{
		ifft(data.data());
	}
	
	/**
	 * used to forcus low frequency part to the center of the signal segment
	 * @param x
	 * a signal segment
	 */
	public static void fftshift(double[] x)
	{
	int idx1,idx2;
	double temp;
	
		if(x.length%2!=0) throw new IllegalArgumentException(
				"even taps required: "+x.length);
		
		for(idx1=0,idx2=x.length/2;idx2<x.length;idx1++,idx2++)
		{
			temp=x[idx1];
			x[idx1]=x[idx2];
			x[idx2]=temp;
		}
	}
	
	/**
	 * expand the real valued signal's fft to the full frequency band using the 
	 * complex conjugate property
	 * @param fx
	 * real valued signal's fft, only have the size of nfft/2+1
	 * @param dest
	 * destination space, null to allocate new space
	 * @return
	 */
	public static double[][] fftExpand(double[][] fx,double[][] dest)
	{
	int nfft;
	
		nfft=(fx[0].length-1)*2;
		if(!isPowerOf2(nfft)) throw new IllegalArgumentException(
				"fft size must be powers of 2: "+nfft);
		if(dest==null) dest=new double[2][nfft];

		for(int f=0;f<fx[0].length;f++) 
		{
			dest[0][f]=fx[0][f];
			dest[1][f]=fx[1][f];
			
			//its complex conjugate counterpart
			if(f>0&&f<nfft/2) 
			{
				dest[0][nfft-f]=fx[0][f];
				dest[1][nfft-f]=-fx[1][f];
			}
		}
		
		return dest;
	}
}
