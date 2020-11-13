import javax.crypto.SecretKeyFactory;
import java.util.*;

class KDCServer {
    private Hashtable<String, ArrayList<String>> servers;
//    private Hashtable<String, String> nonce;
    private Map<String, String> clients;
    private SecretKeyFactory kf;
//    private SharedResource sr;

    KDCServer() {
        try {
            kf = SecretKeyFactory.getInstance("DES");
        } catch (Exception e) {
            e.printStackTrace();
        }
        servers = new Hashtable<>();
        clients = new HashMap<>();
//        nonce = new Hashtable<>();
        servers.put("FolderS1", new ArrayList<>());
        servers.put("FolderS2", new ArrayList<>());
        servers.get("FolderS1").add("12345678");
        servers.get("FolderS2").add("87654321");
        clients.put("client1", "distri20");//9093
        clients.put("client2", "buteds21");//9094
        clients.put("client3", "ystems22");//9095
//        sr = new SharedResource(2);
    }

    //Client Authorization
    void AuthorizeClient(ServerSocketChannel ssc) {
        new AuthorizeClient(ssc).start();
    }

    private class AuthorizeClient extends Thread {
        private ServerSocketChannel ssc;

        AuthorizeClient(ServerSocketChannel ssc) {
            this.ssc = ssc;
        }

        @Override
        public void run() {
            CipherModule kA = new CipherModule(kf);
            CipherModule kB = new CipherModule(kf);
            int l = (int) (1e7);
            int r = (int) (1e8);
            Random random = new Random();

            //Needham-Schroeder
            String[] data = ssc.receive().split(" ");
//            for (String s : data) {
//                System.out.println(s);
//            }
            String client = data[0];
            String server = data[1];

            //Can include server idle check here
            if (clients.containsKey(client)) {
                kA.initialize(clients.get(client));
                kB.initialize(servers.get(server).get(0));
                int kAB = random.nextInt(r - l) + l;
                String send = data[2] + " " + servers.get(server).get(1) + " " + kAB+" ";
                send = send + kB.encrypt(client + " " + kAB);
                send = kA.encrypt(send);
                ssc.send(send);
            }
            ssc.close();
        }
    }

    //Server Registration
    void RegisterServer(ServerSocketChannel ssc) {
        new RegisterServer(ssc).start();
    }

    private class RegisterServer extends Thread {
        private ServerSocketChannel ssc;

        RegisterServer(ServerSocketChannel ssc) {
            this.ssc=ssc;
        }

        @Override
        public void run() {
            CipherModule kB=new CipherModule(kf);
            String get = ssc.receive();
            String[] data = get.split(" ");
            String server="FolderS"+data[0];
            kB.initialize(servers.get(server).get(0));
            data=kB.decrypt(data[1]).split(" ");
            //if statement to stop relay attacks
            if(servers.get(server).size()==1) {
                servers.get(server).add(data[0] + " " + data[1]);
//                nonce.put(server, data[2]);
            }
            ssc.close();
        }
    }
}
