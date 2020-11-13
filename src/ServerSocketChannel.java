import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ServerSocketChannel {
    private Socket s;
    private BufferedReader br;
    private PrintWriter pw;

    void initialize(Socket s) {
        this.s = s;
        try {
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(s.getOutputStream(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }void initialize(Socket s,BufferedReader br) {
        this.s = s;
        this.br=br;
        try {
            pw = new PrintWriter(s.getOutputStream(),true);
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
        try{
            br.close();
            pw.close();
            s.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
