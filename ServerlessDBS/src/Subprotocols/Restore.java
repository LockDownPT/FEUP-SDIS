package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Utilities.Constants.GETCHUNK;
import static Utilities.Utilities.createHash;
import static java.lang.Thread.sleep;


public class Restore {

    /**
     * String is the chunk number
     * byte[] holds the chunk data
     */
    private Map<String, byte[]> chunks = new ConcurrentHashMap<>();
    private String fileName;
    private Peer peer;
    private int numberOfChunks = 0;
    private int restoredChunks = 0;
    private String fileId;

    public Restore(String file, Peer peer) {

        fileName = file;
        this.peer = peer;
    }

    public void start() {

        System.out.println("Gathering file info");
        getFileInfo();
        System.out.print("Requesting chunks");
        requestChunks();
        do {
            try {
                sleep(500);
                System.out.print(".");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (restoredChunks < numberOfChunks);
        System.out.println("Constructing File");
        constructFile();
        System.out.println("Finished Restore");

    }

    private void getFileInfo() {
        long maxSizeChunk = 64 * 1000;
        String path = "./TestFiles/" + fileName;
        File file = new File(path);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        this.fileId = createHash(fileName + sdf.format(file.lastModified()));
        RandomAccessFile fileRaf;
        try {
            fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            this.numberOfChunks = (int) (fileLength / maxSizeChunk) + 1;
            int lastChunkSize = (int) (fileLength - (maxSizeChunk * this.numberOfChunks));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Number of chunks: " + this.numberOfChunks);
    }

    private void requestChunks() {

        int chunkNo = 1;

        while (chunkNo <= numberOfChunks) {
            Message request = new Message(GETCHUNK, peer.getVersion(), peer.getPeerId(), this.fileId, Integer.toString(chunkNo));

            Mailman messageHandler = new Mailman(request, peer);
            messageHandler.startMailmanThread();

            chunkNo++;
        }


    }

    private void constructFile() {

        FileOutputStream fop = null;
        File file;
        try {
            file = new File("./" + fileName);
            fop = new FileOutputStream(file, true);
            Iterator it = chunks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
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

    public void storeChunk(String chunkNo, byte[] chunk) {
        if (chunks.get(chunkNo) == null) {
            chunks.put(chunkNo, chunk);
            restoredChunks++;
            System.out.println("Received chunk: " + chunkNo);
        }
    }
}
