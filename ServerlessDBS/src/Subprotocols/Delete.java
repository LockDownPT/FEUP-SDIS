package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import static Utilities.Constants.DELETE;
import static Utilities.Constants.PUTCHUNK;

public class Delete {

    private String fileName;
    private String senderId;
    private String mdb_addr;
    private int mdb_port;
    private String fileId;
    private String version;

    private Peer peer;


    public Delete(String version, String senderId, String file, String addr, int port, Peer peer){
        this.fileName = file;
        this.senderId = senderId;
        this.mdb_addr =addr;
        this.mdb_port =port;
        this.version=version;
        this.fileId = null;
        this.peer=peer;
    }


    public void deleteChunks() {

        Message request = new Message(DELETE ,version, senderId, fileId);

        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }


}

