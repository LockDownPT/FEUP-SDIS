package Channels;

import Message.Mailman;

import java.io.IOException;
import java.net.DatagramPacket;


public class MC extends Channel{

    private String peerId;
    String mc_address;

    public MC(String address, int port, String peerId) throws IOException {
        super(address, port);
        this.thread = new MC.MCThread();
        this.peerId = peerId;
        this.mc_address=address;
    }

    public class MCThread extends Thread{
        public void run(){
            try{
                while(true){
                    DatagramPacket packet = receiveRequests("");
                    handleRequest(packet);
                }

            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }finally {

            }

        }

        /***
         * Receives backup request and saves chunks to peerId/FileId folder
         * @param request Backup DatagramPacket with file info and chunk content
         */
        public void handleRequest(DatagramPacket request){
            Mailman messageHandeler = new Mailman(request, peerId, mc_address, port_number);
            messageHandeler.startMailmanThread();
        }


    }
}
