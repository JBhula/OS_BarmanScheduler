#!/bin/bash


# Fixed parameters
NO_PATRONS=50
SCHED=2
S=1      #keep s the same (1 I found to be the best value)
SEED=0   #keep seed the same

#check through these q values
Q_VALUES="1 5 10 25 50 69 75 85 100 125"

#store results so we can see them
OUTPUT_FILE="best_q_results.csv"

#the way the data will be stored in the output file
echo "Quantum(q),Avg_Response_Time,Avg_Waiting_Time,Avg_Turnaround_Time,CPU_Utilization,Avg_Throughput" > $OUTPUT_FILE

#use makefile to build and compile code
make clean
make all

#loop through each quantum value
for Q in $Q_VALUES; do
    echo "Testing with quantum: $Q ms"

    #run using make run and store the output of the program in OUTPUT variable
    OUTPUT=$(make run ARGS="$NO_PATRONS $SCHED $S $Q $SEED")

    #exract metrics so we can see which value will be the best
    AVG_RESPONSE=$(echo "$OUTPUT" | grep "Avg response time:" | awk '{print $4}')
    AVG_WAITING=$(echo "$OUTPUT" | grep "Avg waiting time:" | awk '{print $4}')
    AVG_TURNAROUND=$(echo "$OUTPUT" | grep "Avg turnaround time:" | awk '{print $4}')
    CPU_UTIL=$(echo "$OUTPUT" | grep "CPU Utilization:" | awk '{print $3}')
    AVG_THROUGHPUT=$(echo "$OUTPUT" | grep "Avg Throughput:" | awk '{print $3}')

    #write metrics to output file
    echo "$Q,$AVG_RESPONSE,$AVG_WAITING,$AVG_TURNAROUND,$CPU_UTIL,$AVG_THROUGHPUT" >> $OUTPUT_FILE

    echo "Completed testing with q=$Q"
done

echo "Results saved to $OUTPUT_FILE"