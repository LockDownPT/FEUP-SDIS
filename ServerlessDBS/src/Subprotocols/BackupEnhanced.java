package Subprotocols;


import Message.Message;
import Message.Mailman;
import Peer.Peer;

import static Utilities.Constants.STORED;

public class BackupEnhanced extends Backup{

    public BackupEnhanced(String file, int replicationDegree, Peer peer) {
        super(file,replicationDegree,peer);
    }

    /**
     * Waits a random time before actually store the chunk
     * If the replication degree of the chunk is already achieved it doesn't store it
     */
    public void handlePutchunkEnhanced(Message message) {
        try {
            long sleep = (long) (Math.random() * 1000);
            System.out.println("SLEEP: " + sleep);
            Thread.sleep(sleep);
            int desiredRepDeg = Integer.parseInt(message.getMessageHeader().getReplicationDeg());
            int currentRepDeg = getPeer().getReplicationDegreeOfChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
            if (currentRepDeg < desiredRepDeg) {
                getPeer().getBackupProtocol(message.getMessageHeader().getFileId()).handlePutchunk(message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * A peer that stores the chunk upon receiving the PUTCHUNK message, replies by sending
     * on the multicast control channel (MC) a confirmation message with the following format:
     * STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
     * after a random delay uniformly distributed between 0 and 400 ms
     */
    public void deliverStoredMessageEnhanced(Mailman.SenderThread sender, Message message) {

        sender.deliverMessage(message, getPeer().getMc_ip(), getPeer().getMc_port(), STORED);


    }
}

