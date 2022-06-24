#!/usr/bin/env bash

mvn clean package

echo 'Copy files...'

scp -i ~/.ssh/id_rsa \
    target/SearchBot-1.0-SNAPSHOT.jar \
    coollappsus@84.252.140.163:/home/coollappsus/

echo 'Restart server...'

ssh -i ~/.ssh/id_rsa coollappsus@84.252.140.163 << EOF
pgrep java | xargs kill -9
nohup java -jar earchBot-1.0-SNAPSHOT.jar > log.txt &
EOF

echo 'Bye'