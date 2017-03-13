package Peer;

import Channels.MC;
import Channels.MDB;
import Channels.MDR;
import Subprotocols.Backup;

import java.io.File;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements PeerInterface{

    private MC controlChannel;
    private MDB backupChannel;
    private MDR restoreChannel;
    private String mc_ip, mdb_ip, mdr_ip;
    private int mc_port, mdb_port, mdr_port;
    private String peerId;
    private String version;

    public Peer(String version, String peerId, String mc_ip, String mdb_ip, String mdr_ip, int mc_port, int mdb_port, int mdr_port) throws IOException {
        super();

        this.version = version;
        this.peerId = peerId;
        this.mc_ip=mc_ip;
        this.mc_port=mc_port;
        this.mdb_ip=mdb_ip;
        this.mdb_port=mdb_port;
        this.mdr_ip=mdr_ip;
        this.mdr_port=mdr_port;

        backupChannel = new MDB(mdb_ip, mdb_port, peerId);
        restoreChannel = new MDR(mdr_ip, mdr_port);
        controlChannel = new MC(mc_ip,mc_port);


        //Creates peer "disk" storage
        File dir = new File(peerId);
        dir.mkdir();

        backupChannel.listen();
        restoreChannel.listen();
        controlChannel.listen();
    }

    public void backup(String file, int replicationDegree){

        Backup backup = new Backup(version,peerId, file, replicationDegree, mdb_ip, mdb_port);
        backup.readChunks();

        System.out.println("Finished backup");

    }

    public void restore(String file){
        System.out.println(file);
    }

}
