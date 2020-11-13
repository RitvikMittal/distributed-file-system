import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//This class takes a socket and performs read/write on that socket from the Client's POV.
class ClientSocketChannel {
    private Socket s;
    private PrintWriter pw;
    private BufferedReader br;

    void initialize(String IP,int port){
        try {
            s = new Socket(IP, port);
            pw = new PrintWriter(s.getOutputStream(),true);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void send(String data){
        pw.println(data);
    }
    String receive(){
        String data="";
        try {
            data= br.readLine();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }
    void close(){
        try {
            pw.close();
            br.close();
            s.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

