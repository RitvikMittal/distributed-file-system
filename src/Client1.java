import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.lang.*;
import java.io.*;
//****Use Integer Wrapper Class for Arrays.sort()****
public class Client1 {
    private static BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args)throws IOException,NoSuchAlgorithmException {
        // Take password from client. This serves as the string for DESKeySpec
        System.out.print("Enter your username : ");
        String u=br.readLine().trim();
        System.out.print("Enter your password : ");
        String s=br.readLine().trim();
        //password assumed to be correct
        FileSystemClient fsc=new FileSystemClient(s,u);
        System.out.print(fsc.pwd+" >> " );
        String command;
        while((command=br.readLine())!=null){
            String output=fsc.handleCommands(command);
            if(command.equals("exit")){
                break;
            }
            System.out.print(output);
            System.out.print(fsc.pwd+" >> ");
        }
        fsc.close();
    }
}
