package Peer;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class InitPeer {


    public static void main(String[] args) {

        String accessPoint =args[0];

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
