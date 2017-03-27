package Channels;

import Message.Mailman;
import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;

public class MDB extends Channel {

    public MDB(String address, int port, Peer creator) throws IOException {
        super(address, port, creator);
        setThread(new MDB.MDBThread());
    }

    public class MDBThread extends Thread {
        public void run() {
            try {
                while (true) {
                    DatagramPacket packet = receiveRequests("BACKUP");
                    handleRequest(packet);
                }
            } catch (IOException e) {
                System.out.println("Error handling peer:" + e);
            }
        }

        /***
         * Receives backup request and saves chunks to peerId/FileId folder
         * @param request BackupEnhanced DatagramPacket with file info and chunk content
         */
        public void handleRequest(DatagramPacket request) {
            Mailman messageHandeler = new Mailman(request, getPeer());
            messageHandeler.startMailmanThread();
        }

    }
}