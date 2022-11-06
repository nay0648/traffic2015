/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;

/**
 * Scan all elevations at a certain azimuth.
 * 
 * @author nay0648
 *
 */
public class ElevationScan implements ScanningPattern
{
private static final long serialVersionUID = 7364741334694473045L;
private double az=0;//the azimuth
/*
 * parameters for elevation angle
 */
private double el1=-90;
private double el2=90;
private int numelscan=181;
private double del=(el2-el1)/(numelscan-1);
private int elidx=0;

	/**
	 * default azimuth=0, elevation=[-90, 90];
	 */
	public ElevationScan()
	{}
	
	/**
	 * @param azimuth
	 * the specified azimuth
	 */
	public ElevationScan(double azimuth)
	{
		az=azimuth;
	}

	public SphericalPoint nextDirection() 
	{
	SphericalPoint fdir;
		
		fdir=new SphericalPoint(az,el1+elidx*del,1);
		
		elidx++;
		if(elidx>=numelscan) elidx=0;
		
		return fdir;
	}
}
