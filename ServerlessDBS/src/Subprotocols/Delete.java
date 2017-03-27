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
import static Utilities.Utilities.createHash;

public class Delete {

    private String fileName;

    private String fileId;

    private Peer peer;


    public Delete(String file, Peer peer){
        this.fileName = file;
        this.peer=peer;
    }

    private void getFileId() {

        String path = "./src/TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));

    }


    public void deleteChunks() {
        getFileId();
        Message request = new Message(DELETE ,peer.getVersion(), peer.getPeerId(),this.fileId );
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }


}

