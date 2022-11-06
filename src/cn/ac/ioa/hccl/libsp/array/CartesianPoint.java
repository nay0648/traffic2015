/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;
import java.io.*;

/**
 * A 3D point in Cartesian coordinate system.
 * @author nay0648
 */
public class CartesianPoint implements Serializable
{
private static final long serialVersionUID = -9090961280711694820L;
private double x,y,z;//x, y, z coordinate

	/**
	 * @param x
	 * x coordinate
	 * @param y
	 * y coordinate
	 * @param z
	 * z coordinate
	 */
	public CartesianPoint(double x,double y,double z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	/**
	 * construct a point in Cartesian coordinate system with z=0
	 * @param x
	 * x coordinate
	 * @param y
	 * y coordinate
	 */
	public CartesianPoint(double x,double y)
	{
		this(x,y,0);
	}
	
	/**
	 * make a copy
	 * @param another
	 * another point
	 */
	public CartesianPoint(CartesianPoint another)
	{
		x=another.x;
		y=another.y;
		z=another.z;
	}
	
	/**
	 * convert from spherical coordinate to Cartesian coordinate
	 * @param p
	 * a point in spherical coordinate system
	 */
	public CartesianPoint(SphericalPoint p)
	{
	double azrad,elrad,d;
	
		azrad=p.getAzimuth()*Math.PI/180;
		elrad=p.getElevation()*Math.PI/180;
		d=p.getDistance();
		
		setX(d*Math.cos(elrad)*Math.cos(azrad));
		setY(d*Math.cos(elrad)*Math.sin(azrad));
		setZ(d*Math.sin(elrad));
	}
	
	/**
	 * get x coordinate
	 * @return
	 */
	public double getX()
	{
		return x;
	}
	
	/**
	 * set x coordinate
	 * @param x
	 */
	public void setX(double x)
	{
		this.x=x;
	}
	
	/**
	 * get y coordinate
	 * @return
	 */
	public double getY()
	{
		return y;
	}
	
	/**
	 * set y coordinate
	 * @param y
	 */
	public void setY(double y)
	{
		this.y=y;
	}
	
	/**
	 * get z coordinate
	 * @return
	 */
	public double getZ()
	{
		return z;
	}
	
	/**
	 * set z coordinate
	 * @param z
	 */
	public void setZ(double z)
	{
		this.z=z;
	}
	
	public String toString()
	{
		return "Cartesian point: ("+x+", "+y+", "+z+")";
	}
	
	public boolean equals(Object o)
	{
	CartesianPoint p2;
	
		if(o==null) return false;
		else if(this==o) return true;
		
		if(!(o instanceof CartesianPoint)) return false;
		p2=(CartesianPoint)o;
		return x==p2.x&&y==p2.y&&z==p2.z;
	}
	
	public static void main(String[] args)
	{
		System.out.println(new CartesianPoint(new SphericalPoint(0,0,1)));
		System.out.println(new CartesianPoint(new SphericalPoint(180,0,1)));
		System.out.println(new CartesianPoint(new SphericalPoint(-180,0,1)));
		System.out.println(new CartesianPoint(new SphericalPoint(180,90,1)));
		System.out.println(new CartesianPoint(new SphericalPoint(-180,-90,1)));
	}
}
