package cn.ac.ioa.hccl.libsp.util;
import java.io.*;

/**
 * <h1>Description</h1>
 * Represents a complex matrix.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Aug 8, 2013 8:30:25 PM, revision:
 */
public class ComplexMatrix implements Serializable
{
private static final long serialVersionUID=-6965087417728113757L;
private double[][][] data;

	/**
	 * construct an empty matrix
	 * @param m
	 * row number
	 * @param n
	 * column number
	 */
	public ComplexMatrix(int m,int n)
	{
		data=new double[2][m][n];
	}
	
	/**
	 * construct by existing data, data is not copied
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public ComplexMatrix(double[][] real,double[][] imag)
	{
		BLAS.checkSize(real,imag);
		
		data=new double[2][][];
		data[0]=real;
		data[1]=imag;
	}
	
	/**
	 * construct by existing data, data is not copied
	 * @param data
	 * two matrices for real and imaginary part
	 */
	public ComplexMatrix(double[][][] data)
	{
		if(data.length!=2) throw new IllegalArgumentException(
				"two matrices are required for real and imaginary part: "+data.length);
		BLAS.checkSize(data[0],data[1]);
		
		this.data=data;
	}
	
	/**
	 * perform a copy
	 * @param another
	 * another matrix
	 */
	public ComplexMatrix(ComplexMatrix another)
	{
		this(another.numRows(),another.numColumns());
		setReal(another.real());
		setImaginary(another.imaginary());
	}
	
	/**
	 * copy the data from another matrix to this matrix
	 * @param another
	 * another matrix
	 */
	public void copy(ComplexMatrix another)
	{
		BLAS.checkSize(this, another);
		
		BLAS.copy(another.real(), real());
		BLAS.copy(another.imaginary(), imaginary());
	}
	
	/**
	 * get the number of rows
	 * @return
	 */
	public int numRows()
	{
		return data[0].length;
	}
	
	/**
	 * get the number of columns
	 * @return
	 */
	public int numColumns()
	{
		return data[0][0].length;
	}
	
	/**
	 * get real part, data is not copied
	 * @return
	 */
	public double[][] real()
	{
		return data[0];
	}
	
	/**
	 * get imaginary part, data is not copied
	 * @return
	 */
	public double[][] imaginary()
	{
		return data[1];
	}
	
	/**
	 * get complex data
	 * @return
	 * two rows for real and imaginary part
	 */
	public double[][][] data()
	{
		return data;
	}
	
	/**
	 * get the real part of an entry
	 * @param ridx
	 * row index
	 * @param cidx
	 * column index
	 * @return
	 */
	public double getReal(int ridx,int cidx)
	{
		return data[0][ridx][cidx];
	}
	
	/**
	 * set the real part of an entry
	 * @param ridx
	 * row index
	 * @param cidx
	 * column index
	 * @param value
	 * new value
	 */
	public void setReal(int ridx,int cidx,double value)
	{
		data[0][ridx][cidx]=value;
	}
	
	/**
	 * get the imaginary part of an entry
	 * @param ridx
	 * row index
	 * @param cidx
	 * column index
	 * @return
	 */
	public double getImaginary(int ridx,int cidx)
	{
		return data[1][ridx][cidx];
	}
	
	/**
	 * set the imaginary part of an entry
	 * @param ridx
	 * row index
	 * @param cidx
	 * column index
	 * @param value
	 * new value
	 */
	public void setImaginary(int ridx,int cidx,double value)
	{
		data[1][ridx][cidx]=value;
	}
	
	/**
	 * get real part copied
	 * @param m
	 * destination space, null to allocate new space
	 * @return
	 */
	public double[][] getReal(double[][] m)
	{
		return BLAS.copy(data[0],m);
	}
	
	/**
	 * set real part copied
	 * @param m
	 * real part
	 */
	public void setReal(double[][] m)
	{
		BLAS.copy(m,data[0]);
	}
	
	/**
	 * get imaginary part
	 * @param m
	 * destination space, null to allocate new space
	 * @return
	 */
	public double[][] getImaginary(double[][] m)
	{
		return BLAS.copy(data[1],m);
	}
	
	/**
	 * set imaginary part, data is copied
	 * @param m
	 * imaginary part
	 */
	public void setImaginary(double[][] m)
	{
		BLAS.copy(m,data[1]);
	}
	
	/**
	 * get an entry
	 * @param i
	 * row index
	 * @param j
	 * column index
	 * @return
	 */
	public SPComplex getValue(int i,int j)
	{
		return new SPComplex(data[0][i][j],data[1][i][j]);
	}
	
	/**
	 * set an entry
	 * @param i
	 * row index
	 * @param j
	 * column index
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public void setValue(int i,int j,double real,double imag)
	{
		data[0][i][j]=real;
		data[1][i][j]=imag;
	}
	
	/**
	 * get an uncopied row vector
	 * @param idx
	 * row index
	 * @return
	 */
	public ComplexVector row(int idx)
	{
		return new ComplexVector(data[0][idx],data[1][idx]);
	}
	
	/**
	 * get a copy of a row
	 * @param idx
	 * row index
	 * @param dest
	 * destination space, null to allocate new space
	 * @return
	 */
	public ComplexVector getRow(int idx,ComplexVector dest)
	{
		if(dest==null) dest=new ComplexVector(numColumns());
		else BLAS.checkDestinationSize(dest,numColumns());
		
		for(int c=0;c<numColumns();c++) 
			dest.setValue(c,getReal(idx,c),getImaginary(idx,c));
		
		return dest;
	}
	
	/**
	 * set a row
	 * @param idx
	 * row index
	 * @param v
	 * row value
	 */
	public void setRow(int idx,ComplexVector v)
	{
		if(numColumns()!=v.size()) throw new IllegalArgumentException(
				"column size not match: "+numColumns()+", "+v.size());
		
		for(int c=0;c<numColumns();c++) 
			setValue(idx,c,v.getReal(c),v.getImaginary(c));
	}
	
	/**
	 * get a copy of a column
	 * @param idx
	 * column index
	 * @param dest
	 * destination space, null to allocate new space
	 * @return
	 */
	public ComplexVector getColumn(int idx,ComplexVector dest)
	{
		if(dest==null) dest=new ComplexVector(numRows());
		else BLAS.checkDestinationSize(dest,numRows());
		
		for(int r=0;r<numRows();r++) 
			dest.setValue(r,getReal(r,idx),getImaginary(r,idx));
		
		return dest;
	}
	
	/**
	 * set a column
	 * @param idx
	 * column index
	 * @param v
	 * corresponding value
	 */
	public void setColumn(int idx,ComplexVector v)
	{
		if(numRows()!=v.size()) throw new IllegalArgumentException(
				"row size not match: "+numRows()+", "+v.size());
		
		for(int r=0;r<numRows();r++) 
			setValue(r,idx,v.getReal(r),v.getImaginary(r));
	}
	
	/**
	 * clear the matrix, set all zero
	 */
	public void clear()
	{
		BLAS.fill(data, 0);
	}
	
	public String toString()
	{
	StringBuilder s;
		
		s=new StringBuilder();
		for(int i=0;i<numRows();i++)	
		{
			for(int j=0;j<numColumns();j++) 
				s.append(SPComplex.toString(data[0][i][j],data[1][i][j])+" ");
			s.append("\n");
		}
		return s.toString();		
	}
}
