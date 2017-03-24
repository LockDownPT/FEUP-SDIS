package Subprotocols;


import Message.Message;
import Message.Mailman;
import Peer.Peer;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import static Utilities.Constants.PUTCHUNK;

public class Backup {

    private String fileName;
    private int replicationDegree;
    private String senderId;
    private String mdb_addr;
    private int mdb_port;
    private String fileId;
    private String version;
    private byte[][] backupStorage;
    private Peer creator;


    public Backup(String version, String senderId, String file, int replicationDegree, String addr, int port, Peer creator){
        this.fileName = file;
        this.replicationDegree = replicationDegree;
        this.senderId = senderId;
        this.mdb_addr =addr;
        this.mdb_port =port;
        this.version=version;
        this.fileId = null;
        this.creator=creator;
    }

    public void sendChunk(byte[] chunk, int chunkNo){

            Message request = new Message(PUTCHUNK,version, senderId, fileId, Integer.toString(chunkNo), Integer.toString(replicationDegree));
            request.setBody(chunk);

            Mailman messageHandler = new Mailman(request,senderId,mdb_addr,mdb_port, creator);
            messageHandler.startMailmanThread();



    }

    public void readChunks() {
        int chunkNo=0;
        try {
            long maxSizeChunk = 64 * 1000;
            String path = "./TestFiles/" + fileName;
            File file = new File(path);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            this.fileId=  createHash(fileName+senderId + sdf.format(file.lastModified()));

            RandomAccessFile fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            int numSplits = (int) (fileLength/maxSizeChunk);
            int lastChunkSize = (int) (fileLength -(maxSizeChunk*numSplits));

            System.out.println(fileLength);
            System.out.println((int)maxSizeChunk);
            System.out.println(numSplits);
            System.out.println(lastChunkSize);

            for (int chunkId = 1; chunkId <= numSplits; chunkId++) {

                byte[] buf = new byte[(int)maxSizeChunk];
                int val = fileRaf.read(buf);
                if(val != -1) {
                    sendChunk(buf, chunkId);
                }
                chunkNo++;

            }
            if(lastChunkSize >= 0) {

                byte[] buf = new byte[(int) (long)lastChunkSize];
                int val = fileRaf.read(buf);
                if(val != -1) {
                    sendChunk(buf, chunkNo+1);
                }

            }
            fileRaf.close();

        } catch (IOException e) {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }

    /**
     * Returns a hexadecimal encoded SHA-256 hash for the input String.
     * @param data
     * @return string with Hash
     */
    private String createHash(String data) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return bytesToHex(hash); // make it printable
        }catch(Exception ex) {
                      ex.printStackTrace();
        }
        return result;
    }

    /**
      * Use javax.xml.bind.DatatypeConverter class in JDK to convert byte array
      * to a hexadecimal string. Note that this generates hexadecimal in upper case.
      * @param hash
      * @return string with hash in hexadecimal
      */
    private String  bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash);
    }


}

