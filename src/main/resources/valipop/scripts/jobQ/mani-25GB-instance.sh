#!/bin/bash

cd valipop
nohup sh src/main/resources/valipop/scripts/jobQ/start-job-q-instance.sh src/main/resources/valipop/scripts/jobQ/status-mani.txt src/main/resources/valipop/scripts/jobQ/job-q-mani.csv 25 1 20 > runs/job-run-`hostname`-$1.txt
