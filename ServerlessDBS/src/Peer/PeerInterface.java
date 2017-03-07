package Peer;


import java.rmi.Remote;


public interface PeerInterface extends Remote{

    void backup(String file, int replicationDegree);

    void restore(String file);

}
