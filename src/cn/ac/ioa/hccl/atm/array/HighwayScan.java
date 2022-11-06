package cn.ac.ioa.hccl.atm.array;
import java.util.*;

/**
 * Highway traffic flow simulation (four lanes, two uproad and two downroad).
 * @author nay0648
 */
public class HighwayScan implements ScanningPattern
{
private static final long serialVersionUID = 3693736876142232497L;
/*
 * lane positions
 */
private double b11=-17,b12=0,c1=-7.9;
private double b21=0,b22=12.1,c2=6.6;
private double b31=19,b32=25.3,c3=22.4;
private double b41=25.3,b42=29.8,c4=27.7;
/*
 * standard deviations
 */
private double sigma1=(b12-b11)/6;
private double sigma2=(b22-b21)/6;
private double sigma3=(b32-b31)/6;
private double sigma4=(b42-b41)/6;
//private double sigma1=0;
//private double sigma2=0;
//private double sigma3=0;
//private double sigma4=0;

private Random random=new Random();
private GaussianScan gscan=null;

	public SphericalPoint nextDirection() 
	{
	SphericalPoint next=null;
		
		if(gscan!=null) next=gscan.nextDirection();
		
		if(gscan==null||next==null)
		{
			gscan=selectLane();
			next=gscan.nextDirection();
		}
		
		return next;		
	}
	
	/**
	 * randomly select a lane
	 * @return
	 */
	private GaussianScan selectLane()
	{
	int laneidx;
	
		laneidx=(int)(random.nextDouble()*4);
//		System.out.println(laneidx);
		
		switch(laneidx)
		{
			case 0: return new GaussianScan(c1,sigma1,false);
			case 1: return new GaussianScan(c2,sigma2,false);
			case 2: return new GaussianScan(c3,sigma3,true);
			case 3: return new GaussianScan(c4,sigma4,true);
//			case 2: return new GaussianScan(c1,sigma1,false);
//			case 3: return new GaussianScan(c2,sigma2,false);
			default: return null;
		}
	}
	
	/**
	 * Simulates a vehicle in a lane.
	 * @author nay0648
	 */
	private class GaussianScan implements ScanningPattern
	{
	private static final long serialVersionUID = -5893319036647693331L;
	/*
	 * parameters for elevation angle
	 */
	private double el1=-90;
	private double el2=90;
	private int numelscan=181;
	private double del=(el2-el1)/(numelscan-1);
	private int elidx=0;
	private double az;//the azimuth
	private boolean reverse;
	
		/**
		 * @param mu
		 * mean
		 * @param sigma
		 * standard deviation
		 * @param reverse
		 * true to simulate downroad vehicle
		 */
		public GaussianScan(double mu,double sigma,boolean reverse)
		{
			az=random.nextGaussian()*sigma+mu;
			if(az<-90) az=-90;
			else if(az>90) az=90;
			
			this.reverse=reverse;
		}
	
		public SphericalPoint nextDirection() 
		{
			if(++elidx>numelscan) return null;
			if(reverse) return new SphericalPoint(az,el2-(elidx-1)*del,1);
			else return new SphericalPoint(az,el1+(elidx-1)*del,1);
		}
	}
	
	public static void main(String[] args)
	{
	HighwayScan scan;
		
		scan=new HighwayScan();
		for(int i=0;i<10000;i++) 
			System.out.println(scan.nextDirection());
	}
}
