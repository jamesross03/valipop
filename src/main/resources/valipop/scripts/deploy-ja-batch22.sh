#!/bin/bash

TD_LOAD_AVG=`uptime | sed 's/.*load average: //' | awk -F\, '{print $1}'`
TD_NODE_IN_USE=`echo $TD_LOAD_AVG'>'0.5 | bc -l`


if [ $TD_NODE_IN_USE -eq 0 ]; then
	cd valipop
	nohup sh src/main/resources/valipop/scripts/run-factor-search.sh 14 src/main/resources/valipop/inputs/proxy-scotland-population-JA/ 10000 ja-batch22 1 0.5 0.1 /cs/tmp/tsd4/results/ > ja-batch22-`hostname`.txt 1E-1,1E-10,1E-20,1E-30,1E-40,1E-50,1E-60,1E-66
fi
