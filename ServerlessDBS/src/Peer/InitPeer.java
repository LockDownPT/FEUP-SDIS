package Peer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class InitPeer {

    private InitPeer(String[] args) {

        Peer peer;

        String version = args[0];
        String peerId = args[1];
        String mc_ip = args[2];
        int mc_port = Integer.parseInt(args[3]);
        String mdb_ip = args[4];
        int mdb_port = Integer.parseInt(args[5]);
        String mdr_ip = args[6];
        int mdr_port = Integer.parseInt(args[7]);

        try {
            peer = new Peer(version, peerId, mc_ip, mdb_ip, mdr_ip, mc_port, mdb_port, mdr_port);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peerId, peer);
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
