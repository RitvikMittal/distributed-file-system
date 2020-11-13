import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.lang.*;
import java.io.*;
//****Use Integer Wrapper Class for Arrays.sort()****
//Abstraction over Client Stub for application programmer
public class FileSystemClient {
    private final ClientStub cs;
    String pwd;

    FileSystemClient(String key,String ID) {
        cs=new ClientStub(key,ID);
        pwd=cs.pwd();
    }

    //To be done
    //Switch the incoming command from the user to the appropriate stub call
    String handleCommands(String command){
        String ret="";
        switch(command.split(" ")[0]){
            case "pwd":
                ret=this.pwd+"\n";
                break;
            case "cd":
                ret=cs.cd(command);
                pwd=cs.pwd();
                break;
            case "ls":
                ret=cs.ls();
                break;
            case "cat":
                ret=cs.cat(command);
                break;
            case "cp":
                ret=cs.cp(command);
                break;
            case "mv":
                ret=cs.mv(command);
                break;
            case "rm":
                ret=cs.rm(command);
                break;
            case "exit":
                cs.exit();
                break;
            default:
                ret="No such command. Try again\n";
        }
        return ret;
    }

    //To be done
    //Close all the sockets and any other resources
    void close(){

    }
}
