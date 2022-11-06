/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;
import java.io.*;

/**
 * A 3D point in spherical coordinate system.
 * @author nay0648
 *
 */
public class SphericalPoint implements Serializable
{
private static final long serialVersionUID = -5220159515948223642L;
private double az;//azimuth angle in degree
private double el;//elevation angle in degree
private double d;//distance

	/**
	 * @param az
	 * azimuth angle in degree
	 * @param el
	 * elevation angle in degree
	 * @param d
	 * distance
	 */
	public SphericalPoint(double az,double el,double d)
	{
		setAzimuth(az);
		setElevation(el);
		setDistance(d);
	}
	
	/**
	 * convert a point in Cartesian coordinate system in spherical coordinate system
	 * @param p
	 * a point in Cartesian coordinate system
	 */
	public SphericalPoint(CartesianPoint p)
	{
	double x,y,z;
	
		x=p.getX();
		y=p.getY();
		z=p.getZ();

		setAzimuth(180*Math.atan2(y,x)/Math.PI);
		setElevation(180*Math.atan2(z,Math.sqrt(x*x+y*y))/Math.PI);		
		setDistance(Math.sqrt(x*x+y*y+z*z));
	}
	
	/**
	 * get azimuth angle in degree
	 * @return
	 */
	public double getAzimuth()
	{
		return az;
	}
	
	/**
	 * set azimuth angle
	 * @param az
	 * in degree
	 */
	public void setAzimuth(double az)
	{
		if(az<-180||az>180) throw new IllegalArgumentException(
				"illegal azimuth angle: "+az+", must in [-180, 180]");
		this.az=az;
	}
	
	/**
	 * get elevation angle in degree
	 * @return
	 */
	public double getElevation()
	{
		return el;
	}
	
	/**
	 * set elevation angle
	 * @param el
	 * in degree
	 */
	public void setElevation(double el)
	{
		if(el<-90||el>90) throw new IllegalArgumentException(
				"illegal elevation angle: "+el+", must in [-90, 90]");
		this.el=el;
	}
	
	/**
	 * get distance
	 * @return
	 */
	public double getDistance()
	{
		return d;
	}
	
	/**
	 * set distance
	 * @param d
	 */
	public void setDistance(double d)
	{
		if(d<0) throw new IllegalArgumentException(
				"illegal distance: "+d+", must larger than 0");
		this.d=d;
	}
	
	public String toString()
	{
		return "Spherical point: ("+az+", "+el+", "+d+")";
	}
	
	public boolean equals(Object o)
	{
	SphericalPoint p2;
	
		if(o==null) return false;
		else if(this==o) return true;
		
		if(!(o instanceof SphericalPoint)) return false;
		p2=(SphericalPoint)o;
		return az==p2.az&&el==p2.el&&d==p2.d;
	}
	
	public static void main(String[] args)
	{
		System.out.println(new SphericalPoint(new CartesianPoint(1,0,0)));
		System.out.println(new SphericalPoint(new CartesianPoint(-1,0,0)));
		System.out.println(new SphericalPoint(new CartesianPoint(0,0,1)));
		System.out.println(new SphericalPoint(new CartesianPoint(0,0,-1)));
	}
}
