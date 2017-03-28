package Peer;

import Channels.MC;
import Channels.MDB;
import Channels.MDR;
import Subprotocols.Backup;
import Subprotocols.Restore;
import Subprotocols.Delete;
import Subprotocols.SpaceReclaim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Peer extends UnicastRemoteObject implements PeerInterface {

    private Restore restoreProtocol = null;
    private Delete deleteProtocol = null;
    private Backup backup = null;
    private SpaceReclaim spaceReclaimProtocol = null;
    private Map<String, Backup> backupProtocol = new ConcurrentHashMap<>();
    private String mc_ip, mdb_ip, mdr_ip;
    private int mc_port, mdb_port, mdr_port;
    private String peerId;
    private String version;
    private long usedSpace = 0;
    private long diskSpace = 5*64000;

    /**
     * String is the chunkId = fileId+ChunkNo
     * String array has in the first element the fileId and in the second the ChunkNo
     */
    private Map<String, String[]> mapChunkIdToFileAndChunkNo = new ConcurrentHashMap<>();
    /**
     * String is a par of fileId+chunkNo
     * String holds the desired replication degree
     */
    private Map<String, String> storedChunks = new ConcurrentHashMap<>();

    //TODO: MAke this a tree map
    /**
     * Holds information about chunks replication degree in the network
     * String is a par of fileId+chunkNo
     * String holds the current replication degree
     */
    private Map<String, String> chunksReplicationDegree = new ConcurrentHashMap<>();
    /**
     * Holds information regarding if the chunk has been sent
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

        //loads information about chunks replication degree (if such exists)
        loadRepDegFromDisk();

        //Launches a thread for each channel to listen for requests
        backupChannel.listen();
        restoreChannel.listen();
        controlChannel.listen();


        this.backup = new Backup(this);
        this.restoreProtocol = new Restore(this);
        this.deleteProtocol = new Delete(this);
    }

    /***
     * Starts backup protocol
     * @param file file to backup
     * @param replicationDegree  desired replication degree of the file
     */
    public void backup(String file, int replicationDegree) {


        this.backup = new Backup(file, replicationDegree, this);

        //Reads chunks from a file and sends chunks to backup broadcast channel
        backup.readChunks();

        //Starts backup protocol
        backupProtocol.put(backup.getFileId(), backup);

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

    public void spaceReclaim(long spaceToBeReclaimed){

        spaceReclaimProtocol = new SpaceReclaim(this, spaceToBeReclaimed);

        spaceReclaimProtocol.start();

    }



    /***
     * Starts delete protocol
     * @param file
     */
    public void delete(String file){

        //Starts delete protocol
        deleteProtocol = new Delete(file, this);


        deleteProtocol.deleteChunks();


        System.out.println("Finished Reading Chunks");

    }

    public void updateRepDeg(String fileId){
        for (Map.Entry<String, String[]> entry : mapChunkIdToFileAndChunkNo.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            String degree;
            degree = chunksReplicationDegree.get(key);
            String key2 = ""+value[0]+value[1];
            if (value[0] == fileId) {
                storedChunks.remove(key2);
                degree = "0";
                mapChunkIdToFileAndChunkNo.remove(key2);

            }
            chunksReplicationDegree.replace(key2,"0");
        }

        saveRepDegInfoToDisk();

    }

    /**
     * This operation allows to observe the service state. In response to such a request, the peer shall send to the client the following information:
     * For each file whose backup it has initiated:
     * The file pathname ✓
     * The backup service id of the file ✓
     * The desired replication degree ✓
     * For each chunk of the file:
     * Its id ✓
     * Its perceived replication degree ✓
     * For each chunk it stores:
     * Its id  ✓
     * Its size (in KBytes)
     * Its perceived replication degree ✓
     * The peer's storage capacity, i.e. the maximum amount of disk space that can be used to store chunks, and the amount of storage (both in KBytes) used to backup the chunks.
     */
    public void state() {

        String[] state = new String[1024];

        int i = 0;
        for (Backup b : backupProtocol.values()) {
            i++;
            state[i] = "File pathname: " + b.getPeer().peerId + "/" + b.getFileName();
            i++;
            state[i] = "Backup service id: " + b.getPeer().peerId;
            i++;
            state[i] = "Desired Replication degree: " + b.getReplicationDegree();

            for (int n = 1; n <= b.getNumberOfChunks(); n++) {
                i++;
                state[i] = "Chunk id: " + b.getFileId() + n;
                i++;
                state[i] = "Perceived replication degree: " + chunksReplicationDegree.get(b.getFileId() + n);
            }
        }
        for (Map.Entry<String, String> entry : storedChunks.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            i++;
            state[i] = "Stored chunk: " + key;
            i++;
            state[i] = "Perceived replication degree: " + value;

        }
        i++;
        state[i] = "Storage capacity = " + getStorageSpace() + " | Used space: " + getUsedSpace();

        for (String s : state) {
            if (s != null)
                System.out.println(s);
        }
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
        this.addChunkIdToRegistry(fileId,chunkNo);

    }

    /**
     * Increases registry about the number of times a chunk has been replicated
     *
     * @param fileId
     */
    public void increaseReplicationDegree(String fileId, String chunkNo) {
        String chunkId=fileId+chunkNo;
        addChunkIdToRegistry(fileId,chunkNo);
        String currentReplicationDegree = chunksReplicationDegree.get(chunkId);

        if (currentReplicationDegree == null) {
            chunksReplicationDegree.put(chunkId, "1");
            //System.out.println("Replication degree of: "+fileId);
            //System.out.println("1");
        } else {
            int temp = Integer.parseInt(currentReplicationDegree);
            chunksReplicationDegree.put(chunkId, String.valueOf(temp + 1));
            //System.out.println("Replication degree of: "+fileId);
            //System.out.println(String.valueOf(temp+1));
        }

        saveRepDegInfoToDisk();
    }
    /**
     * Decreases registry about the number of times a chunk has been replicated
     *
     * @param fileId
     */
    public void decreaseReplicationDegree(String fileId, String chunkNo) {
        String chunkId=fileId+chunkNo;
        String currentReplicationDegree = chunksReplicationDegree.get(chunkId);

        if (currentReplicationDegree != null) {
            int temp = Integer.parseInt(currentReplicationDegree);
            chunksReplicationDegree.put(chunkId, String.valueOf(temp - 1));
        }
        saveRepDegInfoToDisk();
    }


    /**
     * Saves information about chunks replication degree to non-volatile memory
     */
    public void saveRepDegInfoToDisk() {
        Properties properties = new Properties();

        properties.putAll(chunksReplicationDegree);

        try {
            properties.store(new FileOutputStream(peerId + "/chunksRepDeg.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads information about chunks replication degree (if such exists)
     */
    private void loadRepDegFromDisk() {

        File f = new File(peerId + "/chunksRepDeg.properties");
        if (f.exists() && !f.isDirectory()) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(peerId + "/chunksRepDeg.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String key : properties.stringPropertyNames()) {
                chunksReplicationDegree.put(key, properties.get(key).toString());
            }
        }
    }

    /**
     * Check the replication degree of a certain chunk
     *
     * @param fileId  Id of the file that the chunk belongs to
     * @param chunkNo Chunk number
     * @return returns the replication degree of the chunk
     */
    public int getReplicationDegreeOfChunk(String fileId, String chunkNo) {

        if (chunksReplicationDegree.get(fileId + chunkNo) != null) {
            return Integer.parseInt(chunksReplicationDegree.get(fileId + chunkNo));
        } else {
            return 0;
        }
    }
    /**
     * Check the replication degree of a certain chunk
     *
     * @param key  Id of the file + chunkNo
     * @return returns the replication degree of the chunk
     */
    public int getReplicationDegreeOfChunk(String key) {

        if (chunksReplicationDegree.get(key) != null) {
            return Integer.parseInt(chunksReplicationDegree.get(key));
        } else {
            return 0;
        }
    }


    /**
     * Verifies if the peer has this chunk
     *
     * @param fileId  Id of the file that the chunk belongs to
     * @param chunkNo Chunk number
     * @return returns true if the peer has the chunk and false otherwise
     */
    public boolean hasChunk(String fileId, String chunkNo) {

        return storedChunks.get(fileId + chunkNo) != null;

    }

    /**
     * Reads chunk from the disk
     *
     * @param fileId  Id of the file that the chunk belongs to
     * @param chunkNo Chunk number
     * @return returns a byte array with the chunk
     */
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

    /**
     * Adds boolean to sentChunks registry saying that the chunk has been sent
     *
     * @param fileId  Id of the file that the chunk belongs to
     * @param chunkNo Chunk number
     */
    public void addSentChunkInfo(String fileId, String chunkNo) {
        sentChunks.put(fileId + chunkNo, true);
    }


    /**
     * Returns true if it has a record that a peer has already sent the requested chunk
     *
     * @param fileId  Id of the file that the chunk belongs to
     * @param chunkNo Chunk number
     * @return Returns true if it has a record that a peer has already sent the requested chunk
     */
    public boolean hasChunkBeenSent(String fileId, String chunkNo) {
        return sentChunks.get(fileId + chunkNo) != null;

    }

    public Map<String, String> getStoredChunks() {
        return storedChunks;
    }

    public void setStoredChunks(Map<String, String> storedChunks) {
        this.storedChunks = storedChunks;
    }

    public Map<String, String> getChunksReplicationDegree() {
        return chunksReplicationDegree;
    }

    public SpaceReclaim getSpaceReclaimProtocol() {
        return spaceReclaimProtocol;
    }

    public void setSpaceReclaimProtocol(SpaceReclaim spaceReclaimProtocol) {
        this.spaceReclaimProtocol = spaceReclaimProtocol;
    }

    public void addChunkIdToRegistry(String fileId, String chunkNo){
        String[] parFileIdChunkNo = new String[2];
        parFileIdChunkNo[0]=fileId;
        parFileIdChunkNo[1]=chunkNo;
        this.mapChunkIdToFileAndChunkNo.put(fileId+chunkNo,parFileIdChunkNo);
    }
    
    public String getFileIdFromChunkId(String chunkId){
        return mapChunkIdToFileAndChunkNo.get(chunkId)[0];
    }

    public String getChunkNoFromChunkId(String chunkId){
        return mapChunkIdToFileAndChunkNo.get(chunkId)[1];
    }

    private long getStorageSpace() {
        return this.diskSpace;
    }

    public Backup getBackup() {
        return this.backup;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
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

    public Delete getDeleteProtocol() {
        return deleteProtocol;
    }

}
