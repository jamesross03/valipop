#!/bin/bash

TD_LOAD_AVG=`uptime | sed 's/.*load average: //' | awk -F\, '{print $1}'`
TD_NODE_IN_USE=`echo $TD_LOAD_AVG'>'0.5 | bc -l`


if [ $TD_NODE_IN_USE -eq 0 ]; then
	cd valipop
	nohup sh src/main/resources/valipop/scripts/run-factor-search.sh 14 src/main/resources/valipop/inputs/scotland_test_population/ 15625 batch72-fs 2 0.300625,0.30125,0.301875,0.303125,0.30375,0.304375,0.305625,0.30625,0.306875,0.3075,0.308125,0.30875,0.309375,0.310625,0.31125,0.311875,0.313125,0.31375,0.314375,0.315625,0.31625,0.316875,0.318125,0.31875,0.319375,0.320625,0.32125,0.321875,0.323125,0.32375,0.324375,0.325625,0.32625,0.326875,0.328125,0.32875,0.329375,0.330625,0.33125,0.331875,0.333125,0.33375,0.334375,0.335625,0.33625,0.336875,0.338125,0.33875,0.339375 0.0021875 /cs/tmp/tsd4/results/ > batch72-fs-`hostname`.txt
fi
