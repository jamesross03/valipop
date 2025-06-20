#!/bin/bash

TD_LOAD_AVG=`uptime | sed 's/.*load average: //' | awk -F\, '{print $1}'`
TD_NODE_IN_USE=`echo $TD_LOAD_AVG'>'0.5 | bc -l`


if [ $TD_NODE_IN_USE -eq 0 ]; then
	cd valipop
	nohup sh src/main/resources/valipop/scripts/run-factor-search.sh 14 src/main/resources/valipop/inputs/scotland_test_population/ 15625 batch76-fs 1 0.339257813,0.339296875,0.339335938,0.339375,0.339414063,0.339453125,0.339492188 0.002382813,0.002421875,0.002460938,0.0025,0.002539063,0.002578125,0.002617188 /cs/tmp/tsd4/results/ > batch76-fs-`hostname`.txt
fi
