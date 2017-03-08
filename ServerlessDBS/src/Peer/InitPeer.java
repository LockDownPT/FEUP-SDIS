package Peer;


import Channels.MC;
import Channels.MDB;
import Channels.MDR;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InitPeer {

    MC controlChannel;
    MDB backupChannel;
    MDR restoreChannel;
    String mc_ip;
    int mc_port;
    String mdb_ip;
    int mdb_port;
    String mdr_ip;
    int mdr_port;

    public InitPeer(String[] args){
        //backupChannel = new MDB(mdb_ip, mdb_port);
    }

    public static void main(String[] args) {

        String accessPoint = args[0];

        /* Needed for Mac OS X */
        System.setProperty("java.net.preferIPv4Stack", "true");

        Peer peer = null;

        try {
            peer = new Peer();
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(accessPoint, peer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
