package cn.ac.ioa.hccl.atm.array;
import java.util.*;

/**
 * Select lanes randomly every time.
 * @author nay0648
 */
public class HighwayAzimuthScan implements ScanningPattern
{
private static final long serialVersionUID = 2162245375143533828L;
/*
 * lane positions
 */
private double b11=-17,b12=0,c1=-7.9;
private double b21=0,b22=12.1,c2=6.6;
private double b31=16.9,b32=23.8,c3=20.6;
private double b41=23.8,b42=28.7,c4=26.5;
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
//private int laneidx=0;

private Random random=new Random();

	public SphericalPoint nextDirection() 
	{
	int laneidx;
	double mu=c1,sigma=sigma1;
	
		laneidx=(int)(random.nextDouble()*4);
//		System.out.println(laneidx);
		
		switch(laneidx)
		{
			case 0: 
			{
				mu=c1;
				sigma=sigma1;
			}break;
			case 1: 
			{
				mu=c2;
				sigma=sigma2;
			}break;
			case 2: 
			{
				mu=c3;
				sigma=sigma3;
			}break;
			case 3: 
			{
				mu=c4;
				sigma=sigma4;
			}break;
		}
		
//		laneidx++;
//		if(laneidx>=4) laneidx=0;
		
		return new SphericalPoint(random.nextGaussian()*sigma+mu,0,1);
	}
}
