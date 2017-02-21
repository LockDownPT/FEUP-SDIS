import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by pedroc on 14/02/17.
 */
public class Client {

    private String mcast_addr;
    private int mcast_port;
    private InetAddress srvc_addr;
    private int srvc_port;
    private String oper;
    private String plate_number;
    private String data;
    private DatagramSocket socket = new DatagramSocket();
    private DatagramPacket packet;
    private DatagramPacket multicastPacket;
    private MulticastSocket multicastSocket;
    private int SOCKET_READ_TIMEOUT = 3000;

    Client(String [] args) throws IOException {

        mcast_addr =args[0];
        mcast_port =Integer.parseInt(args[1]);
        oper =args[2];
        plate_number=args[3];

        multicastSocket = new MulticastSocket(mcast_port);
        InetAddress group = InetAddress.getByName(mcast_addr);
        multicastSocket.joinGroup(group);

        validatePlate();

        if(oper.equals("REGISTER")){
            String owner_name=args[4];
            data="REGISTER " + plate_number + " " + owner_name;
        }else{
            data="LOOKUP " + plate_number;
        }
    }

    public void sendRequest() throws IOException {
        byte[] sbuf = data.getBytes();
        packet = new DatagramPacket(sbuf, sbuf.length, srvc_addr, srvc_port);
        socket.send(packet);
        System.out.println("Request Sent!");
        getResponse();
    }

    public void getResponse() throws IOException {

        byte[] buf = new byte[256];
        packet = new DatagramPacket(buf, buf.length);

        //call to receive() for this DatagramSocket will block for only this amount of time.
        socket.setSoTimeout(SOCKET_READ_TIMEOUT);

        //if the client does receive an answer in the given timeout, it resends the packet
        try {
            socket.receive(packet);
        } catch (IOException e) {
            sendRequest();
        }

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Echoed Message: " + received);
    }

    //verifies that the plate has the following format XX-XX-XX
    public void validatePlate() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(!plate_number.matches("^(\\w{2}-?\\w{2}-?\\w{2})$")){
            System.out.println("Plate number not valid, enter new (AA-AA-AA): ");
            plate_number=br.readLine();
        }
    }

    public void getServerInfo() throws IOException {

        byte[] buf = new byte[256];
        multicastPacket = new DatagramPacket(buf, buf.length);

        multicastSocket.receive(multicastPacket);
        System.out.println("Received Server Info");
        srvc_addr = multicastPacket.getAddress();

        String receivedMulticast = new String(multicastPacket.getData(), 0, multicastPacket.getLength());
        //clean extra white spaces
        String cleanRequest = receivedMulticast.trim();
        srvc_port = Integer.parseInt(cleanRequest);

    }

    public static void main(String[] args) throws IOException {

        if (!(args.length == 4 || args.length == 5)) {
            System.out.println("Usage: java Echo <mcast_addr> <mcast_port> <oper> <opnd>");
            return;
        }

        Client client = new Client(args);

        client.getServerInfo();

        client.sendRequest();

    }
}
