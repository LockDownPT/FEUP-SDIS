package TestingClientApplication;


import Peer.PeerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TCA {

    private String peerAccessPoint;
    private String protocol;
    private String file;
    private int replicationDegree;
    private PeerInterface testingPeer;

    public TCA(String[] args){

        peerAccessPoint=args[0];
        protocol=args[1];
        file=args[2];
        if(protocol.equals("BACKUP"))
            replicationDegree=Integer.parseInt(args[3]);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            testingPeer = (PeerInterface) registry.lookup(peerAccessPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testBackup() throws RemoteException {
        testingPeer.backup(file,replicationDegree);
    }

    public void testRestore() throws RemoteException {
        testingPeer.restore(file);
    }

    public static void main(String[] args) throws RemoteException {

        String protocol = args[1];
        TCA testApplication = new TCA(args);
        if(protocol.equals("BACKUP")){
            testApplication.testBackup();
        } else if(protocol.equals("RESTORE")){
            testApplication.testRestore();
        }

    }

}
