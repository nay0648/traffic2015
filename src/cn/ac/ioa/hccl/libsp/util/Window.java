/**
 * Created on: Oct 30, 2014
 */
package cn.ac.ioa.hccl.libsp.util;
import java.io.*;

/**
 * @author nay0648
 * Generate different window functions.
 */
public class Window implements Serializable
{
private static final long serialVersionUID = -8565527865623162073L;

	/**
	 * calculate arccosh(x)
	 * @param x
	 * x>=1
	 * @return
	 */
	private static double acosh(double x)
	{
		return Math.log(x+Math.sqrt(x*x-1));
	}

	/**
	 * This function computes the chebyshev polyomial T_n(x), see: 
	 * http://practicalcryptography.com/miscellaneous/machine-learning/implementing-dolph-chebyshev-window/
	 * @param n
	 * @param x
	 * @return
	 */
	private static double chebyPoly(int n,double x)
	{
	double res;
	
		if(Math.abs(x)<=1) res=Math.cos(n*Math.acos(x));
		else res=Math.cosh(n*acosh(x));
		
		return res;
	}

	/**
	 * generate Dolph-Chebyshev window, see: 
	 * http://practicalcryptography.com/miscellaneous/machine-learning/implementing-dolph-chebyshev-window/
	 * @param length
	 * window length
	 * @param atten
	 * the attenuation parameter in dB
	 * @return
	 */
	public static double[] cheb(int length,double atten)
	{
	double[] out;	
	int nn,i;
	double m,n,sum=0,max=0,tg,x0;
	
		out=new double[length];
		
		tg=Math.pow(10,atten/20);
	    x0=Math.cosh((1.0/(length-1))*acosh(tg));
	    m=(length-1)/2;
	    if(length%2==0) m=m+0.5;
	    
	    for(nn=0;nn<(length/2+1);nn++)
	    {
	    	n=nn-m;
	        sum=0;
	        
	        for(i=1;i<=m;i++) 
	        	sum+=chebyPoly(length-1,x0*Math.cos(Math.PI*i/length))*Math.cos(2.0*n*Math.PI*i/length);
	        
	        out[nn]=tg+2*sum;
	        out[length-nn-1]=out[nn];
	        if(out[nn]>max) max=out[nn];
	    }
	    
	    for(nn=0;nn<length;nn++) out[nn]/=max;

	    return out;
	}
	
	/**
	 * generate Dolph-Chebyshev window,  relative sidelobe attenuation of 100 dB
	 * @param length
	 * window length
	 * @return
	 */
	public static double[] cheb(int length)
	{
		return cheb(length,100);
	}
	
	/**
	 * generate a n-point Hann (Hanning) window as: 0.5*(1-cos((2*pi*x)/(n-1)))
	 * @param n
	 * number of points
	 * @return
	 */
	public static double[] hann(int n)
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
	
	public static void main(String[] args)
	{
	double[] wcheb,whann;
	
		wcheb=Window.cheb(128);
		whann=Window.hann(128);
		Util.plotSignals(wcheb,whann);
	}
}
