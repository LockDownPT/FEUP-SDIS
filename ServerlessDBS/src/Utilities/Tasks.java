package Utilities;


import Message.Message;
import Peer.Peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static Utilities.Constants.DELETE;
import static Utilities.Constants.PUTCHUNK;
import static java.lang.Thread.sleep;

public class Tasks {

    Properties pendingTasks;
    private Peer peer;

    public Tasks(Peer peer) {
        this.peer = peer;
        this.pendingTasks = new Properties();
    }

    public void addTask(String chunkId) {
        pendingTasks.setProperty(chunkId, chunkId);
        saveTasks();
    }

    public void addTask(String fileId, String repDeg) {
        System.out.println(fileId);
        System.out.println(repDeg);

        pendingTasks.setProperty(fileId, repDeg);
        saveTasks();
    }

    public void finishTask(String chunkId) {
        pendingTasks.remove(chunkId);
        saveTasks();
    }

    public void saveTasks() {

        try {
            pendingTasks.store(new FileOutputStream(peer.getPeerId() + "/pendingTasks.properties"), null);
        } catch (IOException e) {
            //..
        }
    }

    public void loadTasks() {

        File pendingTasksFile = new File(peer.getPeerId() + "/pendingTasks.properties");

        if (pendingTasksFile.exists() && !pendingTasksFile.isDirectory()) {
            try {
                pendingTasks.load(new FileInputStream(peer.getPeerId() + "/pendingTasks.properties"));
            } catch (IOException e) {
                //...
            }
        }

    }

    public void finishPendingTasks() {


        for (String chunkId : pendingTasks.stringPropertyNames()) {

            if (peer.getStoredChunks().containsKey(chunkId)) {

                String fileId = peer.getFileIdFromChunkId(chunkId);
                String chunkNo = peer.getChunkNoFromChunkId(chunkId);

                Message putchunk = new Message(PUTCHUNK, peer.getVersion(), peer.getPeerId(), fileId, chunkNo, Integer.toString(peer.getDesiredReplicationDegree(fileId + chunkNo)));

                Path path = Paths.get(peer.getPeerId() + "/" + fileId + "/" + chunkNo);
                try {
                    byte[] data = Files.readAllBytes(path);
                    putchunk.setBody(data);
                } catch (IOException e) {
                    //..
                }
                peer.getBackup().setReplicationDegree(peer.getDesiredReplicationDegree(fileId + chunkNo));
                peer.getBackup().setFileId(fileId);
                peer.getBackup().deliverPutchunkMessage(putchunk);
            } else {

               if(chunkId.length()==64){
                    try {
                        sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message deleteFileMessage = new Message(DELETE, peer.getVersion(), peer.getPeerId(), chunkId);
                    peer.getDeleteProtocol().deliverDeleteMessage(deleteFileMessage);

                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String[] value = pendingTasks.getProperty(chunkId).split("-");

                    System.out.println("CHUNK ID: " + chunkId);
                    System.out.println("VALUE: " + pendingTasks.getProperty(chunkId));
                    System.out.println("REPdeg: " + value[0]);

                    peer.getBackup().setFileId(chunkId);
                    peer.getBackup().setReplicationDegree(Integer.parseInt(value[0]));
                    peer.getBackup().setFileName(value[1]);
                    peer.getBackup().readChunks();
                }
            }
        }
    }
}
