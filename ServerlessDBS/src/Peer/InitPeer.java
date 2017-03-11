package Peer;


import Channels.MC;
import Channels.MDB;
import Channels.MDR;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InitPeer {



    public InitPeer(String[] args) throws IOException {


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
