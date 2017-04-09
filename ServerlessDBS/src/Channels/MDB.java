package Channels;

import Message.Mailman;
import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;

public class MDB extends Channel {

    /**
     * Multicast Data Backup channel
     *
     * @param address multicast address
     * @param port    multicast port
     * @param peer    peer that listens on the multicast
     * @throws IOException
     */
    public MDB(String address, int port, Peer peer) throws IOException {
        super(address, port, peer);
        setThread(new MDB.MDBThread());
    }

    /**
     * Channel thread that concurrently listens for incoming packets
     */
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
         * @param request Backup DatagramPacket with file info and chunk content
         */
        public void handleRequest(DatagramPacket request) {
            Mailman messageHandeler = new Mailman(request, getPeer());
            messageHandeler.startMailmanThread();
        }

    }
}