package cn.ac.ioa.hccl.atm.preprocess;
import java.io.*;
import cn.ac.ioa.hccl.libsp.sio.*;

/**
 * Convert uff to multichannel txt.
 * @author nay0648
 */
public class Uff2Txt
{
	public static void main(String[] args) throws IOException
	{
		args=new String[]{
				"D:/nay0648/data/research/beamforming/dataset/2014-11-24"
		};
		UFFSource.rearrangeUFF(new File(args[0]));
	}
}
