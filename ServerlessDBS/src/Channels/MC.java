package Channels;

import Message.Mailman;

import java.io.IOException;
import java.net.DatagramPacket;
import Peer.Peer;


public class MC extends Channel{

    public MC(String address, int port, String peerId, Peer peer) throws IOException {
        super(address, port, peer);
        this.thread = new MC.MCThread();
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
            Mailman messageHandeler = new Mailman(request, peer);
            messageHandeler.startMailmanThread();
        }


    }
}
