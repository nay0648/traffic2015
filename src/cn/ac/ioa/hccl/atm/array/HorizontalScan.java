package cn.ac.ioa.hccl.atm.array;

/**
 * Used to test the sensor array.
 * @author nay0648
 *
 */
public class HorizontalScan implements ScanningPattern
{
private static final long serialVersionUID = -5763096645425318149L;
private double az1=-90;
private double az2=90;
private double daz;//delta azimuth per scan
private int numscan=181;//number of directions required
private int scanidx=0;
	
	public HorizontalScan()
	{
		daz=(az2-az1)/(numscan-1);
	}
	
	public SphericalPoint nextDirection() 
	{
	SphericalPoint fdir;
		
		fdir=new SphericalPoint(az1+daz*scanidx++,0,1);

		if(scanidx>=numscan) scanidx=0;
		return fdir;
	}
}
