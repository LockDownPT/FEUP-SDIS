
/**
 * Created by pedroc on 21/02/17.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class MulticastServerThread {

    private long ONE_SECOND = 1000;
    private String MCAST_ADDR;
    private int MCAST_PORT;
    private int SRVC_PORT;
    private InetAddress GROUP_ADDR;
    private MulticastSocket serverSocket;
    private DatagramPacket packet;

    MulticastServerThread(String[] args) throws IOException {
        MCAST_PORT = Integer.parseInt(args[2]);
        MCAST_ADDR = args[1];
        SRVC_PORT = Integer.parseInt(args[0]);

        // Get the address that we are going to connect to
        GROUP_ADDR = InetAddress.getByName(MCAST_ADDR);

        serverSocket = new MulticastSocket();
        byte[] buf = Integer.toString(SRVC_PORT).getBytes();
        packet = new DatagramPacket(buf, buf.length, GROUP_ADDR, MCAST_PORT);

    }

    public void run() {
        ScheduledThreadPoolExecutor repetitiveTask = new ScheduledThreadPoolExecutor(1);
        repetitiveTask.scheduleAtFixedRate(() -> {

            try {
                serverSocket.send(packet);
                System.out.println("Packet Sent");
            } catch (IOException e) {
                System.out.println("Error in Task");
            }

            //serverSocket.leaveGroup(GROUP_ADDR);
            //serverSocket.close();
        },0, 1, TimeUnit.SECONDS);

    }
}