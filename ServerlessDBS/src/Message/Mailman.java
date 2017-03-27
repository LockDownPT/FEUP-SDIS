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

    public class SenderThread extends Thread {
        public void run() {
            System.out.println("Sended request:" + message.getMessageHeader().getMessageType());
            switch (message.getMessageHeader().getMessageType()) {
                case PUTCHUNK:
                    deliverPutchunkMessage();
                    break;
                case STORED:
                    deliverStoredMessage();
                    break;
                case GETCHUNK:
                    deliverGetchunkMessage();
                    break;
                case CHUNK:
                    deliverChunkMessage();
                    break;
                case DELETE:
                    deliverDeleteMessage();
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
                    storeChunk();
                    break;
                case STORED:
                    peer.increaseReplicationDegree(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo());
                    break;
                case GETCHUNK:
                    sendChunk();
                    break;
                case CHUNK:
                    saveChunk();
                    break;
                case DELETE:
                    //creator.deleteChunks(message.getMessageHeader().getFileId());
                    break;
                default:
                    break;
            }


        }

        /**
         * If the peer doesn't have the chunk, it will store it inside it's "disk" and send a STORED
         * message for the sender
         */
        public void storeChunk() {
            if (!peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
                OutputStream output = null;
                try {
                    //Creates sub folders structure -> peerId/FileId/ChunkNo
                    File outFile = new File(peer.getPeerId() + "/" + message.getMessageHeader().getFileId() + "/" + message.getMessageHeader().getChunkNo());
                    outFile.getParentFile().mkdirs();
                    outFile.createNewFile();
                    output = new FileOutputStream(peer.getPeerId() + "/" + message.getMessageHeader().getFileId() + "/" + message.getMessageHeader().getChunkNo());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output.write(message.getBody(), 0, message.getBody().length);
                    peer.setUsedSpace(peer.getUsedSpace()+message.getBody().length);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        peer.addChunkToRegistry(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo(), message.getMessageHeader().getReplicationDeg());
                        peer.increaseReplicationDegree(message.getMessageHeader().getFileId() + message.getMessageHeader().getChunkNo());
                        Message stored = new Message(STORED, "1.0", peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                        Mailman sendStored = new Mailman(stored, peer);
                        sendStored.startMailmanThread();

                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        /**
         * If the peer has the chunk and it hasn't been sent by another peer, it will send it.
         */
        public void sendChunk() {
            if (peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
                try {
                    Thread.sleep((long) (Math.random() * 400));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (!peer.hasChunkBeenSent(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
                        Message chunk = new Message(CHUNK, "1.0", peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                        chunk.setBody(peer.getChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo()));
                        Mailman sendChunk = new Mailman(chunk, peer);
                        sendChunk.startMailmanThread();
                        System.out.println("Sent CHUNK");
                    }
                }
            }
        }

        /**
         * Saves received chunk, if it has asked for it
         */
        public void saveChunk() {
            if (peer.getRestoreProtocol() != null) {
                peer.getRestoreProtocol().storeChunk(message.getMessageHeader().getChunkNo(), message.getBody());
            } else {
                peer.addSentChunkInfo(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
            }
        }

    }

}
