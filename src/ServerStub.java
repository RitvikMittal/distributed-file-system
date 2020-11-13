import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.lang.*;
import java.io.*;
class ServerStub {
    private ServerSocket ss;
    private Random random;
    private CipherModule kB;
    private static final int RandomRange = (int) (1e9);
    private SecretKeyFactory kf;
    private SharedResource sr;
    private Services s;
    ServerStub(Services s) {
        this.s=s;
        int PORT=9096+s.id;
        System.out.print(PORT);
        random=new Random();
        String IP="127.0.0.1";
        sr=new SharedResource();
        try {
            kf=SecretKeyFactory.getInstance("DES");
            kB=new CipherModule(kf);
            kB.initialize(s.password);
            ss = new ServerSocket(PORT);
        }catch (Exception e){
            e.printStackTrace();
        }

        //Register File sever with the KDC
        ClientSocketChannel KDC=new ClientSocketChannel();
        KDC.initialize("127.0.0.1",9091);
        int KDCNonce=random.nextInt(RandomRange);
        String send="2\n"+ s.id+" "+kB.encrypt(IP+" "+PORT+" "+KDCNonce);
        PORT++;
        KDC.send(send);
        KDC.close();
        while (true){
            try {
                new ClientThread(ss.accept()).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    //Maintain concurrency on the file server between the client threads
    //Supports multiple readers, single writer
    private class SharedResource{
        boolean exclusive;
        int readerCount;
        SharedResource(){
            exclusive=false;
            readerCount=0;
        }
        synchronized boolean handleReaderEntry(){
            if(!exclusive){
                readerCount++;
            }
            return !exclusive;
        }
        synchronized void handleReaderExit(){
            readerCount--;
        }
        synchronized boolean handleWriterEntry(){
            if(readerCount==0&&!exclusive){
                exclusive=true;
                return true;
            }else{
                return false;
            }
        }
        synchronized void handleWriterExit(){
            exclusive=false;
        }
    }

    //Client Thread
    private class ClientThread extends Thread{
        private CipherModule kAB;
        private ServerSocketChannel client;
        private String ClientNonce;
        private String c;

        //Current directory which the client is using
        private String dir="/";
        ClientThread(Socket s){
            kAB=new CipherModule(kf);
            client=new ServerSocketChannel();
            client.initialize(s);
        }

        @Override
        public void run() {
            System.out.println(s.id);
            acceptClient();
        }

        private void acceptClient() {
            //Needham-Schroeder
            String get = client.receive();//3
            String[] data = get.split(" ");
            data[1] = kB.decrypt(data[1]);
            String[] use = data[1].split(" ");
            c = use[0];
            String symKey = use[1];
            kAB.initialize(symKey);
            data[0] = kAB.decrypt(data[0]);

            int nonce = random.nextInt(RandomRange);//4
            String send = (Integer.parseInt(data[0]) - 1) + " " + nonce;
            send = kAB.encrypt(send);
            client.send(send);

            get = client.receive();//5
            if (get == null) {
                client.close();
//            changeClient();
            } else {
                get = kAB.decrypt(get);
                data=get.split(" ");
                if (data[0].equals((nonce - 1) + "")) {
                    ClientNonce=data[1];
                    String tempNonce=random.nextInt(RandomRange)+"";
                    client.send(kAB.encrypt(ClientNonce+" "+tempNonce));
                    ClientNonce=tempNonce;
                    beginServing();
                } else {
                    client.close();
//                changeClient();
                }
            }
        }

        private void beginServing() {
            String mssg;
            while ((mssg = client.receive()) != null) {
                //handle the incoming command from the connected client
                mssg = kAB.decrypt(mssg);
                System.out.println(mssg);
                String[] data=mssg.split(" ");
                String chk=data[0];
                String newNonce;
                String command;
                try{
                    command=data[1];
                    if(chk.equals(ClientNonce)) {
                        if (command.equals("close")) {
                            client.close();
                            System.out.println("Closed Connection with "+c);
                            break;
                        } else {
                            newNonce=data[data.length-1];
                            StringBuilder send=new StringBuilder(newNonce+" ");
                            ClientNonce=random.nextInt(RandomRange)+"";
                            String ret="";
                            String arg="";
                            String argg="";
                            switch (command){
                                case "cd":
                                    while(!sr.handleReaderEntry()){};
                                    arg=data[2];
                                    ret=cd(arg);
                                    sr.handleReaderExit();
                                    break;
                                case "ls":
                                    while(!sr.handleReaderEntry()){};
                                    ret=ls();
                                    sr.handleReaderExit();
                                    break;
                                case "cat":
                                    while(!sr.handleReaderEntry()){};
                                    arg=data[2];
                                    ret=cat(arg);
                                    sr.handleReaderExit();
                                    break;
                                case "cp":
                                    while(!sr.handleWriterEntry()){};
                                    arg=data[2];
                                    argg=data[3];
                                    ret=cp(arg,argg);
                                    sr.handleWriterExit();
                                    break;
                                case "mv":
                                    while(!sr.handleWriterEntry()){};
                                    arg=data[2];
                                    argg=data[3];
                                    ret=mv(arg,argg);
                                    sr.handleWriterExit();
                                    break;
                                case "rm":
                                    while(!sr.handleWriterEntry()){};
                                    arg=data[2];
                                    ret=rm(arg);
                                    sr.handleWriterExit();
                                    break;
                                default:
                                    System.out.println(command+" is a bad request");
                            }
                            send.append(ret);
                            if(ret.length()>0) {
                                send.append(" ");
                            }
                            send.append(ClientNonce);
                            client.send(kAB.encrypt(send.toString()));
                            //call the required service
                        }
                    }
                }catch (ArrayIndexOutOfBoundsException e){
                    System.out.println("Array Index out of bounds. A false request is received, continue.");
                }
            }
        }
        //implements rm
        private String rm(String file){
            boolean pos=s.rm(dir+file);
            String ret="";
            if(!pos){
                ret="There is no such file\n";
            }
            return ret;
        }

        //implements mv
        private String mv(String src,String dst){
            return s.mv(dir+src,dir+dst);
        }

        //implements cp
        private String cp(String src,String dst){
            String ret="";
            boolean pos=s.cp(dir+src,dir+dst);
            if(!pos){
                ret="Source File not found\n";
            }
            return ret;
        }

        //implements cat
        private String cat(String file){
            String get=s.cat(dir+file);
            if(get==null){
                return "@#$%%$#@";
            }else{
                return get;
            }
        }

        //implements cd
        private String cd(String arg){
            String ret="";
            if(arg.equals("..")){
                String[] path = dir.split("/");
                StringBuilder join=new StringBuilder("/");
                for(int i=1;i<path.length-1;i++){
                    join.append(path[i]);
                    join.append("/");
                }
                dir=join.toString();
            }else{
                boolean exist=s.cd(dir+arg);
                if(exist){
                    dir=dir+arg+"/";
                }
                ret=exist+"";
            }
            return ret;
        }

        //implements ls
        private String ls(){
            String[] items=s.ls(dir);
            StringBuilder ret=new StringBuilder("");
            if(items!=null&&items.length>0){
                for(int i=0;i<items.length-1;i++){
                    ret.append(items[i]);
                    ret.append(" ");
                }
                ret.append(items[items.length-1]);
            }
            return ret.toString();
        }
    }
}
