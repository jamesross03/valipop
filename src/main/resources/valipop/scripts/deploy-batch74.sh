#!/bin/bash

TD_LOAD_AVG=`uptime | sed 's/.*load average: //' | awk -F\, '{print $1}'`
TD_NODE_IN_USE=`echo $TD_LOAD_AVG'>'0.5 | bc -l`


if [ $TD_NODE_IN_USE -eq 0 ]; then
	cd valipop
	nohup sh src/main/resources/valipop/scripts/run-factor-search.sh 14 src/main/resources/valipop/inputs/scotland_test_population/ 15625 batch74-fs 2 0.3,0.3025,0.305,0.31,0.3125,0.315,0.3175,0.32,0.3225,0.325,0.3275,0.33,0.3325 0.0021875,0.0028125 /cs/tmp/tsd4/results/ > batch74-fs-`hostname`.txt
fi
