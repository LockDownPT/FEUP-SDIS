package Peer;


import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements PeerInterface{

    public Peer() throws IOException {
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
