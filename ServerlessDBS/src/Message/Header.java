package Message;

import static Utilities.Constants.CRLF;
import static Utilities.Constants.SPACE;

public class Header {

    private String messageType;
    private String version;
    private String senderId;
    private String fileId;
    private String chunkNo;
    private String replicationDeg;

    public Header(String MessageType, String Version, String SenderId, String FileId, String ChunkNo, String ReplicationDeg) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();
        this.chunkNo = ChunkNo.trim();
        this.replicationDeg = ReplicationDeg.trim();

    }

    public Header(String MessageType, String Version, String SenderId, String FileId, String ChunkNo) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();
        this.chunkNo = ChunkNo.trim();

    }

    public Header(String MessageType, String Version, String SenderId, String FileId) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();

    }

    public Header() {

    }

    public String getHeaderString() {

        return messageType + SPACE + version + SPACE + senderId + SPACE + fileId + SPACE + chunkNo + SPACE + replicationDeg + SPACE + CRLF + CRLF;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion(){
        return this.version;
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
