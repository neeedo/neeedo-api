#!/bin/bash
codecov

printf -v __ %q "$TRAVIS_COMMIT"
ssh -oStrictHostKeyChecking=no deployuser@178.62.252.23 "cd api;\
git pull origin master;\
git checkout $__;\
./sbt clean;\
./sbt stage;\
./neeedo restart"