package Peer;

public class Peer {

    private int peerID;
    int mc_ip;
    int mc_port;
    int mdb_ip;
    int mdb_port;
    int mdr_ip;
    int mdr_port;

    Peer(String [] args) {
        peerID = Integer.parseInt(args[0]);

        InitiatorPeer initiator = null;
    }


}
