package cn.ac.ioa.hccl.libsp.util;
import org.apache.commons.math3.complex.*;

/**
 * <h1>Description</h1>
 * The complex number used in this project.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Jul 31, 2013 4:49:41 PM, revision:
 */
public class SPComplex extends Complex
{
private static final long serialVersionUID=-4707863939326336949L;
public static SPComplex ZERO=new SPComplex(0,0);
public static SPComplex ONE=new SPComplex(1,0);

	/**
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public SPComplex(double real,double imag)
	{
		super(real,imag);
	}
	
	/**
	 * get the magnitude
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 * @return
	 */
	public static double abs(double real,double imag)
	{
		return Math.sqrt(absSquare(real,imag));
	}
	
	/**
	 * calculate |a|^2
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 * @return
	 */
	public static double absSquare(double real,double imag)
	{
		return real*real+imag*imag;
	}
	
	/**
	 * convert a complex number to string
	 * @param r
	 * real part
	 * @param i
	 * imaginary part
	 * @return
	 */
	public static String toString(double r,double i)
	{
		//real part is zero
		if(r==0)
		{
			if(i==0) return "0";
			else return Double.toString(i)+"j";
		}
		//real part is not zero
		else
		{
			if(i==0) return Double.toString(r);
			else if(i>0) return Double.toString(r)+"+"+Double.toString(i)+"j";
			else return Double.toString(r)+Double.toString(i)+"j";
		}
	}
}
