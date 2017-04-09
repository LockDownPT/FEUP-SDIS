package Subprotocols;

import Message.Mailman;
import Message.Message;
import Peer.Peer;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Utilities.Constants.*;
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
    /* Socket to be used in enhanced protocol (version: 1.1) */
    private Socket enhancedSocket;
    private ServerSocket listener;
    /* Socket ouput for enhanced protocol (version: 1.1) */
    private OutputStream out = null;
    private DataOutputStream dos = null;
    private boolean tcpConnected = false;
    private boolean finishedRestore;

    public Restore(String file, Peer peer) {

        fileName = file;
        this.peer = peer;
        this.finishedRestore=false;
    }

    public Restore(Peer peer) {
        this.peer = peer;
        this.finishedRestore=false;
    }

    public void start() {

        System.out.println("Gathering file info");
        getFileInfo();

        if (peer.getVersion().equals("1.1")) {
            Runnable enhancedRestore = new RestoreEnhanced(this);
            peer.getDeliverExecutor().submit(enhancedRestore);
        }

        System.out.print("Requesting chunks");
        requestChunks();
        do {
            try {
                sleep(500);
                System.out.print(".");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Restored Chunks: " + restoredChunks);
            System.out.println("Number of Chunks: " + numberOfChunks);
        } while (restoredChunks < numberOfChunks);
        System.out.println("Constructing File");
        constructFile();
        System.out.println("Finished Restore");
        if(peer.getVersion().equals("1.1")){
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            this.numberOfChunks = (int) Math.floor(fileLength / maxSizeChunk) + 1;
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
            System.out.println("Requesting chunk number: " + chunkNo);
            chunkNo++;
        }
    }

    private void constructFile() {

        FileOutputStream fop = null;
        File file;
        File dir;
        try {
            dir = new File("./" + peer.getPeerId() + "/Restored Files");
            dir.mkdir();
            file = new File("./" + peer.getPeerId() + "/Restored Files/" + fileName);
            fop = new FileOutputStream(file, true);
            for (int i = 1; i <= chunks.size(); i++) {
                System.out.println((chunks.get(Integer.toString(i))).length);
                fop.write(chunks.get(Integer.toString(i)));
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

    /**
     * If the peer has the chunk and it hasn't been sent by another peer, it will send it.
     */
    public void sendChunk(Message message) {
        if (peer.hasChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
            try {
                Thread.sleep((long) (Math.random() * 400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (!peer.hasChunkBeenSent(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo())) {
                    Message chunk = new Message(CHUNK, peer.getVersion(), peer.getPeerId(), message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
                    chunk.setBody(peer.getChunk(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo()));
                    deliverChunkMessage(chunk, message);
                }
                peer.removeChunkFromSentChunks(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
            }
        }
    }

    /**
     * Saves received chunk, if it has asked for it
     */
    public void saveChunk(Message message) {
        if (peer.getRestoreProtocol() != null) {
            peer.getRestoreProtocol().storeChunk(message.getMessageHeader().getChunkNo(), message.getBody());
        } else {
            peer.addSentChunkInfo(message.getMessageHeader().getFileId(), message.getMessageHeader().getChunkNo());
        }
    }

    private void storeChunk(String chunkNo, byte[] chunk) {
        if (chunks.get(chunkNo) == null) {
            chunks.put(chunkNo, chunk);
            restoredChunks++;
            System.out.println("Received chunk: " + chunkNo + "Chunk Size: " + chunk.length);
        }
    }

    /**
     * Sends a GETCHUNK request for the multicast control channel (MC) with the following format:
     * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
     */
    public void deliverGetchunkMessage(Message message) {
        Mailman mailman = new Mailman(message, peer.getMc_ip(), peer.getMc_port(), STORED, peer);
        mailman.startMailmanThread();
    }

    /**
     * Sends a CHUNK message for the multicast data restore channel (MDR) with the following format:
     * CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> <Body>
     */
    public void deliverChunkMessage(Message newMessage, Message request) {
        if (request.getMessageHeader().getVersion().equals("1.1") && peer.getVersion().equals("1.1")) {

            if (!tcpConnected) {
                connectToServerSocket(request.getPacketIP(), peer.getMdr_port());
                tcpConnected = true;
            }

            try {
                while (enhancedSocket.getInputStream().available() != 0) {
                    System.out.println("Waiting for socket to be empty");
                }
                dos.writeInt(newMessage.getMessageBytes(CHUNK).length);
                dos.write(newMessage.getMessageBytes(CHUNK));
                System.out.println("SENT CHUNK " + request.getMessageHeader().getChunkNo());
            } catch (IOException e) {
                connectToServerSocket(request.getPacketIP(), peer.getMdr_port());
            }
        } else {
            Mailman mailman = new Mailman(newMessage, peer.getMdr_ip(), peer.getMdr_port(), CHUNK, peer);
            mailman.startMailmanThread();
            System.out.println("Sent CHUNK: " + request.getMessageHeader().getChunkNo());
        }

    }

    public void connectToServerSocket(InetAddress ip, int port) {
        System.out.println("TCP ip: " + ip);
        System.out.println("TCP port: " + port);
        try {
            enhancedSocket = new Socket(ip, port);
            out = enhancedSocket.getOutputStream();
            dos = new DataOutputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class RestoreEnhanced implements Runnable {

        private Restore restore;

        public RestoreEnhanced(Restore restore) {
            this.restore=restore;
        }

        public void run() {
            System.out.println("Connecting to socket");
            try {
                listener = new ServerSocket(peer.getMdr_port());
                System.out.println("Connected to socket");
                while (true) {
                    Runnable requestHandler = new RequestHandler(listener.accept(), restore);
                    peer.getDeliverExecutor().submit(requestHandler);
                    System.out.println("RECEIVED CHUNK");
                }
            } catch (IOException e) {
                try {
                    listener.close();
                    tcpConnected=false;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    public class RequestHandler implements Runnable {

        private Socket socket;
        private Restore restore;

        public RequestHandler(Socket socket, Restore restore) {
            this.socket = socket;
            this.restore=restore;
        }

        public void run() {
            System.out.println("REQUEST HANDLER STARTED");
            while (!restore.finishedRestore) {
                try {
                    InputStream in = socket.getInputStream();
                    DataInputStream dis = new DataInputStream(in);

                    int len = dis.readInt();
                    byte[] data = new byte[len];
                    if (len > 0) {
                        dis.readFully(data);
                    }

                    Message requestMessage = new Message(data);
                    saveChunk(requestMessage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                socket.close();
                System.out.println("CLOSED SOCKET");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
