package Channels;

import java.io.IOException;
import java.net.DatagramPacket;

public class MDB extends Channel {
    public MDB(String address, int port) throws IOException {
        super(address, port);
        this.thread = new MDBThread();

    }

    public class MDBThread extends Thread{
        public void run(){
            try{
                while(true){
                    DatagramPacket packet = receiveRequests();

                    String received = new String(packet.getData());
                    System.out.println("Echoed Message: " );
                }
            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }
        }


    }
}
