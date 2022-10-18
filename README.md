# README

This project is designed to work on NFS mounted systems, like UTM's lab machines

## **Dependencies**

npm (Node Package Manager)

## **Instructions on running the code**

Clone the repo

Running

> python3 run.py
>
> executes the orchestration script with default values. I.e, number of hosts to deploy will be 1, number of database nodes will be 1, replication factor will be 1

To run it with 5 hosts, 4 database nodes and replication factor of 2, the command will be

> python3 run.py -hosts 5 -rf 2 -dbnodes 4

## **Admin Credentials**

Username: admin@admin.com
Password: test123
