#!/bin/bash
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null target/neeedo-api_1.0-SNAPSHOT_all.deb deployuser@178.62.252.23:~
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null deployuser@178.62.252.23 "dpkg -i neeedo-api_1.0-SNAPSHOT_all.deb \
/etc/init.d/neeedo-api stop \
/etc/init.d/neeedo-api start"