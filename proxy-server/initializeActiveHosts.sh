#!/bin/bash

rm -rf activeHosts.txt
rm -rf nohup.out

let ACTIVEHOSTS=null
let CURRENTHOSTS=0

if [ $# -ne 2 ]
then
	echo "Invalid Arguments"
	echo "Must be in the format: ./initializeActiveHosts numHosts remotePort"
    exit 1
fi

if [ -z $1 ]
then
	echo "Defaulting to 4 number of hosts"
	let ACTIVEHOSTS=4
else
	let ACTIVEHOSTS=$1
fi

for host in `cat hosts`
do
	if [ $CURRENTHOSTS -eq $ACTIVEHOSTS ]
	then
		break;
	fi
        let CURRENTHOSTS=$CURRENTHOSTS+1
		nohup ssh $host "cd /student/changj87/CSC409/Assignments/repo_a1group68/a1/a1/host && javac URLShortner.java && java URLShortner $2" >/dev/null & 
		ssh $host "hostname"
		ssh $host "hostname" >> activeHosts.txt
done
