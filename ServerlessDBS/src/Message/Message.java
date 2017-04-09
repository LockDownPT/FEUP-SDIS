package Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import static Utilities.Constants.*;

public class Message {

    private Header messageHeader;
    private byte[] body;
    private InetAddress packetIP;
    private int packetPort;

    /**
     * Message constructor for PUTCHUNKS messages
     * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
     *
     * @param messageType       indicates message type
     * @param version           indicates the version of the peer that sends the message
     * @param senderId          indicates the sender id
     * @param fileId            indicates the file id
     * @param chunkNo           indicate the chunk number
     * @param replicationDegree indicates the desired replication degree
     */
    public Message(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDegree) {
        messageHeader = new Header(messageType, version, senderId, fileId, chunkNo, replicationDegree);

    }

    /**
     * Message header for CHUNK, GETCHUNK and STORED messages
     * <MessageType> <Version> <SenderId> <FileId> <ChunkNo><CRLF>
     *
     * @param messageType indicates message type
     * @param version     indicates the version of the peer that sends the message
     * @param senderId    indicates the sender id
     * @param fileId      indicates the file id
     * @param chunkNo     indicate the chunk number
     */
    public Message(String messageType, String version, String senderId, String fileId, String chunkNo) {
        messageHeader = new Header(messageType, version, senderId, fileId, chunkNo);
    }

    /**
     * Message header for DELETE messages
     * <MessageType> <Version> <SenderId> <FileId> <CRLF>
     *
     * @param messageType indicates message type
     * @param version     indicates the version of the peer that sends the message
     * @param senderId    indicates the sender id
     * @param fileId      indicates the file id
     */
    public Message(String messageType, String version, String senderId, String fileId) {
        messageHeader = new Header(messageType, version, senderId, fileId);
    }


    /**
     * Constructs a message from a datagram packet
     *
     * @param packet packet received from the multicast
     */
    public Message(DatagramPacket packet) {

        messageHeader = new Header();
        try {
            getMessageFromPacket(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Constructs a message from byte array
     *
     * @param message byte array
     */
    public Message(byte[] message) {

        messageHeader = new Header();
        try {
            getMessageFromBytes(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Constructs message header and body from byte array
     *
     * @param bytes buffer array
     * @throws IOException
     */
    private void getMessageFromBytes(byte[] bytes) throws IOException {

        int length = bytes.length;
        ByteArrayInputStream message = new ByteArrayInputStream(bytes);

        StringBuilder header = new StringBuilder();
        byte character;
        while ((character = (byte) message.read()) != CR) {
            header.append((char) character);
        }

        if ((byte) message.read() != LF || (byte) message.read() != CR || (byte) message.read() != LF) {
            throw new IOException("Wrong Header Format.");
        }

        String[] requestHeader = header.toString().split(" ");

        messageHeader.setMessageType(requestHeader[0]);
        byte[] bodyContent;
        messageHeader.setVersion(requestHeader[1]);
        messageHeader.setSenderId(requestHeader[2]);
        messageHeader.setFileId(requestHeader[3]);
        messageHeader.setChunkNo(requestHeader[4]);
        bodyContent = new byte[length - header.length() - 4];
        message.read(bodyContent);
        setBody(bodyContent);

    }

    /**
     * Constructs message header and body from datagram packet
     *
     * @param packet datagram packet
     * @throws IOException
     */
    private void getMessageFromPacket(DatagramPacket packet) throws IOException {

        ByteArrayInputStream message = new ByteArrayInputStream(packet.getData());

        StringBuilder header = new StringBuilder();
        byte character;
        while ((character = (byte) message.read()) != CR) {
            header.append((char) character);
        }

        if ((byte) message.read() != LF || (byte) message.read() != CR || (byte) message.read() != LF) {
            throw new IOException("Wrong Header Format.");
        }

        String[] requestHeader = header.toString().split(" ");

        messageHeader.setMessageType(requestHeader[0]);
        byte[] bodyContent;
        switch (messageHeader.getMessageType()) {
            case "PUTCHUNK":
                messageHeader.setVersion(requestHeader[1]);
                messageHeader.setSenderId(requestHeader[2]);
                messageHeader.setFileId(requestHeader[3]);
                messageHeader.setChunkNo(requestHeader[4]);
                messageHeader.setReplicationDeg(requestHeader[5]);
                bodyContent = new byte[packet.getLength() - header.length() - 4];
                message.read(bodyContent);
                setBody(bodyContent);
                break;
            case "GETCHUNK":
                setPacketIP(packet.getAddress());
                setPacketPort(packet.getPort());
            case "STORED":
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
                bodyContent = new byte[packet.getLength() - header.length() - 4];
                message.read(bodyContent);
                setBody(bodyContent);
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

    /**
     * @param protocol protocol calling the function
     * @return returns message bytes
     */
    public byte[] getMessageBytes(String protocol) {

        byte[] headerBytes = messageHeader.getHeaderString().getBytes();
        byte[] buf;
        if (protocol.equals(PUTCHUNK) || protocol.equals(CHUNK)) {
            buf = new byte[headerBytes.length + body.length];
            System.arraycopy(headerBytes, 0, buf, 0, headerBytes.length);
            System.arraycopy(body, 0, buf, headerBytes.length, body.length);
        } else {
            buf = new byte[headerBytes.length];
            System.arraycopy(headerBytes, 0, buf, 0, headerBytes.length);
        }

        return buf;
    }

    public Header getMessageHeader() {
        return messageHeader;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public InetAddress getPacketIP() {
        return packetIP;
    }

    public void setPacketIP(InetAddress packetIP) {
        this.packetIP = packetIP;
    }

    public int getPacketPort() {
        return packetPort;
    }

    public void setPacketPort(int packetPort) {
        this.packetPort = packetPort;
    }
}
