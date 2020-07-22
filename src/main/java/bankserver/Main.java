package bankserver;

public class Main {
    public static void main(String[] args) {
//        String port, debug;
//        port = "1234";
//        debug = "1";
//        try {
//            Server server = new Server(Integer.parseInt(port), debug.equals("1"));
//            server.start();
//            System.out.println("Server started");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Account account = new Account("hi", "hi", "hi", "hi");
        account = new Account("hey", "hi", "hi", "hi");
    }
}
