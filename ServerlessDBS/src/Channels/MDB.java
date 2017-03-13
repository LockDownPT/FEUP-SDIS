package Channels;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

                    handleRequest(packet);
                }
            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }
        }

        public void handleRequest(DatagramPacket request){

            byte[] buffer = request.getData();
            String received = new String(request.getData());

            //TODO: Get filename from received String
            OutputStream output = new FileOutputStream(filename);
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