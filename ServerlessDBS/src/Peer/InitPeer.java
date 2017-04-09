package Peer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class InitPeer {

    private InitPeer(String[] args) {

        Peer peer;

        String version = args[0];
        String peerId = args[1];
        String peerAccessPoint = args[2];
        String mc_ip = args[3];
        int mc_port = Integer.parseInt(args[4]);
        String mdb_ip = args[5];
        int mdb_port = Integer.parseInt(args[6]);
        String mdr_ip = args[7];
        int mdr_port = Integer.parseInt(args[8]);

        try {
            peer = new Peer(version, peerId, peerAccessPoint, mc_ip, mdb_ip, mdr_ip, mc_port, mdb_port, mdr_port);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peerAccessPoint, peer);
        } catch (Exception e) {
            System.out.println("Failed to bind peer to registry");
        }

    }

    public static void main(String[] args) {

        /* Needed for Mac OS X */
        System.setProperty("java.net.preferIPv4Stack", "true");

        InitPeer initPeer = new InitPeer(args);


    }


}
