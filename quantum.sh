#!/usr/bin/env bash
set -euo pipefail

# output directory
mkdir -p results/seed_q

# fixed parameters
P=100       # number of patrons
ALG=2       # RR scheduling
S=3         # context-switch time

# seeds to try
for SEED in 5 21 123 478 945; do
  for Q in $(seq 60 5 105); do
    LABEL="seed${SEED}_q${Q}"
    echo "=== Running P=${P} ALG=${ALG} S=${S} Q=${Q} SEED=${SEED} ==="
    make run ARGS="$P $ALG $S $Q $SEED"
    mv metrics.csv results/seed_q/metrics_${LABEL}.csv
  done
done

echo "All done! Check results/seed_q/"
