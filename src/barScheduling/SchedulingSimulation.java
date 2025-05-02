//M. M. Kuttel 2025 mkuttel@gmail.com

package barScheduling;
// the main class, starts all threads and the simulation


import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

//for array and lists (used for throughput calculation)



public class SchedulingSimulation {
	static int noPatrons=50; //number of customers - default value if not provided on command line
	static int sched=1; //default scheduling algorithm, 0= FCFS, 1=SJF, 2=RR
	static int q=10000, s=5;
	static long seed=0;
	static CountDownLatch startSignal;	
	static Patron[] patrons; // array for customer threads
	static Barman Sarah;

	//this is for CPU Utilization calculations
	static long startSimulationTime;
	static long endSimulationTime;
	static long totalSimulationTime;
	


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
      	
		//this is where simulation starts
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

		List<Double> throughputs = Sarah.calculateThroughputOverTime();
		System.out.println("\nThroughput over time (orders/second in 5-second windows):");
		for (int i = 0; i < throughputs.size(); i++) {
    		System.out.printf("Window %d: %.2f orders/second\n", i+1, throughputs.get(i));
		}

 	}

}
