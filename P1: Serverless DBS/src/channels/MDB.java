package channels;

import java.io.IOException;

/**
 * Created by pedroc on 06/03/17.
 */
public class MDB extends Channel {
    public MDB(String address, String port) throws IOException {
        super(address, port);
        this.thread = new MDBThread();


    }

    public class MDBThread extends Thread{
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
