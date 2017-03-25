package Subprotocols;

import Message.Message;
import Peer.Peer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Message.Mailman;

import static Utilities.Constants.GETCHUNK;
import static Utilities.Utilities.createHash;


public class Restore {

    /**
     * String is the chunk number
     * byte[] holds the chunk data
     */
    private Map<String,byte[]> chunks = new ConcurrentHashMap<>();
    private String fileName;
    private Peer peer;
    private int numberOfChunks=0;
    private int lastChunkSize=0;
    private int restoredChunks=0;
    private String fileId;

    public Restore(String file,Peer peer){

        fileName=file;
        this.peer=peer;
    }

    public void start(){

        getFileInfo();
        requestChunks();
        while(restoredChunks<numberOfChunks){
            //getting chunks
        }
        constructFile();

    }

    public void getFileInfo(){
        long maxSizeChunk = 64 * 1000;
        String path = "./TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));
        RandomAccessFile fileRaf = null;
        try {
            fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            this.numberOfChunks = (int) (fileLength/maxSizeChunk);
            this.lastChunkSize = (int) (fileLength -(maxSizeChunk*this.numberOfChunks));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestChunks(){

        int chunkNo=1;

        //TODO: finish this part and add GETCHUNK method to mailman
        while(chunkNo<numberOfChunks){
            Message request = new Message(GETCHUNK,peer.getVersion(), peer.getPeerId(), this.fileId, Integer.toString(chunkNo));

            Mailman messageHandler = new Mailman(request, peer);
            messageHandler.startMailmanThread();

            chunkNo++;
        }



    }

    public void constructFile(){

        FileOutputStream fop = null;
        File file;
        try {
            file = new File("./"+fileName);
            fop = new FileOutputStream(file, true);
            Iterator it = chunks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                fop.write((byte[]) pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public void storeChunk(String chunkNo, byte[] chunk){
        if(chunks.get(chunkNo)==null){
            chunks.put(chunkNo,chunk);
            restoredChunks++;
        }
    }
}
