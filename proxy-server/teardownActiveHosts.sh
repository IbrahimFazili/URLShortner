#!/bin/bash

rm -rf nohup.out

for host in `cat hosts`
do
	ssh -o StrictHostKeyChecking=no $host "pkill java" &
done
