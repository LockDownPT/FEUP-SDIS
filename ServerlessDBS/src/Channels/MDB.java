package Channels;

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
                    DatagramPacket packet = receiveRequests();
                    handleRequest(packet);
                }
            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }
        }

        public void handleRequest(DatagramPacket request){

            byte[] buffer = request.getData();
            String received = new String(request.getData());

            //TODO: optimize for getting only header from data
            String[] requestHeader = received.split(" ");

            OutputStream output = null;
            try {
                //Creates sub folders structure -> peerId/FileId/ChunkNo
                File outFile = new File(peerId+"/"+requestHeader[3]+"/"+requestHeader[4]);
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
                output = new FileOutputStream(peerId+"/"+requestHeader[3]+"/"+requestHeader[4]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.write(buffer, 0, request.getLength());
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