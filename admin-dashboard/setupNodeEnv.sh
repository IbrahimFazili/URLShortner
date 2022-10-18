#!/bin/bash

# install dependencies
npm i -s --ignore-scripts

# update env
> .env
echo "#FOR LAB PCS" >> .env
echo "#Vite requires VITE_ prefix" >> .env
echo "VITE_LOAD_BALANCER=http://$(hostname -f):8001" >> .env
echo "VITE_LOAD_BALANCER_API=http://$(hostname -f):7000" >> .env
echo "VITE_DB_CONTROLLER=http://$(hostname -f):3000" >> .env
