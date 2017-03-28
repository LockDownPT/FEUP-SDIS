package Message;

import Peer.Peer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static Utilities.Constants.*;

public class Mailman {

    private Message message;
    private Thread thread;
    private Peer peer;
    private String addr;
    private int port;
    private String messageType;


    public Mailman(DatagramPacket message, Peer creator) {
        this.message = new Message(message);
        this.thread = new ReceiverThread();
        this.peer = creator;
    }

    public Mailman(Message message, Peer creator) {
        this.message = message;
        this.thread = new SenderThread();
        this.peer = creator;
    }

    public Mailman(Message message, String addr, int port, String messageType) {
        this.message = message;
        this.addr = addr;
        this.port = port;
        this.messageType = messageType;
        this.thread = new DeliverMessageThread();
    }

    /**
     * Starts thread
     */
    public void startMailmanThread() {
        this.thread.start();
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
    private void deliverMessage(Message message, String addr, int port, String messageType) {

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

    public class DeliverMessageThread extends Thread {
        public void run() {
            deliverMessage(message, addr, port, messageType);
        }
    }

    public class SenderThread extends Thread {
        public void run() {
            System.out.println("Sended request:" + message.getMessageHeader().getMessageType());
            switch (message.getMessageHeader().getMessageType()) {
                case PUTCHUNK:
                    peer.getBackup().deliverPutchunkMessage(message);
                    break;
                case STORED:
                    if (peer.getVersion().equals("1.0"))
                        peer.getBackup().deliverStoredMessage(message);
                    else
                        peer.getBackup().deliverStoredMessageEnhanced(message);
                    break;
                case GETCHUNK:
                    peer.getRestoreProtocol().deliverGetchunkMessage(message);
                    break;
                case CHUNK:
                    peer.getRestoreProtocol().deliverChunkMessage(message);
                    break;
                case DELETE:
                    peer.getDeleteProtocol().deliverDeleteMessage(message);
                    break;
                case REMOVED:
                    peer.getSpaceReclaimProtocol().deliverRemovedMessage(message);
                    break;
                default:
                    break;
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
                    if (peer.getVersion().equals("1.0"))
                        peer.getBackup().storeChunk(message);
                    else
                        peer.getBackup().storeChunkEnhanced(message);
                    break;
                case STORED:
                    peer.increaseReplicationDegree(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                    break;
                case GETCHUNK:
                    peer.getRestoreProtocol().sendChunk(message);
                    break;
                case CHUNK:
                    peer.getRestoreProtocol().saveChunk(message);
                    break;
                case REMOVED:
                    peer.getSpaceReclaimProtocol().updateChunkRepDegree(message);
                    break;
                case DELETE:
                    peer.getDeleteProtocol().deleteChunks(message.getMessageHeader().getFileId());
                    break;
                default:
                    break;
            }
        }
    }




}
