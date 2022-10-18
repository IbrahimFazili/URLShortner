#!/bin/bash

if [ $# -ne 1 ]
then
	echo "Invalid Arguments"
	echo "Must be in the format: ./teardownHost hostName"
    exit 1
fi

ssh -o StrictHostKeyChecking=no $1 "pkill java" &
