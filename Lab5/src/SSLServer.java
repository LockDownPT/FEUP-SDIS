import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;

/**
 * Created by pedroc on 01/03/17.
 */
public class SSLServer {

    private HashMap<String, String> license_plates;
    /*is the port number the server shall use to provide the service*/
    private int port_number;
    /*is a sequence, possibly empty, of strings specifying the combination of cryptographic algorithms the server should use, in order of preference. If no cypher suite is specified, the server shall use any of the cypher-suites negotiated by default by the SSL provider of JSE.*/
    String [] cypher_suite;
    SSLServerSocket listener = null;
    SSLServerSocketFactory ssf = null;
    private int clientNumber=0;

    SSLServer(String [] args) throws IOException {

        license_plates = new HashMap<>();

        port_number=Integer.parseInt(args[0]);

        cypher_suite = new String[args.length-1];
        for(int i = 1;i<args.length;i++) {
            cypher_suite[i]=args[i];
        }

        ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {
            listener = (SSLServerSocket) ssf.createServerSocket(port_number);
            listener.setEnabledCipherSuites(cypher_suite);
        }
        catch( IOException e) {
            System.out.println("Server - Failed to create SSLServerSocket");
            e.getMessage();
            return;
        }
    }

    public void run() throws IOException{
        try{
            while(true){
                new ServerThread((SSLSocket)listener.accept(), clientNumber++, license_plates).start();
            }
        } finally {
            listener.close();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Echo <port_number>");
            return;
        }

        SSLServer SSLServer = new SSLServer(args);
        SSLServer.run();
    }


    public static class ServerThread extends Thread{

        private Socket socket;
        private int clientNumber;
        private HashMap<String, String> license_plates;

        public ServerThread(Socket socket, int clientNumber, HashMap<String, String> license_plates){
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.license_plates = license_plates;
        }

        public void run(){

            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String request = in.readLine();
                handleRequest(request, out);

            } catch (IOException e){
                System.out.println("Error handling client# " + clientNumber + ": " + e);
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing a socket");
                }
                System.out.println("Connection with client# " + clientNumber + " closed");
            }

        }

        public void handleRequest(String received, PrintWriter out) throws IOException {

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

                //Verify if license plate is already registered
                if(license_plates.get(plate_number)==null){
                    //add license plate to database
                    license_plates.put(plate_number,owner_name);
                    n_registered_licenses = Integer.toString(license_plates.size());
                    answer = n_registered_licenses;
                }else{
                    answer = "Plate already registered";
                }

            }else if(request_type.equals("LOOKUP")){

                if(license_plates.size() > 0){
                    plate_number = request[1];
                    owner_name = license_plates.get(plate_number);
                    //If plate not found
                    if(owner_name==null){
                        answer="NOT_FOUND";
                    }else{
                        n_registered_licenses = Integer.toString(license_plates.size());
                        answer = n_registered_licenses + " " + plate_number+ " " + owner_name;
                    }
                }else{
                    answer = "NOT_FOUND";
                }
            }else{
                answer = "ERROR";
            }
            System.out.println(answer);
            out.println(answer);
        }

    }


}
