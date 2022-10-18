#!/bin/bash

CURDIR="$(readlink -f $0)"
CURDIR=${CURDIR%"/initializeHost.sh"}

echo $CURDIR

if [ $# -ne 4 ]
then
	echo "Invalid Arguments"
	echo "Must be in the format: ./initializeHost hostName localHost remotePort localPort"
    exit 1
fi

rm -rf output.err
# nohup ssh $1 "nc -l -k -p $2" > /dev/null &
# nohup ssh -o 'ConnectTimeout 5' $1 "hostname" > /dev/null > output.err &

if ! ssh -o 'ConnectTimeout 3' -o StrictHostKeyChecking=no $1 "hostname" > /dev/null
then
	echo "error in $1"
	exit 1
else
	nohup ssh -o StrictHostKeyChecking=no $1 "cd $CURDIR && cd ../host && ./startServer.sh $2 $3 $4" > /dev/null &
fi
