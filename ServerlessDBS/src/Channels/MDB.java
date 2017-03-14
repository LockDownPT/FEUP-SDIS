package Channels;

import Message.Message;

import java.io.*;
import java.net.DatagramPacket;

public class MDB extends Channel {


    private String peerId;

    public MDB(String address, int port, String peerId) throws IOException {
        super(address, port);
        this.thread = new MDBThread();
        this.peerId = peerId;
    }

    public class MDBThread extends Thread{
        public void run(){
            try{
                while(true){
                    DatagramPacket packet = receiveRequests("BACKUP");
                    handleRequest(packet);
                }
            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }
        }

        public void handleRequest(DatagramPacket request){

            Message message = new Message(request);

            OutputStream output = null;
            try {
                //Creates sub folders structure -> peerId/FileId/ChunkNo
                File outFile = new File(peerId+"/"+message.getMessageHeader().getFileId()+"/"+message.getMessageHeader().getChunkNo());
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
                output = new FileOutputStream(peerId+"/"+message.getMessageHeader().getFileId()+"/"+message.getMessageHeader().getChunkNo());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(message.getBody().length);
                output.write(message.getBody(), 0, message.getBody().length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


    }

}
}