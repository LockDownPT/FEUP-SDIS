import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

import static java.lang.Thread.sleep;

/**
 * Created by pedroc on 01/03/17.
 */
public class Client {

    private String host_name;
    private int port_number;
    private String oper;
    private String plate_number;
    private String data;
    private int SOCKET_READ_TIMEOUT = 3000;
    private Socket clientSocket;
    private PrintWriter out = null;
    private BufferedReader in = null;


    Client(String [] args) throws IOException {

        host_name=args[0];
        port_number=Integer.parseInt(args[1]);
        oper =args[2];
        plate_number=args[3];

        clientSocket = new Socket(host_name, port_number);

        validatePlate();

        if(oper.equals("REGISTER")){
            String owner_name=args[4];
            data="REGISTER " + plate_number + " " + owner_name;
        }else{
            data="LOOKUP " + plate_number;
        }
    }

    public void connectToServer() throws IOException {

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }

    public void sendRequest(){

        out.println(data);

    }

    public void receiveResponse() throws IOException {
        String response = in.readLine();
        System.out.println(response);
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

        client.connectToServer();

        client.sendRequest();

        client.receiveResponse();


    }
}
