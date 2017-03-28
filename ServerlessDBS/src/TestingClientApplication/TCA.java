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
        if (!"STATE".equals(protocol)) {
            file = args[2];
        }
        if ("BACKUP".equals(protocol))
            replicationDegree = Integer.parseInt(args[3]);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            testingPeer = (PeerInterface) registry.lookup(peerAccessPoint);
        } catch (Exception e) {
            System.out.println("Failed to connect to peer:" + args[0]);
        }

    }

    public static void main(String[] args) throws RemoteException {

        String protocol = args[1];
        TCA testApplication = new TCA(args);
        switch (protocol) {
            case "BACKUP":
                testApplication.testBackup();
                break;
            case "RESTORE":
                testApplication.testRestore();
                break;
            case "STATE":
                testApplication.state();
                break;
            case "DELETE":
                testApplication.testDelete();
                break;
            default:
                System.out.println("WRONG PROTOCOL");
                break;
        }

    }

    private void testBackup() throws RemoteException {
        testingPeer.backup(file, replicationDegree);
    }

    private void testRestore() throws RemoteException {
        testingPeer.restore(file);
    }


    private void testDelete() throws RemoteException {
        testingPeer.delete(file);
    }


    private void state() throws RemoteException {
        testingPeer.state();
    }

}
