#!/bin/bash

TD_LOAD_AVG=`uptime | sed 's/.*load average: //' | awk -F\, '{print $1}'`
TD_NODE_IN_USE=`echo $TD_LOAD_AVG'>'0.5 | bc -l`


if [ $TD_NODE_IN_USE -eq 0 ]; then
	cd valipop
	nohup sh src/main/resources/valipop/scripts/run-factor-search.sh 14 src/main/resources/valipop/inputs/scotland_test_population/ 15625 batch75-fs 2 0.335,0.3375,0.34,0.340625,0.34125,0.341875,0.3425,0.343125,0.34375,0.344375,0.345,0.345625,0.34625,0.346875,0.3475,0.348125,0.34875,0.349375,0.35 0.0021875,0.0025,0.0028125 /cs/tmp/tsd4/results/ > batch75-fs-`hostname`.txt
fi
