package Channels;

import Message.Mailman;
import Peer.Peer;

import java.io.*;
import java.net.DatagramPacket;

public class MDB extends Channel {


    private String peerId;
    private String mc_addr;
    private int mc_port;

    public MDB(String address, int port, String mc_addr, int mc_port, String peerId, Peer creator) throws IOException {
        super(address, port, creator);
        this.thread = new MDBThread();
        this.peerId = peerId;
        this.mc_addr=mc_addr;
        this.mc_port=mc_port;
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

        /***
         * Receives backup request and saves chunks to peerId/FileId folder
         * @param request Backup DatagramPacket with file info and chunk content
         */
        public void handleRequest(DatagramPacket request){
           Mailman messageHandeler = new Mailman(request, peerId, mc_addr, mc_port, creator);
           messageHandeler.startMailmanThread();
        }

    }
}