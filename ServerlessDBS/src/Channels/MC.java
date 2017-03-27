package Channels;

import Message.Mailman;
import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;


public class MC extends Channel {

    public MC(String address, int port, Peer peer) throws IOException {
        super(address, port, peer);
        setThread(new MC.MCThread());
    }

    public class MCThread extends Thread {
        public void run() {
            try {
                while (true) {
                    DatagramPacket packet = receiveRequests("");
                    handleRequest(packet);
                }

            } catch (IOException e) {
                System.out.println("Error handling peer:" + e);
            }
        }

        /***
         * Receives control message
         * @param request
         */
        public void handleRequest(DatagramPacket request) {
            Mailman messageHandeler = new Mailman(request, getPeer());
            messageHandeler.startMailmanThread();
        }


    }
}
