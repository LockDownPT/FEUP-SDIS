#!/bin/bash
javac $(find . -name \*.java)
echo "Project compiled."

path=$(realpath ./out/production/ServerlessDBS/)

gnome-terminal -e "bash -c \"rmiregistry -J-Djava.rmi.server.codese=file://$path; exec bash\""
sleep 1
gnome-terminal -e "bash -c \"cd $path; java Peer.InitPeer 1; exec bash\""
sleep 1
case $1 in
	BACKUP )
		gnome-terminal -e "bash -c \"cd $path; java TestingClientApplication.TCA 1 BACKUP pdf 3; exec bash\"" ;;
	RESTORE )
		gnome-terminal -e "bash -c \"cd $path; java TestingClientApplication.TCA 1 RESTORE pdf; exec bash\"" ;;
esac
		
