package Channels;

import Message.Mailman;

import java.io.IOException;
import java.net.DatagramPacket;
import Peer.Peer;


public class MC extends Channel{

    private String peerId;
    String mc_address;

    public MC(String address, int port, String peerId, Peer creator) throws IOException {
        super(address, port, creator);
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
         * Receives control message
         * @param request
         */
        public void handleRequest(DatagramPacket request){
            Mailman messageHandeler = new Mailman(request, peerId, mc_address, port_number, creator);
            messageHandeler.startMailmanThread();
        }


    }
}
