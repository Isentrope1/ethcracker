#!/bin/bash -eu
cd `dirname $0`
JAVA=java
CP='target/ethcracker-0.1.jar'

WALLET=$1
PWFILE=$2

$JAVA -cp $CP com.isentropy.ethcrack.Ethcracker  --wallet $WALLET --pwfile $PWFILE
