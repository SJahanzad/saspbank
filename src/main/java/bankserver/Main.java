package bankserver;

public class Main {
    public static void main(String[] args) {
        String port, debug;
        port = "2222";
        debug = "0";
        try {
            Server server = new Server(Integer.parseInt(port), debug.equals("1"));
            server.start();
            System.out.println("Server started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
