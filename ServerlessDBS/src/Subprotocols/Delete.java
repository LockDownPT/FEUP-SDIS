package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;
import Utilities.Utilities;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import static Utilities.Constants.DELETE;
import static Utilities.Constants.PUTCHUNK;
import static Utilities.Utilities.createHash;

public class Delete {

    private String fileName;
    private String fileId;

    private Peer peer;
    private int numberOfChunks = 0;

    public Delete(String file, Peer peer){
        this.fileName = file;
        this.peer=peer;
    }

    private void getFileId() {
        long maxSizeChunk = 64 * 1000;
        String path = "./src/TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));
        RandomAccessFile fileRaf;
        try {
            fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            this.numberOfChunks = (int) (fileLength / maxSizeChunk) + 1;
            int lastChunkSize = (int) (fileLength - (maxSizeChunk * this.numberOfChunks));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Number of chunks: " + this.numberOfChunks);
    }


    public void deleteChunks() {
        getFileId();
        Message request = new Message(DELETE ,peer.getVersion(), peer.getPeerId(),this.fileId );
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }


    public void updateRD1() {
        getFileId();
        for(int i = 1; i <= this.numberOfChunks; i++ ){
            String temp = ""+this.fileId+i;
            this.peer.updateRD(temp);
            String path = "./"+peer.getPeerId()+"/"+"chunksRepDeg.properties";
            deleteLine(path,temp);
        }
    }

    public void deleteLine(String path, String hash) {
        peer.saveRepDegInfoToDisk();
    }


}

