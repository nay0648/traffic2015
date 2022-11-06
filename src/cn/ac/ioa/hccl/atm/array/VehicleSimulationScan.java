package cn.ac.ioa.hccl.atm.array;

/**
 * Simulats vehicle passing through the monitor from different azimuth angle.
 * @author nay0648
 *
 */
public class VehicleSimulationScan implements ScanningPattern
{
private static final long serialVersionUID = 8929700947049994650L;
/*
 * parameters for azimuth angle
 */
private double az1=-90;
private double az2=90;
private int numazscan=19;
private double daz=(az2-az1)/(numazscan-1);
private int azidx=0;
/*
 * parameters for elevation angle
 */
private double el1=-90;
private double el2=90;
private int numelscan=181;
private double del=(el2-el1)/(numelscan-1);
private int elidx=0;

	public SphericalPoint nextDirection() 
	{
	SphericalPoint fdir;
	
		fdir=new SphericalPoint(az1+azidx*daz,el1+elidx*del,1);
		
		elidx++;
		if(elidx>=numelscan) 
		{
			elidx=0;
			
			azidx++;
			if(azidx>=numazscan) azidx=0;
		}
		
		return fdir;
	}
	
	public static void main(String[] args)
	{
	VehicleSimulationScan scan;
	
		scan=new VehicleSimulationScan();
		for(int i=0;i<1000;i++) System.out.println(scan.nextDirection());
	}
}
