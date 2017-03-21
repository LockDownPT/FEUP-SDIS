package Channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel {

    int port_number;
    InetAddress channel_addr;
    MulticastSocket mc_socket;
    Thread thread;

    public Channel(String address, int port) throws IOException {
        channel_addr = InetAddress.getByName(address);
        port_number=port;

        System.out.println(address);
        System.out.println(port);

        mc_socket = new MulticastSocket(port_number);
        mc_socket.joinGroup(channel_addr);

    }

    public DatagramPacket receiveRequests(String protocol) throws IOException {

        byte[] buf;

        if(protocol.equals("BACKUP")){
            buf = new byte[70000];
        }else{
            buf = new byte[256];
        }
        DatagramPacket request = new DatagramPacket(buf, buf.length);
        mc_socket.receive(request);
        return request;
    }


    public void listen(){
        this.thread.start();

    }

    public void closeChannel() throws IOException {
        mc_socket.leaveGroup(channel_addr);
    }
}
