package Channels;

import java.io.IOException;

/**
 * Created by pedroc on 08/03/17.
 */
public class MC extends Channel{
    public MC(String address, int port) throws IOException {
        super(address, port);
        this.thread = new MC.MCThread();

    }

    public class MCThread extends Thread{
        public void run(){
            try{
                socket.joinGroup(mc_addr);

            } catch (IOException e){
                System.out.println("Error handling peer:" + e);
            }finally {

            }

        }


    }
}
