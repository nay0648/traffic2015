package cn.ac.ioa.hccl.atm;
import java.io.*;

/**
 * Common parameters.
 * 
 * @author nay0648
 *
 */
public class Param implements Serializable
{
private static final long serialVersionUID = 4361893090888547271L;
/*
 * global parameters
 */
/**
 * default array geometory
 */
public static final File DEFARRAYGEOM=new File("arraygeometry/crossarray16k.txt");
/**
 * default sampling rate (Hz)
 */
public static final double DEFFS=32000;
/**
 * frame shift
 */
public static final int FRAMESHIFT=512;
/**
 * FFT size
 */
public static final int FFTSIZE=1024;
/**
 * detection zone elevation (degree)
 */
public static final double ZONE_ELEVATION=8;
/**
 * number of lanes
 */
public static final int NUM_LANES=4;

/*
 * parameters for correlator
 */
/**
 * sliding time window size (ms)
 */
public static final int CORR_TIME_WINDOW=500;
/**
 * lower boundary of expected working frequency (Hz)
 */
public static final double CORR_LOWER_FEXP=13500;
/**
 * upper boundary of expected working frequency (Hz)
 */
public static final double CORR_UPPER_FEXP=14000;

/*
 * parameters for adaptive gain control
 */
/**
 * local time interval (ms)
 */
public static final long AGC_LOCALINTERVAL=2000L;
/**
 * global time interval (ms)
 */
public static final long AGC_GLOBALINTERVAL=600000L;
/**
 * heap size
 */
public static final int AGC_HEAP_SIZE=400;
/**
 * number of vehicle to update the AGC threshold
 */
public static final int AGC_UPDATE_THRESHOLD=100;

/*
 * parameters for lane detection
 */
/**
 * lower boundary of the scanning azimuth (degree)
 */
public static final double LANE_SCANAZ1=-90;
/**
 * upper boundary of the scanning azimuth (degree)
 */
public static final double LANE_SCANAZ2=90;
/**
 * number of scans
 */
public static final int LANE_NUMSCANS=361;
/**
 * valid sliced response threshold for lane detection
 */
public static final double LANE_SENSITIVITY=0.5;
/**
 * number of available frames per detect
 */
public static final int LANE_NUMFRAMES_PER_DETECT=50;
/**
 * max lane radius in degree
 */
public static final double LANE_MAX_RADIUS_DEGREE=9;

/*
 * parameters for vehicle detection
 */
/**
 * threshold to activate a detection zone
 */
public static final double VEHICLE_SENSITIVITY=0.075;
/**
 * the smooth filter order
 */
public static final int VEHICLE_POST_SMOOTHER_ORDER=10;

/*
 * parameters for GUI
 */
/**
 * buffered time window size for visualization (ms)
 */
public static final long GUI_TIMEWINDOWSIZE=5000L;

	/**
	 * sound propagation speed (m/s)
	 * @return
	 */
	public static double soundSpeed()
	{
		return 340;
	}
}
