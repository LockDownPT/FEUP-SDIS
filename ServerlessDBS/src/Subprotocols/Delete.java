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


    /**
     * This function starts the DELETE protocol by sendig the DELETE request and updating the
     * Replication Degree of the deleted file.
     */
    public void start() {
        setFileId();
        Message request = new Message(DELETE, peer.getVersion(), peer.getPeerId(), this.fileId);
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
        updateRepDeg(this.fileId);
    }


    /**
     * This function updates the Replication Degree of the deleted file and the related chunks.
     * @param file
     */
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



    /**
     *  After received tha DELETE request, the peer deletes the folder with the given fileId
     *  and updates the Replication Degree.
     * @param fileId
     */
    public void deleteChunks(String fileId) {

        String path = "./" + peer.getPeerId() + "/" + fileId;
        File file = new File(path);
        deleteFolder(file);
        if (peer.getDeleteProtocol() != null) {
            peer.getDeleteProtocol().updateRepDeg(fileId);
        }

    }

    /**
     * This function delete all the chunks of the folder and update the size of the peer.
     * @param folder
     */
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
     * Sends DELETE request for the multicast control channel (MC) with the following format:
     * DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
     * It sends 3 DELETE requests to ensure that all peers receive it
     */
    public void deliverDeleteMessage(Message message) {
        for (int i = 0; i < 3; i++) {
            Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), DELETE, peer);
            mailman.startMailmanThread();
        }

    }


    /**
     * When a peer starts to run, it sends
     * on the multicast control channel (MC) a message to announce that it's alive with the following format:
     * ALIVE <Version> <SenderId> <CRLF><CRLF>
     */
    public void sendAliveMessage(){

        Message request = new Message(ALIVE, peer.getVersion(), peer.getPeerId());
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }


    /**
     * When a peer starts to run, it sends
     * on the multicast control channel (MC) a message to announce that it's alive with the following format:
     * ALIVE <Version> <SenderId> <CRLF><CRLF>
     * @param message
     */
    public void deliverAliveMessage(Message message) {

        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), ALIVE, peer);
        mailman.startMailmanThread();
    }


    /**
     * When a peer receive a ALIVE request, it checks all the delete messages that it already sent
     * and resend all of it.
     */
    public void resendDeleteMessage() {

        for (Map.Entry<String, Message> entry : peer.getStackDeleteMessage().entrySet()) {
            Message message = entry.getValue();
            deliverDeleteMessage(message);
        }
    }

    /**
     * This function set the parameter fileID.
     */
    private void setFileId() {

        String path = "./TestFiles/" + this.fileName;
        File file = new File(path);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));
    }

}

