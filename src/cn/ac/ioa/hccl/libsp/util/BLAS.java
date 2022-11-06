package cn.ac.ioa.hccl.libsp.util;
import java.io.*;
import java.util.*;
import org.apache.commons.math3.linear.*;

/**
 * <h1>Description</h1>
 * Basic linear algebra subprograms.
 * <h1>abstract</h1>
 * <h1>keywords</h1>
 * @author nay0648<br>
 * if you have any questions, advices, suggests, or find any bugs, 
 * please mail me: <a href="mailto:nay0648@163.com">nay0648@163.com</a>
 * @version created on: Dec 25, 2009 3:01:55 PM, revision:
 */
public class BLAS implements Serializable
{
private static final long serialVersionUID=6106369074645421830L;
/**
 * A small positive number.
 */
public static final double EPS=2.220446049250313e-16;

	/**
	 * throw exception if two vector's size not match
	 * @param v1, v2
	 * two vectors
	 */
	public static void checkSize(double[] v1,double[] v2)
	{
		if(v1.length!=v2.length) throw new IllegalArgumentException(
				"inconsistant vector dimension: "+v1.length+", "+v2.length);
	}
	
	/**
	 * throw exception if two vector's size not match
	 * @param v1, v2
	 * two vectors
	 */
	public static void checkSize(ComplexVector v1,ComplexVector v2)
	{
		if(v1.size()!=v2.size()) throw new IllegalArgumentException(
				"inconsistant vector dimension: "+v1.size()+", "+v2.size());
	}
	
	/**
	 * throw exception if two matrics dimension not match
	 * @param m1, m2
	 * two matrices
	 */
	public static void checkSize(int[][] m1,int[][] m2)
	{
		if(m1.length!=m2.length||m1[0].length!=m2[0].length) 
			throw new IllegalArgumentException("insistant matrix dimension: "+
					m1.length+" x "+m1[0].length+", "+m2.length+" x "+m2[0].length);
	}
	
	/**
	 * throw exception if two matrics dimension not match
	 * @param m1, m2
	 * two matrices
	 */
	public static void checkSize(double[][] m1,double[][] m2)
	{
		if(m1.length!=m2.length||m1[0].length!=m2[0].length) 
			throw new IllegalArgumentException("insistant matrix dimension: "+
					m1.length+" x "+m1[0].length+", "+m2.length+" x "+m2[0].length);
	}
	
	/**
	 * throw exception if two matrics dimension not match
	 * @param m1, m2
	 * two matrices
	 */
	public static void checkSize(ComplexMatrix m1,ComplexMatrix m2)
	{
		if(m1.numRows()!=m2.numRows()||m1.numColumns()!=m2.numColumns()) 
			throw new IllegalArgumentException("insistant matrix dimension: "+
					m1.numRows()+" x "+m1.numRows()+", "+m2.numColumns()+" x "+m2.numColumns());
	}
	
	/**
	 * throw exception if destination vector size not match
	 * @param dest
	 * the destination vector
	 * @param d
	 * the required dimension
	 */
	public static void checkDestinationSize(double[] dest,int d)
	{
		if(dest.length!=d) throw new IllegalArgumentException(
				"illegal destination vector dimension: "+dest.length+", required: "+d);
	}
	
	/**
	 * throw exception if destination vector size not match
	 * @param dest
	 * the destination vector
	 * @param d
	 * the required dimension
	 */
	public static void checkDestinationSize(ComplexVector dest,int d)
	{
		if(dest.size()!=d) throw new IllegalArgumentException(
				"illegal destination vector dimension: "+dest.size()+", required: "+d);
	}
	
	/**
	 * throw exception if destination matrix size not match
	 * @param dest
	 * the destination matrix
	 * @param m, n
	 * the required matrix dimension
	 */
	public static void checkDestinationSize(int[][] dest,int m,int n)
	{
		if(dest.length!=m||dest[0].length!=n) throw new IllegalArgumentException(
				"illegal destination matrix dimension: "+
				dest.length+" x "+dest[0].length+", required: "+
				m+" x "+n);
	}
	
	/**
	 * throw exception if destination matrix size not match
	 * @param dest
	 * the destination matrix
	 * @param m, n
	 * the required matrix dimension
	 */
	public static void checkDestinationSize(double[][] dest,int m,int n)
	{
		if(dest.length!=m||dest[0].length!=n) throw new IllegalArgumentException(
				"illegal destination matrix dimension: "+
				dest.length+" x "+dest[0].length+", required: "+
				m+" x "+n);
	}
	
	/**
	 * throw exception if destination matrix size not match
	 * @param dest
	 * the destination matrix
	 * @param m, n
	 * the required matrix dimension
	 */
	public static void checkDestinationSize(ComplexMatrix dest,int m,int n)
	{
		if(dest.numRows()!=m||dest.numColumns()!=n) throw new IllegalArgumentException(
				"illegal destination matrix dimension: "+
				dest.numRows()+" x "+dest.numColumns()+", required: "+
				m+" x "+n);
	}
	
	/**
	 * print a matrix
	 * @param matrix
	 * a matrix
	 * @return
	 */
	public static String toString(int[][] matrix)
	{
	StringBuilder s;
	
		s=new StringBuilder();
		for(int i=0;i<matrix.length;i++)	
		{
			for(int j=0;j<matrix[i].length;j++) 
				s.append(matrix[i][j]+" ");
			s.append("\n");
		}
		return s.toString();	
	}

	/**
	 * print a matrix
	 * @param matrix
	 * a matrix
	 * @return
	 */
	public static String toString(double[][] matrix)
	{
	StringBuilder s;
	
		s=new StringBuilder();
		for(int i=0;i<matrix.length;i++)	
		{
			for(int j=0;j<matrix[i].length;j++) 
				s.append(matrix[i][j]+" ");
			s.append("\n");
		}
		return s.toString();	
	}
	
	/**
	 * print a vector
	 * @param v
	 * a vector
	 */
	public static void println(int[] v)
	{
		System.out.println(Arrays.toString(v));
	}
	
	/**
	 * print a vector
	 * @param v
	 * a vector
	 */
	public static void println(double[] v)
	{
		System.out.println(Arrays.toString(v));
	}
	
	/**
	 * print a vector
	 * @param v
	 * a vector
	 */
	public static void println(ComplexVector v)
	{
		System.out.println(v);
	}
	
	/**
	 * print a matrix
	 * @param matrix
	 * a matrix
	 */
	public static void println(int[][] matrix)
	{
		System.out.println(BLAS.toString(matrix));
	}
	
	/**
	 * print a matrix
	 * @param matrix
	 * a matrix
	 */
	public static void println(double[][] matrix)
	{
		System.out.println(BLAS.toString(matrix));
	}
	
	/**
	 * print a matrix
	 * @param matrix
	 * a matrix
	 */
	public static void println(ComplexMatrix matrix)
	{
		System.out.println(matrix);
	}
	
	/**
	 * write a matrix into file
	 * @param matrix
	 * a matrix
	 * @param file
	 * the destination file path
	 * @throws IOException
	 */
	public static void save(int[][] matrix,File file) throws IOException
	{
	BufferedWriter out=null;
	
		try
		{
			out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			out.write(toString(matrix));
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
	 * write a matrix into file
	 * @param matrix
	 * a matrix
	 * @param file
	 * the destination file path
	 * @throws IOException
	 */
	public static void save(double[][] matrix,File file) throws IOException
	{
	BufferedWriter out=null;
	
		try
		{
			out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			out.write(toString(matrix));
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
	 * load a integer matrix which is written in text format from file
	 * @param path
	 * file path
	 * @return
	 * @throws IOException
	 */
	public static int[][] loadIntMatrix(File path) throws IOException
	{
	BufferedReader in=null;
	List<int[]> lrow;
	String[] srow;
	int[] row;
	int[][] matrix;
	int r=0;
	
		try
		{
			lrow=new LinkedList<int[]>();
			in=new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			for(String ts=null;(ts=in.readLine())!=null;)
			{
				ts=ts.trim();
				if(ts.length()==0) continue;
				srow=ts.split("\\s+");
				row=new int[srow.length];
				//convert a row to double
				for(int i=0;i<row.length;i++) row[i]=Integer.parseInt(srow[i]);
				lrow.add(row);
			}
			matrix=new int[lrow.size()][];
			for(int[] temp:lrow) matrix[r++]=temp;
			return matrix;
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
	 * load a double matrix which is written in text format from file
	 * @param path
	 * file path
	 * @return
	 * @throws IOException
	 */
	public static double[][] loadDoubleMatrix(File path) throws IOException
	{
	BufferedReader in=null;
	List<double[]> lrow;
	String[] srow;
	double[] row;
	double[][] matrix;
	int r=0;
	
		try
		{
			lrow=new LinkedList<double[]>();
			in=new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			for(String ts=null;(ts=in.readLine())!=null;)
			{
				ts=ts.trim();
				if(ts.length()==0) continue;
				srow=ts.split("\\s+");
				row=new double[srow.length];
				//convert a row to double
				for(int i=0;i<row.length;i++) row[i]=Double.parseDouble(srow[i]);
				lrow.add(row);
			}
			matrix=new double[lrow.size()][];
			for(double[] temp:lrow) matrix[r++]=temp;
			return matrix;
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
	 * copy a matrix
	 * @param matrix
	 * a matrix
	 * @param destination
	 * The destination, null to allocate new space
	 * @return
	 */
	public static int[][] copy(int[][] matrix,int[][] destination)
	{
		if(destination==null) destination=new int[matrix.length][matrix[0].length];
		else checkDestinationSize(destination,matrix.length,matrix[0].length);
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[i].length;j++) destination[i][j]=matrix[i][j];
		return destination;
	}
	
	/**
	 * copy a matrix
	 * @param matrix
	 * a matrix
	 * @param destination
	 * The destination, null to allocate new space
	 * @return
	 */
	public static double[][] copy(double[][] matrix,double[][] destination)
	{
		if(destination==null) destination=new double[matrix.length][matrix[0].length];
		else checkDestinationSize(destination,matrix.length,matrix[0].length);
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[i].length;j++) destination[i][j]=matrix[i][j];
		return destination;
	}
	
	/**
	 * generate an identity matrix
	 * @param row
	 * row number
	 * @param column
	 * column number
	 * @return
	 */
	public static double[][] eye(int row,int column)
	{
	double[][] eye;
	int len;
		
		eye=new double[row][column];
		
		len=Math.min(row,column);
		for(int i=0;i<len;i++) eye[i][i]=1;
	
		return eye;
	}
	
	/**
	 * generate an identity matrix
	 * @param row
	 * row number
	 * @param column
	 * column number
	 * @return
	 */
	public static ComplexMatrix eyeComplex(int row,int column)
	{
	double[][] real,imag;
	
		real=eye(row,column);
		imag=new double[row][column];
		return new ComplexMatrix(real,imag);
	}
	
	/**
	 * fill the matrix to a specified value
	 * @param matrix
	 * the matrix
	 * @param value
	 * the specified value
	 */
	public static void fill(double[][] matrix,double value)
	{
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[i].length;j++) matrix[i][j]=value;
	}
	
	/**
	 * fill a multi-channel matrix a specified value
	 * @param multichannel
	 * a multi-channel value
	 * @param value
	 * the filled value
	 */
	public static void fill(double[][][] multichannel,double value)
	{
		for(int y=0;y<multichannel.length;y++)
			for(int x=0;x<multichannel[y].length;x++)
				Arrays.fill(multichannel[y][x],value);
	}
	
	/**
	 * fill the matrix to a specified value
	 * @param matrix
	 * a complex matrix
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public static void fill(ComplexMatrix matrix,double real,double imag)
	{
		BLAS.fill(matrix.real(), real);
		BLAS.fill(matrix.imaginary(), imag);
	}
	
	/**
	 * fill the vector to a specified value
	 * @param vector
	 * a complex vector
	 * @param real
	 * real part
	 * @param imag
	 * imaginary part
	 */
	public static void fill(ComplexVector vector,double real,double imag)
	{
		Arrays.fill(vector.real(), real);
		Arrays.fill(vector.imaginary(), imag);
	}
	
	/**
	 * normalize a vector x to unit vector so that ||x||=1
	 * @param x
	 * a vector
	 */
	public static void normalize(double[] x)
	{
	double temp=0;
	
		for(double xi:x) temp+=xi*xi;
		if(temp==0) return;//the zero vector
		temp=Math.sqrt(temp);
		for(int i=0;i<x.length;i++) x[i]/=temp;
	}
	
	/**
	 * normalize a vector x to unit vector so that ||x||=1
	 * @param x
	 * a vector
	 */
	public static void normalize(ComplexVector x)
	{
	double[] real,imag;
	double temp=0;
		
		real=x.real();
		imag=x.imaginary();
		
		for(int i=0;i<real.length;i++) temp+=real[i]*real[i]+imag[i]*imag[i];
		
		if(temp==0) return;//zero vector
		temp=1.0/Math.sqrt(temp);
		for(int i=0;i<real.length;i++) 
		{
			real[i]*=temp;
			imag[i]*=temp;
		}	
	}
	
	/**
	 * calculate a vector's 2-norm
	 * @param x
	 * a vector
	 * @return
	 */
	public static double norm2(double[] x)
	{
	double n2=0;
	
		for(int i=0;i<x.length;i++) n2+=x[i]*x[i];
		return Math.sqrt(n2);
	}
	
	/**
	 * calculate |x|^2
	 * @param x
	 * a vector
	 * @return
	 */
	public static double norm2Square(ComplexVector x)
	{
	double n2=0;
	double[] real,imag;
			
		real=x.real();
		imag=x.imaginary();
		for(int i=0;i<real.length;i++) n2+=real[i]*real[i]+imag[i]*imag[i];
		return n2;		
	}
	
	/**
	 * calculate a vector's 2-norm
	 * @param x
	 * a vector
	 * @return
	 */
	public static double norm2(ComplexVector x)
	{
		return Math.sqrt(BLAS.norm2Square(x));
	}
	
	/**
	 * calculate the maximum norm for a vector: max(|x1|, |x2|, ..., |xn|)
	 * @param x
	 * a vector
	 * @return
	 */
	public static double normInf(double[] x)
	{
	double n=0,absxx;
	
		for(double xx:x) 
		{
			absxx=Math.abs(xx);
			if(absxx>n) n=absxx;
		}
		
		return n;
	}
	
	/**
	 * calculate the maximum norm for a matrix
	 * @param m
	 * a matrix
	 * @return
	 */
	public static double normInf(double[][] m)
	{
	double n=0,absxx;
		
		for(int i=0;i<m.length;i++) 
			for(int j=0;j<m[i].length;j++) 
			{
				absxx=Math.abs(m[i][j]);
				if(absxx>n) n=absxx;
			}
		
		return n;		
	}
	
	/**
	 * calculate the maximum norm for a matrix
	 * @param m
	 * a matrix
	 * @return
	 */
	public static double normInf(ComplexMatrix m)
	{
	double[][] real,imag;
	double n=0,absxx;
		
		real=m.real();
		imag=m.imaginary();
	
		for(int i=0;i<m.numRows();i++) 
			for(int j=0;j<m.numColumns();j++) 
			{
				absxx=SPComplex.abs(real[i][j],imag[i][j]);
				if(absxx>n) n=absxx;
			}

		return n;
	}
	
	/**
	 * calculate the inner product of two vectors
	 * @param x, y
	 * two vectors
	 * @return
	 */
	public static double innerProduct(double[] x,double[] y)
	{
	double temp=0;
	
		checkSize(x,y);
		for(int i=0;i<x.length;i++) temp+=x[i]*y[i];
		return temp;
	}
	
	/**
	 * calculate the inner product of two complex vectors: x'*y
	 * @param x, y
	 * two complex vectors
	 * @return
	 */
	public static SPComplex innerProduct(ComplexVector x,ComplexVector y)
	{
	double real=0,imag=0;
	double[] a,b,c,d;
	
		checkSize(x,y);
		a=x.real();
		b=x.imaginary();
		c=y.real();
		d=y.imaginary();
		
		for(int i=0;i<x.size();i++) 
		{
			real+=a[i]*c[i]+b[i]*d[i];
			imag+=a[i]*d[i]-b[i]*c[i];
		}
		
		return new SPComplex(real,imag);
	}
	
	/**
	 * calculate |x'*y|
	 * @param x
	 * a complex vector
	 * @param y
	 * a complex vector
	 * @return
	 */
	public static double innerProductAbs(ComplexVector x,ComplexVector y)
	{
		return Math.sqrt(BLAS.innerProductAbsSquare(x, y));
	}
	
	/**
	 * calculate |x'*y|^2
	 * @param x
	 * a complex vector
	 * @param y
	 * a complex vector
	 * @return
	 */
	public static double innerProductAbsSquare(ComplexVector x,ComplexVector y)
	{
	double real=0,imag=0;
	double[] a,b,c,d;
		
		checkSize(x,y);
		a=x.real();
		b=x.imaginary();
		c=y.real();
		d=y.imaginary();
			
		for(int i=0;i<x.size();i++) 
		{
			real+=a[i]*c[i]+b[i]*d[i];
			imag+=a[i]*d[i]-b[i]*c[i];
		}
			
		return real*real+imag*imag;
	}
	
	/**
	 * calculate the outer product of two vectors
	 * @param x
	 * a vector
	 * @param y
	 * another vector
	 * @param result
	 * The result destination, null to allocate new space
	 * @return
	 */
	public static double[][] outerProduct(double[] x,double[] y,double[][] result)
	{
		if(result==null) result=new double[x.length][y.length];
		else checkDestinationSize(result,x.length,y.length);
		for(int i=0;i<x.length;i++)
			for(int j=0;j<y.length;j++) result[i][j]=x[i]*y[j];
		return result;
	}
	
	/**
	 * calculate the outer product of two vectors
	 * @param x
	 * a vector
	 * @param y
	 * another vector
	 * @param result
	 * The result destination, null to allocate new space
	 * @return
	 */
	public static ComplexMatrix outerProduct(ComplexVector x,ComplexVector y,ComplexMatrix result)
	{
	double[] a,b,c,d;	
		
		if(result==null) result=new ComplexMatrix(x.size(),y.size());
		else checkDestinationSize(result,x.size(),y.size());
		
		a=x.real();
		b=x.imaginary();
		c=y.real();
		d=y.imaginary();
		
		for(int i=0;i<x.size();i++)
			for(int j=0;j<y.size();j++) 
				result.setValue(i,j,
						a[i]*c[j]+b[i]*d[j],
						-a[i]*d[j]+b[i]*c[j]);
		
		return result;
	}

	/**
	 * calculate m1 * m2
	 * @param m1
	 * the first matrix
	 * @param m2
	 * the second matrix
	 * @param result
	 * The result destination, null to allocate new space
	 * @return
	 */
	public static double[][] multiply(double[][] m1,double[][] m2,double[][] result)
	{
	double[] temp;
	
		if(m1[0].length!=m2.length) throw new IllegalArgumentException(
				"inconsistant matrix dimension for multiplication: "+m1[0].length+", "+m2.length);
		if(result==null) result=new double[m1.length][m2[0].length];
		else checkDestinationSize(result,m1.length,m2[0].length);
		//the first matrix is used to store result
		if(result==m1)
		{
			temp=new double[result[0].length];//a row for result
			//calculate each row of the result matrix
			for(int i=0;i<result.length;i++)
			{
				Arrays.fill(temp,0);
				//fill each column of current row
				for(int j=0;j<temp.length;j++)
					//fill an entry
					for(int k=0;k<m1[i].length;k++) temp[j]+=m1[i][k]*m2[k][j];
				System.arraycopy(temp,0,result[i],0,temp.length);
			}
		}
		//the second matrix is used to store result
		else if(result==m2)
		{
			temp=new double[m2.length];//a column of result
			//calculate each column of the result matrix
			for(int j=0;j<result[0].length;j++)
			{
				Arrays.fill(temp,0);
				//fill each row of current column
				for(int i=0;i<temp.length;i++)
					//fill an entry
					for(int k=0;k<m1[i].length;k++) temp[i]+=m1[i][k]*m2[k][j];
				for(int k=0;k<temp.length;k++) result[k][j]=temp[k];
			}
		}
		//other destination
		else
		{
			for(int i=0;i<result.length;i++)
				for(int j=0;j<result[i].length;j++)
					for(int k=0;k<m1[i].length;k++) result[i][j]+=m1[i][k]*m2[k][j];
		}
		return result;
	}
	
	/**
	 * calculate m1 * m2 for complex matrices
	 * @param m1
	 * a complex matrix
	 * @param m2
	 * another complex matrix
	 * @param result
	 * space for result, or null to allocate new space
	 * @return
	 */
	public static ComplexMatrix multiply(ComplexMatrix m1,ComplexMatrix m2,ComplexMatrix result)
	{
	double[][] a,b,c,d,real,imag;
		
		if(m1.numColumns()!=m2.numRows()) throw new IllegalArgumentException(
				"inconsistant matrix dimension for multiplication: "+m1.numColumns()+", "+m2.numRows());
		if(result==null) result=new ComplexMatrix(m1.numRows(),m2.numColumns());
		else checkDestinationSize(result,m1.numRows(),m2.numColumns());
		
		a=m1.real();
		b=m1.imaginary();
		c=m2.real();
		d=m2.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		//the first matrix is used to store result
		if(result==m1)
		{
		double[][] temp;	
			
			temp=new double[2][result.numColumns()];//a row for result
			
			//calculate each row of the result matrix
			for(int i=0;i<result.numRows();i++)
			{
				fill(temp,0);	
				
				//fill each column of current row
				for(int j=0;j<result.numColumns();j++)
					//fill an entry
					for(int k=0;k<m1.numColumns();k++) 
					{
						temp[0][j]+=a[i][k]*c[k][j]-b[i][k]*d[k][j];
						temp[1][j]+=a[i][k]*d[k][j]+b[i][k]*c[k][j];
					}
				
				for(int k=0;k<result.numColumns();k++) 
				{
					real[i][k]=temp[0][k];
					imag[i][k]=temp[1][k];
				}
			}
		}
		//the second matrix is used to store result
		else if(result==m2)
		{
		double[][] temp;
			
			temp=new double[2][result.numRows()];//a column of result
			
			//calculate each column of the result matrix
			for(int j=0;j<result.numColumns();j++)
			{
				fill(temp,0);
				
				//fill each row of current column
				for(int i=0;i<result.numRows();i++)
					//fill an entry
					for(int k=0;k<m1.numColumns();k++) 
					{
						temp[0][i]+=a[i][k]*c[k][j]-b[i][k]*d[k][j];
						temp[1][i]+=a[i][k]*d[k][j]+b[i][k]*c[k][j];
					}

				for(int k=0;k<result.numRows();k++) 
				{
					real[k][j]=temp[0][k];
					imag[k][j]=temp[1][k];
				}
			}
		}
		//other destination
		else
		{
		double rr,ii;
		
			for(int i=0;i<result.numRows();i++)
				for(int j=0;j<result.numColumns();j++)
				{
					rr=0;
					ii=0;
					
					for(int k=0;k<m1.numColumns();k++) 
					{
						rr+=a[i][k]*c[k][j]-b[i][k]*d[k][j];
						ii+=a[i][k]*d[k][j]+b[i][k]*c[k][j];
					}
					
					real[i][j]=rr;
					imag[i][j]=ii;
				}
		}
		
		return result;
	}
	
	/**
	 * perform matrix-vector multiplication
	 * @param m
	 * a matrix
	 * @param v
	 * a vector
	 * @param result
	 * space for result, null to allocate new space
	 * @return
	 */
	public static ComplexVector multiply(ComplexMatrix m,ComplexVector v,ComplexVector result)
	{
	ComplexVector res;
	double[][] mr,mi;
	double[] vr,vi,resr,resi;
	
		if(m.numColumns()!=v.size()) throw new IllegalArgumentException(
				"matrix-vector size not match: "+m.numColumns()+", "+v.size());
		if(result==null) 
		{
			result=new ComplexVector(m.numRows());
			res=result;
		}
		else 
		{
			BLAS.checkDestinationSize(result, m.numRows());
			//cannot use the vector as the destination space
			if(result==v) res=new ComplexVector(m.numRows());
			else res=result;
		}
		
		mr=m.real();
		mi=m.imaginary();
		vr=v.real();
		vi=v.imaginary();
		resr=res.real();
		resi=res.imaginary();
		
		for(int i=0;i<m.numRows();i++) 
		{
			resr[i]=0;
			resi[i]=0;
			
			for(int j=0;j<m.numColumns();j++)
			{
				resr[i]+=mr[i][j]*vr[j]-mi[i][j]*vi[j];
				resi[i]+=mr[i][j]*vi[j]+mi[i][j]*vr[j];
			}
		}
		
		if(result==v) result.copy(res);
		return result;
	}
	
	/**
	 * perform entry by entry mulitplication
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * buffer for result, null to allocate new space
	 * @return
	 */
	public static double[] entryMultiply(double[] v1,double[] v2,double[] result)
	{
		checkSize(v1,v2);
		if(result==null) result=new double[v1.length];
		else checkSize(result,v1);
		for(int i=0;i<result.length;i++) result[i]=v1[i]*v2[i];
		return result;
	}
	
	/**
	 * perform entry by entry mulitplication
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * buffer for result, null to allocate new space
	 * @return
	 */
	public static ComplexVector entryMultiply(ComplexVector v1,ComplexVector v2,ComplexVector result)
	{
	double[] a,b,c,d;
	double real,imag;
	
		checkSize(v1,v2);
		if(result==null) result=new ComplexVector(v1.size());
		else checkSize(result,v1);
	
		a=v1.real();
		b=v1.imaginary();
		c=v2.real();
		d=v2.imaginary();
		
		for(int i=0;i<result.size();i++) 
		{
			real=a[i]*c[i]-b[i]*d[i];
			imag=a[i]*d[i]+b[i]*c[i];
			
			result.setValue(i,real,imag);
		}
		
		return result;
	}
	
	/**
	 * perform entry by entry mulitplication
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * buffer for result, null to allocate new space
	 * @return
	 */
	public static ComplexVector entryMultiply(double[] v1,ComplexVector v2,ComplexVector result)
	{
	double[] c,d,real,imag;
	
		if(v1.length!=v2.size()) throw new IllegalArgumentException(
				"vector length not match: "+v1.length+", "+v2.size());
		
		if(result==null) result=new ComplexVector(v1.length);
		else checkDestinationSize(result, v1.length);
		
		c=v2.real();
		d=v2.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		for(int i=0;i<result.size();i++) 
		{
			real[i]=v1[i]*c[i];
			imag[i]=v1[i]*d[i];
		}
		
		return result;
	}
	
	/**
	 * perform entry by entry multiplication
	 * @param m1
	 * a matrix
	 * @param m2
	 * another matrix
	 * @param result
	 * buffer for result, null to allocate new space
	 * @return
	 */
	public static double[][] entryMultiply(double[][] m1,double[][] m2,double[][] result)
	{
		checkSize(m1,m2);
		if(result==null) result=new double[m1.length][m1[0].length];
		else checkSize(result,m1);
		for(int i=0;i<result.length;i++)
			for(int j=0;j<result[i].length;j++) result[i][j]=m1[i][j]*m2[i][j];
		return result;
	}
	
	/**
	 * perform entry by entry mulitplication
	 * @param m1
	 * a matrix
	 * @param m2
	 * another matrix
	 * @param result
	 * buffer for result, null to allocate new space
	 * @return
	 */
	public static ComplexMatrix entryMultiply(ComplexMatrix m1,ComplexMatrix m2,ComplexMatrix result)
	{
	double[][] a,b,c,d;
	double real,imag;
	
		checkSize(m1,m2);
		if(result==null) result=new ComplexMatrix(m1.numRows(),m2.numColumns());
		else checkSize(result,m1);
	
		a=m1.real();
		b=m1.imaginary();
		c=m2.real();
		d=m2.imaginary();
		
		for(int i=0;i<result.numRows();i++) 
			for(int j=0;j<result.numColumns();j++) 
			{
				real=a[i][j]*c[i][j]-b[i][j]*d[i][j];
				imag=a[i][j]*d[i][j]+b[i][j]*c[i][j];
			
				result.setValue(i,j,real,imag);
			}
		
		return result;
	}
	
	/**
	 * calculate the scalar multiplication of a scalar and a vector
	 * @param s
	 * a scalar
	 * @param v
	 * a vector
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static double[] scalarMultiply(double s,double[] v,double[] result)
	{
		if(result==null) result=new double[v.length];
		else checkDestinationSize(result,v.length);
		for(int i=0;i<result.length;i++) result[i]=s*v[i];
		return result;
	}
	
	/**
	 * calculate the scalar multiplication of a scalar and a vector
	 * @param s
	 * a scalar
	 * @param v
	 * a vector
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static ComplexVector scalarMultiply(double s,ComplexVector v,ComplexVector result)
	{
	double[] a,b;
	
		if(result==null) result=new ComplexVector(v.size());
		else checkDestinationSize(result,v.size());
	
		a=v.real();
		b=v.imaginary();
		
		for(int i=0;i<v.size();i++) result.setValue(i,s*a[i],s*b[i]);	
		
		return result;
	}
	
	/**
	 * calculate the scalar multiplication of a scalar and a vector
	 * @param cr
	 * a scalar's real part
	 * @param ci
	 * a scalar's imaginary part
	 * @param v
	 * a vector
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static ComplexVector scalarMultiply(double cr,double ci,ComplexVector v,ComplexVector result)
	{
	double[] a,b;
	double real,imag;
	
		if(result==null) result=new ComplexVector(v.size());
		else checkDestinationSize(result,v.size());
		
		a=v.real();
		b=v.imaginary();
		
		for(int i=0;i<v.size();i++) 
		{
			real=cr*a[i]-ci*b[i];
			imag=cr*b[i]+ci*a[i];
			
			result.setValue(i,real,imag);
		}
		
		return result;
	}
	
	/**
	 * calculate the scalar multiplication of a scalar and a matrix
	 * @param s
	 * a scalar
	 * @param m
	 * a matrix
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static double[][] scalarMultiply(double s,double[][] m,double[][] result)
	{
		if(result==null) result=new double[m.length][m[0].length];
		else checkDestinationSize(result,m.length,m[0].length);
		for(int i=0;i<result.length;i++)
			for(int j=0;j<result[i].length;j++) result[i][j]=s*m[i][j];
		return result;
	}
	
	/**
	 * calculate the scalar multiplication of a scalar and a matrix
	 * @param s
	 * a scalar
	 * @param m
	 * a matrix
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static ComplexMatrix scalarMultiply(double s,ComplexMatrix m,ComplexMatrix result)
	{
	double[][] a,b;
	
		if(result==null) result=new ComplexMatrix(m.numRows(),m.numColumns());
		else checkDestinationSize(result,m.numRows(),m.numColumns());
		
		a=m.real();
		b=m.imaginary();
		
		for(int i=0;i<result.numRows();i++)
			for(int j=0;j<result.numColumns();j++) 
				result.setValue(i,j,s*a[i][j],s*b[i][j]);
		
		return result;
	}
	
	/**
	 * calculate the scalar multiplication of a scalar and a matrix
	 * @param cr
	 * real part
	 * @param ci
	 * imaginary part
	 * @param m
	 * a matrix
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static ComplexMatrix scalarMultiply(double cr,double ci,ComplexMatrix m,ComplexMatrix result)
	{
	double[][] c,d;
	double real,imag;
		
		if(result==null) result=new ComplexMatrix(m.numRows(),m.numColumns());
		else checkDestinationSize(result,m.numRows(),m.numColumns());
		
		c=m.real();
		d=m.imaginary();
		
		for(int i=0;i<result.numRows();i++)
			for(int j=0;j<result.numColumns();j++) 
			{
				real=cr*c[i][j]-ci*d[i][j];
				imag=cr*d[i][j]+ci*c[i][j];
				
				result.setValue(i,j,real,imag);
			}
		
		return result;		
	}
	
	/**
	 * calculate the sum of two vectors
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static double[] add(double[] v1,double[] v2,double[] result)
	{
		checkSize(v1,v2);
		if(result==null) result=new double[v1.length];
		else checkDestinationSize(result,v1.length);
		for(int i=0;i<result.length;i++) result[i]=v1[i]+v2[i];
		return result;
	}
	
	/**
	 * calculate the sum of two vectors
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static ComplexVector add(ComplexVector v1,ComplexVector v2,ComplexVector result)
	{
	double[] a,b,c,d,real,imag;
	
		if(result==null) result=new ComplexVector(v1.size());
	
		a=v1.real();
		b=v1.imaginary();
		c=v2.real();
		d=v2.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		add(a,c,real);
		add(b,d,imag);
		
		return result;
	}
	
	/**
	 * calculate the sum of two matrices
	 * @param m1, m2
	 * two matrices
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static int[][] add(int[][] m1,int[][] m2,int[][] result)
	{
		checkSize(m1,m2);
		if(result==null) result=new int[m1.length][m1[0].length];
		else checkDestinationSize(result,m1.length,m1[0].length);
		for(int i=0;i<result.length;i++)
			for(int j=0;j<result[i].length;j++) result[i][j]=m1[i][j]+m2[i][j];
		return result;
	}
	
	/**
	 * calculate the sum of two matrices
	 * @param m1, m2
	 * two matrices
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static double[][] add(double[][] m1,double[][] m2,double[][] result)
	{
		checkSize(m1,m2);
		if(result==null) result=new double[m1.length][m1[0].length];
		else checkDestinationSize(result,m1.length,m1[0].length);
		for(int i=0;i<result.length;i++)
			for(int j=0;j<result[i].length;j++) result[i][j]=m1[i][j]+m2[i][j];
		return result;
	}
	
	/**
	 * calculate the sum of two matrices
	 * @param m1, m2
	 * two matrices
	 * @param result
	 * Result destination, null to allocate new space.
	 * @return
	 */
	public static ComplexMatrix add(ComplexMatrix m1,ComplexMatrix m2,ComplexMatrix result)
	{
	double[][] a,b,c,d,real,imag;
	
		if(result==null) result=new ComplexMatrix(m1.numRows(),m1.numColumns());
		
		a=m1.real();
		b=m1.imaginary();
		c=m2.real();
		d=m2.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		add(a,c,real);
		add(b,d,imag);
		
		return result;
	}
	
	/**
	 * calculate the difference of two vectors
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * Result destination, null to allocate new space
	 * @return
	 */
	public static double[] subtract(double[] v1,double[]v2,double[] result)
	{
		checkSize(v1,v2);
		if(result==null) result=new double[v1.length];
		else checkDestinationSize(result,v1.length);
		for(int i=0;i<result.length;i++) result[i]=v1[i]-v2[i];
		return result;
	}
	
	/**
	 * calculate the difference of two vectors
	 * @param v1
	 * a vector
	 * @param v2
	 * another vector
	 * @param result
	 * Result destination, null to allocate new space
	 * @return
	 */
	public static ComplexVector subtract(ComplexVector v1,ComplexVector v2,ComplexVector result)
	{
	double[] a,b,c,d,real,imag;
	
		if(result==null) result=new ComplexVector(v1.size());
		
		a=v1.real();
		b=v1.imaginary();
		c=v2.real();
		d=v2.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		subtract(a,c,real);
		subtract(b,d,imag);
		
		return result;
	}
	
	/**
	 * calculate the difference of two matrices
	 * @param m1
	 * a matrix
	 * @param m2
	 * another matrix
	 * @param result
	 * Result destination, null to allocate new space
	 * @return
	 */
	public static double[][] subtract(double[][] m1,double[][] m2,double[][] result)
	{
		checkSize(m1,m2);
		if(result==null) result=new double[m1.length][m1[0].length];
		else checkDestinationSize(result,m1.length,m1[0].length);
		for(int i=0;i<result.length;i++)
			for(int j=0;j<result[i].length;j++) result[i][j]=m1[i][j]-m2[i][j];
		return result;
	}
	
	/**
	 * calculate the difference of two matrices
	 * @param m1
	 * a matrix
	 * @param m2
	 * another matrix
	 * @param result
	 * Result destination, null to allocate new space
	 * @return
	 */
	public static ComplexMatrix subtract(ComplexMatrix m1,ComplexMatrix m2,ComplexMatrix result)
	{
	double[][] a,b,c,d,real,imag;	
		
		if(result==null) result=new ComplexMatrix(m1.numRows(),m1.numColumns());
		
		a=m1.real();
		b=m1.imaginary();
		c=m2.real();
		d=m2.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		subtract(a,c,real);
		subtract(b,d,imag);
		
		return result;
	}
	
	/**
	 * add a scalar into a vector
	 * @param s
	 * a scalar
	 * @param v
	 * a vector
	 * @param result
	 * space for result, null to allocate new space
	 * @return
	 */
	public static double[] scalarAdd(double s,double[] v,double[] result)
	{
		if(result==null) result=new double[v.length];
		else checkSize(v,result);
		for(int i=0;i<v.length;i++) result[i]=s+v[i];
		return result;
	}
	
	/**
	 * add a scalar into a matrix
	 * @param s
	 * a scalar
	 * @param m
	 * a matrix
	 * @param result
	 * space for result, null to allocate new space
	 * @return
	 */
	public static double[][] scalarAdd(double s,double[][] m,double[][] result)
	{
		if(result==null) result=new double[m.length][m[0].length];
		else checkSize(m,result);
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++) result[i][j]=s+m[i][j];
		return result;
	}
	
	/**
	 * calculate the sum of entries of a vector
	 * @param v
	 * a vector
	 * @return
	 */
	public static int sum(int[] v)
	{
	int sum=0;
		
		for(double e:v) sum+=e;
		return sum;		
	}
	
	/**
	 * calculate the sum of entries of a vector
	 * @param v
	 * a vector
	 * @return
	 */
	public static double sum(double[] v)
	{
	double sum=0;
	
		for(double e:v) sum+=e;
		return sum;
	}
	
	/**
	 * calculate the sum of entries of a matrix
	 * @param m
	 * a matrix
	 * @return
	 */
	public static double sum(double[][] m)
	{
	double sum=0;
	
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++) sum+=m[i][j];
		return sum;
	}
	
	/**
	 * calculate the sum of entries of a vector
	 * @param v
	 * a vector
	 * @return
	 */
	public static SPComplex sum(ComplexVector v)
	{
		return new SPComplex(sum(v.real()),sum(v.imaginary()));
	}
	
	/**
	 * calculate the mean value of a vector
	 * @param v
	 * a vector
	 * @return
	 */
	public static double mean(double[] v)
	{
		return sum(v)/v.length;
	}
	
	/**
	 * calculate the mean velue of a matrix
	 * @param m
	 * a matrix
	 * @return
	 */
	public static double mean(double[][] m)
	{
		return sum(m)/(m.length*m[0].length);
	}
	
	/**
	 * select the max value from a vector
	 * @param v
	 * a vector
	 * @return
	 */
	public static double maxValue(double[] v)
	{
	double max=Double.NEGATIVE_INFINITY;
	
		for(double d:v) if(d>max) max=d;
		
		return max;
	}
	
	/**
	 * calculate the determinant of a square matrix
	 * @param matrix
	 * a square matrix
	 * @return
	 */
	public static double det(double[][] matrix)
	{
	double[][] m;
	double[] tempr;
	int nonzero;
	double sign=1,temp;
	
		if(matrix.length!=matrix[0].length) throw new IllegalArgumentException(
				"a square matrix is required");
		/*
		 * convert to the upper trangular matrix then 
		 * calculate the determinant
		 */
		//copy the original matrix because the data will be modified
		m=copy(matrix,null);
		//process the first n-1 row
		for(int i=0;i<m.length-1;i++)
		{
			//find the row the ith element is not zero
			for(nonzero=i;nonzero<m.length;nonzero++) if(m[nonzero][i]!=0) break;
			//can not find such row
			if(nonzero>=m.length) return 0;
			//swap m[i][] and m[nonzero][]
			else if(nonzero!=i)
			{
				tempr=m[i];
				m[i]=m[nonzero];
				m[nonzero]=tempr;
				sign*=-1;//the sign is changed when swap two rows
			}
			/*
			 * eliminate the elements in the ith column from the 
			 * i+1th row
			 */
			for(int ii=i+1;ii<m.length;ii++)
			{
				if(m[ii][i]==0) continue;
				temp=-m[ii][i]/m[i][i];
				for(int j=i;j<m[i].length;j++) m[ii][j]+=(m[i][j]*temp);
			}
		}
		/*
		 * calculate the final result
		 */
		if(m[m.length-1][m.length-1]==0) return 0;
		for(int i=0;i<m.length;i++) sign*=m[i][i];
		return sign;
	}
	
	/**
	 * calculate the determinant of a square matrix
	 * @param matrix
	 * a square matrix
	 * @return
	 */
	public static SPComplex det(ComplexMatrix matrix)
	{
	ComplexMatrix m;
	double[][] a,b;
	double[] tempr;
	int nonzero;
	double t,tr,ti,sign=1,c,d;
	
		if(matrix.numRows()!=matrix.numColumns()) throw new IllegalArgumentException(
				"a square matrix is required");

		/*
		 * convert to the upper trangular matrix then 
		 * calculate the determinant
		 */
		//copy the original matrix because the data will be modified
		m=new ComplexMatrix(matrix);
		a=m.real();
		b=m.imaginary();
		
		//process the first n-1 row
		for(int i=0;i<m.numRows()-1;i++)
		{
			//find the row the ith element is not zero
			for(nonzero=i;nonzero<m.numRows();nonzero++) 
				if(a[nonzero][i]!=0&&b[nonzero][i]!=0) break;

			//can not find such row
			if(nonzero>=m.numRows()) return SPComplex.ZERO;
			//swap m[i][] and m[nonzero][]
			else if(nonzero!=i)
			{
				tempr=a[i];
				a[i]=a[nonzero];
				a[nonzero]=tempr;
				
				tempr=b[i];
				b[i]=b[nonzero];
				b[nonzero]=tempr;
				
				sign*=-1;//the sign is changed when swap two rows
			}
			
			/*
			 * eliminate the elements in the ith column from the 
			 * i+1th row
			 */
			for(int ii=i+1;ii<m.numRows();ii++)
			{
				if(a[ii][i]==0&&b[ii][i]==0) continue;
				
				t=SPComplex.absSquare(a[i][i],b[i][i]);
				tr=(-a[ii][i]*a[i][i]-b[ii][i]*b[i][i])/t;
				ti=(a[ii][i]*b[i][i]-b[ii][i]*a[i][i])/t;
				
				for(int j=i;j<m.numColumns();j++) 
				{
					a[ii][j]+=a[i][j]*tr-b[i][j]*ti;
					b[ii][j]+=a[i][j]*ti+b[i][j]*tr;
				}
			}
		}

		/*
		 * calculate the final result
		 */
		if(a[m.numRows()-1][m.numColumns()-1]==0&&
				b[m.numRows()-1][m.numColumns()-1]==0) return SPComplex.ZERO;
		
		c=a[0][0];
		d=b[0][0];
		for(int i=1;i<m.numRows();i++) 
		{
			tr=a[i][i]*c-b[i][i]*d;
			ti=a[i][i]*d+b[i][i]*c;
			
			c=tr;
			d=ti;
		}
		
		return new SPComplex(sign*c,sign*d);
	}
	
	/**
	 * calculate the inverse matrix
	 * @param matrix
	 * the input matrix
	 * @param inv
	 * The result destination, null to allocate new space.
	 * @return
	 * return null if the matrix is singular
	 */
	public static double[][] inv(double[][] matrix,double[][] inv)
	{
	double[][] m;
	double[] tempr;
	int nonzero;
	double temp;
	
		if(matrix.length!=matrix[0].length) throw new IllegalArgumentException(
				"a square matrix is required");
		if(inv==null) inv=new double[matrix.length][matrix[0].length];
		else checkDestinationSize(inv,matrix.length,matrix[0].length);
		m=copy(matrix,null);//to prevent the original matrix be modified
		//starts from the identity matrix
		for(int i=0;i<inv.length;i++) 
			for(int j=0;j<inv[i].length;j++) 
				if(i==j) inv[i][j]=1;else inv[i][j]=0;
		//process the first n-1 row
		for(int i=0;i<m.length-1;i++)
		{
			//find the pivotal row
			for(nonzero=i;nonzero<m.length;nonzero++) if(m[nonzero][i]!=0) break;
			//can not find such row
			if(nonzero>=m.length) return null;
			//swap m[i][] and the pivotal row
			else if(nonzero!=i)
			{
				tempr=m[i];
				m[i]=m[nonzero];
				m[nonzero]=tempr;
				/*
				 * the same elementary row operation to inverse matrix
				 */
				tempr=inv[i];
				inv[i]=inv[nonzero];
				inv[nonzero]=tempr;
			}
			/*
			 * make the pivot to 1
			 */
			temp=1/m[i][i];
			for(int j=i;j<m[i].length;j++) m[i][j]*=temp;
			for(int j=0;j<inv[i].length;j++) inv[i][j]*=temp;
			//eliminate the elements in the ith column from the i+1th row
			for(int ii=i+1;ii<m.length;ii++)
			{
				if(m[ii][i]==0) continue;
				temp=-m[ii][i];//the pivot is 1
				for(int j=i;j<m[i].length;j++) m[ii][j]+=(m[i][j]*temp);
				//the same elementary row operation to inverse matrix
				for(int j=0;j<inv[i].length;j++) inv[ii][j]+=(inv[i][j]*temp);
			}
		}
		/*
		 * process the last row
		 */
		if(m[m.length-1][m[m.length-1].length-1]==0) return null;
		else
		{
			temp=1/m[m.length-1][m[m.length-1].length-1];
			m[m.length-1][m[m.length-1].length-1]=1;
			for(int j=0;j<inv[inv.length-1].length;j++) inv[inv.length-1][j]*=temp;
		}
		/*
		 * back substitution
		 */
		for(int i=inv.length-1;i>=1;i--)
			for(int ii=i-1;ii>=0;ii--)
			{
				temp=-m[ii][i];
				for(int j=0;j<inv[i].length;j++) inv[ii][j]+=(inv[i][j]*temp);	
			}
		return inv;
	}
	
	/**
	 * calculate the inverse matrix of a complex matrix
	 * @param matrix
	 * a complex matrix
	 * @param inv
	 * destination for result, null to allocate new space
	 * @return
	 */
	public static ComplexMatrix inv(ComplexMatrix matrix,ComplexMatrix inv)
	{
	double[][] temp;
	double real,imag;
		
		if(matrix.numRows()!=matrix.numColumns()) throw new IllegalArgumentException(
				"a square matrix is required");
		if(inv==null) inv=new ComplexMatrix(matrix.numRows(),matrix.numColumns());
		else checkDestinationSize(inv,matrix.numRows(),matrix.numColumns());
		
		/*
		 * construct [real(M) imag(M); -imag(M) real(M)]
		 */
		temp=new double[matrix.numRows()*2][matrix.numColumns()*2];
		for(int i=0;i<matrix.numRows();i++)
			for(int j=0;j<matrix.numColumns();j++)
			{
				real=matrix.getReal(i,j);
				imag=matrix.getImaginary(i,j);
				
				temp[i][j]=real;
				temp[i+matrix.numRows()][j+matrix.numColumns()]=real;
				temp[i][j+matrix.numColumns()]=imag;
				temp[i+matrix.numRows()][j]=-imag;
			}
		
		/*
		 * calculate the inverse, the result is: 
		 * [real(inv(M)) imag(inv(M)); -imag(inv(M)) real(inv(M))]
		 */
		temp=BLAS.inv(temp,temp);
		if(temp==null) return null;
		
		//get the result
		for(int i=0;i<inv.numRows();i++)
			for(int j=0;j<inv.numColumns();j++) 
				inv.setValue(i,j,temp[i][j],temp[i][j+inv.numColumns()]);
		
		return inv;
	}
	
	/**
	 * calculate the pseudo inverse (M'*inv(M*M')) for a matrix
	 * @param matrix
	 * a matrix
	 * @param inv
	 * space for result matrix, or null to allocate new space
	 * @return
	 */
	public static double[][] pinv(double[][] matrix,double[][] inv)
	{
	double[][] product;
				
		if(inv==null) inv=new double[matrix[0].length][matrix.length];
		else checkDestinationSize(inv,matrix[0].length,matrix.length);
			
		inv=BLAS.transpose(matrix,inv);//M'
		product=BLAS.multiply(matrix,inv,null);//M*M'
		BLAS.inv(product,product);//inv(M*M')
		BLAS.multiply(inv,product,inv);//M'*inv(M*M')

		return inv;
	}
	
	/**
	 * calculate the pseudo inverse (M'*inv(M*M')) for a complex matrix
	 * @param matrix
	 * a complex matrix
	 * @param inv
	 * space for result matrix, or null to allocate new space
	 * @return
	 */
	public static ComplexMatrix pinv(ComplexMatrix matrix,ComplexMatrix inv)
	{
	double[][] temp,temp2,product;
	double real,imag;
			
		if(inv==null) inv=new ComplexMatrix(matrix.numColumns(),matrix.numRows());
		else checkDestinationSize(inv,matrix.numColumns(),matrix.numRows());
			
		/*
		 * construct [real(M) imag(M); -imag(M) real(M)]
		 */
		temp=new double[matrix.numRows()*2][matrix.numColumns()*2];
		for(int i=0;i<matrix.numRows();i++)
			for(int j=0;j<matrix.numColumns();j++)
			{
				real=matrix.getReal(i,j);
				imag=matrix.getImaginary(i,j);
				
				temp[i][j]=real;
				temp[i+matrix.numRows()][j+matrix.numColumns()]=real;
				temp[i][j+matrix.numColumns()]=imag;
				temp[i+matrix.numRows()][j]=-imag;
			}
		
		temp2=BLAS.transpose(temp,null);//M'
		product=BLAS.multiply(temp,temp2,null);//M*M'
		BLAS.inv(product,product);//inv(M*M')
		BLAS.multiply(temp2,product,temp2);//M'*inv(M*M')
		
		//get the result
		for(int i=0;i<inv.numRows();i++)
			for(int j=0;j<inv.numColumns();j++) 
				inv.setValue(i,j,temp2[i][j],temp2[i][j+inv.numColumns()]);
		
		return inv;
	}
	
	/**
	 * perform the transpose
	 * @param matrix
	 * input matrix
	 * @param result
	 * space for transposed matrix, or null to allocate new space
	 * @return
	 */
	public static int[][] transpose(int[][] matrix,int[][] result)
	{
	int temp;
	
		if(result==null) result=new int[matrix[0].length][matrix.length];
		else BLAS.checkDestinationSize(result,matrix[0].length,matrix.length);
		
		if(matrix==result) 
		{
			for(int i=0;i<result.length;i++) 
				for(int j=i+1;j<result[i].length;j++) 
				{
					temp=result[i][j];
					result[i][j]=result[j][i];
					result[j][i]=temp;
				}
		}
		else
		{
			for(int i=0;i<matrix.length;i++)
				for(int j=0;j<matrix[i].length;j++)
					result[j][i]=matrix[i][j];
		}
		
		return result;
	}
	
	/**
	 * perform the transpose
	 * @param matrix
	 * input matrix
	 * @param result
	 * space for transposed matrix, or null to allocate new space
	 * @return
	 */
	public static double[][] transpose(double[][] matrix,double[][] result)
	{
	double temp;
		
		if(result==null) result=new double[matrix[0].length][matrix.length];
		else BLAS.checkDestinationSize(result,matrix[0].length,matrix.length);
		
		if(matrix==result) 
		{
			for(int i=0;i<result.length;i++) 
				for(int j=i+1;j<result[i].length;j++) 
				{
					temp=result[i][j];
					result[i][j]=result[j][i];
					result[j][i]=temp;
				}
		}
		else
		{
			for(int i=0;i<matrix.length;i++)
				for(int j=0;j<matrix[i].length;j++)
					result[j][i]=matrix[i][j];
		}
		
		return result;
	}
	
	/**
	 * perform Hermitian transpose for a complex matrix
	 * @param matrix
	 * input matrix
	 * @param result
	 * space for transposed matrix, or null to allocate new space
	 * @return
	 */
	public static ComplexMatrix transpose(ComplexMatrix matrix,ComplexMatrix result)
	{
		if(result==null) result=new ComplexMatrix(matrix.numColumns(),matrix.numRows());

		transpose(matrix.real(),result.real());
		transpose(matrix.imaginary(),result.imaginary());
		scalarMultiply(-1,result.imaginary(),result.imaginary());
		
		return result;
	}
	
	/**
	 * get the complex conjugate
	 * @param matrix
	 * input complex matrix
	 * @param result
	 * space for result, null to allocate new space
	 * @return
	 */
	public static ComplexMatrix conjugate(ComplexMatrix matrix,ComplexMatrix result)
	{
	double[][] a,b,real,imag;
	
		if(result==null) result=new ComplexMatrix(matrix.numRows(),matrix.numColumns());
		else BLAS.checkSize(matrix,result);
	
		a=matrix.real();
		b=matrix.imaginary();
		real=result.real();
		imag=result.imaginary();
		
		for(int i=0;i<matrix.numRows();i++)
			for(int j=0;j<matrix.numColumns();j++) 
			{
				real[i][j]=a[i][j];
				imag[i][j]=-b[i][j];
			}
		
		return result;
	}
	
	/**
	 * get the magnitude of a complex vector
	 * @param v
	 * a complex vector
	 * @param result
	 * space for results, null to allocate new space
	 * @return
	 */
	public static double[] abs(ComplexVector v,double[] result)
	{
	double[] a,b;
	
		if(result==null) result=new double[v.size()];
		else checkDestinationSize(result,v.size());
	
		a=v.real();
		b=v.imaginary();
		
		for(int i=0;i<v.size();i++) result[i]=SPComplex.abs(a[i],b[i]);

		return result;
	}
	
	/**
	 * get the magnitude of a complex matrix
	 * @param m
	 * a complex matrix
	 * @param result
	 * space for results, null to allocate new space
	 * @return
	 */
	public static double[][] abs(ComplexMatrix m,double[][] result)
	{
	double[][] a,b;
	
		if(result==null) result=new double[m.numRows()][m.numColumns()];
		else checkDestinationSize(result,m.numRows(),m.numColumns());
	
		a=m.real();
		b=m.imaginary();
		
		for(int i=0;i<result.length;i++)
			for(int j=0;j<result[i].length;j++) 
				result[i][j]=SPComplex.abs(a[i][j],b[i][j]);
		
		return result;
	}
	
	/**
	 * generate a random matrix of uniform distribution of [0, 1]
	 * @param row
	 * row dimension
	 * @param col
	 * column dimension
	 * @return
	 */
	public static double[][] randMatrix(int row,int col)
	{
	double[][] m;
	
		m=new double[row][col];
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++) m[i][j]=Math.random();
		return m;
	}
	
	/**
	 * generate a random complex matrix of uniform distribution of [0, 1]
	 * @param row
	 * row dimension
	 * @param col
	 * column dimension
	 * @return
	 */
	public static ComplexMatrix randComplexMatrix(int row,int col)
	{
	ComplexMatrix m;
	
		m=new ComplexMatrix(row,col);

		randomize(m.real());
		randomize(m.imaginary());
		
		return m;
	}
	
	/**
	 * generate a random vector of uniform distribution of [0, 1]
	 * @param d
	 * vector dimension
	 * @return
	 */
	public static double[] randVector(int d)
	{
	double[] v;
	
		v=new double[d];
		for(int i=0;i<v.length;i++) v[i]=Math.random();
		return v;
	}
	
	/**
	 * generate a random complex vector of uniform distribution of [0, 1]
	 * @param d
	 * vector dimension
	 * @return
	 */
	public static ComplexVector randComplexVector(int d)
	{
	ComplexVector v;
	
		v=new ComplexVector(d);
		
		randomize(v.real());
		randomize(v.imaginary());

		return v;
	}
	
	/**
	 * fill the vector with [0, 1] uniformly destributed random values
	 * @param v
	 */
	public static void randomize(double[] v)
	{
		for(int i=0;i<v.length;i++) v[i]=Math.random();
	}
	
	/**
	 * fill the matrix with [0, 1] uniformly destributed random values
	 * @param m
	 */
	public static void randomize(double[][] m)
	{
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++) m[i][j]=Math.random();
	}
	
	/**
	 * perform Gram-Schmidt orthogonalization on rows of the input matrix
	 * @param matrix
	 * a matrix
	 */
	public static void orthogonalize(double[][] matrix)
	{
	double[] temp=null;
		
		for(int i=0;i<matrix.length;i++)
		{
			//orthogonalization
			for(int j=0;j<i;j++)
			{
				temp=BLAS.scalarMultiply(BLAS.innerProduct(matrix[j],matrix[i]),matrix[j],temp);
				BLAS.subtract(matrix[i],temp,matrix[i]);
			}
			//normalization
			BLAS.normalize(matrix[i]);
		}
	}
	
	/**
	 * perform Gram-Schmidt orthogonalization on rows of the input matrix
	 * @param matrix
	 * a matrix
	 */
	public static void orthogonalize(ComplexMatrix matrix)
	{
	ComplexVector temp=null,ai,aj;
	SPComplex p;
	
		for(int i=0;i<matrix.numRows();i++)
		{
			ai=matrix.row(i);
			
			//orthogonalization
			for(int j=0;j<i;j++)
			{	
				aj=matrix.row(j);
				
				//<a,b>=<b,a>*, the order is important!!!
				p=innerProduct(aj,ai);
				temp=BLAS.scalarMultiply(p.getReal(),p.getImaginary(),aj,temp);
				subtract(ai,temp,ai);
			}
			
			BLAS.normalize(ai);//normalization
		}
	}
	
	/**
	 * to see whteher two matrices are equal or not
	 * @param m1, m2
	 * two matrices
	 * @return
	 */
	public static boolean equals(int[][] m1,int[][] m2)
	{
		if(m1==null&&m2==null) return true;
		else if(m1==m2) return true;
		else if(m1==null||m2==null) return false;
		else if(m1.length!=m2.length||m1[0].length!=m2[0].length) return false;
		else
		{
			for(int i=0;i<m1.length;i++) 
				for(int j=0;j<m1[i].length;j++) 
					if(m1[i][j]!=m2[i][j]) return false;
		}
		
		return true;
	}
	
	/**
	 * solve the linear equations Ax=b
	 * @param a
	 * the matrix A, must invertable
	 * @param b
	 * the vector b
	 * @param x
	 * space for the result, null to allocate new space
	 * @return
	 * return null if the unique solution cannot be found
	 */
	public static double[] linearEquations(double[][] a,double[] b,double[] x)
	{
	RealMatrix ma;
	DecompositionSolver solver;
	RealVector vb,vx;
	
		if(x==null) x=new double[a[0].length];
		else BLAS.checkDestinationSize(x, a[0].length);
		
		ma=new Array2DRowRealMatrix(a,false);
		solver=new LUDecomposition(ma).getSolver();
		
		vb=new ArrayRealVector(b,false);
		try
		{
			vx=solver.solve(vb);
		}
		catch(SingularMatrixException e)
		{
			return null;
		}
		
		for(int i=0;i<x.length;i++) x[i]=vx.getEntry(i);
		return x;
	}
	
	/**
	 * solve the linear equations Ax=b
	 * @param a
	 * the matrix A, must invertable
	 * @param b
	 * the vector b
	 * @param x
	 * space for the result, null to allocate new space
	 * @return
	 * return null if the unique solution cannot be found
	 */
	public static ComplexVector linearEquations(ComplexMatrix a,ComplexVector b,ComplexVector x)
	{
	double[][] aa;
	double[] ab,ax=null;
	double real,imag;
	
		if(x==null) x=new ComplexVector(a.numColumns());
		else BLAS.checkDestinationSize(x, a.numColumns());
			
		/*
		 * construct [real(A) -imag(A); imag(A) real(A)]
		 */
		aa=new double[a.numRows()*2][a.numColumns()*2];
		for(int i=0;i<a.numRows();i++)
			for(int j=0;j<a.numColumns();j++)
			{
				real=a.getReal(i,j);
				imag=a.getImaginary(i,j);
				
				aa[i][j]=real;
				aa[i+a.numRows()][j+a.numColumns()]=real;
				aa[i][j+a.numColumns()]=-imag;
				aa[i+a.numRows()][j]=imag;
			}
		
		/*
		 * construct [real(b); imag(b)]
		 */
		ab=new double[b.size()*2];
		System.arraycopy(b.real(), 0, ab, 0, b.size());
		System.arraycopy(b.imaginary(), 0, ab, b.size(), b.size());
		
		//solve the linear system
		ax=BLAS.linearEquations(aa, ab, ax);
		
		if(ax==null) return null;
		else
		{
			System.arraycopy(ax, 0, x.real(), 0, x.size());
			System.arraycopy(ax, x.size(), x.imaginary(), 0, x.size());
			return x;
		}
	}
	
	/**
	 * solve the linear system Ax=b in the least square form
	 * @param a
	 * the matrix A
	 * @param b
	 * the vector b
	 * @param x
	 * space for the result, null to allocate new space
	 * @return
	 */
	public static double[] leastSquare(double[][] a,double[] b,double[] x)
	{
	RealMatrix ma;
	DecompositionSolver solver;
	RealVector vb,vx;
		
		if(x==null) x=new double[a[0].length];
		else BLAS.checkDestinationSize(x, a[0].length);
			
		ma=new Array2DRowRealMatrix(a,false);
		solver=new QRDecomposition(ma).getSolver();
			
		vb=new ArrayRealVector(b,false);
		try
		{
			vx=solver.solve(vb);
		}
		catch(SingularMatrixException e)
		{
			return null;
		}
			
		for(int i=0;i<x.length;i++) x[i]=vx.getEntry(i);
		return x;
	}
	
	/**
	 * solve the linear system Ax=b in the least square form
	 * @param a
	 * the matrix A
	 * @param b
	 * the vector b
	 * @param x
	 * space for the result, null to allocate new space
	 * @return
	 */
	public static ComplexVector leastSquare(ComplexMatrix a,ComplexVector b,ComplexVector x)
	{
	double[][] aa;
	double[] ab,ax=null;
	double real,imag;
		
		if(x==null) x=new ComplexVector(a.numColumns());
		else BLAS.checkDestinationSize(x, a.numColumns());
				
		/*
		 * construct [real(A) -imag(A); imag(A) real(A)]
		 */
		aa=new double[a.numRows()*2][a.numColumns()*2];
		for(int i=0;i<a.numRows();i++)
			for(int j=0;j<a.numColumns();j++)
			{
				real=a.getReal(i,j);
				imag=a.getImaginary(i,j);
				
				aa[i][j]=real;
				aa[i+a.numRows()][j+a.numColumns()]=real;
				aa[i][j+a.numColumns()]=-imag;
				aa[i+a.numRows()][j]=imag;
			}
			
		/*
		 * construct [real(b); imag(b)]
		 */
		ab=new double[b.size()*2];
		System.arraycopy(b.real(), 0, ab, 0, b.size());
		System.arraycopy(b.imaginary(), 0, ab, b.size(), b.size());
			
		//solve the linear system
		ax=BLAS.leastSquare(aa, ab, ax);
			
		if(ax==null) return null;
		else
		{
			System.arraycopy(ax, 0, x.real(), 0, x.size());
			System.arraycopy(ax, x.size(), x.imaginary(), 0, x.size());
			return x;
		}		
	}
	
	/**
	 * calculate the quadratic form v'*M*v
	 * @param hm
	 * the Hermitian matrix M
	 * @param v
	 * the vector v
	 * @return
	 */
	public static double quadraticForm(ComplexMatrix hm,ComplexVector v)
	{
	double[][] mr,mi;
	double[] vr,vi;
	double res=0,real,imag;
	
		if(hm.numRows()!=hm.numColumns()) throw new IllegalArgumentException(
				"square matrix is required: "+hm.numRows()+" x "+hm.numColumns());
		if(v.size()!=hm.numRows()) throw new IllegalArgumentException(
				"vector, matrix size not match: "+v.size()+", "+hm.numRows());

		mr=hm.real();
		mi=hm.imaginary();		
		vr=v.real();
		vi=v.imaginary();
		
		for(int i=0;i<mr.length;i++) 
			for(int j=i;j<mr[i].length;j++) 
			{
				if(i==j) res+=(vr[j]*vr[j]+vi[j]*vi[j])*mr[j][i];
				else 
				{
					real=vr[j]*vr[i]+vi[j]*vi[i];
					imag=vr[j]*vi[i]-vi[j]*vr[i];
					
					res+=(real*mr[j][i]-imag*mi[j][i])*2;
				}
			}
		
		return res;
	}
	
	/**
	 * Perform the Cholesky decomposition for a Hermitian positive 
	 * definite matrix A, decompose A=R^H*R, R is an upper trangular matrix. 
	 * See the paper: A. Krishnamoorthy, D. Menon, Matrix Inversion Using 
	 * Cholesky Decomposition.
	 * @param matrix
	 * a Hermitian matrix
	 * @param result
	 * space for result, null to allocate new space
	 * @return
	 * the upper trangular part of the decomposition, null if failed
	 */
	public static ComplexMatrix chol(ComplexMatrix matrix,ComplexMatrix result)
	{
	double[][] ar,ai,rr,ri;
	
		if(matrix.numRows()!=matrix.numColumns()) throw new IllegalArgumentException(
				"square matrix is required");
		if(result==null) result=new ComplexMatrix(matrix.numRows(),matrix.numColumns());
		else BLAS.checkDestinationSize(result, matrix.numRows(), matrix.numColumns());
		
		ar=matrix.real();
		ai=matrix.imaginary();
		rr=result.real();
		ri=result.imaginary();
	
		for(int i=0;i<result.numRows();i++) 
			//upper trangular part
			for(int j=i;j<result.numColumns();j++) 
			{
				if(i==j) 
				{
					rr[i][i]=ar[i][i];
					ri[i][i]=0;
					
					for(int k=0;k<i;k++) 
						rr[i][i]-=rr[k][i]*rr[k][i]+ri[k][i]*ri[k][i];
					
					if(rr[i][i]<0) return null;
					rr[i][i]=Math.sqrt(rr[i][i]);
				}
				else
				{
					rr[i][j]=ar[i][j];
					ri[i][j]=ai[i][j];
					
					for(int k=0;k<i;k++) 
					{
						rr[i][j]-=rr[k][i]*rr[k][j]+ri[k][i]*ri[k][j];
						ri[i][j]-=rr[k][i]*ri[k][j]-ri[k][i]*rr[k][j];
					}
					
					rr[i][j]/=rr[i][i];
					ri[i][j]/=rr[i][i];
					
					/*
					 * the lower trangular part is zero
					 */
					rr[j][i]=0;
					ri[j][i]=0;
				}
			}
		
		return result;
	}
	
	/**
	 * decompose a Hermitian matrix A to a lower trangular part R^H, a 
	 * diagonal part D, and a upper trangular part R. See the paper: 
	 * A. Krishnamoorthy, D. Menon, Matrix Inversion Using Cholesky 
	 * Decomposition.
	 * @param ma
	 * a Hermitian matrix
	 * @param mr
	 * space for result, null to allocate new space
	 * @return
	 * the upper trangular part, and the diagonal part in its diagonal
	 */
	public static ComplexMatrix ldl(ComplexMatrix ma,ComplexMatrix mr)
	{
	double[][] ar,ai,rr,ri;
		
		if(ma.numRows()!=ma.numColumns()) throw new IllegalArgumentException(
				"square matrix is required");
		if(mr==null) mr=new ComplexMatrix(ma.numRows(),ma.numColumns());
		else BLAS.checkDestinationSize(mr, ma.numRows(), ma.numColumns());
		
		ar=ma.real();
		ai=ma.imaginary();
		rr=mr.real();
		ri=mr.imaginary();
		
		for(int i=0;i<mr.numRows();i++) 
			//upper trangular part
			for(int j=i;j<mr.numColumns();j++) 
			{
				if(i==j) 
				{
					rr[i][i]=ar[i][i];
					ri[i][i]=0;
					
					for(int k=0;k<i;k++) 
						rr[i][i]-=(rr[k][i]*rr[k][i]+ri[k][i]*ri[k][i])*rr[k][k];
				}
				else
				{
					rr[i][j]=ar[i][j];
					ri[i][j]=ai[i][j];
					
					for(int k=0;k<i;k++) 
					{
						rr[i][j]-=(rr[k][i]*rr[k][j]+ri[k][i]*ri[k][j])*rr[k][k];
						ri[i][j]-=(rr[k][i]*ri[k][j]-ri[k][i]*rr[k][j])*rr[k][k];
					}
					
					rr[i][j]/=rr[i][i];
					ri[i][j]/=rr[i][i];
					
					/*
					 * the lower trangular part is zero
					 */
					rr[j][i]=0;
					ri[j][i]=0;
				}
			}
		
		return mr;
	}
	
	/**
	 * calculate the inverse of a Hermitian positive definite matrix, see the 
	 * paper: A. Krishnamoorthy, D. Menon, Matrix Inversion Using Cholesky 
	 * Decomposition.
	 * @param matrix
	 * a Hermitian positive definite matrix
	 * @param result
	 * space for result, null to allocate new spance
	 * @return
	 * null if failed
	 */
	public static ComplexMatrix hinv(ComplexMatrix matrix,ComplexMatrix result)
	{
	ComplexMatrix mr;
	double[][] rr,ri,xr,xi;
			
		if(result==null) result=new ComplexMatrix(matrix.numRows(),matrix.numColumns());
		else BLAS.checkDestinationSize(result, matrix.numRows(), matrix.numColumns());
		
		//perform LDL decomposition
		mr=BLAS.ldl(matrix, null);
		
		rr=mr.real();
		ri=mr.imaginary();
		xr=result.real();
		xi=result.imaginary();
			
		//perform back substitution
		for(int j=result.numColumns()-1;j>=0;j--) 
			for(int i=j;i>=0;i--) 
			{
				if(i==j) 
				{
					xr[i][i]=1.0/rr[i][i];
					xi[i][i]=0;
						
					for(int k=i+1;k<mr.numColumns();k++) 
						xr[i][j]-=rr[i][k]*xr[k][j]-ri[i][k]*xi[k][j];
				}
				else
				{
					xr[i][j]=0;
					xi[i][j]=0;
						
					for(int k=i+1;k<mr.numColumns();k++) 
					{
						xr[i][j]-=rr[i][k]*xr[k][j]-ri[i][k]*xi[k][j];
						xi[i][j]-=rr[i][k]*xi[k][j]+ri[i][k]*xr[k][j];
					}
						
					/*
					 * Hermitian
					 */
					xr[j][i]=xr[i][j];
					xi[j][i]=-xi[i][j];
				}
			}
		
		return result;		
	}
}
