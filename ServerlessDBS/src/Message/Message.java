package Message;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import static Utilities.Constants.CR;
import static Utilities.Constants.LF;

public class Message {

    Header messageHeader;
    byte[] body = null;

    public Message(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDegree){
        messageHeader = new Header(messageType,version, senderId, fileId, chunkNo, replicationDegree);

    }

    public Message(DatagramPacket packet){

        messageHeader = new Header();
        try {
            getMessageFromPacket(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getMessageFromPacket(DatagramPacket packet) throws IOException {

        ByteArrayInputStream message = new ByteArrayInputStream(packet.getData());

        String header="";
        byte character;
        while((character = (byte) message.read()) != CR){
            header+= (char) character;
        }
        if(message.read() != LF || message.read() != CR || message.read() != LF){
            throw new IOException("Wrong Header Format.");
        }

        String[] requestHeader = header.split(" ");

        messageHeader.setMessageType(requestHeader[0]);

        switch (messageHeader.getMessageType()) {
            case "PUTCHUNK":
                messageHeader.setVersion(requestHeader[1]);
                messageHeader.setSenderId(requestHeader[2]);
                messageHeader.setFileId(requestHeader[3]);
                messageHeader.setChunkNo(requestHeader[4]);
                messageHeader.setReplicationDeg(requestHeader[5]);
                message.read(this.body);
                break;
            case "STORED":
            case "GETCHUNK":
            case "REMOVED":
                messageHeader.setVersion(requestHeader[1]);
                messageHeader.setSenderId(requestHeader[2]);
                messageHeader.setFileId(requestHeader[3]);
                messageHeader.setChunkNo(requestHeader[4]);
                break;
            case "CHUNK":
                messageHeader.setVersion(requestHeader[1]);
                messageHeader.setSenderId(requestHeader[2]);
                messageHeader.setFileId(requestHeader[3]);
                messageHeader.setChunkNo(requestHeader[4]);
                message.read(this.body);
                break;
            case "DELETE":
                messageHeader.setVersion(requestHeader[1]);
                messageHeader.setSenderId(requestHeader[2]);
                messageHeader.setFileId(requestHeader[3]);
                break;
            default:
                break;
        }

    }

    public byte[] getMessageBytes(){

        byte[] headerBytes = messageHeader.getHeaderString().getBytes();

        byte[] buf = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, buf, 0, headerBytes.length);
        System.arraycopy(body, 0, buf, headerBytes.length, body.length);

        return buf;
    }

    public Header getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDegree) {
        this.messageHeader = new Header(messageType,version, senderId, fileId, chunkNo, replicationDegree);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }



}
