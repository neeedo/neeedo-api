#!/bin/sh
codecov
function sshDeploy {
    printf -v __ %q "$1"
    ssh -oStrictHostKeyChecking=no deployuser@178.62.252.23 "cd api; git pull origin master; git checkout $__;./sbt clean; ./sbt stage; ./neeedo restart; exit $?"
}
sshDeploy $TRAVIS_COMMIT
exit $?

