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
import static Utilities.Constants.DELETED;
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

    public void getFileId() {
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

    public void updateRepDeg(String file) {

        for (Map.Entry<String, String[]> entry : peer.getMapChunkIdToFileAndChunkNo().entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (Objects.equals(value[0], file)) {
                peer.removeChunkFromStoredChunks(key);
                peer.removeFromChunksReplicationDegree(key);
                peer.removeMapingChunkIdToFileAndChunkNo(key);
            }

        }

        peer.saveRepDegInfoToDisk();
    }

    public void deleteChunks(String fileId) {

        String path = "./" + peer.getPeerId() + "/" + fileId;
        File file = new File(path);
        deleteFolder(file);
        if (peer.getDeleteProtocol() != null)
            peer.getDeleteProtocol().updateRepDeg(fileId);
    }

    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
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


        Message request = new Message(DELETED, peer.getVersion(), peer.getPeerId(), fileId);
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();

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

    public void deliverDeletedMessageEnhanced(Message message) {

        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), DELETED, peer);
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

