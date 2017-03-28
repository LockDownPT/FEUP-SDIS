package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.util.Map;

import static Utilities.Constants.REMOVED;

public class SpaceReclaim {

    private Peer peer;
    private long spaceToBeReduced=0;


    public SpaceReclaim(Peer peer, long spaceToBeReduced) {
        this.peer=peer;
        this.spaceToBeReduced=spaceToBeReduced;
    }

    public void start(){

            if(findExtraChunks()){
                System.out.println("Deleted Extra chunks");
            }
            else{
                while (removeChunksWithLowerRepDeg()){
                    System.out.println("Removing chunks with lower replication degree");
                }
                System.out.println("Deleted chunks with lower replication degree");
            }
        System.out.println("Finished Reclaim Space");

    }

    public boolean findExtraChunks(){

        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int tempRepDeg=peer.getReplicationDegreeOfChunk(key);
            if(tempRepDeg > Integer.parseInt(value)){
                removeChunk(key);
                sendRemovedMessage(key);
            }
            if(spaceToBeReduced==0)
                return true;
        }
        return false;
    }

    public boolean removeChunksWithLowerRepDeg(){
        for (Map.Entry<String, String> entry : peer.getStoredChunks().entrySet()) {
            String key = entry.getKey();

            int tempRepDeg=peer.getReplicationDegreeOfChunk(key);
            if(tempRepDeg>1){
                removeChunk(key);
                sendRemovedMessage(key);
            }
            if(spaceToBeReduced==0)
                return true;
        }
        return false;
    }

    public boolean removeChunk(String chunkId){

        //TODO: usar codigo do protocolo delete

        return true;
    }

    public void sendRemovedMessage(String chunkId){

        Message message = new Message(REMOVED, peer.getVersion(), peer.getPeerId(), peer.getFileIdFromChunkId(chunkId), peer.getChunkNoFromChunkId(chunkId));
        Mailman mailman = new Mailman(message,peer);
        mailman.startMailmanThread();

    }

    public void deliverRemovedMessage(Message message) {

        Mailman mailman = new Mailman(message, peer.getMc_ip(),peer.getMc_port(),REMOVED);
        mailman.startMailmanThread();

    }

    public void updateChunkRepDegree(Message message) {

        peer.decreaseReplicationDegree(message.getMessageHeader().getFileId(),message.getMessageHeader().getChunkNo());

    }
}
