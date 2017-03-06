package Peer;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class InitiatorPeer extends UnicastRemoteObject implements PeerInterface{

    public InitiatorPeer() throws RemoteException {
        super();
    }


    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "PeerInterface";
            PeerInterface peerInterface = new InitiatorPeer();
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peerInterface, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("InitiatorPeer bound");
        } catch (Exception e) {
            System.err.println("InitiatorPeer exception:");
            e.printStackTrace();
        }
    }

    public void backup(String file, int replicationDegree){
        System.out.println(file);
        System.out.println(replicationDegree);
    }

    public void restore(String file){
        System.out.println(file);
    }

}
