package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;

import static Utilities.Constants.ALIVE;
import static Utilities.Constants.DELETE;
import static Utilities.Utilities.createHash;

public class Delete {

    private String fileName;
    private String fileId;

    private Peer peer;

    public Delete(String file, Peer peer) {
        this.fileName = file;
        this.peer = peer;

    }

    public Delete(Peer peer) {
        this.peer = peer;
    }

    private void getFileId() {
        String path = "./TestFiles/" + this.fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));
    }

    public void deleteChunks() {
        getFileId();
        Message request = new Message(DELETE, peer.getVersion(), peer.getPeerId(), this.fileId);
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
        updateRepDeg(this.fileId);
    }

    private void updateRepDeg(String file) {

        for (Map.Entry<String, String> entry : peer.getChunksReplicationDegree().entrySet()) {
            String key = entry.getKey();
            String value = peer.getFileIdFromChunkId(entry.getKey());
            if (Objects.equals(value, file)) {
                peer.removeChunkFromStoredChunks(key);
                peer.removeFromChunksReplicationDegree(key);
            }
        }
        peer.saveMetadataToDisk();
    }

    public void deleteChunks(String fileId) {

        String path = "./" + peer.getPeerId() + "/" + fileId;
        File file = new File(path);
        deleteFolder(file);
        if (peer.getDeleteProtocol() != null) {
            peer.getDeleteProtocol().updateRepDeg(fileId);
        }

    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    peer.setUsedSpace(peer.getUsedSpace() - (int) f.length());
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     *
     */
    public void deliverDeleteMessage(Message message) {
        for (int i = 0; i < 3; i++) {
            Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), DELETE, peer);
            mailman.startMailmanThread();
        }

    }


    public void deliverDeleteMessageEhnanced(Message message) {
        for (int i = 0; i < 3; i++) {
            Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), DELETE, peer);
            mailman.startMailmanThread();
        }
    }

    public void deleteChunksEnhanced(Message message) {
        fileId = message.getMessageHeader().getFileId();
        String path = "./" + peer.getPeerId() + "/" + fileId;
        File file = new File(path);
        deleteFolder(file);
        if (peer.getDeleteProtocol() != null)
            peer.getDeleteProtocol().updateRepDeg(fileId);



    }

    public void sendAliveMessage(){

        Message request = new Message(ALIVE, peer.getVersion(), peer.getPeerId());
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }

    public void deliverAliveMessage(Message message) {

        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), ALIVE, peer);
        mailman.startMailmanThread();
    }



    public void resendDeleteMessage() {

        for (Map.Entry<String, Message> entry : peer.getStackDeleteMessage().entrySet()) {
            String key = entry.getKey();
            Message message = entry.getValue();

            deliverDeleteMessage(message);

        }

    }
}

