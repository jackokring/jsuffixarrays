#!/bin/bash

#
# Measure the execution time on random input, constant alphabet size,
# increasing input size.
#

. ./setup.sh

#
# Which algorithms to test?
#
ALGORITHMS="NS SKEW DIVSUFSORT SAIS BPR DEEP_SHALLOW QSUFSORT"

#
# Run evaluations.
#
OUTPUT_DIR=results/random-input
mkdir -p $OUTPUT_DIR

for algorithm in $ALGORITHMS; do
    if [ -f $OUTPUT_DIR/$algorithm.log ]; then
        echo "Skipping: $algorithm (log exists)."
        continue
    fi

    run_java org.jsuffixarrays.TimeOnRandomInput \
        $algorithm \
        --alphabet-size 32 \
        --start-size 1000000 --increment 1000000 \
        --rounds 20 --warmup-rounds 2 \
    | tee $OUTPUT_DIR/$algorithm.log
done

#
# Render plots.
#

./render-input-time.sh $OUTPUT_DIR results/random-input-time
./render-input-memory.sh $OUTPUT_DIR results/random-input-memory
