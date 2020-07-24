package bankserver;

import java.util.UUID;

public class DataManager {
    public static String getNewId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
