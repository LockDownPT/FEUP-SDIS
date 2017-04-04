package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static Utilities.Constants.PUTCHUNK;
import static Utilities.Constants.REMOVED;

public class SpaceReclaim {

    private Peer peer;
    private int spaceToBeReduced = 0;
    private int receivedPUTCHUNK = 0;
    private String chunkId = null;

    public SpaceReclaim(Peer peer, int spaceToBeReduced) {
        this.peer = peer;
        this.spaceToBeReduced = spaceToBeReduced;
    }

    public SpaceReclaim(Peer peer) {
        this.peer = peer;
    }

    private boolean updatePeerStorage() {
        int storageSpace = peer.getStorageSpace();
        int usedSpace = peer.getUsedSpace();
        int freeSpace = storageSpace - usedSpace;

        if ((freeSpace - this.spaceToBeReduced) > 0) {
            peer.setStorageSpace(storageSpace - spaceToBeReduced);
            return false;
        } else {
            peer.setStorageSpace(storageSpace - spaceToBeReduced);
            spaceToBeReduced -= freeSpace;
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


    private boolean findExtraChunks() {

        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int tempRepDeg = peer.getReplicationDegreeOfChunk(key);
            while (tempRepDeg > Integer.parseInt(value)) {
                removeChunk(key);
                tempRepDeg--;
            }
            if (spaceToBeReduced == 0)
                return true;
        }
        return false;
    }

    private boolean removeChunksWithLowerRepDeg() {
        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();

            int tempRepDeg = peer.getReplicationDegreeOfChunk(key);
            if (tempRepDeg > 1) {
                removeChunk(key);
            }
            if (spaceToBeReduced == 0)
                return true;
        }
        return false;
    }

    private void removeChunksWithOneRepDeg() {
        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();
            removeChunk(key);
            if (spaceToBeReduced == 0)
                return;
        }
    }

    private void removeChunk(String chunkId) {

        System.out.println("CHUNK ID: " + chunkId);

        Path path = FileSystems.getDefault().getPath("./" + peer.getPeerId() + "/" + peer.getFileIdFromChunkId(chunkId) + "/", peer.getChunkNoFromChunkId(chunkId));
        int chunkSize = 0;
        try {
            chunkSize = (int) Files.size(path);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Error deleting chunk: " + chunkId + "|| Error getting chunk size:" + chunkSize);
            return;
        }

        sendRemovedMessage(chunkId);

        peer.decreaseReplicationDegree(peer.getFileIdFromChunkId(chunkId), peer.getChunkNoFromChunkId(chunkId));
        peer.removeChunkFromStoredChunks(chunkId);
        peer.setUsedSpace(peer.getUsedSpace() - chunkSize);
        this.spaceToBeReduced -= chunkSize;

    }

    private void sendRemovedMessage(String chunkId) {

        Message message = new Message(REMOVED, peer.getVersion(), peer.getPeerId(), peer.getFileIdFromChunkId(chunkId), peer.getChunkNoFromChunkId(chunkId));
        Mailman mailman = new Mailman(message, peer);
        mailman.startMailmanThread();

    }

    public void deliverRemovedMessage(Message message) {

        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), REMOVED, peer);
        mailman.startMailmanThread();

    }

    public void updateChunkRepDegree(Message message) {

        peer.decreaseReplicationDegree(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
        int currentRepDeg = peer.getReplicationDegreeOfChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
        int desiredRepDeg = peer.getDesiredReplicationDegree(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo());

        if (peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
            if (currentRepDeg < desiredRepDeg) {
                try {
                    receivedPUTCHUNK = 0;
                    chunkId = message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo();
                    Thread.sleep((long) (Math.random() * 400));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (receivedPUTCHUNK == 0) {
                        Message messagePUTCHUNK = new Message(PUTCHUNK, peer.getVersion(), peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo(), String.valueOf(desiredRepDeg));
                        peer.getBackup().deliverPutchunkMessage(messagePUTCHUNK);
                    }
                    receivedPUTCHUNK = 0;
                }
            }
        }

    }

    public void increaseReceivedPUTCHUNK(Message message) {
        if (chunkId != null) {
            if (chunkId.equals(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo())) {
                receivedPUTCHUNK = 1;
            }
        }

    }
}
