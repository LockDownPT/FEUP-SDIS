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
        int port_number = port;

        System.out.println(address);
        System.out.println(port);

        mc_socket = new MulticastSocket(port_number);
        mc_socket.joinGroup(channel_addr);

        this.peer = peer;

    }

    DatagramPacket receiveRequests(String protocol) throws IOException {

        byte[] buf;

        if (protocol.equals("BACKUP") || protocol.equals("RESTORE")) {
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

    void setThread(Thread thread) {
        this.thread = thread;
    }

    Peer getPeer() {
        return peer;
    }

}
