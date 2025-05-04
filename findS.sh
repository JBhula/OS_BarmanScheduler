#!/usr/bin/bash


# Fixed parameters
NO_PATRONS=50
SCHED=2 
Q=50     
SEED=0 #keep seed the same

#check for values between 1-5 and also up to 50 (to see if bigger values are better)
S_VALUES="1 2 3 4 5 10 20 30 50"

#store results so we can see them
OUTPUT_FILE="best_s_results.csv"

#the way the data will be stored in the output file
echo "Context_Switch(s),Avg_Response_Time,Avg_Waiting_Time,Avg_Turnaround_Time,CPU_Utilization,Avg_Throughput" > $OUTPUT_FILE

#use makefile to build and compile code
make clean
make all

#loop through each context switching value
for S in $S_VALUES; do

    #run using make run and store the output of the program in OUTPUT variable
    OUTPUT=$(make run ARGS="$NO_PATRONS $SCHED $S $Q $SEED")

    #exract metrics so we can see which value will be the best
    #print entire programs output, look for metric, split line into array and store the metric
    AVG_RESPONSE=$(echo "$OUTPUT" | grep "Avg response time:" | awk '{print $4}')
    AVG_WAITING=$(echo "$OUTPUT" | grep "Avg waiting time:" | awk '{print $4}')
    AVG_TURNAROUND=$(echo "$OUTPUT" | grep "Avg turnaround time:" | awk '{print $4}')
    CPU_UTIL=$(echo "$OUTPUT" | grep "CPU Utilization:" | awk '{print $3}')
    AVG_THROUGHPUT=$(echo "$OUTPUT" | grep "Avg Throughput:" | awk '{print $3}')

    #write metrics to output file
    echo "$S,$AVG_RESPONSE,$AVG_WAITING,$AVG_TURNAROUND,$CPU_UTIL,$AVG_THROUGHPUT" >> $OUTPUT_FILE
done

echo "Results saved to $OUTPUT_FILE"