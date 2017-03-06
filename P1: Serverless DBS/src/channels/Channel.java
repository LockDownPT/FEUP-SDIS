package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Created by pedroc on 06/03/17.
 */
public class Channel {

    private int port_number;
    InetAddress mc_addr;
    MulticastSocket socket;
    Thread thread;

    public Channel(String address, String port) throws IOException {
        mc_addr = InetAddress.getByName(address);
        port_number=Integer.parseInt(port);
        socket = new MulticastSocket(port_number);

    }


}
