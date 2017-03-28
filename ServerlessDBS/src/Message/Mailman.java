package Message;

import Peer.Peer;

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
                    deliverDeleteMessage();
                case REMOVED:
                    peer.getSpaceReclaimProtocol().deliverRemovedMessage(message);
                    break;
                default:
                    break;
            }
        }

        /**
         * Sends PUTCHUNK request for the multicast backup channel (MDB) with the following format:
         * PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
         * Then waits one second and checks if the desired replication degree
         * has been accomplished. Otherwise it resends the PUTCHUNK request, a maximum of 5 times.
         */
        public void deliverPutchunkMessage() {

            deliverMessage(message, peer.getMdb_ip(), peer.getMdb_port(), PUTCHUNK);

            int repDeg = 0;
            int numberOfTries = 0;
            while (repDeg < Integer.parseInt(message.getMessageHeader().getReplicationDeg()) && numberOfTries < 5) {
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    repDeg = peer.getReplicationDegreeOfChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                    if (repDeg < Integer.parseInt(message.getMessageHeader().getReplicationDeg()))
                        deliverMessage(message, peer.getMdb_ip(), peer.getMdb_port(), PUTCHUNK);
                    numberOfTries++;
                    System.out.println("Tentativa: " + numberOfTries);
                    System.out.println("RepDeg: " + repDeg);
                }
            }
            if (numberOfTries == 5 && repDeg < Integer.parseInt(message.getMessageHeader().getReplicationDeg())) {
                System.out.println("Replication degree not achived");
            }
        }

        /**
         * A peer that stores the chunk upon receiving the PUTCHUNK message, replies by sending
         * on the multicast control channel (MC) a confirmation message with the following format:
         * STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
         * after a random delay uniformly distributed between 0 and 400 ms
         */
        public void deliverStoredMessage() {
            try {
                Thread.sleep((long) (Math.random() * 400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                deliverMessage(message, peer.getMc_ip(), peer.getMc_port(), STORED);
            }

        }


        /**
         *
         */
        public void deliverDeleteMessage(){
            for(int i =0; i < 3; i++)
                deliverMessage(message, peer.getMc_ip(), peer.getMc_port(),DELETE);
        }

        /**
         * Sends a GETCHUNK request for the multicast control channel (MC) with the following format:
         * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
         */
        public void deliverGetchunkMessage() {
            deliverMessage(message, peer.getMc_ip(), peer.getMc_port(), STORED);
        }

        /**
         * Sends a CHUNK message for the multicast data restore channel (MDR) with the following format:
         * CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> <Body>
         */
        public void deliverChunkMessage() {
            deliverMessage(message, peer.getMdr_ip(), peer.getMdr_port(), CHUNK);
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
                    deleteChunks(message.getMessageHeader().getFileId());
                    break;
                default:
                    break;
            }
        }
    }

    private void deleteChunks(String fileId) {

        String path = "./"+peer.getPeerId()+"/"+fileId;
        File file = new File(path);
        deleteFolder(file);
        if(peer.getDeleteProtocol() != null)
        peer.getDeleteProtocol().updateRD1();
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }




}
