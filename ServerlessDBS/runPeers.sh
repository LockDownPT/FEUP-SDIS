#!/bin/bash

function usage {

	echo "Usage: <Number of Peers> <Version>"
	exit

}

function compile {

	mkdir bin
	javac $(find ./src/* | grep .java) -d bin
	cp -r ./src/TestFiles ./bin
}

function startRMI {
	
	killall rmiregistry
	xterm -e "rmiregistry -J-Djava.rmi.server.codese=file://$(pwd)/" &
}

function launchPeers {

        count=1
	while [ "$count" -le $1 ]
	do	
    		xterm -e "java Peer.InitPeer $2 $count 200$count 224.0.0.0 4445 224.0.0.2 4446 224.0.0.4 4447" & $SHELL &
		count=$(( $count + 1 ))
	done
}

if (( $# != 2 )); then
    usage
fi

compile
cd bin
startRMI
launchPeers $1 $2
cd .. &
