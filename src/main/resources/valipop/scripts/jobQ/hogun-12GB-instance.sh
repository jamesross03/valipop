#!/bin/bash

cd valipop
nohup sh src/main/resources/valipop/scripts/jobQ/start-job-q-instance.sh src/main/resources/valipop/scripts/jobQ/status-hogun.txt src/main/resources/valipop/scripts/jobQ/job-q-clusters.csv 12 1 1.5 > runs/job-run-`hostname`.txt
