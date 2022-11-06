/**
 * Created on: 2015Äê1ÔÂ6ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;
import java.util.*;
import cn.ac.ioa.hccl.atm.vehicle.Vehicle.Type;

/**
 * Perform vehicle classification by energy.
 * 
 * @author nay0648
 */
public class EnergyVehicleClassifier implements VehicleClassifier
{
private static final long serialVersionUID = 6686495053427300420L;
private int count=0;//vehicle count
private double sume=0;//energy sum
private double sumvar=0;//variance sum
	
	public Type type(Vehicle v) 
	{
	List<Double> rawfeature;
	double e=0,temp;
	double mean,stddev;
		
		/*
		 * calculate average energy in dB
		 */
		rawfeature=v.rawFeature();
		for(Double d:rawfeature) e+=d;
		e=10*Math.log10(e/rawfeature.size());
		
		/*
		 * accumulate parameters
		 */
		count++;
		sume+=e;
		mean=sume/count;
		
		temp=e-mean;
		sumvar+=temp*temp;
		stddev=Math.sqrt(sumvar/count);
		
		//perform classification by energy
		if(e>mean+stddev*3) return Type.large;
		else return Type.small;
	}
}
