package cn.ac.ioa.hccl.atm.agc;
import java.io.*;
import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.array.*;

/**
 * Performing gain control by heap structure.
 * @author nay0648
 */
public class HeapGainControl extends AutomaticGainControl
{
private static final long serialVersionUID = -5455454944034070921L;
private MonitorArray monitor;//monitor reference
private long localms;//local interval size (ms)
private long globalms;//global interval size(ms)
private MaxValue localmax;//local max value
private int localreached=0;//number of added local max
private long locallimit;//time limit to find a new local max
private MaxValue[] gheap;//the heap to keep the maximums in the global interval
private double agcth=0;//the threshold for agc
private double globalmax=0;//the temp threshold used before the median threshold is found

	/**
	 * use default parameters
	 * @param monitor
	 * array reference
	 */
	public HeapGainControl(MonitorArray monitor)
	{
		this(monitor,Param.AGC_LOCALINTERVAL,Param.AGC_GLOBALINTERVAL,Param.AGC_HEAP_SIZE);
	}

	/**
	 * @param monitor
	 * array reference
	 * @param localms
	 * local interval size (ms)
	 * @param globalms
	 * global interval size (ms)
	 * @param heapsize
	 * heap size
	 */
	public HeapGainControl(MonitorArray monitor,long localms,long globalms,int heapsize)
	{
		this.monitor=monitor;
		
		localmax=new MaxValue(0,0);
		locallimit=monitor.frameTime()+localms;
		
		this.localms=localms;
		this.globalms=globalms;
		
		gheap=new MaxValue[heapsize];
		for(int i=0;i<gheap.length;i++) gheap[i]=new MaxValue(0,0);
	}
	
	/**
	 * adjust the heap array to form the heap structure
	 * @param heap
	 * the heap
	 * @param len
	 * heap length
	 * @param index
	 * root node index
	 */
	private void adjustHeap(MaxValue[] heap,int len,int index)
	{
	int lchidx,rchidx;
	MaxValue cv,lv=null,rv=null,swap;
		
		rchidx=(index+1)*2;
		if(rchidx<len) rv=heap[rchidx];
		lchidx=rchidx-1;
		if(lchidx<len) lv=heap[lchidx];

		//this is a leaf node
		if(lv==null&&rv==null) return;
		
		cv=heap[index];
		//only have left child
		if(rv==null) 
		{
			if(lv.value<cv.value) 
			{
				swap=heap[index];
				heap[index]=heap[lchidx];
				heap[lchidx]=swap;
				
				adjustHeap(heap,len,lchidx);
			}
		}
		//have both children
		else
		{
			//check the left child
			if(lv.value<=rv.value)
			{
				if(lv.value<cv.value) 
				{
					swap=heap[index];
					heap[index]=heap[lchidx];
					heap[lchidx]=swap;
					
					adjustHeap(heap,len,lchidx);
				}
			}
			//check the right child
			else
			{
				if(rv.value<cv.value) 
				{
					swap=heap[index];
					heap[index]=heap[rchidx];
					heap[rchidx]=swap;
					
					adjustHeap(heap,len,rchidx);
				}
			}
		}
	}
	
	/**
	 * sort the heap structure
	 * @param heap
	 * the heap
	 */
//	private void sortHeap(MaxValue[] heap)
//	{
//	MaxValue swap;
	
//		for(int len=heap.length;len>1;len--) 
//		{
//			swap=heap[0];
//			heap[0]=heap[len-1];
//			heap[len-1]=swap;
			
//			adjustHeap(heap,len-1,0);
//		}
//	}
	
	public double normalize(double gain) 
	{
	long t;
		
		t=monitor.frameTime();
		
		//add a new local maximum
		if(t>=locallimit) 
		{
			localreached++;
			
			if(localmax.value>gheap[0].value) 
			{
				gheap[0].framems=localmax.framems;
				gheap[0].value=localmax.value;
				adjustHeap(gheap,gheap.length,0);
			}
			
			/*
			 * clear current local peak
			 */
			localmax.value=0;
			locallimit=t+localms;
		}
		
		//update local max
		if(gain>localmax.value) 
		{	
			localmax.framems=t;
			localmax.value=gain;
		}
		
		if(gain>globalmax) globalmax=gain;
		
		//select the threshold
		if(localreached>=gheap.length/2) 
		{
		int count=0;
		double mean=0,stddev=0,temp,hmax=0;
			
			/*
			 * calculate the mean and the standard deviation
			 */
			for(MaxValue lm:gheap) 
				if(lm.value>0) 
				{
					mean+=lm.value;
					count++;
					
					if(lm.value>hmax) hmax=lm.value;
				}
			mean/=count;
			
			for(MaxValue lm:gheap) 
				if(lm.value>0) 
				{
					temp=lm.value-mean;
					stddev+=temp*temp;
				}
			stddev=Math.sqrt(stddev/count);
			
			/*
			 * the threshold for agc
			 */
			agcth=mean+stddev/2;
			if(agcth>hmax) agcth=hmax;
			
			//remove obsolete data
			for(int i=0;i<gheap.length;i++) 
			{
			int idx,pidx;
			MaxValue swap;
				
				if(t-gheap[i].framems>globalms) 
				{
					gheap[i].framems=0;
					gheap[i].value=0;
					
					idx=i;
					pidx=idx-1;
					for(;pidx>=0;) 
					{
						pidx/=2;//parent node index
						if(gheap[idx].value<gheap[pidx].value) 
						{
							swap=gheap[pidx];
							gheap[pidx]=gheap[idx];
							gheap[idx]=swap;
						}
						
						idx=pidx;
						pidx--;
					}
				}
			}
			
			localreached=0;
		}
		
		/*
		 * perform normalization
		 */
		if(agcth==0) 
		{	
			if(globalmax==0) return 0;
			else gain/=globalmax;
		}
		else gain/=agcth;
		
		if(gain<0) gain=0;
		else if(gain>1) gain=1;
		return gain;
	}
	
	/**
	 * Max value and corresponding frame time
	 * @author nay0648
	 */
	private class MaxValue implements Serializable
	{
	private static final long serialVersionUID = -2145772490720471477L;
	long framems;
	double value;
	
		/**
		 * @param framems
		 * frame time (ms)
		 * @param value
		 * corresponding value
		 */
		public MaxValue(long framems,double value)
		{
			this.framems=framems;
			this.value=value;
		}
		
		public String toString()
		{
			return Double.toString(value);
		}
	}
}
