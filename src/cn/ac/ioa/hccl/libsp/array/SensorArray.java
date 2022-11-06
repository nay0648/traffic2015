/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;
import java.io.*;
import java.util.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Used to manage sensor elements.
 * @author nay0648
 *
 */
public class SensorArray implements Serializable
{
private static final long serialVersionUID = 664379357164043377L;
private List<CartesianPoint> locl=new ArrayList<CartesianPoint>(60);//element location

	/**
	 * construct an empty sensor array
	 */
	public SensorArray()
	{}
	
	/**
	 * load array element location from file, each row is a xyz coordinate
	 * @param locpath
	 * location file path
	 * @throws IOException
	 */
	public SensorArray(File locpath) throws IOException
	{
	double[][] loc;
	
		loc=BLAS.loadDoubleMatrix(locpath);
		for(int m=0;m<loc.length;m++) addElement(
				new CartesianPoint(loc[m][0],loc[m][1],loc[m][2]));
	}

	/**
	 * get the number of array elements
	 * @return
	 */
	public int numElements()
	{
		return locl.size();
	}
	
	/**
	 * get array element location
	 * @param index
	 * array element index
	 * @return
	 */
	public CartesianPoint elementLocation(int index)
	{
		return locl.get(index);
	}
	
	/**
	 * add an array element
	 * @param loc
	 * element location
	 */
	public void addElement(CartesianPoint loc)
	{
		locl.add(loc);
	}
	
	/**
	 * get a subarray of this array
	 * @param indices
	 * element indices for the subarray
	 * @return
	 */
	public SensorArray subarray(int... indices)
	{
	SensorArray subarray;
	
		subarray=new SensorArray();
		for(int index:indices) subarray.addElement(elementLocation(index));
		return subarray;
	}
	
	/**
	 * Get the time delay of the recerived wave at a specified array 
	 * element corresponding to the phase center. The phase center has 
	 * zero time delay.
	 * @param index
	 * element index
	 * @param focaldir
	 * focal direction
	 * @param c
	 * wave speed (m/s)
	 * @return
	 * negative delay means anticipated time
	 */
	public double timeDelay(int index,SphericalPoint focaldir,double c)
	{
	CartesianPoint loc,fdir;
	
		loc=elementLocation(index);
		
		/*
		 * focal direction in Cartesian coordinate
		 */
		focaldir.setDistance(1);
		fdir=new CartesianPoint(focaldir);

		/*
		 * the (negative) inner product is the differential distance 
		 * from the phase center to the array element
		 */
		return -(loc.getX()*fdir.getX()+loc.getY()*fdir.getY()+loc.getZ()*fdir.getZ())/c;
	}
	
	/**
	 * get the steering vector of the sensor array
	 * @param focaldir
	 * forcal direction of the array
	 * @param f
	 * frequency (Hz)
	 * @param c
	 * wave speed (m/s)
	 * @return
	 */
	public ComplexVector steeringVector(SphericalPoint focaldir,double f,double c)
	{
	ComplexVector sv;
	double temp;
	
		sv=new ComplexVector(numElements());
		for(int m=0;m<numElements();m++) 
		{
			temp=2*Math.PI*f*timeDelay(m, focaldir, c);
			sv.setValue(m, Math.cos(temp), -Math.sin(temp));	
		}
		
		return sv;
	}
	
	public String toString()
	{
	StringBuilder s;
	
		s=new StringBuilder();
		
		for(CartesianPoint loc:locl) 
			s.append(loc.getX()+" "+loc.getY()+" "+loc.getZ()+"\n");
		
		return s.toString();
	}
	
	public static void main(String[] args) throws IOException
	{
	SensorArray array;
	
		array=new SensorArray(new File("arraygeometry/ourula.txt"));
		
		for(int m=0;m<array.numElements();m++) 
			System.out.println(array.timeDelay(m, new SphericalPoint(90,0,1), 340));
		
		System.out.println(array.steeringVector(
				new SphericalPoint(45,10,1), 8000, 340));
	}
}
