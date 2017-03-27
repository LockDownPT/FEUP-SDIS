package Message;

import Peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static Utilities.Constants.*;

public class Mailman {

    private Message message;
    private Thread thread;
    private Peer peer;


    public Mailman(DatagramPacket message, Peer creator) {
        DatagramPacket request = message;
        this.message = new Message(request);
        this.thread = new ReceiverThread();
        this.peer = creator;
    }

    public Mailman(Message message, Peer creator) {
        this.message = message;
        this.thread = new SenderThread();
        this.peer = creator;
    }

    public void startMailmanThread() {
        this.thread.start();
    }

    public Message getMessage() {
        return message;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public class SenderThread extends Thread {
        public void run() {
            System.out.println("Sended request:" + message.getMessageHeader().getMessageType());
            switch (message.getMessageHeader().getMessageType()) {
                case PUTCHUNK:
                    peer.getBackupProtocol(message.getMessageHeader().getFileId()).deliverPutchunkMessage(this, message);
                    break;
                case STORED:
                    peer.getBackupProtocol(message.getMessageHeader().getFileId()).deliverStoredMessage(this, message);
                    break;
                case GETCHUNK:
                    peer.getRestoreProtocol().deliverGetchunkMessage(this, message);
                    break;
                case CHUNK:
                    peer.getRestoreProtocol().deliverChunkMessage(this, message);
                    break;
                default:
                    break;
            }
        }

        /**
         * Delivers a message with the following format:
         * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> [<ReplicationDeg>] <CRLF><CRLF>[<Body>]
         * to the specified multicast channel
         *
         * @param message     message containing the necessary info
         * @param addr        address of the multicast channel
         * @param port        port of the multicast channel
         * @param messageType message type, in case of PUTCHUNK, the type is necessary for retrieving the body of the message
         */
        public void deliverMessage(Message message, String addr, int port, String messageType) {

            DatagramSocket socket;
            DatagramPacket packet;

            try {
                socket = new DatagramSocket();
                byte[] buf = message.getMessageBytes(messageType);
                InetAddress address = InetAddress.getByName(addr);
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ReceiverThread extends Thread {
        public void run() {
            System.out.println("Received request:" + message.getMessageHeader().getMessageType());
            //Ignores requests sent by itself
            if (message.getMessageHeader().getSenderId().equals(peer.getPeerId()))
                return;
            switch (message.getMessageHeader().getMessageType()) {
                case PUTCHUNK:
                    peer.getBackupProtocol(message.getMessageHeader().getFileId()).handlePutchunk(message);
                    break;
                case STORED:
                    peer.increaseReplicationDegree(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo());
                    break;
                case GETCHUNK:
                    peer.getRestoreProtocol().sendChunk(message);
                    break;
                case CHUNK:
                    peer.getRestoreProtocol().saveChunk(message);
                    break;
                default:
                    break;
            }
        }
    }

}
