package Subprotocols;


import Message.Message;

import java.io.*;
import java.net.*;

public class Backup {

    private DatagramSocket socket;
    private DatagramPacket packet;
    private String fileName;
    private int replicationDegree;
    private String senderId;
    private String mc_addr;
    private int mc_port;
    //TODO: get fileId from file hash
    private String fileId="fileId";
    private String version;



    public Backup(String version, String senderId, String file, int replicationDegree, String addr, int port){
        this.fileName = file;
        this.replicationDegree = replicationDegree;
        this.senderId = senderId;
        this.mc_addr=addr;
        this.mc_port=port;
        this.version=version;
    }

    public void sendChunk(byte[] chunk, int chunkNo){

        //TODO: Create constants file
        Message request = new Message("PUTCHUNK",version, senderId, fileId, Integer.toString(chunkNo), Integer.toString(replicationDegree));
        request.setBody(chunk);

        try {
            socket = new DatagramSocket();
            byte[] buf = request.getMessageBytes();
            InetAddress addr = InetAddress.getByName(mc_addr);
            packet = new DatagramPacket(buf, buf.length, addr, mc_port);
            socket.send(packet);

        } catch (IOException e) {
            System.out.println("Error sending chunk nº" + chunkNo);
            e.printStackTrace();
        }


    }

    public void readChunks() {
        int chunkNo=0;
        try {
            long maxSizeChunk = 64 * 1024;
            String path = "./TestFiles/" + fileName;
            File file = new File(path);
            RandomAccessFile fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            int numSplits = (int) (fileLength/maxSizeChunk);
            int lastChunkSize = (int) (fileLength -(maxSizeChunk*numSplits));

            System.out.println(fileLength);
            System.out.println(maxSizeChunk);
            System.out.println(numSplits);
            System.out.println(lastChunkSize);

            for (int chunkId = 1; chunkId <= numSplits; chunkId++) {

                byte[] buf = new byte[(int) maxSizeChunk];
                int val = fileRaf.read(buf);
                if(val != -1) {
                    sendChunk(buf, chunkId);
                }
                chunkNo++;

            }
            if(lastChunkSize >= 0) {

                byte[] buf = new byte[(int) (long)lastChunkSize];
                int val = fileRaf.read(buf);
                if(val != -1) {
                    sendChunk(buf, chunkNo+1);
                }

            }
            fileRaf.close();

        } catch (IOException e) {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }
}
