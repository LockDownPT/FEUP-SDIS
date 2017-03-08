package Channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel {

    private int port_number;
    InetAddress mc_addr;
    MulticastSocket mc_socket;
    Thread thread;

    public Channel(String address, int port) throws IOException {
        mc_addr = InetAddress.getByName(address);
        port_number=port;
        mc_socket = new MulticastSocket(port_number);
        mc_socket.joinGroup(mc_addr);

    }

    public byte[] receiveRequests() throws IOException {

        byte[] buf = new byte[1000];
        DatagramPacket request = new DatagramPacket(buf, buf.length);
        mc_socket.receive(request);

        return buf;
    }


    public void listen(){
        this.thread.start();
    }

    public void closeChannel() throws IOException {
        mc_socket.leaveGroup(mc_addr);
    }
}
