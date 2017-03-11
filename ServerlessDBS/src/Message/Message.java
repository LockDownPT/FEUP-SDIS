package Message;


public class Message {

    Header messageHeader;
    byte[] body = null;

    public Message(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDegree){
        messageHeader = new Header("BACKUP","1.0", senderId, fileId, chunkNo, replicationDegree);

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
        this.messageHeader = new Header("BACKUP","1.0", senderId, fileId, chunkNo, replicationDegree);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }



}
