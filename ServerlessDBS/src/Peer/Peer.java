package Peer;

import Channels.MC;
import Channels.MDB;
import Channels.MDR;
import Subprotocols.Backup;
import Subprotocols.Restore;

import java.io.File;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Peer extends UnicastRemoteObject implements PeerInterface{

    private MC controlChannel;
    private MDB backupChannel;
    private MDR restoreChannel;
    private String mc_ip, mdb_ip, mdr_ip;
    private int mc_port, mdb_port, mdr_port;
    private String peerId;
    private String version;
    /**
     * String is a par of fileId+chunkNo
     * int holds the desired replication degree
     */
    private Map<String,String> storedChunks = new ConcurrentHashMap<>();
    /**
     * String is a par of fileId+chunkNo
     * int holds the current replication degree
     */
    private Map<String,String> chunksReplicationDegree = new ConcurrentHashMap<>();


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

        backupChannel = new MDB(mdb_ip, mdb_port, mc_ip, mc_port, peerId, this);
        restoreChannel = new MDR(mdr_ip, mdr_port, this);
        controlChannel = new MC(mc_ip,mc_port, peerId, this);


        //Creates peer "disk storage"
        File dir = new File(peerId);
        dir.mkdir();

        //Launches a thread for each channel to listen for requests
        backupChannel.listen();
        restoreChannel.listen();
        controlChannel.listen();
    }

    /***
     * Starts backup protocol
     * @param file
     * @param replicationDegree
     */
    public void backup(String file, int replicationDegree){

        //Starts backup protocol
        Backup backup = new Backup(file, replicationDegree,this);

        //Reads chunks from a file and sends chunks to backup broadcast channel
        backup.readChunks();

        System.out.println("Finished Reading Chunks");

    }


    //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>


    public void restore(String file){

        Restore restore = new Restore(file, this);

        restore.start();

        System.out.println("Restore completed");

    }

    /**
     * Adds a string with the par, fileId and chunkNo,
     * identifying a stored chunk and the desired replication degree
     * @param fileId
     * @param chunkNo
     * @param desiredReplicationDegree
     */
    public void addChunkToRegistry(String fileId, String chunkNo, String desiredReplicationDegree){

        this.storedChunks.put(fileId+chunkNo, desiredReplicationDegree);

    }

    public void increaseReplicationDegree(String fileId){

        String currentReplicationDegree = chunksReplicationDegree.get(fileId);

        if(currentReplicationDegree==null){
            chunksReplicationDegree.put(fileId,"1");
            System.out.println("Replication degree of: "+fileId);
            System.out.println("1");
        }else{
            int temp = Integer.parseInt(currentReplicationDegree);
            chunksReplicationDegree.put(fileId,String.valueOf(temp+1));
            System.out.println("Replication degree of: "+fileId);
            System.out.println(String.valueOf(temp+1));
        }

    }

    public int getReplicationDegreeOfChunk(String fileId, String chunkNo){

        if(chunksReplicationDegree.get(fileId+chunkNo)!=null){
            return Integer.parseInt(chunksReplicationDegree.get(fileId+chunkNo));
        }else{
            return 0;
        }


    }
    public boolean hasChunk(String fileId, String ChunkNo){

        if(storedChunks.get(fileId+ChunkNo)!=null)
            return true;
        else
            return false;

    }

    public String getMc_ip() {
        return mc_ip;
    }

    public void setMc_ip(String mc_ip) {
        this.mc_ip = mc_ip;
    }

    public String getMdb_ip() {
        return mdb_ip;
    }

    public void setMdb_ip(String mdb_ip) {
        this.mdb_ip = mdb_ip;
    }

    public String getMdr_ip() {
        return mdr_ip;
    }

    public void setMdr_ip(String mdr_ip) {
        this.mdr_ip = mdr_ip;
    }

    public int getMc_port() {
        return mc_port;
    }

    public void setMc_port(int mc_port) {
        this.mc_port = mc_port;
    }

    public int getMdb_port() {
        return mdb_port;
    }

    public void setMdb_port(int mdb_port) {
        this.mdb_port = mdb_port;
    }

    public int getMdr_port() {
        return mdr_port;
    }

    public void setMdr_port(int mdr_port) {
        this.mdr_port = mdr_port;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
