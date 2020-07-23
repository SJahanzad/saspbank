package bankserver;

public class Main {
    public static void main(String[] args) {
        String port, debug;
//        port = args[0];
//        debug = args[1];
        port = "1234";
        debug = "1";
        try {
            Server server = new Server(Integer.parseInt(port), debug.equals("1"));
            server.start();
            System.out.println("Server started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
