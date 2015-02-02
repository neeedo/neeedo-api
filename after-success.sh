#!/bin/sh
codecov
ssh -v -oStrictHostKeyChecking=no deployuser@178.62.252.23 "cd api; git pull origin master; ./sbt clean; ./sbt stage; ./neeedo restart; exit $?"