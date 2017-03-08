package Peer;

import java.io.*;

public class Peer {

    Peer(String[] args) {

        String fileName = args[0];

    }


    public void createChunks(String fileName) {
        try {
            long maxSizeChunk = 64 * 1024;
            String path = ".\\TestFiles\\" + fileName;
            File file = new File(path);
            RandomAccessFile fileRaf = new RandomAccessFile(file, "r");
            long fileLength = fileRaf.length();
            int numSplits = (int) (fileLength/maxSizeChunk);
            int lastChunkSize = (int) (fileLength -(maxSizeChunk*numSplits));

            System.out.println(fileLength);
            System.out.println(maxSizeChunk);
            System.out.println(numSplits);
            System.out.println(lastChunkSize);

            for (int destIx = 1; destIx <= numSplits; destIx++) {
                BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(".\\storage\\"+destIx+".data"));

                    readWrite(fileRaf, bw, maxSizeChunk);

                bw.close();
            }
            if(lastChunkSize >= 0) {
                BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(".\\storage\\"+(numSplits+1)+".data"));
                readWrite(fileRaf, bw,(long)lastChunkSize);
                bw.close();
            }
            fileRaf.close();
            
        } catch (IOException e) {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }


    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }



    public static void main(String[] args) throws IOException {

       Peer Peer = new Peer(args);

       Peer.createChunks(args[0]);
    }
}
