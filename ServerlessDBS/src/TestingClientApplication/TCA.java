package TestingClientApplication;


import Peer.PeerInterface;

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
        replicationDegree=Integer.parseInt(args[3]);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(peerAccessPoint);
            testingPeer = (PeerInterface) registry.lookup("PeerInterface");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testBackup(){
        testingPeer.backup(file,replicationDegree);
    }

    public void testRestore(){
        testingPeer.restore(file);
    }

    public static void main(String[] args) {

        String protocol = args[1];
        TCA testApplication = new TCA(args);
        if(protocol.equals("BACKUP")){
            testApplication.testBackup();
        }

    }

}
