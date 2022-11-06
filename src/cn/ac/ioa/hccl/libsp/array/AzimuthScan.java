/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.array;

/**
 * Scan all azimuth angles [-180, 180] at a certain elevation.
 * 
 * @author nay0648
 */
public class AzimuthScan implements ScanningPattern
{
private static final long serialVersionUID = -654375303866217786L;
private double el=0;//the elevation angle
/*
 * parameters for azimuth angle
 */
private double az1=-180;
private double az2=180;
private int numelscan=361;
private double daz=(az2-az1)/(numelscan-1);
private int azidx=0;

	/**
	 * default azimuth=[-180, 180], elevation=0;
	 */
	public AzimuthScan()
	{}
	
	/**
	 * @param elevation
	 * the specified elevation
	 */
	public AzimuthScan(double elevation)
	{
		el=elevation;
	}

	public SphericalPoint nextDirection() 
	{
	SphericalPoint fdir;
		
		fdir=new SphericalPoint(az1+azidx*daz,el,1);
		
		azidx++;
		if(azidx>=numelscan) azidx=0;
		
		return fdir;
	}
	
	public static void main(String[] args)
	{
	AzimuthScan azscan;
	
		azscan=new AzimuthScan();
		for(int i=0;i<800;i++) System.out.println(azscan.nextDirection());
	}
}
