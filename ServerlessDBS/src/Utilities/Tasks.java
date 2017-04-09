package Utilities;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import Message.Message;
import Peer.Peer;

import static Utilities.Constants.PUTCHUNK;

public class Tasks {

    Properties pendingTasks = new Properties();
    private Peer peer;

    public Tasks(Peer peer){
        this.peer=peer;
    }

    public void addTask(String chunkId){
        pendingTasks.setProperty(chunkId,chunkId);
        saveTasks();
    }

    public void finishTask(String chunkId){
        pendingTasks.remove(chunkId);
        saveTasks();
    }

    public void saveTasks(){

        try{
            pendingTasks.store(new FileOutputStream(peer.getPeerId() + "/pendingTasks.properties"), null);
        } catch (IOException e) {
            //..
        }
    }

    public void loadTasks(){

        File pendingTasksFile = new File(peer.getPeerId() + "/pendingTasks.properties");

        if (pendingTasksFile.exists() && !pendingTasksFile.isDirectory()) {
            try {
                pendingTasks.load(new FileInputStream(peer.getPeerId() + "/pendingTasks.properties"));
            } catch (IOException e) {
                //...
            }
        }

    }

    public void finishPendingTasks(){


        for (String chunkId : pendingTasks.stringPropertyNames()) {

            String fileId = peer.getFileIdFromChunkId(chunkId);
            String chunkNo = peer.getChunkNoFromChunkId(chunkId);

            if(peer.getStoredChunks().containsKey(fileId+chunkNo)){

                Message putchunk = new Message(PUTCHUNK,peer.getVersion(),peer.getPeerId(),fileId,chunkNo,Integer.toString(peer.getDesiredReplicationDegree(fileId+chunkNo)));

                Path path = Paths.get(peer.getPeerId() + "/" + fileId + "/" + chunkNo);
                try {
                    byte[] data = Files.readAllBytes(path);
                    putchunk.setBody(data);
                } catch (IOException e) {
                    //..
                }
                peer.getBackup().setReplicationDegree(peer.getDesiredReplicationDegree(fileId+chunkNo));
                peer.getBackup().setFileId(fileId);
                peer.getBackup().deliverPutchunkMessage(putchunk);
            }else{
                
            }
        }
    }
}
