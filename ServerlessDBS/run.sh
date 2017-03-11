#!/bin/bash
path=$(realpath ./out/production/ServerlessDBS/)

gnome-terminal -e "bash -c \"rmiregistry -J-Djava.rmi.server.codese=file://$path; exec bash\""
sleep 1
gnome-terminal -e "bash -c \"cd $path; java Peer.InitPeer 1.0 1 224.0.0.0 4445 224.0.0.1 4446 224.0.0.2 4447; exec bash\""
sleep 1
case $1 in
	BACKUP )
		gnome-terminal -e "bash -c \"cd $path; java TestingClientApplication.TCA 1 BACKUP lbaw.pdf 3; exec bash\"" ;;
	RESTORE )
		gnome-terminal -e "bash -c \"cd $path; java TestingClientApplication.TCA 1 RESTORE pdf; exec bash\"" ;;
esac
		
