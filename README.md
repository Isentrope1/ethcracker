Ethcracker is a multi-threaded, pure java dictionary password cracker for Ethereum wallets. It relies on [web3j](https://github.com/web3j/web3j). 

Ethcracker is Copyright 2017 [Isentropy LLC](https://isentropy.com) and licensed under the [LGPL version 3](https://www.gnu.org/copyleft/lesser.html).

For lawful use only. 

## Building
  * git clone https://github.com/isentropy/ethcracker.git
  * cd ethcracker
  * mvn package

## Running
You can use the included wrapper script:
./ethcrack.sh [wallet file] [passwords file]

or run with java:
java -cp target/ethcracker-0.1.jar com.isentropy.ethcrack.Ethcracker [OPTIONS]

## Options
  * --wallet [FILE] : the wallet file to crack [required]
  * --pwfile [FILE] : newline separted list of passwords to try [optional, uses stdin if absent]
  * --out [FILE] : a file to write the password, if found [optional]
  * --threads [N] : # consumer threads [optional, chosen automatically]

## Checking Results

The discovered password is written to stdout as "FOUND PASSWORD: ...", but consider using the --out option, which outputs it to a file automatically.

Ethcracker exits with retcode 0 if it found the password and 1 if it ran without error but didn't find password. Other return codes indicate an error. 
