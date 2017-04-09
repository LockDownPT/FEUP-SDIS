package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.*;
import java.text.SimpleDateFormat;

import static Utilities.Constants.PUTCHUNK;
import static Utilities.Constants.STORED;
import static Utilities.Utilities.createHash;

public class Backup {

    private String fileName;
    private int replicationDegree;
    private String fileId;
    private Peer peer;
    private int numberOfChunks = 1;

    public Backup(String file, int replicationDegree, Peer peer) {
        this.fileName = file;
        this.replicationDegree = replicationDegree;
        this.fileId = null;
        this.peer = peer;
    }

    public Backup(Peer peer) {
        this.fileId = null;
        this.peer = peer;
    }

    public void sendChunk(byte[] chunk, int chunkNo) {

        Message request = new Message(PUTCHUNK, peer.getVersion(), peer.getPeerId(), fileId, Integer.toString(chunkNo), Integer.toString(replicationDegree));
        request.setBody(chunk);
        Mailman m = new Mailman(request, peer);
        m.startMailmanThread();

    }

    /**
     * If the peer doesn't have the chunk and it has enough space,
     * it will store the chunk and send a STORED message for the sender
     */
    public void storeChunk(Message message) {
        if (!peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
            long availableSpace = peer.getStorageSpace() - peer.getUsedSpace();
            if (availableSpace > message.getBody().length) {
                Message stored = new Message(STORED, peer.getVersion(), peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                if (peer.getVersion().equals("1.1")) {
                    deliverStoredMessageEnhanced(stored);
                } else {
                    deliverStoredMessage(stored);
                }
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
                    assert output != null;
                    output.write(message.getBody(), 0, message.getBody().length);
                    peer.setUsedSpace(peer.getUsedSpace() + message.getBody().length);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        peer.addChunkToRegistry(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo(), message.getMessageHeader().getReplicationDeg());
                        peer.increaseReplicationDegree(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                        assert output != null;
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Waits a random time before actually store the chunk
     * If the replication degree of the chunk is already achieved it doesn't store it
     */
    public void storeChunkEnhanced(Message message) {
        if (!peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
            try {
                Thread.sleep((long) (Math.random() * 1500));
                int desiredRepDeg = Integer.parseInt(message.getMessageHeader().getReplicationDeg());
                int currentRepDeg = peer.getReplicationDegreeOfChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                if (currentRepDeg < desiredRepDeg) {
                    storeChunk(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Sends PUTCHUNK request for the multicast backup channel (MDB) with the following format:
     * PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
     * Then waits one second and checks if the desired replication degree
     * has been accomplished. Otherwise it resends the PUTCHUNK request, a maximum of 5 times.
     */
    public void deliverPutchunkMessage(Message message) {

        Mailman mailman = new Mailman(message, peer.getMdb_ip(), peer.getMdb_port(), PUTCHUNK, peer);
        mailman.startMailmanThread();

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
                    mailman.startMailmanThread();
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
    public void deliverStoredMessage(Message message) {
        try {
            Thread.sleep((long) (Math.random() * 400));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), STORED, peer);
            mailman.startMailmanThread();
        }

    }

    /**
     * A peer that stores the chunk upon receiving the PUTCHUNK message, replies by sending
     * on the multicast control channel (MC) a confirmation message with the following format:
     * STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
     */
    public void deliverStoredMessageEnhanced(Message message) {

        Mailman mailman = new Mailman(message, getPeer().getMc_ip(), getPeer().getMc_port(), STORED, peer);
        mailman.startMailmanThread();

    }

    public void readChunks() {
        int chunkNo = 1;
        try {
            long maxSizeChunk = 64 * 1000;
            //String path = "./TestFiles/" + fileName; linux
            String path = "./" + "TestFiles/" + fileName; // windows
            File file = new File(path);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            this.fileId = createHash(fileName + sdf.format(file.lastModified()));

            RandomAccessFile fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            int numSplits = (int) Math.floor(fileLength / maxSizeChunk);
            int lastChunkSize = (int) (fileLength - (maxSizeChunk * numSplits));
            numSplits++;

            System.out.println(fileLength);
            System.out.println((int) maxSizeChunk);
            System.out.println(numSplits);
            System.out.println(lastChunkSize);

            for (int chunkId = 1; chunkId < numSplits; chunkId++) {

                byte[] buf = new byte[(int) maxSizeChunk];
                int val = fileRaf.read(buf);
                if (val != -1) {
                    sendChunk(buf, chunkId);
                }
                chunkNo++;
                this.numberOfChunks++;

            }
            if (lastChunkSize >= 0) {

                byte[] buf = new byte[(int) (long) lastChunkSize];
                int val = fileRaf.read(buf);
                if (val != -1) {
                    System.out.println("LASTCHUNK Size: " + lastChunkSize);
                    System.out.println("BUF length: " + buf.length);
                    sendChunk(buf, chunkNo);
                }
                this.numberOfChunks++;
            }
            fileRaf.close();

        } catch (IOException e) {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int repDeg) {
        this.replicationDegree = repDeg;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String id) {
        this.fileId = id;
    }

    public Peer getPeer() {
        return peer;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
    }

}

