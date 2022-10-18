#!/bin/bash

cp ./utils/*.java ./proxy-server/
cp ./utils/*.java ./host/
cd ./host/ && javac *.java
cd ..
cd ./proxy-server && javac *.java
