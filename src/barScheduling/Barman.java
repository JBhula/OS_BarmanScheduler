//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;

import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;

/*
 Barman Thread class.
 */

public class Barman extends Thread {
	

	private CountDownLatch startSignal;
	private BlockingQueue<DrinkOrder> orderQueue;
	int schedAlg =0;
	int q=10000; //really big if not set, so FCFS
	private int switchTime;

	private long totalIdleTime;
	private long totalBusyTime;

	//this is for throughput calculations
	private List<Long> completionTimes = new ArrayList<>(); //list that stores all completion time for patrons
	private int windowSize = 5000;	//size of window to calculate throughput on.
	
	
	Barman(  CountDownLatch startSignal,int sAlg) {
		//which scheduling algorithm to use
		this.schedAlg=sAlg;
		if (schedAlg==1) this.orderQueue = new PriorityBlockingQueue<>(5000, Comparator.comparingInt(DrinkOrder::getExecutionTime)); //SJF
		else this.orderQueue = new LinkedBlockingQueue<>(); //FCFS & RR
	    this.startSignal=startSignal;
	}
	
	Barman(  CountDownLatch startSignal,int sAlg,int quantum, int sTime) { //overloading constructor for RR which needs q
		this(startSignal, sAlg);
		q=quantum;
		switchTime=sTime;
	}

	public void placeDrinkOrder(DrinkOrder order) throws InterruptedException {
        orderQueue.put(order);
    }
	
	public void run() {
		int interrupts=0;
		try {
			DrinkOrder currentOrder;
			
			startSignal.countDown(); //barman ready
			startSignal.await(); //check latch - don't start until told to do so

			if ((schedAlg==0)||(schedAlg==1)) { //FCFS and non-preemptive SJF
				
				while(true) {
					//CPU (barman) is waiting to get a drink to prepare - this is time spent idle
					long startIdleTime = System.currentTimeMillis();
					currentOrder=orderQueue.take();
					long endIdleTime = System.currentTimeMillis();
					//cumulatively add to totalIdleTime
					totalIdleTime += (endIdleTime - startIdleTime);

					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString());

					//CPU (barman) is getting used while preparing the drink - this is the time spent busy
					long startBusyTime = System.currentTimeMillis();
					sleep(currentOrder.getExecutionTime()); //processing order (="CPU burst")
					long endBusyTime = System.currentTimeMillis();
					//cumulatively add to totalBusyTime
					totalBusyTime += (endBusyTime - startBusyTime);
					System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
					currentOrder.orderDone();
					completionTimes.add(System.currentTimeMillis()); //add completion time to list
					sleep(switchTime);//cost for switching orders
					//totalBusyTime += switchTime;
				}
			}
			else { // RR 
				int burst=0;
				int timeLeft=0;
				System.out.println("---Barman started with q= "+q);

				while(true) {
					System.out.println("---Barman waiting for next order ");
					currentOrder=orderQueue.take();

					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString() );
					burst=currentOrder.getExecutionTime();
					if(burst<=q) { //within the quantum
						sleep(burst); //processing complete order ="CPU burst"
						System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
						currentOrder.orderDone();
						completionTimes.add(System.currentTimeMillis()); //add completion time to list
					}
					else {
						sleep(q);
						timeLeft=burst-q;
						System.out.println("--INTERRUPT---preparation of drink for patron "+ currentOrder.toString()+ " time left=" + timeLeft);
						interrupts++;
						currentOrder.setRemainingPreparationTime(timeLeft);
						orderQueue.put(currentOrder); //put back on queue at end
					}
					sleep(switchTime);//switching orders
				}
			}
				
		} catch (InterruptedException e1) {
			System.out.println("---Barman is packing up ");
			System.out.println("---number interrupts="+interrupts);
		}
	}

	public long getIdleTime(){
		return totalIdleTime;
	}

	public long getBusyTime(){
		return totalBusyTime;
	}

	//This method calculates the throughput over the entire simulation on a specific window time frame
	public List<Double> calculateThroughputOverTime() {
		List<Double> throughputs = new ArrayList<>();
		
		//get the start time of the first and last completion time (so that we can loop through the times)
		long startTime = completionTimes.get(0);
		long endTime = completionTimes.get(completionTimes.size() - 1);
		
		//loop from start time to end time and calculate throughput for every windowSize (5s in this case)
		for (long windowStart = startTime; windowStart < endTime; windowStart += 5000) {
			long windowEnd = windowStart + windowSize;
			int count = 0;
			
			//if the completion time falls within the time frame add 1 to count
			for (Long time : completionTimes) {
				if (time >= windowStart && time < windowEnd) {
					count++;
				} else if (time >= windowEnd) {
					break; //we are out of the time frame we are checking so leave
				}
			}
			
			double throughput = count / (windowSize / 1000.0); // orders per second
			throughputs.add(throughput);
		}
		
		return throughputs;
	}
}


