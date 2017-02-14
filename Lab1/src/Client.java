import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by pedroc on 14/02/17.
 */
public class Client {

    private String host_name;
    private int port_number;
    private String oper;
    private String plate_number;
    private String data;
    private DatagramSocket socket = new DatagramSocket();
    private DatagramPacket packet;

    Client(String [] args) throws IOException {

        host_name=args[0];
        port_number=Integer.parseInt(args[1]);
        oper =args[2];
        plate_number=args[3];

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
        InetAddress address = InetAddress.getByName(host_name);
        packet = new DatagramPacket(sbuf, sbuf.length, address, port_number);
        socket.send(packet);

    }

    public void getResponse() throws IOException {

        byte[] buf = new byte[256];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
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

    public static void main(String[] args) throws IOException {

        if (!(args.length == 4 || args.length == 5)) {
            System.out.println("Usage: java Echo <hostname> <port_number> <oper> <opnd>");
            return;
        }

        Client client = new Client(args);

        client.sendRequest();
        client.getResponse();

    }
}
