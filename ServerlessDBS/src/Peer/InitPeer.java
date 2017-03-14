package Peer;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InitPeer {

    private String version;
    private String peerId;
    private String mc_ip, mdb_ip, mdr_ip;
    private int mc_port, mdb_port, mdr_port;

    public InitPeer(String[] args) throws IOException {



        Peer peer = null;


        this.version = args[0];
        this.peerId = args[1];
        this.mc_ip = args[2];
        this.mc_port = Integer.parseInt(args[3]);
        this.mdb_ip = args[4];
        this.mdb_port = Integer.parseInt(args[5]);
        this.mdr_ip = args[6];
        this.mdr_port = Integer.parseInt(args[7]);

        try {
            peer = new Peer(version, peerId, mc_ip, mdb_ip, mdr_ip, mc_port, mdb_port, mdr_port);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peerId, peer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        /* Needed for Mac OS X */
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            InitPeer initPeer = new InitPeer(args);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
