package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Utilities.Constants.*;
import static Utilities.Utilities.createHash;
import static java.lang.Thread.sleep;


public class Restore {

    /**
     * String is the chunk number
     * byte[] holds the chunk data
     */
    private Map<String, byte[]> chunks = new ConcurrentHashMap<>();
    private String fileName;
    private Peer peer;
    private int numberOfChunks = 0;
    private int restoredChunks = 0;
    private String fileId;
    private Runnable restoreEnhanced;

    public Restore(String file, Peer peer) {

        fileName = file;
        this.peer = peer;
    }

    public Restore(Peer peer) {
        this.peer = peer;
    }

    public class RestoreEnhanced implements Runnable{
        public void run() {
            DatagramPacket packet;
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(Integer.parseInt(peer.getPeerId()));
            } catch (SocketException e) {
                e.printStackTrace();
            }
            byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);

            while(true){
                //if the client does receive an answer in the given timeout, it resends the packet
                try {
                    socket.receive(packet);
                    Message chunk = new Message(packet);
                    saveChunk(chunk);
                } catch (IOException e) {
                    System.out.println("RECEIVED CHUNK");
                }
            }

        }
    }

    public void start() {

        System.out.println("Gathering file info");
        getFileInfo();

        if(!peer.getVersion().equals("1.0")){
            this.restoreEnhanced = new RestoreEnhanced();
            peer.getDeliverExecutor().submit(restoreEnhanced);

        }

        System.out.print("Requesting chunks");
        requestChunks();
        do {
            try {
                sleep(500);
                System.out.print(".");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Restored Chunks: " + restoredChunks);
            System.out.println("Number of Chunks: " + numberOfChunks);
        } while (restoredChunks < numberOfChunks);
        System.out.println("Constructing File");
        constructFile();
        System.out.println("Finished Restore");

    }

    private void getFileInfo() {
        long maxSizeChunk = 64 * 1000;
        String path = "./TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));
        RandomAccessFile fileRaf;
        try {
            fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            this.numberOfChunks = (int) Math.ceil(fileLength / maxSizeChunk);
            int lastChunkSize = (int) (fileLength - (maxSizeChunk * this.numberOfChunks));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Number of chunks: " + this.numberOfChunks);
    }

    private void requestChunks() {

        int chunkNo = 1;

        while (chunkNo <= numberOfChunks) {
            Message request = new Message(GETCHUNK, peer.getVersion(), peer.getPeerId(), this.fileId, Integer.toString(chunkNo));

            Mailman messageHandler = new Mailman(request, peer);
            messageHandler.startMailmanThread();
            System.out.println("Requesting chunk number: " + chunkNo);
            chunkNo++;
        }
    }

    private void constructFile() {

        FileOutputStream fop = null;
        File file;
        try {
            file = new File("./" + fileName);
            fop = new FileOutputStream(file, true);
            for (int i = 1; i <= chunks.size(); i++) {
                System.out.println((chunks.get(Integer.toString(i))).length);
                fop.write(chunks.get(Integer.toString(i)));
            }
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    /**
     * If the peer has the chunk and it hasn't been sent by another peer, it will send it.
     */
    public void sendChunk(Message message) {
        if (peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
            try {
                Thread.sleep((long) (Math.random() * 400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (!peer.hasChunkBeenSent(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
                    Message chunk = new Message(CHUNK, peer.getVersion(), peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                    chunk.setBody(peer.getChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo()));
                    deliverChunkMessage(chunk, message);
                    System.out.println("Sent CHUNK");
                }
            }
        }
    }

    /**
     * Saves received chunk, if it has asked for it
     */
    public void saveChunk(Message message) {
        if (peer.getRestoreProtocol() != null) {
            peer.getRestoreProtocol().storeChunk(message.getMessageHeader().getChunkNo(), message.getBody());
        } else {
            peer.addSentChunkInfo(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
        }
    }

    private void storeChunk(String chunkNo, byte[] chunk) {
        if (chunks.get(chunkNo) == null) {
            chunks.put(chunkNo, chunk);
            restoredChunks++;
            System.out.println("Received chunk: " + chunkNo + "Chunk Size: " + chunk.length);
        }
    }

    /**
     * Sends a GETCHUNK request for the multicast control channel (MC) with the following format:
     * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
     */
    public void deliverGetchunkMessage(Message message) {
        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), STORED, peer);
        mailman.startMailmanThread();
    }

    /**
     * Sends a CHUNK message for the multicast data restore channel (MDR) with the following format:
     * CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> <Body>
     */
    public void deliverChunkMessage(Message newMessage, Message request) {
        if(request.getMessageHeader().getVersion().equals("1.1")){
            Mailman mailman = new Mailman(newMessage, request.getPacketIP().toString(), Integer.parseInt(request.getMessageHeader().getSenderId()), CHUNK, peer);
            mailman.startMailmanThread();
        }else{
            Mailman mailman = new Mailman(newMessage, peer.getMdr_ip(), peer.getMdr_port(), CHUNK, peer);
            mailman.startMailmanThread();
        }

    }
}
