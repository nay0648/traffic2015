/**
 * Created on: 2015Äê4ÔÂ21ÈÕ
 */
package cn.ac.ioa.hccl.atm.vehicle;

/**
 * Select the threshold by averaging.
 * 
 * @author nay0648
 */
public class AvgSensitivity implements ZoneSensitivity
{
private static final long serialVersionUID = 6963378695666020473L;
private double weight=0.001;//weight for new value
private double meanth=0.75;//the coefficient for the threshold
private static double mean=0;//the mean value
	
	public double sensitivity() 
	{
		return mean/meanth;
	}
	
	public boolean isActive(double response) 
	{
		mean=mean*(1-weight)+response*weight;
		
		if(response>=mean/meanth) return true;
		else return false;
	}
}
