package Channels;

import Message.Message;
import Message.Mailman;
import Subprotocols.Backup;

import java.io.*;
import java.net.DatagramPacket;

public class MDB extends Channel {


    private String peerId;
    private String mc_addr;
    private int mc_port;

    public MDB(String address, int port, String mc_addr, int mc_port, String peerId) throws IOException {
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

        /***
         * Receives backup request and saves chunks to peerId/FileId folder
         * @param request Backup DatagramPacket with file info and chunk content
         */
        public void handleRequest(DatagramPacket request){
           Mailman messageHandeler = new Mailman(request, peerId, mc_addr, mc_port);
           messageHandeler.startMailmanThread();
        }

    }
}