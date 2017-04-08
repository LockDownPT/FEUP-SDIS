package Channels;

import Message.Mailman;
import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;


public class MDR extends Channel {
    public MDR(String address, int port, Peer creator) throws IOException {
        super(address, port, creator);
        setThread(new MDR.MDRThread());

    }

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
