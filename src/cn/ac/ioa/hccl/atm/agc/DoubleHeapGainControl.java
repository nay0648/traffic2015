package cn.ac.ioa.hccl.atm.agc;
import java.io.*;

import cn.ac.ioa.hccl.atm.*;
import cn.ac.ioa.hccl.atm.array.*;

/**
 * AGC for dB spectrum by two heap data structure, choosing the maximal and 
 * minimal local max values.
 * 
 * @author nay0648
 */
public class DoubleHeapGainControl extends AutomaticGainControl
{
private static final long serialVersionUID = 3605656403957472010L;
private MonitorArray monitor;//monitor reference
private long localms;//local interval size (ms)
private long globalms;//global interval size(ms)
private LocalMax localmax;//local max value
private int localreached=0;//number of added local max
private long locallimit;//time limit to find a new local max
private LocalMax[] fgheap;//the heap to keep the maximums in the global interval
private LocalMax[] bgheap;//the heap to keep the minimums of the local maximal
private boolean fgonly=false;//true means only use foreground threshold for the normalization
private double fgth=0;//the threshold for agc
private double bgth=0;//the threshold for denoise
private int thdetectduration=1;//local max count for threshold detection

	/**
	 * use default parameters
	 * @param monitor
	 * array reference
	 */
	public DoubleHeapGainControl(MonitorArray monitor)
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
	public DoubleHeapGainControl(MonitorArray monitor,long localms,long globalms,int heapsize)
	{
		this.monitor=monitor;
		
		localmax=new LocalMax(0,0);
		locallimit=monitor.frameTime()+localms;
			
		this.localms=localms;
		this.globalms=globalms;
			
		fgheap=new LocalMax[heapsize];
		for(int i=0;i<fgheap.length;i++) fgheap[i]=new LocalMax(0,0);
		
		bgheap=new LocalMax[heapsize];
		for(int i=0;i<bgheap.length;i++) bgheap[i]=new LocalMax(0,Double.MAX_VALUE);
	}
	
	/**
	 * to see if only use the foreground threshold for the normalization
	 * @return
	 */
	public boolean getForegroundOnly()
	{
		return fgonly;
	}
	
	/**
	 * set if only use the foreground threshold for the normalization
	 * @param fgonly
	 * true to use the foreground threshold only
	 */
	public void setForegroundOnly(boolean fgonly)
	{
		this.fgonly=fgonly;
	}
		
	/**
	 * adjust the heap array to keep the largest elements
	 * @param heap
	 * the heap
	 * @param len
	 * heap length
	 * @param index
	 * root node index
	 */
	private void adjustFgHeap(LocalMax[] heap,int len,int index)
	{
	int lchidx,rchidx;
	LocalMax cv,lv=null,rv=null,swap;
			
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
					
				adjustFgHeap(heap,len,lchidx);
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
						
					adjustFgHeap(heap,len,lchidx);
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
						
					adjustFgHeap(heap,len,rchidx);
				}
			}
		}
	}
	
	/**
	 * adjust the heap array to keep the smallest elements
	 * @param heap
	 * the heap
	 * @param len
	 * heap length
	 * @param index
	 * root node index
	 */
	private void adjustBgHeap(LocalMax[] heap,int len,int index)
	{
	int lchidx,rchidx;
	LocalMax cv,lv=null,rv=null,swap;
			
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
			if(lv.value>cv.value) 
			{
				swap=heap[index];
				heap[index]=heap[lchidx];
				heap[lchidx]=swap;
					
				adjustBgHeap(heap,len,lchidx);
			}
		}
		//have both children
		else
		{
			//check the left child
			if(lv.value>=rv.value)
			{
				if(lv.value>cv.value) 
				{
					swap=heap[index];
					heap[index]=heap[lchidx];
					heap[lchidx]=swap;
						
					adjustBgHeap(heap,len,lchidx);
				}
			}
			//check the right child
			else
			{
				if(rv.value>cv.value) 
				{
					swap=heap[index];
					heap[index]=heap[rchidx];
					heap[rchidx]=swap;
						
					adjustBgHeap(heap,len,rchidx);
				}
			}
		}
	}
	
	/**
	 * sort the foreground heap structure in descending order
	 * @param heap
	 * the heap
	 */
//	private void sortFgHeap(LocalMax[] heap)
//	{
//	LocalMax swap;
	
//		for(int len=heap.length;len>1;len--) 
//		{
//			swap=heap[0];
//			heap[0]=heap[len-1];
//			heap[len-1]=swap;
			
//			adjustFgHeap(heap,len-1,0);
//		}
//	}
	
	/**
	 * sort the background heap structure in ascending order
	 * @param heap
	 * the heap
	 */
//	private void sortBgHeap(LocalMax[] heap)
//	{
//	LocalMax swap;
	
//		for(int len=heap.length;len>1;len--) 
//		{
//			swap=heap[0];
//			heap[0]=heap[len-1];
//			heap[len-1]=swap;
			
//			adjustBgHeap(heap,len-1,0);
//		}
//	}
		
	public double normalize(double gain) 
	{
	long t;
		
		//values smaller than 0 are discarded
		if(gain<=0) return 0;
		t=monitor.frameTime();
		
		//update local max
		if(gain>localmax.value) 
		{	
			localmax.framems=t;
			localmax.value=gain;
		}
		
		//add a new local maximum
		if(t>=locallimit) 
		{			
			//keep largests local maximums
			if(localmax.value>fgheap[0].value) 
			{
				fgheap[0].framems=localmax.framems;
				fgheap[0].value=localmax.value;
				adjustFgHeap(fgheap,fgheap.length,0);
			}
			
			//keep smallest local maximums
			if(localmax.value<bgheap[0].value) 
			{
				bgheap[0].framems=localmax.framems;
				bgheap[0].value=localmax.value;
				adjustBgHeap(bgheap,bgheap.length,0);
			}
			
			/*
			 * clear current local peak
			 */
			localmax.value=0;
			locallimit=t+localms;
			
			/*
			 * select the threshold
			 */
			localreached++;
			
			if(localreached>=thdetectduration) 
			{		
				calculateThreshold();
				localreached=0;
				
				//perform more threshod selection at the beginning
				if(thdetectduration<Param.AGC_UPDATE_THRESHOLD)
				{
					thdetectduration*=2;
					if(thdetectduration>Param.AGC_UPDATE_THRESHOLD) 
						thdetectduration=Param.AGC_UPDATE_THRESHOLD;	
				}
			}
		}
		
		//perform normalization
		{
		double fth,bth;
		
			if(localmax.value>fgth) fth=localmax.value;
			else fth=fgth;
			
			bth=bgth;
			
			if(fgonly) gain=gain/fth;
			else gain=(gain-bth)/(fth-bth);
			
			if(gain<0) gain=0;
			else if(gain>1) gain=1;
			
			if(Double.isNaN(gain)) return 0;
			else return gain;
		}
	}
	
	/**
	 * remove old data from the foreground heap
	 */
	private void cleanFgHeap()
	{
	long t;
	int idx,pidx;
	LocalMax swap;
		
		t=monitor.frameTime();
		
		for(int i=0;i<fgheap.length;i++) 
		{	
			if(t-fgheap[i].framems>globalms) 
			{
				fgheap[i].framems=0;
				fgheap[i].value=0;
						
				idx=i;
				pidx=idx-1;
				for(;pidx>=0;) 
				{
					pidx/=2;//parent node index
						
					if(fgheap[idx].value<fgheap[pidx].value) 
					{
						swap=fgheap[pidx];
						fgheap[pidx]=fgheap[idx];
						fgheap[idx]=swap;
					}
					else break;
								
					idx=pidx;
					pidx--;
				}
			}
		}
	}
	
	/**
	 * remove old data from the background heap
	 */
	private void cleanBgHeap()
	{
	long t;
	int idx,pidx;
	LocalMax swap;	
	
		t=monitor.frameTime();
		
		for(int i=0;i<bgheap.length;i++) 
		{				
			if(t-bgheap[i].framems>globalms) 
			{
				bgheap[i].framems=0;
				bgheap[i].value=Double.MAX_VALUE;
						
				idx=i;
				pidx=idx-1;
				for(;pidx>=0;) 
				{
					pidx/=2;//parent node index
						
					if(bgheap[idx].value>bgheap[pidx].value) 
					{
						swap=bgheap[pidx];
						bgheap[pidx]=bgheap[idx];
						bgheap[idx]=swap;
					}
					else break;
								
					idx=pidx;
					pidx--;
				}
			}
		}
	}
	
	/**
	 * calculate the agc threshold
	 */
	private void calculateThreshold()
	{
	double fgmean=0,bgmean=0;
	double fgstddev=0,bgstddev=0;
	
		//calculate the mean and the standard deviation for foreground threshold
		{
		int count=0;
		double temp;
			
			for(LocalMax lm:fgheap) 
				if(lm.value>0) 
				{
					fgmean+=lm.value;
					count++;
				}
			fgmean/=count;
						
			for(LocalMax lm:fgheap) 
				if(lm.value>0) 
				{
					temp=lm.value-fgmean;
					fgstddev+=temp*temp;
				}
			fgstddev=Math.sqrt(fgstddev/count);
										
			//remove obsolete data
			cleanFgHeap();
		}
	
		//calculate the mean and the standard deviation for background threshold
		{
		int count=0;
		double temp;
			
			for(LocalMax lm:bgheap) 
				if(lm.value<Double.MAX_VALUE) 
				{
					bgmean+=lm.value;
					count++;
				}
			bgmean/=count;
						
			for(LocalMax lm:bgheap) 
				if(lm.value<Double.MAX_VALUE) 
				{
					temp=lm.value-bgmean;
					bgstddev+=temp*temp;
				}
			bgstddev=Math.sqrt(bgstddev/count);
										
			//remove obsolete data
			cleanBgHeap();
		}

		//calculate the new threshold
		if(fgmean-fgstddev>bgmean+bgstddev)
		{
			fgth=fgmean-fgstddev/2;
			bgth=bgmean+bgstddev/2;			
		}
		else if(fgmean-fgstddev/2>bgmean+bgstddev/2)
		{
			fgth=fgmean;
			bgth=bgmean;
		}
		else
		{
			fgth=fgmean+fgstddev/2;
			bgth=bgmean-bgstddev/2;
		}
	}
	
	/**
	 * Max value and corresponding frame time
	 * @author nay0648
	 */
	private class LocalMax implements Serializable
	{
	private static final long serialVersionUID = -7098188807540269089L;
	long framems;//corresponding frame time
	double value;//response value
		
		/**
		 * @param framems
		 * frame time (ms)
		 * @param value
		 * corresponding value
		 */
		public LocalMax(long framems,double value)
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
