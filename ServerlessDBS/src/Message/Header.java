package Message;

import static Utilities.Constants.*;

public class Header {

    private String messageType;
    private String version;
    private String senderId;
    private String fileId;
    private String chunkNo;
    private String replicationDeg;

    /**
     * Message header for PUTCHUNKS messages
     * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
     *
     * @param MessageType    indicates message type
     * @param Version        indicates the version of the peer that sends the message
     * @param SenderId       indicates the sender id
     * @param FileId         indicates the file id
     * @param ChunkNo        indicate the chunk number
     * @param ReplicationDeg indicates the desired replication degree
     */
    public Header(String MessageType, String Version, String SenderId, String FileId, String ChunkNo, String ReplicationDeg) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();
        this.chunkNo = ChunkNo.trim();
        this.replicationDeg = ReplicationDeg.trim();

    }

    /**
     * Message header for CHUNK, GETCHUNK and STORED messages
     * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <CRLF>
     *
     * @param MessageType indicates message type
     * @param Version     indicates the version of the peer that sends the message
     * @param SenderId    indicates the sender id
     * @param FileId      indicates the file id
     * @param ChunkNo     indicate the chunk number
     */
    public Header(String MessageType, String Version, String SenderId, String FileId, String ChunkNo) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();
        this.chunkNo = ChunkNo.trim();

    }

    /**
     * Message header for Delete messages
     * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <CRLF>
     *
     * @param MessageType indicates message type
     * @param Version     indicates the version of the peer that sends the message
     * @param SenderId    indicates the sender id
     * @param FileId      indicates the file id
     */
    public Header(String MessageType, String Version, String SenderId, String FileId) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();
        this.fileId = FileId.trim();

    }

    /**
     * Message header for Alive messages
     * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <CRLF>
     *
     * @param MessageType indicates message type
     * @param Version     indicates the version of the peer that sends the message
     * @param SenderId    indicates the sender id
     */
    public Header(String MessageType, String Version, String SenderId) {

        this.messageType = MessageType.trim();
        this.version = Version.trim();
        this.senderId = SenderId.trim();

    }

    /**
     * Empty header constructor
     */
    public Header() {

    }


    public String getHeaderString() {

        switch (messageType) {
            case PUTCHUNK:
                return messageType + SPACE + version + SPACE + senderId + SPACE + fileId + SPACE + chunkNo + SPACE + replicationDeg + SPACE + CRLF + CRLF;
            case DELETE:
                return messageType + SPACE + version + SPACE + senderId + SPACE + fileId + SPACE + CRLF + CRLF;
            default:
                return messageType + SPACE + version + SPACE + senderId + SPACE + fileId + SPACE + chunkNo + SPACE + CRLF + CRLF;

        }
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return this.version;
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
