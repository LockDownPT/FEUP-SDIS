import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;

/**
 * Created by pedroc on 14/02/17.
 */
public class Server {

    private Hashtable<String, String> license_plate;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private int port_number;

    Server (String [] args) throws IOException {

        license_plate = new Hashtable<String, String>();
        port_number=Integer.parseInt(args[0]);
        socket = new DatagramSocket(port_number);

    }

    public void getRequest() throws IOException {

        byte[] rbuf = new byte[256];

        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);

        socket.close();
    }

    public void sendResponse() throws IOException {
        String ack = "AKC";
        byte[] buf = ack.getBytes();
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Echo <port_number>");
            return;
        }

        Server server = new Server(args);

        while(true){
            server.getRequest();
            server.sendResponse();
        }
    }

}
