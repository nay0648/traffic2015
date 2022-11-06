package cn.ac.ioa.hccl.atm.event;

/**
 * Classes to be noticed when the lanes are detected.
 * @author nay0648
 */
public interface LaneDetectionListener
{
	/**
	 * this method will be called when the lanes are detected
	 * @param e
	 */
	public void laneDetected(LaneDetectionEvent e);
}
