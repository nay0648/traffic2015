/**
 * Created on: 2015Äê3ÔÂ10ÈÕ
 */
package cn.ac.ioa.hccl.libsp.agc;
import java.io.*;

/**
 * Used to control the amplitude of signals.
 * 
 * @author nay0648
 */
public abstract class AutomaticGainControl implements Serializable
{
private static final long serialVersionUID = 4765054475783904493L;

	/**
	 * normalize the value to the specified interval
	 * @param s
	 * a sample
	 * @return
	 */
	public abstract double normalize(double s);
	
	/**
	 * normalize a single channel
	 * @param s
	 * signals
	 * @param offset
	 * AGC starting offset
	 * @param len
	 * number of samples need to be normalized
	 */
	public void normalize(double[] s, int offset, int len)
	{
		for(int t=offset;t<offset+len;t++) s[t]=normalize(s[t]);
	}
	
	/**
	 * normalize multichannel signals
	 * @param s
	 * each row is a channel
	 * @param offset
	 * AGC starting offset
	 * @param len
	 * number of frames need to be normalized
	 */
	public void normalize(double[][] s, int offset, int len)
	{
		for(int m=0;m<s.length;m++) normalize(s[m], offset, len);
	}
	
	/**
	 * normalize single channel signal
	 * @param s
	 * a single channel signal
	 * @return
	 */
	public void normalize(double[] s)
	{
		normalize(s, 0, s.length);
	}
	
	/**
	 * normalize multichannel signals
	 * @param s
	 * each row is a channel
	 */
	public void normalize(double[][] s)
	{
		normalize(s, 0, s[0].length);
	}
}
