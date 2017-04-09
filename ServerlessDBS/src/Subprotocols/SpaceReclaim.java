package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Utilities.Constants.REMOVED;

public class SpaceReclaim {

    private Peer peer;
    private int spaceToBeReduced = 0;
    private Map<String, Boolean> receivedPutchunks = new ConcurrentHashMap<>();

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

        if (this.spaceToBeReduced > peer.getStorageSpace()) {
            this.spaceToBeReduced = peer.getStorageSpace();
        }

        if ((freeSpace - this.spaceToBeReduced) > 0) {
            peer.setStorageSpace(storageSpace - spaceToBeReduced);
            return false;
        } else {
            spaceToBeReduced -= freeSpace;
            peer.setStorageSpace(peer.getStorageSpace() - freeSpace);
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
            if (spaceToBeReduced >= 0)
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
            System.out.println("SPACE TO BE RECLAIMED" + this.spaceToBeReduced);

            if (spaceToBeReduced <= 0 || spaceToBeReduced > peer.getStorageSpace())
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
            if (spaceToBeReduced <= 0 || spaceToBeReduced > peer.getStorageSpace())
                return true;
        }
        return false;
    }

    private void removeChunksWithOneRepDeg() {
        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();
            removeChunk(key);
            if (spaceToBeReduced <= 0 || spaceToBeReduced > peer.getStorageSpace())
                return;
        }
    }

    public void removeChunk(String chunkId) {

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
        peer.setStorageSpace(peer.getStorageSpace() - chunkSize);
        this.spaceToBeReduced -= chunkSize;
        peer.saveMetadataToDisk();

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
                startBackupProtocol(message, desiredRepDeg);
            }
        }

    }

    public void startBackupProtocol(Message message, int desiredRepDeg) {
        try {
            receivedPutchunks.put(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo(), false);
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!receivedPutchunks.get(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo())) {
                peer.getBackup().setReplicationDegree(desiredRepDeg);
                peer.getBackup().setFileId(message.getMessageHeader().getFileId());
                Path path = Paths.get(peer.getPeerId() + "/" + message.getMessageHeader().getFileId() + "/" + message.getMessageHeader().getChunkNo());
                byte[] data = new byte[0];
                try {
                    data = Files.readAllBytes(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                peer.getBackup().sendChunk(data, Integer.parseInt(message.getMessageHeader().getChunkNo()));
            }
        }
    }

    public void increaseReceivedPUTCHUNK(Message message) {

        receivedPutchunks.put(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo(), true);

    }
}
