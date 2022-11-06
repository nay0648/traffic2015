package cn.ac.ioa.hccl.atm.event;
import cn.ac.ioa.hccl.atm.lane.*;

/**
 * Lane detected event.
 * @author nay0648
 */
public class LaneDetectionEvent
{
private LaneDetector detector;//the detector reference

	/**
	 * @param detector
	 * the detector reference
	 */
	public LaneDetectionEvent(LaneDetector detector)
	{
		this.detector=detector;
	}
	
	/**
	 * get the number of lanes
	 * @return
	 */
	public int numLanes()
	{
		return detector.numLanes();
	}
	
	/**
	 * get lane position
	 * @param index
	 * lane index
	 * @return
	 * null if the lane is not detected
	 */
	public LaneDetector.LanePosition lanePosition(int index)
	{
		return detector.lanePosition(index);
	}
}
