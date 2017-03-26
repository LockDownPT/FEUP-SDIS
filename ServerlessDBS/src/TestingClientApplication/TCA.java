package TestingClientApplication;


import Peer.PeerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class TCA {

    private String file;
    private int replicationDegree;
    private PeerInterface testingPeer;

    private TCA(String[] args) {

        String peerAccessPoint = args[0];
        String protocol = args[1];
        file = args[2];
        if (protocol.equals("BACKUP"))
            replicationDegree = Integer.parseInt(args[3]);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            testingPeer = (PeerInterface) registry.lookup(peerAccessPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws RemoteException {

        String protocol = args[1];
        TCA testApplication = new TCA(args);
        if (protocol.equals("BACKUP")) {
            testApplication.testBackup();
        } else if (protocol.equals("RESTORE")) {
            testApplication.testRestore();
        }

    }

    private void testBackup() throws RemoteException {
        testingPeer.backup(file, replicationDegree);
    }

    private void testRestore() throws RemoteException {
        testingPeer.restore(file);
    }

}
