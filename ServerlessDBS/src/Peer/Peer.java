package Peer;


import Channels.MC;
import Channels.MDB;
import Channels.MDR;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements PeerInterface{

    MC controlChannel;
    MDB backupChannel;
    MDR restoreChannel;

    String mc_ip, mdb_ip, mdr_ip;
    int mc_port, mdb_port, mdr_port;

    public Peer() throws IOException {
        super();
        backupChannel = new MDB(mdb_ip, mdb_port);
        restoreChannel = new MDR(mdr_ip, mdr_port);
        controlChannel = new MC(mc_ip,mc_port);

        backupChannel.listen();
        restoreChannel.listen();
        controlChannel.listen();

    }

    public void backup(String file, int replicationDegree){
        System.out.println(file);
        System.out.println(replicationDegree);


    }

    public void restore(String file){
        System.out.println(file);
    }

}
