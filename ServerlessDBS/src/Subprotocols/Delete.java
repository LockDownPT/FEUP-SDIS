package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.*;
import java.text.SimpleDateFormat;

import static Utilities.Constants.DELETE;
import static Utilities.Utilities.createHash;

public class Delete {

    private String fileName;
    private String fileId;

    private Peer peer;
    private int numberOfChunks = 0;

    public Delete(String file, Peer peer){
        this.fileName = file;
        this.peer=peer;
        this.fileId = getFileId();
    }

    public String getFileId(){
        String path = "./src/TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return createHash(fileName + sdf.format(file.lastModified()));
    }

    public void deleteChunks() {
        Message request = new Message(DELETE ,peer.getVersion(), peer.getPeerId(),fileId );
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }


    public void updateRepDeg1() {
        for(int i = 1; i <= this.numberOfChunks; i++ ){
            String temp = ""+this.fileId+i;
            this.peer.updateRepDeg(temp);
            String path = "./"+peer.getPeerId()+"/"+"chunksRepDeg.properties";
            deleteLine(path,temp);
        }
    }

    public void deleteLine(String path, String hash) {
        peer.saveRepDegInfoToDisk();
    }


}

