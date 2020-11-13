import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class Services {
    //helps in getting the initial folder
    final int id;
    final String password;
    private final String folder;

    Services(int id, String password) {
        this.id = id;
        this.password = password;
        this.folder = "FolderS" + id;
    }

    //returns whether the requested directory exists in the current directory or not
    boolean cd(String path) {
        File f = new File(folder + path);
        return f.exists() && f.isDirectory();
    }

    //returns a list of the items present in the current directory
    String[] ls(String path) {
        return new File(folder + path).list();
    }

    //returns the content of the file in a string
    String cat(String file) {
        File f = new File(folder + file);
        StringBuilder s = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String read = "";
            while ((read = br.readLine()) != null) {
                s.append(read);
                s.append("\n");
            }
            return s.toString();
        } catch (Exception e) {
            return null;
        }
    }

    //returns whether it is possible to copy src file into dst file
    boolean cp(String src,String dst){
        File f1=new File(folder+src);
        File f2=new File(folder+dst);
        if(f1.exists()&&f1.isFile()){
            if(f2.exists()){
                if(!f2.isFile()){
                    return false;
                }
            }else{
                try {
                    f2.createNewFile();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Path from=f1.toPath();
            Path to=f2.toPath();
            try {
                Files.copy(from,to,StandardCopyOption.REPLACE_EXISTING);
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    //returns the result of mv command
    String mv(String src,String dst){
        File f1=new File(folder+src);
        File f2=new File(folder+dst);
        int len=dst.length();
        boolean mode=true;
        if(len>4&&dst.substring(len-4).equals(".txt")){
            mode=false;
        }
        if(f1.exists()&&f1.isFile()){
            if(!f2.exists()){
                if(!mode){
                    f1.renameTo(f2);
                    return "";
                }else{
                    return "Destination Folder not found\n";
                }
            }else{
                if(!mode){
                    Path from=f1.toPath();
                    Path to=f2.toPath();
                    try {
                        Files.copy(from,to,StandardCopyOption.REPLACE_EXISTING);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    f1.delete();
                    return "";
                }else{
                    f1.renameTo(new File(folder+dst+"/"+src.substring(src.lastIndexOf('/')+1)));
                    f1.delete();
                    return "";
                }
            }
        }
        return "Source File not found\n";
    }

    //returns if possible to delete file
    boolean rm(String file){
        File f1=new File(folder+file);
        if(f1.exists()&&f1.isFile()){
            f1.delete();
            return true;
        }
        return false;
    }
}
