#!/bin/bash

TD_LOAD_AVG=`uptime | sed 's/.*load average: //' | awk -F\, '{print $1}'`
TD_NODE_IN_USE=`echo $TD_LOAD_AVG'>'0.5 | bc -l`


if [ $TD_NODE_IN_USE -eq 0 ]; then
	cd valipop
	nohup sh src/main/resources/valipop/scripts/run-factor-search.sh 14 src/main/resources/valipop/inputs/scotland_test_population/ 15625 batch77-fs 3 0.339335938,0.339351197,0.339366456,0.339381714,0.339396973,0.339412232,0.339427491,0.33944275,0.339458008,0.339473267,0.339488526,0.339503785,0.339519043,0.339534302,0.339549561,0.33956482,0.339580079,0.339595337 0.002460938,0.002421875,0.0025 /cs/tmp/tsd4/results/ > batch77-fs-`hostname`.txt
fi
