package Message;


import javax.sound.midi.SysexMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static Utilities.Constants.PUTCHUNK;
import static Utilities.Constants.STORED;

public class Mailman {

    private DatagramPacket request;
    private String peerId;
    private String mc_addr;
    private int mc_port;
    private Message message;
    Thread thread;


    public Mailman(DatagramPacket message, String peerHome, String mc_addr, int mc_port){

        this.request=message;
        this.peerId = peerHome;
        this.mc_addr = mc_addr;
        this.mc_port = mc_port;
        this.message = new Message(request);
        this.thread = new ReceiverThread();
    }
    public Mailman(Message message, String peerHome, String mc_addr, int mc_port){
        this.message = message;
        this.peerId = peerHome;
        this. mc_addr = mc_addr;
        this.mc_port = mc_port;
        this.thread = new SenderThread();
    }

    public void startMailmanThread(){
        this.thread.start();
    }

    public class SenderThread extends Thread {
        public void run() {

            switch (message.getMessageHeader().getMessageType()) {
                case PUTCHUNK:
                    deliverMessage(message,mc_addr,mc_port, PUTCHUNK);
                    break;
                case STORED:
                    deliverStoredMessage();
                default:
                    break;
            }


        }

        /**
         * A peer that stores the chunk upon receiving the PUTCHUNK message, replies by sending
         * on the multicast control channel (MC) a confirmation message with the following format:
         * STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
         * after a random delay uniformly distributed between 0 and 400 ms
         */
        public void deliverStoredMessage(){
            try {
                Thread.sleep((long)(Math.random() * 400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                deliverMessage(message, mc_addr, mc_port,STORED);
            }

        }

    }
    public class ReceiverThread extends Thread {
        public void run(){

            //Ignores requests sent by itself
            if(message.getMessageHeader().getSenderId().equals(peerId))
                return;
            switch (message.getMessageHeader().getMessageType()) {
                case PUTCHUNK:
                    storeChunk(message);
                    break;
                case STORED:
                        System.out.println("Received STORED");
                default:
                    break;
            }


        }

        public void storeChunk(Message message){

            OutputStream output = null;
            try {
                //Creates sub folders structure -> peerId/FileId/ChunkNo
                File outFile = new File(peerId+"/"+message.getMessageHeader().getFileId()+"/"+message.getMessageHeader().getChunkNo());
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
                output = new FileOutputStream(peerId+"/"+message.getMessageHeader().getFileId()+"/"+message.getMessageHeader().getChunkNo());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.write(message.getBody(), 0, message.getBody().length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    Message stored = new Message(STORED,"1.0", peerId,message.getMessageHeader().getFileId(),message.getMessageHeader().getChunkNo());
                    Mailman sendStored = new Mailman(stored, peerId, mc_addr, mc_port);
                    sendStored.startMailmanThread();
                    //deliverStoredMessage(peerId, message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        public void restoreMessage(){

        }

    }

    public void deliverMessage(Message message, String addr, int port, String messageType){

        DatagramSocket socket;
        DatagramPacket packet;

        try {
            socket = new DatagramSocket();
            byte[] buf = message.getMessageBytes(messageType);
            InetAddress address = InetAddress.getByName(addr);
            packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
