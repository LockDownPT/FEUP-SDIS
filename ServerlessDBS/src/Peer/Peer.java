package Peer;

import Channels.MC;
import Channels.MDB;
import Channels.MDR;
import Subprotocols.Backup;
import Subprotocols.Restore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Peer extends UnicastRemoteObject implements PeerInterface {

    private Restore restoreProtocol = null;
    private Backup backupProtocol = null;
    private String mc_ip, mdb_ip, mdr_ip;
    private int mc_port, mdb_port, mdr_port;
    private String peerId;
    private String version;
    /**
     * String is a par of fileId+chunkNo
     * String holds the desired replication degree
     */
    private Map<String, String> storedChunks = new ConcurrentHashMap<>();
    /**
     * String is a par of fileId+chunkNo
     * String holds the current replication degree
     */
    private Map<String, String> chunksReplicationDegree = new ConcurrentHashMap<>();
    /**
     * String is a par of fileId+chunkNo
     * Boolean holds the current replication degree
     */
    private Map<String, Boolean> sentChunks = new ConcurrentHashMap<>();


    public Peer(String version, String peerId, String mc_ip, String mdb_ip, String mdr_ip, int mc_port, int mdb_port, int mdr_port) throws IOException {
        super();

        this.version = version;
        this.peerId = peerId;
        this.mc_ip = mc_ip;
        this.mc_port = mc_port;
        this.mdb_ip = mdb_ip;
        this.mdb_port = mdb_port;
        this.mdr_ip = mdr_ip;
        this.mdr_port = mdr_port;

        MDB backupChannel = new MDB(mdb_ip, mdb_port, this);
        MDR restoreChannel = new MDR(mdr_ip, mdr_port, this);
        MC controlChannel = new MC(mc_ip, mc_port, this);


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
     * @param file file to backup
     * @param replicationDegree  desired replication degree of the file
     */
    public void backup(String file, int replicationDegree) {

        //Starts backup protocol
        backupProtocol = new Backup(file, replicationDegree, this);

        //Reads chunks from a file and sends chunks to backup broadcast channel
        backupProtocol.readChunks();

        System.out.println("Finished Reading Chunks");

    }

    /**
     * Starts restore protocol
     *
     * @param file file to be restored
     */
    public void restore(String file) {

        restoreProtocol = new Restore(file, this);

        restoreProtocol.start();

        System.out.println("Restore completed");

    }

    /**
     * Adds a string with the par, fileId and chunkNo,
     * identifying a stored chunk and the desired replication degree
     *
     * @param fileId
     * @param chunkNo
     * @param desiredReplicationDegree
     */
    public void addChunkToRegistry(String fileId, String chunkNo, String desiredReplicationDegree) {

        this.storedChunks.put(fileId + chunkNo, desiredReplicationDegree);

    }

    public void increaseReplicationDegree(String fileId) {

        String currentReplicationDegree = chunksReplicationDegree.get(fileId);

        if (currentReplicationDegree == null) {
            chunksReplicationDegree.put(fileId, "1");
            //System.out.println("Replication degree of: "+fileId);
            //System.out.println("1");
        } else {
            int temp = Integer.parseInt(currentReplicationDegree);
            chunksReplicationDegree.put(fileId, String.valueOf(temp + 1));
            //System.out.println("Replication degree of: "+fileId);
            //System.out.println(String.valueOf(temp+1));
        }

    }

    public int getReplicationDegreeOfChunk(String fileId, String chunkNo) {

        if (chunksReplicationDegree.get(fileId + chunkNo) != null) {
            return Integer.parseInt(chunksReplicationDegree.get(fileId + chunkNo));
        } else {
            return 0;
        }


    }

    public boolean hasChunk(String fileId, String chunkNo) {

        return storedChunks.get(fileId + chunkNo) != null;

    }

    public byte[] getChunk(String fileId, String chunkNo) {
        byte[] chunk = null;

        Path path = Paths.get(peerId + "/" + fileId + "/" + chunkNo);
        try {
            chunk = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunk;
    }

    public void addSentChunkInfo(String fileId, String chunkNo) {
        sentChunks.put(fileId + chunkNo, true);
    }

    public boolean hasChunkBeenSent(String fileId, String chunkNo) {
        return sentChunks.get(fileId + chunkNo) != null;

    }

    public Restore getRestoreProtocol() {
        return restoreProtocol;
    }

    public String getMc_ip() {
        return mc_ip;
    }

    public String getMdb_ip() {
        return mdb_ip;
    }

    public String getMdr_ip() {
        return mdr_ip;
    }

    public int getMc_port() {
        return mc_port;
    }

    public int getMdb_port() {
        return mdb_port;
    }

    public int getMdr_port() {
        return mdr_port;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getVersion() {
        return version;
    }

}
