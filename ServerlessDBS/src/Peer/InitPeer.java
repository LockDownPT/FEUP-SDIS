package Peer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InitPeer {

    Peer peer;
    String accessPoint;

    InitPeer(String [] args) {
        accessPoint =args[0];


    }


    public static void main(String[] args) {

        String accessPoint =args[0];
        /* Needed for Mac OS X */
        System.setProperty("java.net.preferIPv4Stack", "true");

        InitPeer initPeer = new InitPeer(args);

        PeerInterface peerInterface = null;


        try {
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(accessPoint, peerInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
