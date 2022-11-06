/**
 * Created on: 2015Äê2ÔÂ27ÈÕ
 */
package cn.ac.ioa.hccl.libsp.beam;
import java.io.*;
import cn.ac.ioa.hccl.libsp.array.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Different beamformer design policies. The array output is assumed to be 
 * unit vector.
 * 
 * @author nay0648
 */
public class BeamformerDesigner implements Serializable
{
private static final long serialVersionUID = -557751787287178502L;
private SensorArray array;//array reference
private double f;//working frequency
private double c;//wave propagation speed

	/**
	 * @param array
	 * sensor array reference
	 * @param f
	 * working frequency (Hz)
	 * @param c
	 * wave propagation speed (m/s)
	 */
	public BeamformerDesigner(SensorArray array,double f,double c)
	{
		this.array=array;
		this.f=f;
		this.c=c;
	}
	
	/**
	 * get sensor array reference
	 * @return
	 */
	public SensorArray sensorArray()
	{
		return array;
	}
	
	/**
	 * get array working frequency (Hz)
	 * @return
	 */
	public double workingFrequency()
	{
		return f;
	}
	
	/**
	 * get wave propagation speed (m/s)
	 * @return
	 */
	public double waveSpeed()
	{
		return c;
	}
	
	/**
	 * generate scanning directions via azimuth viration (elevation=0)
	 * @param az1
	 * starting azimuth (degree)
	 * @param az2
	 * ending azimuth (degree)
	 * @param n
	 * number of scanning directions
	 * @return
	 */
	public static SphericalPoint[] azimuthScan(double az1,double az2,int n)
	{
	SphericalPoint[] scan;
	double delta;
	
		scan=new SphericalPoint[n];
		delta=(az2-az1)/(n-1);
			
		for(int i=0;i<scan.length;i++) scan[i]=new SphericalPoint(az1+delta*i,0,1);
		scan[scan.length-1].setAzimuth(az2);
		
		return scan;
	}
	
	/**
	 * generate scanning directions via elevation viration (azimuth=0)
	 * @param el1
	 * starting elevation (degree)
	 * @param el2
	 * ending elevation (degree)
	 * @param n
	 * number of scanning directions
	 * @return
	 */
	public static SphericalPoint[] elevationScan(double el1,double el2,int n)
	{
	SphericalPoint[] scan;
	double delta;
		
		scan=new SphericalPoint[n];
		delta=(el2-el1)/(n-1);
				
		for(int i=0;i<scan.length;i++) scan[i]=new SphericalPoint(0,el1+delta*i,1);
		scan[scan.length-1].setElevation(el2);
				
		return scan;		
	}
	
	/**
	 * scan in both azimuth and elevation directions
	 * @param az1
	 * the starting azimuth angle (degree)
	 * @param az2
	 * the ending azimuth angle (degree)
	 * @param numaz
	 * number of scans in azimuth direction
	 * @param el1
	 * the starting elevation angle (degree)
	 * @param el2
	 * the ending elevation angle (degree)
	 * @param numel
	 * number of scans in elevation direction
	 * @return
	 */
	public static SphericalPoint[] azelScan(
			double az1, double az2, int numaz, double el1, double el2, int numel)
	{
	SphericalPoint[] scan;
	double daz,del,az,el;
	int idx=0;
		
		scan=new SphericalPoint[numaz*numel];
		daz=(az2-az1)/(numaz-1);
		del=(el2-el1)/(numel-1);
				
		for(int elidx=0;elidx<numel;elidx++) 
		{
			if(elidx==numel-1) el=el2;
			else el=el1+del*elidx;
			
			for(int azidx=0;azidx<numaz;azidx++) 
			{
				if(azidx==numaz-1) az=az2;
				else az=az1+daz*azidx;
				
				scan[idx++]=new SphericalPoint(az,el,1);
			}
		}
		
		return scan;
	}
	
	/**
	 * calculate the beam pattern
	 * @param w
	 * the beamformer
	 * @param scan
	 * scanning direction
	 * @return
	 * the energy spectrum of the array response
	 */
	public double[] beamPattern(ComplexVector w,SphericalPoint[] scan)
	{
	double[] res;
	ComplexVector sv;
	
		res=new double[scan.length];
		for(int i=0;i<res.length;i++) 
		{
			sv=array.steeringVector(scan[i], f, c);
			BLAS.normalize(sv);
			res[i]=BLAS.innerProductAbsSquare(w, sv);
		}
		
		return res;
	}
	
	/**
	 * delay-and-sum beamformer
	 * @param focaldir
	 * looking direction
	 * @return
	 */
	public ComplexVector delayAndSumBeamformer(SphericalPoint focaldir)
	{
	ComplexVector w;
	
		w=array.steeringVector(focaldir, f, c);
		BLAS.normalize(w);
		
		return w;
	}
	
	/**
	 * design beamformer by least square criteria
	 * @param scan
	 * scanning directions
	 * @param dres
	 * desired responses corresponding to the scanning directions
	 * @return
	 * return null if failed to generate the beformer
	 */
	public ComplexVector leastSquareBeamformer(SphericalPoint[] scan,double[] dres)
	{
	ComplexMatrix ma;
	ComplexVector sv,w=null,row,cdres;
	double temp,nm=0;
	
		if(scan.length!=dres.length) throw new IllegalArgumentException(
				"number of scanning directions and desired responses not match: "+
				scan.length+", "+dres.length);
		
		/*
		 * each row is an Hermitian transposed array input
		 */
		ma=new ComplexMatrix(scan.length,array.numElements());
		
		for(int i=0;i<scan.length;i++) 
		{
			sv=array.steeringVector(scan[i], f, c);
			BLAS.normalize(sv);
			//conjugate
			BLAS.scalarMultiply(-1.0, sv.imaginary(), sv.imaginary());
			ma.setRow(i, sv);
		}
		
		cdres=new ComplexVector(dres);//the desired response
		
		//solve the least square problem
		w=BLAS.leastSquare(ma, cdres, w);
		if(w==null) return null;
		
		/*
		 * normalization
		 */
		for(int i=0;i<ma.numRows();i++) 
		{
			row=ma.row(i);
			//has already conjugated at last time
			BLAS.scalarMultiply(-1.0, row.imaginary(), row.imaginary());
			temp=BLAS.innerProductAbsSquare(row, w);
			
			if(temp>nm) nm=temp;
		}
		
		BLAS.scalarMultiply(1.0/Math.sqrt(nm), w, w);

		return w;
	}
}
