#!/bin/bash
scp ../neeedo-api_1.0-SNAPSHOT_all.deb -oStrictHostKeyChecking=no deployuser@178.62.252.23:~
#ssh -oStrictHostKeyChecking=no deployuser@178.62.252.23 "cd api;\
#git pull origin master;\
#git checkout $__;\
#./sbt clean;\
#./sbt stage;\
#./neeedo restart"