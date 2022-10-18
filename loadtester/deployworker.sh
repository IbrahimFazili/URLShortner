#!/bin/bash

WORKER_PATH=/student/guptalak/Desktop/CSC409/repo_a1group68/a1/a1/loadtester

nohup ssh $1 "cd $WORKER_PATH && python3 -m locust -f 'locustfile.py' --worker --master-host $2" &
