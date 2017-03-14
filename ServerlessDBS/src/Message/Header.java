package Message;

public class Header {

    String messageType;
    String version;
    String senderId;
    String fileId;
    String chunkNo;
    String replicationDeg;
    String headerString;
    String CRLF = "\r\n";
    String space = " ";

    public Header(String MessageType, String Version, String SenderId, String FileId, String ChunkNo, String ReplicationDeg){

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();
        this.chunkNo = ChunkNo.trim();
        this.replicationDeg = ReplicationDeg.trim();

    }

    public Header(){

    }

    public String getHeaderString(){

        headerString = messageType + space + version + space + senderId + space + fileId + space + chunkNo + space + replicationDeg + space + CRLF;

        return headerString;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(String chunkNo) {
        this.chunkNo = chunkNo;
    }

    public String getReplicationDeg() {
        return replicationDeg;
    }

    public void setReplicationDeg(String replicationDeg) {
        this.replicationDeg = replicationDeg;
    }
}
