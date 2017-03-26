package Subprotocols;


import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;

import static Utilities.Constants.PUTCHUNK;
import static Utilities.Utilities.createHash;

public class Backup {

    private String fileName;
    private int replicationDegree;
    private String fileId;
    private Peer creator;


    public Backup(String file, int replicationDegree, Peer creator) {
        this.fileName = file;
        this.replicationDegree = replicationDegree;
        this.fileId = null;
        this.creator = creator;
    }

    private void sendChunk(byte[] chunk, int chunkNo) {

        Message request = new Message(PUTCHUNK, creator.getVersion(), creator.getPeerId(), fileId, Integer.toString(chunkNo), Integer.toString(replicationDegree));
        request.setBody(chunk);

        Mailman messageHandler = new Mailman(request, creator);
        messageHandler.startMailmanThread();


    }

    public void readChunks() {
        int chunkNo = 0;
        try {
            long maxSizeChunk = 64 * 1000;
            String path = "./TestFiles/" + fileName;
            File file = new File(path);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            this.fileId = createHash(fileName + sdf.format(file.lastModified()));

            RandomAccessFile fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            int numSplits = (int) (fileLength / maxSizeChunk);
            int lastChunkSize = (int) (fileLength - (maxSizeChunk * numSplits));

            System.out.println(fileLength);
            System.out.println((int) maxSizeChunk);
            System.out.println(numSplits);
            System.out.println(lastChunkSize);

            for (int chunkId = 1; chunkId <= numSplits; chunkId++) {

                byte[] buf = new byte[(int) maxSizeChunk];
                int val = fileRaf.read(buf);
                if (val != -1) {
                    sendChunk(buf, chunkId);
                }
                chunkNo++;

            }
            if (lastChunkSize >= 0) {

                byte[] buf = new byte[(int) (long) lastChunkSize];
                int val = fileRaf.read(buf);
                if (val != -1) {
                    sendChunk(buf, chunkNo + 1);
                }

            }
            fileRaf.close();

        } catch (IOException e) {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }


}

