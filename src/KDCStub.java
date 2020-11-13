import java.net.ServerSocket;
import java.net.Socket;

public class KDCStub {

    public static void main(String[] args) {
        KDCServer ks = new KDCServer();
        try {
            ServerSocket ss = new ServerSocket(9091);
            while (true) {
                new SelectMode(ss.accept(),ks).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SelectMode extends Thread {
        private Socket s;
        private KDCServer ks;
        SelectMode(Socket s,KDCServer ks) {
            this.ks=ks;
            this.s = s;
        }

        @Override
        public void run() {
            try {
                ServerSocketChannel ssc = new ServerSocketChannel();
                ssc.initialize(s);
                String mode = ssc.receive();
                switch (mode) {
                    case "1":
                        ks.AuthorizeClient(ssc);
                        break;
                    case "2":
                        ks.RegisterServer(ssc);
                        break;
                    default:
                        ssc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
