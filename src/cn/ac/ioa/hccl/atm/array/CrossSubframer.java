package cn.ac.ioa.hccl.atm.array;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.libsp.util.*;

/**
 * Generate Mills cross array's horizontal subarray data.
 * @author nay0648
 */
public class CrossSubframer extends Subframer
{
private static final long serialVersionUID = 7847611598314726670L;
private int[] hidx;//horizontal subarray indices
private int[] vidx;//vertical subarray indices
private ComplexVector w=null;//the beamformer
private ComplexVector x,vx;//array data

	/**
	 * @param monitor
	 * monitor reference
	 * @param fi
	 * work frequency bin index
	 * @param elevation
	 * elevation angle (degree)
	 */
	public CrossSubframer(CrossMonitor monitor,int fi,double elevation)
	{
		super(monitor,fi);
		
		/*
		 * subarray indices
		 */
		hidx=monitor.horizontalSubarrayIndices();
		vidx=monitor.verticalSubarrayIndices();
		
		/*
		 * beamformer for vertical subarray
		 */
		Beamformer bfdesign=new Beamformer(
				monitor.subarray(vidx),
				monitor.binIndex2Frequency(fi),
				Param.soundSpeed());
		w=bfdesign.delayAndSumBeamformer(new SphericalPoint(0,elevation,1));
			
		//apply a window
		BLAS.entryMultiply(Window.cheb(w.size()), w, w);
	}

	public SensorArray subarray() 
	{
		return this.monitorArray().subarray(hidx);
	}

	public ComplexVector arrayFrame(ComplexVector arrayin) throws IOException 
	{
	double[] a,b,c,d;
	double real=0,imag=0;
	
		if(arrayin==null) arrayin=new ComplexVector(this.subframeSize());
		else BLAS.checkDestinationSize(arrayin, this.subframeSize());
	
		//read original data
		x=this.monitorArray().arrayFrame(this.workingFrequencyBin(), x);
	
		/*
		 * prepare subarray data
		 */
		//horizontal subarray
		arrayin=x.subvector(arrayin, hidx);
		//vertical subarray
		vx=x.subvector(vx, vidx);
		
		/*
		 * apply vertical beamformer
		 */
		c=vx.real();
		d=vx.imaginary();
		a=w.real();
		b=w.imaginary();
			
		for(int i=0;i<vx.size();i++) 
		{
			real+=a[i]*c[i]+b[i]*d[i];
			imag+=a[i]*d[i]-b[i]*c[i];
		}
		
		//generate new subarray data
		BLAS.scalarMultiply(real, -imag, arrayin, arrayin);
		return arrayin;
	}
}
