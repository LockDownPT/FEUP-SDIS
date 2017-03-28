package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static Utilities.Constants.PUTCHUNK;
import static Utilities.Constants.REMOVED;

public class SpaceReclaim {

    private Peer peer;
    private long spaceToBeReduced = 0;
    private int receivedPUCHUNK =0;
    private String chunkId=null;
    public SpaceReclaim(Peer peer, long spaceToBeReduced) {
        this.peer = peer;
        this.spaceToBeReduced = spaceToBeReduced;
    }

    public boolean updatePeerStorage() {
        long storageSpace = peer.getStorageSpace();
        long usedSpace = peer.getUsedSpace();
        long freeSpace = storageSpace - usedSpace;

        if ((freeSpace - this.spaceToBeReduced) > 0) {
            peer.setStorageSpace(storageSpace - spaceToBeReduced);
            return false;
        }else{
            peer.setStorageSpace(storageSpace - spaceToBeReduced);
            spaceToBeReduced-=freeSpace;
        }
        return true;
    }

    public void start() {
        if (updatePeerStorage()) {
            if (findExtraChunks()) {
                System.out.println("Deleted Extra chunks");
            } else
                while (!removeChunksWithLowerRepDeg()) {
                    System.out.println("Removing chunks with lower replication degree");
                }
            System.out.println("Deleted chunks with lower replication degree");
            if (spaceToBeReduced != 0)
                removeChunksWithOneRepDeg();
            System.out.println("Finished Reclaim Space");
        }
    }


    public boolean findExtraChunks() {

        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int tempRepDeg = peer.getReplicationDegreeOfChunk(key);
            while (tempRepDeg > Integer.parseInt(value)) {
                removeChunk(key);
                sendRemovedMessage(key);
                tempRepDeg--;
            }
            if (spaceToBeReduced == 0)
                return true;
        }
        return false;
    }

    public boolean removeChunksWithLowerRepDeg() {
        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();

            int tempRepDeg = peer.getReplicationDegreeOfChunk(key);
            if (tempRepDeg > 1) {
                removeChunk(key);
                sendRemovedMessage(key);
            }
            if (spaceToBeReduced == 0)
                return true;
        }
        return false;
    }

    public void removeChunksWithOneRepDeg() {
        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();
            removeChunk(key);
            sendRemovedMessage(key);
            if (spaceToBeReduced == 0)
                return;
        }
    }

    public boolean removeChunk(String chunkId) {

        Path path = FileSystems.getDefault().getPath("./"+peer.getPeerId()+"/"+peer.getFileIdFromChunkId(chunkId)+"/", peer.getChunkNoFromChunkId(chunkId));
        long chunkSize = 0;
        try {
            chunkSize = Files.size(path);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Error deleting chunk: " + chunkId + "|| Error getting chunk size:" + chunkSize);
            return false;
        }

        peer.setUsedSpace(peer.getUsedSpace()-chunkSize);
        this.spaceToBeReduced-=chunkSize;

        return true;
    }

    public void sendRemovedMessage(String chunkId) {

        Message message = new Message(REMOVED, peer.getVersion(), peer.getPeerId(), peer.getFileIdFromChunkId(chunkId), peer.getChunkNoFromChunkId(chunkId));
        Mailman mailman = new Mailman(message, peer);
        mailman.startMailmanThread();

    }

    public void deliverRemovedMessage(Message message) {

        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), REMOVED);
        mailman.startMailmanThread();

    }

    public void updateChunkRepDegree(Message message) {

        peer.decreaseReplicationDegree(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
        int currentRepDeg = peer.getReplicationDegreeOfChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
        int desiredRepDeg = peer.getDesiredReplicationDegree(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo());

        if (peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
            if (currentRepDeg < desiredRepDeg) {
                try {
                    receivedPUCHUNK=0;
                    chunkId=message.getMessageHeader().getFileId()+message.getMessageHeader().getChunkNo();
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if(receivedPUCHUNK==1){
                        Message messagePUTCHUNK = new Message(PUTCHUNK, peer.getVersion(), peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo(), String.valueOf(desiredRepDeg));
                        peer.getBackup().deliverPutchunkMessage(messagePUTCHUNK);
                    }
                    receivedPUCHUNK=0;
                }
            }
        }

    }

    public void increaseReceivedPUTCHUNK(Message message){
        if(chunkId.equals(message.getMessageHeader().getFileId()+message.getMessageHeader().getChunkNo())){
            receivedPUCHUNK=1;
        }

    }
}
