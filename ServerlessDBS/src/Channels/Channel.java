package Channels;

import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel {

    private int port_number;
    private InetAddress channel_addr;
    private MulticastSocket mc_socket;
    private Thread thread;
    private Peer peer;

    public Channel(String address, int port, Peer peer) throws IOException {
        channel_addr = InetAddress.getByName(address);
        port_number = port;

        System.out.println(address);
        System.out.println(port);

        mc_socket = new MulticastSocket(port_number);
        mc_socket.joinGroup(channel_addr);

        this.peer = peer;

    }

    public DatagramPacket receiveRequests(String protocol) throws IOException {

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

    public void closeChannel() throws IOException {
        mc_socket.leaveGroup(channel_addr);
    }

    public int getPort_number() {
        return port_number;
    }

    public void setPort_number(int port_number) {
        this.port_number = port_number;
    }

    public InetAddress getChannel_addr() {
        return channel_addr;
    }

    public void setChannel_addr(InetAddress channel_addr) {
        this.channel_addr = channel_addr;
    }

    public MulticastSocket getMc_socket() {
        return mc_socket;
    }

    public void setMc_socket(MulticastSocket mc_socket) {
        this.mc_socket = mc_socket;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
