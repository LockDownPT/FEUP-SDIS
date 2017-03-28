package Message;

import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static Utilities.Constants.*;

public class Mailman {

    private Message message;
    private Peer peer;
    private String addr;
    private int port;
    private Runnable mailman;
    private String messageType;
    private String type;


    public Mailman(DatagramPacket message, Peer creator) {
        this.message = new Message(message);
        this.mailman = new ReceiverThread();
        this.type="RECEIVER";
        this.peer = creator;
    }

    public Mailman(Message message, Peer creator) {
        this.message = message;
        this.mailman = new SenderThread();
        this.type="SENDER";
        this.peer = creator;
    }

    public Mailman(Message message, String addr, int port, String messageType, Peer peer) {
        this.message = message;
        this.addr = addr;
        this.port = port;
        this.type="DELIVER";
        this.messageType = messageType;
        this.mailman = new DeliverMessageThread();
        this.peer = peer;
    }

    /**
     * Starts thread
     */
    public void startMailmanThread() {
        switch (type){
            case "SENDER":
                peer.getSenderExecutor().execute(mailman);
                System.out.println("SENDER");
                break;
            case "RECEIVER":
                peer.getReceiverExecutor().execute(mailman);
                System.out.println("RECEIVER");
                break;
            case "DELIVER":
                peer.getDeliverExecutor().execute(mailman);
                System.out.println("DELIVER");
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

    public class DeliverMessageThread implements Runnable {
        public void run() {
            deliverMessage(message, addr, port, messageType);
        }
    }

    public class SenderThread implements Runnable {
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

    public class ReceiverThread implements Runnable {
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
                    peer.getSpaceReclaimProtocol().increaseReceivedPUTCHUNK(message);
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
