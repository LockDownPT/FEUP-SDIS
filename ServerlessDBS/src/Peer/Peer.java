package Peer;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements PeerInterface{

    int mc_ip;
    int mc_port;
    int mdb_ip;
    int mdb_port;
    int mdr_ip;
    int mdr_port;

    public Peer() throws RemoteException {
        super();
    }

    public void backup(String file, int replicationDegree){
        System.out.println(file);
        System.out.println(replicationDegree);
    }

    public void restore(String file){
        System.out.println(file);
    }

}
