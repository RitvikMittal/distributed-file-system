import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.util.Base64;

//This class initialises a cipher given a string key and performs encryption/decryption
class CipherModule {
    private SecretKeyFactory kf;
    private SecretKey kA;
    private Cipher desCipher;

    CipherModule(SecretKeyFactory kf) {
        this.kf = kf;
        try {
            desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void initialize(String key) {
        try {
            kA = kf.generateSecret(new DESKeySpec(key.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String encrypt(String s){
        String ret="";
        try {
            desCipher.init(1,kA);
            byte[] ba=desCipher.doFinal(s.getBytes());
            ret=Base64.getEncoder().encodeToString(ba);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
    String decrypt(String s){
        String ret="";
        try {
            desCipher.init(2,kA);
            byte[] ba=Base64.getDecoder().decode(s);
            ret=new String(desCipher.doFinal(ba));
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
}
