# Barman Scheduling Simulation (OS Algorithms)

## Overview
This project simulates an operating system's process scheduling by modeling a bar environment. It evaluates how different scheduling algorithms handle incoming requests and compares their efficiency using standard OS metrics. 

The simulation maps core computer architecture concepts to a real-world scenario:
* **The CPU:** Represented by the Barman (Sarah), who prepares the drinks and waits for new orders.
* **Processes:** Represented by the Patrons. A process is completed once all of a patron's orders are fulfilled.
* **Jobs (CPU Bursts):** Represented by individual drink orders.

## Algorithms Evaluated
The simulation tests and compares three core scheduling algorithms:
1.  **First-Come, First-Serve (FCFS)**
2.  **Shortest Job First (SJF)**
3.  **Round Robin (RR)**

*Note on Configuration:* To maximize CPU utilization and throughput while minimizing response, waiting, and turnaround times, the optimal context switch time (s) was determined to be **1**. For the Round Robin algorithm, the optimal time quantum (q) was calculated to be **100**. 

## Performance Metrics
The system logs and calculates the following metrics for each algorithm:
* **Waiting Time:** The total time a patron spends waiting for all their drinks to be served.
* **Response Time:** The initial waiting period before the patron receives their very first drink.
* **Turnaround Time:** The total time elapsed from ordering the first drink to finishing the final drink.
* **CPU Utilization:** The ratio of time the barman spends actively preparing drinks versus the total simulation time.
* **Throughput:** The rate of patrons served, calculated over 1-second (1000ms) sliding windows.

## Key Findings & Conclusion
Data was collected by running the simulation across 50 patrons using 10 different constant seeds to ensure fair averages. 

**Conclusion:** **First-Come, First-Serve (FCFS)** was found to be the most effective algorithm for this specific barman implementation.
* **Highest Work Rate:** FCFS achieved the highest average throughput at 4.17 patrons per second and completed the simulation in the shortest time (16.7 seconds).
* **Best Utilization:** FCFS resulted in the hardest-working CPU, achieving a peak utilization of 95.9% (compared to SJF at 93.3% and RR at 87.1%).
* **Customer Satisfaction:** FCFS maintained excellent response times for patrons arriving early. 

While SJF showed a better median turnaround time (5000-5850 ms), its results were highly sporadic with a large variance, making FCFS the reliable choice overall.
