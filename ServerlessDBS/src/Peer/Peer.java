package Peer;


import Channels.MC;
import Channels.MDB;
import Channels.MDR;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;

public class Peer extends UnicastRemoteObject implements PeerInterface{

    MC controlChannel;
    MDB backupChannel;
    MDR restoreChannel;

    String mc_ip, mdb_ip, mdr_ip;
    int mc_port, mdb_port, mdr_port;

    public Peer() throws IOException {
        super();
        backupChannel = new MDB(mdb_ip, mdb_port);
        restoreChannel = new MDR(mdr_ip, mdr_port);
        controlChannel = new MC(mc_ip,mc_port);

        backupChannel.listen();
        restoreChannel.listen();
        controlChannel.listen();

    }

    public void backup(String file, int replicationDegree){
        System.out.println(file);
        System.out.println(replicationDegree);


    }

    public void restore(String file){
        System.out.println(file);
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
