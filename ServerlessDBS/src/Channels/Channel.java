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

    Channel(String address, int port, Peer peer) throws IOException {
        InetAddress channel_addr = InetAddress.getByName(address);

        System.out.println(address);
        System.out.println(port);

        mc_socket = new MulticastSocket(port);
        mc_socket.joinGroup(channel_addr);

        this.peer = peer;

    }

    public DatagramPacket receiveRequests(String protocol) throws IOException {

        byte[] buf;

        if ("BACKUP".equals(protocol) || "RESTORE".equals(protocol)) {
            buf = new byte[70000];
        } else {
            buf = new byte[256];
        }
        DatagramPacket request = new DatagramPacket(buf, buf.length);
        mc_socket.receive(request);
        return request;
    }


    public void listen() {
        this.thread.start();

    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Peer getPeer() {
        return peer;
    }

}
