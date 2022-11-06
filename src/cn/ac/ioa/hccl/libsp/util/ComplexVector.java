package cn.ac.ioa.hccl.libsp.util;
import java.io.*;

/**
 * <h1>Description</h1>
 * Represent a complex vector.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Aug 8, 2013 8:29:10 PM, revision:
 */
public class ComplexVector implements Serializable
{
private static final long serialVersionUID=509855931247322372L;
private double[][] data;//two rows for real and imaginary part

	/**
	 * @param len
	 * vector length
	 */
	public ComplexVector(int len)
	{
		data=new double[2][len];
	}
	
	/**
	 * default imaginary part is 0
	 * @param real
	 * the real part, data is not copied
	 */
	public ComplexVector(double[] real)
	{
		data=new double[2][];
		data[0]=real;
		data[1]=new double[real.length];
	}
	
	/**
	 * construct by existing data, data is not copied
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public ComplexVector(double[] real,double[] imag)
	{
		BLAS.checkSize(real,imag);
		
		data=new double[2][];
		data[0]=real;
		data[1]=imag;
	}
	
	/**
	 * construct by existing data, data is not copied
	 * @param data
	 * two rows for real and imaginary part
	 */
	public ComplexVector(double[][] data)
	{
		if(data.length!=2) throw new IllegalArgumentException(
				"two rows are required for real and imaginary part: "+data.length);
		BLAS.checkSize(data[0],data[1]);
		
		this.data=data;
	}

	/**
	 * perform a copy
	 * @param another
	 * another matrix
	 */
	public ComplexVector(ComplexVector another)
	{
		this(another.size());
		setReal(another.real());
		setImaginary(another.imaginary());
	}
	
	/**
	 * copy another vector into this vector
	 * @param another
	 * another vector
	 */
	public void copy(ComplexVector another)
	{
		if(size()!=another.size()) throw new IllegalArgumentException(
				"vector size not match: "+size()+", "+another.size());
		
		System.arraycopy(another.real(), 0, real(), 0, size());
		System.arraycopy(another.imaginary(), 0, imaginary(), 0, size());
	}
	
	/**
	 * get vector length
	 * @return
	 */
	public int size()
	{
		return data[0].length;
	}
	
	/**
	 * get real part, data is not copied
	 * @return
	 */
	public double[] real()
	{
		return data[0];
	}
	
	/**
	 * get imaginary part, data is not copied
	 * @return
	 */
	public double[] imaginary()
	{
		return data[1];
	}
	
	/**
	 * get complex data
	 * @return
	 * two rows for real and imaginary part
	 */
	public double[][] data()
	{
		return data;
	}
	
	/**
	 * get the real part of an entry
	 * @param idx
	 * entry index
	 * @return
	 */
	public double getReal(int idx)
	{
		return data[0][idx];
	}
	
	/**
	 * set the real part of an entry
	 * @param idx
	 * entry index
	 * @param value
	 * new value
	 */
	public void setReal(int idx,double value)
	{
		data[0][idx]=value;
	}
	
	/**
	 * get the imaginary part of an entry
	 * @param idx
	 * entry index
	 * @return
	 */
	public double getImaginary(int idx)
	{
		return data[1][idx];
	}
	
	/**
	 * set the imaginary part of an entry
	 * @param idx
	 * entry index
	 * @param value
	 * new value
	 */
	public void setImaginary(int idx,double value)
	{
		data[1][idx]=value;
	}
	
	/**
	 * get a copy of the real part
	 * @param dest
	 * data space, null to allocate new space
	 * @return
	 */
	public double[] getReal(double[] dest)
	{
		if(dest==null) dest=new double[data[0].length];
		else BLAS.checkSize(data[0],dest);
		
		System.arraycopy(data[0],0,dest,0,dest.length);
		
		return dest;
	}
	
	/**
	 * set real part, data is copied
	 * @param v
	 * real part
	 */
	public void setReal(double[] v)
	{
		BLAS.checkSize(data[0],v);
		System.arraycopy(v,0,data[0],0,v.length);
	}
	
	/**
	 * get a copy of imaginary part
	 * @param dest
	 * destination space, null to allocate new space
	 * @return
	 */
	public double[] getImaginary(double[] dest)
	{
		if(dest==null) dest=new double[data[1].length];
		else BLAS.checkSize(data[1],dest);
		
		System.arraycopy(data[1],0,dest,0,dest.length);
		
		return dest;
	}
	
	/**
	 * set imaginary part, data is copied
	 * @param v
	 * imaginary part
	 */
	public void setImaginary(double[] v)
	{
		BLAS.checkSize(data[1],v);
		System.arraycopy(v,0,data[1],0,v.length);
	}
	
	/**
	 * get an entry
	 * @param idx
	 * vector index
	 * @return
	 */
	public SPComplex getValue(int idx)
	{
		return new SPComplex(data[0][idx],data[1][idx]);
	}
	
	/**
	 * set an entry
	 * @param idx
	 * entry index
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public void setValue(int idx,double real,double imag)
	{
		data[0][idx]=real;
		data[1][idx]=imag;
	}

	public String toString()
	{
	StringBuilder s;
		
		s=new StringBuilder("[");
		for(int i=0;i<data[0].length;i++)
		{
			s.append(SPComplex.toString(data[0][i],data[1][i]));
			if(i<data[0].length-1) s.append(", ");
		}
		s.append("]");
		return s.toString();		
	}
	
	/**
	 * get a subvector from this vector
	 * @param sub
	 * space for subvector, null to allocate new space
	 * @param indices
	 * indices in the subvector
	 * @return
	 */
	public ComplexVector subvector(ComplexVector sub,int... indices)
	{
	double[] real,imag,subreal,subimag;
	
		if(sub==null) sub=new ComplexVector(indices.length);
		else BLAS.checkDestinationSize(sub, indices.length);
		
		real=this.real();
		imag=this.imaginary();
		subreal=sub.real();
		subimag=sub.imaginary();
		
		for(int i=0;i<indices.length;i++) 
		{
			subreal[i]=real[indices[i]];
			subimag[i]=imag[indices[i]];
		}
		
		return sub;
	}
}
