import org.omg.PortableInterceptor.INACTIVE;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.util.Arrays;
import java.util.Random;

class ClientStub {
    //Encryption/Decryption metadata
    private CipherModule kAB;
    private CipherModule kA;
    private int KDCPort;
    //Stub Metadata
    private String pwd;
    private final String ID;
    private Random random;
    private final int RandomRange = (int) (1e9);
    private ClientSocketChannel file;
    private String ServerNonce;

    //initialize the key to be used for encryption/decryption (kA to be used/key to be communicated)
    //After the constructor, the client has kA and can now receive requests
    ClientStub(String key, String ID) {
        pwd = "home/";
        this.ID = ID;
        KDCPort=9091;
        try {
            random = new Random();
            SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
            file=new ClientSocketChannel();
            kAB=new CipherModule(kf);
            kA=new CipherModule(kf);
            kA.initialize(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Get the present working directory stored locally by the stub
    String pwd() {
        return this.pwd;
    }

    //List the items of the pwd
    String ls() {
        StringBuilder ret = new StringBuilder();
        ret.append("> ");
        ret.append(pwd);
        ret.append("\n");
        if (pwd.equals("home/")) {
            ret.append("~ FolderS1\n");
            ret.append("~ FolderS2\n");
        } else {
            //Call the Server bound with the current socket
            //and list all the files in the appropriate directory
            String send=ServerNonce+" ls ";
            ServerNonce=random.nextInt(RandomRange)+"";
            send=send+ServerNonce;
            file.send(kAB.encrypt(send));
            String get=kAB.decrypt(file.receive());
            String[] data=get.split(" ");
            if(data[0].equals(ServerNonce)){
                ServerNonce=data[data.length-1];
                for(int i=1;i<data.length-1;i++){
                    ret.append("~ ");
                    ret.append(data[i]);
                    ret.append("\n");
                }
            }
        }
        return ret.toString();
    }

    //remove file
    String rm(String req){
        if(req.split(" ").length!=2){
            return "~ Wrong arguments for cp command\n";
        }
        String ret="";
        if(pwd.equals("home/")){
            ret = "~ Source File not found\n";
        }else{
            send(req);
            String get=kAB.decrypt(file.receive());
            ret=check(get);
            if(ret.length()!=0){
                ret="~ "+ret;
            }
        }
        return ret;
    }

    //move one file from one folder to another
    String mv(String req){
        if(req.split(" ").length!=3){
            return "~ Wrong arguments for cp command\n";
        }
        String ret="";
        if(pwd.equals("home/")){
            ret = "~ Source File not found\n";
        }else{
            send(req);
            String get=kAB.decrypt(file.receive());
            ret=check(get);
            if(ret.length()!=0){
                ret="~ "+ret;
            }
        }
        return ret;
    }

    //Copy one file to another in same folder
    String cp(String req){
        if(req.split(" ").length!=3){
            return "~ Wrong arguments for cp command\n";
        }
        String ret="";
        if(pwd.equals("home/")){
            ret = "~ Source File not found\n";
        }else{
            send(req);
            String get = kAB.decrypt(file.receive());
            ret=check(get);
            if(ret.length()!=0) {
                ret = "~ " + ret;
            }
        }
        return ret;
    }

    //Display the contents of the file
    String cat(String req){
        if(req.split(" ").length!=2){
            return "~ Wrong arguments for cat command\n";
        }
        String ret="";
        if(pwd.equals("home/")){
            ret = "~ There is no such file\n";
        }else {
            send(req);
            String get = kAB.decrypt(file.receive());
            ret=check(get);
            if(ret.equals("@#$%%$#@")){
                ret="~ There is no such file\n";
            }else {
                ret = "~ " + req.split(" ")[1] + "\n" + ret;
            }
        }
        return ret;
    }

    //Change the current directory
    String cd(String dir) {
        String cdret = "";
        String use;
        try {
            use = dir.split(" ")[1];
        }catch (ArrayIndexOutOfBoundsException e){
            return "~ Missing arguments for cd command\n";
        }
        StringBuilder ret = new StringBuilder();
        boolean flag = false;
        if (use.equals("..")) {
            String[] path = pwd.split("/");
            if (path.length == 1) {
                ret.append("home/");
            } else {
                if (path.length == 2) {
                    flag = true;
                }else{
                    String tempNonce=random.nextInt(RandomRange)+"";
                    String send=ServerNonce+" cd .. "+tempNonce;
                    file.send(kAB.encrypt(send));
                    ServerNonce=tempNonce;
                    String get=file.receive();
                    String[] data=kAB.decrypt(get).split(" ");
                    if(data[0].equals(ServerNonce)){
                        ServerNonce=data[1];
                    }
                }
                for (int i = 0; i < path.length - 1; i++) {
                    ret.append(path[i]);
                    ret.append("/");
                }
            }
        } else {
            ret.append(pwd);
            if (pwd.equals("home/")) {
                if (use.equals("FolderS1") || use.equals("FolderS2")) {
                    //call KDC and perform authentication as our present working directory changes to one of the server
                    //Needham-Schroeder protocol here as we need address of the required file server
                    StringBuilder send = new StringBuilder();
                    send.append("1\n");//to support KDC multithreading. 1 is the mode for client authorization.
                    send.append(ID);
                    send.append(" ");
                    send.append(use);
                    send.append(" ");
                    int nonce=random.nextInt(RandomRange);
                    send.append(nonce);

                    //First and Second Step of protocol
                    ClientSocketChannel KDC = new ClientSocketChannel();
                    KDC.initialize("127.0.0.1",KDCPort);
                    KDC.send(send.toString());//1
                    String get=KDC.receive();//2
                    if(get==null){
                        cdret="~ User authorization failed.\n";
                    }else{
                        String[] data=kA.decrypt(get).split(" ");
                        if(Integer.parseInt(data[0])!=nonce){
                            //Some kind of attack as nonce does not match what user has
                            //or password mismatch with the KDC
                            cdret="~ User authorization failed.\n";
                            KDC.close();
                        }else {
                            KDC.close();
                            //Next steps of the protocol
                            kAB.initialize(data[3]);
                            nonce=random.nextInt(RandomRange);
                            send=new StringBuilder();
                            send.append(kAB.encrypt(nonce+"")+" ");
                            send.append(data[4]);
                            file.initialize(data[1],Integer.parseInt(data[2]));
                            file.send(send.toString());//3

                            get=file.receive();//4
                            data=kAB.decrypt(get).split(" ");
                            String chk=data[0];
                            if(chk.equals((nonce-1)+"")){
                                nonce=Integer.parseInt(data[1]);
                                ServerNonce=random.nextInt(RandomRange)+"";
                                file.send(kAB.encrypt((nonce-1)+" "+ServerNonce));//5

                                get=file.receive();
                                data=kAB.decrypt(get).split(" ");
                                if(data[0].equals(ServerNonce)){
                                    ret.append(use + "/");
                                    ServerNonce=data[1];
                                }else {
                                    file.close();
                                }
                            }else{
                                file.close();
                                //also handle for the server
                            }
                        }
                    }
                } else {
                    cdret = "~ No such directory in the current directory. Try the command ls.\n";
                }
            } else {
                //call the server bound with the current socket
                //and check if the server has the requested directory
                StringBuilder send=new StringBuilder(ServerNonce);
                send.append(" ");
                ServerNonce=random.nextInt(RandomRange)+"";
                send.append("cd ");
                send.append(use);
                send.append(" ");
                send.append(ServerNonce);
                file.send(kAB.encrypt(send.toString()));
                String get=kAB.decrypt(file.receive());
                String[] data=get.split(" ");
                if(data[0].equals(ServerNonce)){
                    ServerNonce=data[2];
                    if(data[1].equals("true")){
                        ret.append(use);
                        ret.append("/");
                    }else{
                        cdret = "~ No such directory in the current directory. Try the command ls.\n";
                    }
                }
            }
        }
        try {
            if (flag) {
                //free server
                file.send(kAB.encrypt(ServerNonce+" "+"close"));
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pwd = ret.toString();
        return cdret;
    }

    //close the connection
    void exit(){
        file.send(kAB.encrypt(ServerNonce+" "+"close"));
        file.close();
    }

    private void send(String req){
        String send = ServerNonce + " " + req + " ";
        ServerNonce = random.nextInt(RandomRange) + "";
        send = send + ServerNonce;
        file.send(kAB.encrypt(send));
    }

    private String check(String get) {
        String ret="";
        int fi = get.indexOf(" ");
        int la = get.lastIndexOf(" ");
        if (get.substring(0, fi).equals(ServerNonce)) {
            ServerNonce = get.substring(la + 1);
            if (fi + 1 < la) {
                ret = get.substring(fi + 1, la);
            }
        }
        return ret;
    }
}
