#!/bin/bash
scp -i docker -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null target/neeedo-api_1.0-SNAPSHOT_all.deb neeedo-api@46.101.162.213:~
ssh -i docker -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null neeedo-api@46.101.162.213 "sudo dpkg -i --force-confold neeedo-api_1.0-SNAPSHOT_all.deb; \
exit;"