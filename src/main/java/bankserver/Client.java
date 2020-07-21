package bankserver;

import java.net.Socket;
import java.util.UUID;

public class Client {
    private UUID id;
    private Socket socket;

    public Client(Socket socket) {
        id = UUID.randomUUID();
        this.socket = socket;
    }

    public String getId() {
        return id.toString();
    }
}
