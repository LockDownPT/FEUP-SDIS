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

function runSnooper {

	xterm -e "java -jar McastSnooper.jar $MCip:$MCport $MDBip:$MDBport $MDRip:$MDRport
" &
	
}

function startRMI {
	
	killall rmiregistry
	xterm -e "rmiregistry -J-Djava.rmi.server.codese=file://$(pwd)/" &
}

function launchPeers {

        count=1
	while [ "$count" -le $1 ]
	do	
    		xterm -e "java Peer.InitPeer $2 $count 200$count $MCip:$MCport $MDBip:$MDBport $MDRip:$MDRport" & $SHELL &
		count=$(( $count + 1 ))
	done
}

if (( $# != 8 )); then
    usage
fi


$MCip=$3
$MCport=$4
$MDBip=$5
$MDBport=$6
$MDRip=$7
$MDRport=$8
compile
runSnooper
cd bin
startRMI
launchPeers $1 $2
cd .. &
