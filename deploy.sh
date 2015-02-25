#!/bin/bash
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null target/neeedo-api_1.0-SNAPSHOT_all.deb deployuser@178.62.252.23:~
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null deployuser@178.62.252.23 "sudo dpkg -i --force-confold neeedo-api_1.0-SNAPSHOT_all.deb \
exit"