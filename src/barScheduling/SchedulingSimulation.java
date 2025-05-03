//M. M. Kuttel 2025 mkuttel@gmail.com

package barScheduling;
// the main class, starts all threads and the simulation


import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

//for array (used for throughput calculation) and collections so i can sort the array in ascending timing
import java.util.ArrayList;
import java.util.Collections;


public class SchedulingSimulation {
	static int noPatrons=50; //number of customers - default value if not provided on command line
	static int sched=2; //default scheduling algorithm, 0= FCFS, 1=SJF, 2=RR
	static int q=50, s=1;
	static long seed=0;
	static CountDownLatch startSignal;	
	static Patron[] patrons; // array for customer threads
	static Barman Sarah;

	//this is for CPU Utilization calculations
	static long startSimulationTime;
	static long endSimulationTime;
	static long totalSimulationTime;
	
	static long windowSize = 3000;


	public static void main(String[] args) throws InterruptedException, IOException {

		//deal with command line arguments if provided
		if (args.length>=1) noPatrons=Integer.parseInt(args[0]);  //total people to enter room
		if (args.length>=2) sched=Integer.parseInt(args[1]); 	// alg to use
		if (args.length>=3) s=Integer.parseInt(args[2]);  //context switch 
		if(args.length>=4) q=Integer.parseInt(args[3]);  // time slice for RR
		if(args.length>=5) seed=Integer.parseInt(args[4]); // random number seed- set to compare apples with apples		
		

		startSignal= new CountDownLatch(noPatrons+2);//Barman and patrons and main method must be ready
		
		//create barman
        Sarah= new Barman(startSignal,sched,q,s); 
     	Sarah.start();
		//this is where simulation starts
		startSimulationTime = System.currentTimeMillis();

	    //create all the patrons, who all need access to Barman
		patrons = new Patron[noPatrons];
		for (int i=0;i<noPatrons;i++) {
			patrons[i] = new Patron(i,startSignal,Sarah,seed);
			patrons[i].start();
		}
		
		if (seed>0) DrinkOrder.random = new Random(seed);// for consistent Patron behaviour

		
		System.out.println("------Sarah the Barman Scheduling Simulation------");
		System.out.println("-------------- with "+ Integer.toString(noPatrons) + " patrons---------------");
		switch(sched) {
		  case 0:
			  System.out.println("-------------- and FCSF scheduling ---------------");
		    break;
		  case 1:
			  System.out.println("-------------- and SJF scheduling ---------------");
		    break;
		  case 2:
			  System.out.println("-------------- and RR scheduling with q="+q+"-------------");
		}
		
			
      	startSignal.countDown(); //main method ready
      	
		
		//startSimulationTime = System.currentTimeMillis();



      	//wait till all patrons done, otherwise race condition on the file closing!
      	for (int i=0;i<noPatrons;i++)  patrons[i].join();

    	System.out.println("------Waiting for Barman------");
    	Sarah.interrupt();   //tell Barman to close up
    	Sarah.join(); //wait till she has
		//this is where simulation ends
		endSimulationTime = System.currentTimeMillis();
      	System.out.println("------Bar closed------");

		for (Patron patron : patrons) {
            long patronWaitingTime = patron.getWaitingTime();
			long rT = patron.getResponseTime();
			long tT = patron.getTurnaroundTime();


            System.out.println("Patron " + patron.ID + " wait time: " + patronWaitingTime + "ms");
			System.out.println("Patron " + patron.ID + " response time: " + rT + "ms");
			System.out.println("Patron " + patron.ID + " turnaround time: " + tT + "ms");
        }
		totalSimulationTime = endSimulationTime - startSimulationTime;

		double busyTime = (double)Sarah.getBusyTime();
		System.out.println("Busy Time: " + Sarah.getBusyTime());
		System.out.println("Idle Time: " + (totalSimulationTime - Sarah.getBusyTime()));
		System.out.println("Simulation Time: " + totalSimulationTime);
		double a = (double)(totalSimulationTime);
		System.out.println("CPU Utilization: " + busyTime/a);
		List<Long> completionTimes = new ArrayList<>();
		for (Patron patron : patrons) {
			//System.out.println(patron.getCompletionTime());
    		completionTimes.add(patron.getCompletionTime());
		}
		Collections.sort(completionTimes);
		List<Double> throughputs = calculateThroughputOverTime(completionTimes);
		//System.out.println(throughputs.size());
		System.out.println("\nThroughput over time (patrons/second in 3-second windows):");
		for (int i = 0; i < throughputs.size(); i++) {
    		System.out.printf("Window %d: %.2f orders/second\n", i+1, throughputs.get(i));
		}


		//this is where output all the data to a .csv file
		//It is in the form:
		// list of all the response time of all 50 patrons with the average at the end (eg. 23, 43, 52,..avg)
		// list of all the wait times of all 50 patrons with the average at the end (eg. 2345, 3546, 5769,..avg)
		// list of all the turnaround times of all 50 patrons with the average at the end (eg. 3456, 4657, 6809,..avg)
		// busy time, idle time, simulation time, cpu utilisation (eg. 14183, 1692, 16505, 0.897)
		// throughput for each window (window 1, window 2...) (eg. 2.8, 4.4, 2.8)
		// I use this format to make my graphs
		try {
			String filename;
			if(sched == 0){
				filename = "FCFS_results.csv";
			}
			else if(sched==1){
				filename = "SJF_results.csv";
			}
			else{
				filename = "RR_results.csv";
			}
			java.io.FileWriter csvWriter = new java.io.FileWriter(filename);

			// Write response times
			long totalResponseTime = 0;
			for (Patron patron : patrons) {
				long rT = patron.getResponseTime();
				csvWriter.append(String.valueOf(rT)).append(",");
				totalResponseTime += rT;
			}
			double avgResponseTime = (double) totalResponseTime / noPatrons;
			csvWriter.append(String.valueOf(avgResponseTime)).append("\n");

			// Write waiting times
			long totalWaitingTime = 0;
			for (Patron patron : patrons) {
				long wT = patron.getWaitingTime();
				csvWriter.append(String.valueOf(wT)).append(",");
				totalWaitingTime += wT;
			}
			double avgWaitingTime = (double) totalWaitingTime / noPatrons;
			csvWriter.append(String.valueOf(avgWaitingTime)).append("\n");

			// Write turnaround times
			long totalTurnaroundTime = 0;
			for (Patron patron : patrons) {
				long tT = patron.getTurnaroundTime();
				csvWriter.append(String.valueOf(tT)).append(",");
				totalTurnaroundTime += tT;
			}
			double avgTurnaroundTime = (double) totalTurnaroundTime / noPatrons;
			csvWriter.append(String.valueOf(avgTurnaroundTime)).append("\n");

			// Write system metrics
			csvWriter.append(String.valueOf(Sarah.getBusyTime())).append(","); //busy time
			csvWriter.append(String.valueOf(totalSimulationTime - Sarah.getBusyTime())).append(","); //idle time
			csvWriter.append(String.valueOf(totalSimulationTime)).append(","); //total simulation time
			csvWriter.append(String.valueOf(busyTime / a)).append("\n"); //cpu utilization

			// Write throughputs
			for (int i = 0; i < throughputs.size(); i++) {
				csvWriter.append(String.valueOf(throughputs.get(i))).append(",");
			}

			csvWriter.flush();
			csvWriter.close();
			System.out.println("\nResults written to " + filename);
		} catch (IOException e) {
			System.err.println("Error writing to CSV file:");
		}


 	}

		//This method calculates the throughput over the entire simulation on a specific window time frame
		public static List<Double> calculateThroughputOverTime(List<Long> completionTimes) {
			

			List<Double> throughputs = new ArrayList<>();
			
			//get the start time of the first and last completion time (so that we can loop through the times)
			long startTime = completionTimes.get(0);
			long endTime = completionTimes.get(completionTimes.size() - 1);
			
			//loop from start time to end time and calculate throughput for every windowSize (5s in this case)
			for (long windowStart = startTime; windowStart < endTime; windowStart += 3000) {
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
