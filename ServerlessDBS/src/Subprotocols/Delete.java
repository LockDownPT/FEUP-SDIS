package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.*;
import java.text.SimpleDateFormat;

import static Utilities.Constants.DELETE;
import static Utilities.Constants.STORED;
import static Utilities.Utilities.createHash;

public class Delete {

    private String fileName;
    private String fileId;

    private Peer peer;

    public Delete(String file, Peer peer){
        this.fileName = file;
        this.peer=peer;
        this.fileId = getFileId();
    }

    public Delete(Peer peer){
        this.peer=peer;
    }

    public String getFileId(){
        String path = "./src/TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return createHash(fileName + sdf.format(file.lastModified()));
    }

    public void deleteChunks() {
        Message request = new Message(DELETE ,peer.getVersion(), peer.getPeerId(),this.fileId );
        Mailman messageHandler = new Mailman(request, peer);
        messageHandler.startMailmanThread();
    }


    public void updateRepDeg1() {


            this.peer.updateRepDeg(fileId);
            //this.peer.saveRepDegInfoToDisk();

            }



    public void deleteChunks(String fileId) {

        String path = "./"+peer.getPeerId()+"/"+fileId;
        File file = new File(path);
        deleteFolder(file);
        if(peer.getDeleteProtocol() != null)
            peer.getDeleteProtocol().updateRepDeg1();
    }

    public  void deleteFolder(File folder) {
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

    /**
     *
     */
    public void deliverDeleteMessage(Message message){
        for(int i =0; i < 3; i++){
            Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(),DELETE);
            mailman.startMailmanThread();
        }
    }


}

