package Channels;

import Message.Mailman;
import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;


public class MDR extends Channel {

    /**
     * Multicast Data Restore channel
     *
     * @param address multicast address
     * @param port    multicast port
     * @param peer    peer that listens on the multicast
     * @throws IOException
     */
    public MDR(String address, int port, Peer peer) throws IOException {
        super(address, port, peer);
        setThread(new MDR.MDRThread());

    }

    /**
     * Channel thread that concurrently listens for incoming packets
     */
    public class MDRThread extends Thread {
        public void run() {
            try {
                while (true) {
                    DatagramPacket packet = receiveRequests("RESTORE");
                    handleRequest(packet);
                }
            } catch (IOException e) {
                System.out.println("Error handling peer:" + e);
            }
        }

        /***
         * Receives chunk datagram
         * @param chunk Chunk Datagram
         */
        public void handleRequest(DatagramPacket chunk) {
            Mailman messageHandeler = new Mailman(chunk, getPeer());
            messageHandeler.startMailmanThread();
        }


    }
}
