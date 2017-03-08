package Channels;

import java.io.IOException;

public class MDB extends Channel {
    public MDB(String address, int port) throws IOException {
        super(address, port);
        this.thread = new MDBThread();

    }

    public class MDBThread extends Thread{
        public void run(){
            try{
                while(true){
                    byte[] buf = new byte[1000];
                    buf = receiveRequests();

                    System.out.println(buf);
                }
            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }
        }


    }
}
