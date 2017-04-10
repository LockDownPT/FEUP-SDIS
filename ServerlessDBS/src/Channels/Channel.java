package Channels;

import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel {

    private MulticastSocket mc_socket;
    private Thread thread;
    private Peer peer;

    /**
     * Class that connects and listens to a multicast
     *
     * @param address multicast address
     * @param port    multicast port
     * @param peer    peer that listens on the multicast
     * @throws IOException
     */
    Channel(String address, int port, Peer peer) throws IOException {
        InetAddress channel_addr = InetAddress.getByName(address);

        System.out.println(address);
        System.out.println(port);

        mc_socket = new MulticastSocket(port);
        mc_socket.joinGroup(channel_addr);

        this.peer = peer;

    }

    /**
     * Listens for incoming packets
     *
     * @param protocol protocol calling the listener
     * @return packet
     * @throws IOException
     */
    DatagramPacket receiveRequests(String protocol) throws IOException {

        byte[] buf;
        buf = new byte[70000];

        /*if ("BACKUP".equals(protocol) || "RESTORE".equals(protocol)) {
            buf = new byte[70000];
        } else {
            buf = new byte[256];
        }*/
        DatagramPacket request = new DatagramPacket(buf, buf.length);
        mc_socket.receive(request);
        System.out.println("PACKET LENGHT: " + request.getLength());
        return request;
    }


    /**
     * Starts channel listener thread
     */
    public void listen() {
        this.thread.start();

    }

    /**
     * Sets channel thread
     *
     * @param thread
     */
    void setThread(Thread thread) {
        this.thread = thread;
    }


    /**
     * @return peer that connected to the channel
     */
    Peer getPeer() {
        return peer;
    }

}
