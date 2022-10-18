#!/bin/bash

if [ $# -ne 3 ]
then
	echo "Invalid Arguments"
	echo "Must be in the format: ./startServer.sh loadBalancerHost port loadBalancerPort"
    exit 1
fi

javac URLShortner.java
javac URLShortnerThread.java
javac Node.java
javac NodeList.java

java URLShortner $1 $2 $3