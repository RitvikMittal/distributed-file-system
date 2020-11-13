public class Server2 {
    public static void main(String[] args) {
        Services s=new Services(2,"87654321");
        new ServerStub(s);
    }
}
