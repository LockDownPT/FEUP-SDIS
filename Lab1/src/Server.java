import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by pedroc on 14/02/17.
 */
public class Server {

    private HashMap<String, String> license_plates;
    private DatagramSocket socket;
    private int port_number;

    Server (String [] args) throws IOException {

        license_plates = new HashMap<>();
        port_number=Integer.parseInt(args[0]);
        socket = new DatagramSocket(port_number);

    }

    public void answerRequest() throws IOException {

        byte[] rbuf = new byte[256];

        DatagramPacket packet;
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);

        handleRequest(received, packet);
    }

    public void handleRequest(String received,DatagramPacket packet) throws IOException {

        String answer;
        String plate_number, owner_name, n_registered_licenses;
        //clean extra white spaces
        String cleanRequest = received.trim();
        //make array from string
        String[] request = cleanRequest.split(" ");

        String request_type = request[0];

        if (request_type.equals("REGISTER")){

            plate_number = request[1];
            owner_name = request[2];

            //add license plate to databse
            license_plates.put(plate_number,owner_name);
            n_registered_licenses = Integer.toString(license_plates.size());
            answer = n_registered_licenses;

        }else if(request_type.equals("LOOKUP")){

            plate_number = request[1];
            owner_name = license_plates.get(plate_number);
            n_registered_licenses = Integer.toString(license_plates.size());
            if(license_plates.size() > 0){
                answer = n_registered_licenses + " " + plate_number+ " " + owner_name;
            }else{
                answer = "-1";
            }
        }else{
            answer = "ERROR";
        }
        System.out.println(answer);
        sendResponse(answer, packet);
    }

    public void sendResponse(String answer, DatagramPacket packet) throws IOException {

        byte[] buf = answer.getBytes();
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
            server.answerRequest();
        }
    }

}
