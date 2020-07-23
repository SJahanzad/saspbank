package bankserver;

import com.google.gson.Gson;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class FileManager {
    private String directory;

    public FileManager(String directory) {
        if (directory.charAt(directory.length() - 1) != '/')
            directory += '/';
        this.directory = directory;
    }

    public static void updateAccount(Account account) throws DataBaseException {
        writeObjectToJsonFileWithName(account, account.getUsername());
        writeObjectToJsonFileWithName(Account.getAccountRecord(), ".records");
    }

    public static boolean fileExists(String username) throws DataBaseException {
        String address = getJsonFileAddress(username);
        try {
            File file = new File(address);
            return file.exists();
        } catch (Exception e) {
            throw new DataBaseException();
        }
    }

    public static Account getAccount(String username) throws DataBaseException {
        try {
            File file = new File(getJsonFileAddress(username));
            Gson gson = new Gson();
            return gson.fromJson(readWholeFile(file), Account.class);
        } catch (Exception e) {
            throw new DataBaseException();
        }
    }

    public static String readWholeFile(File file) throws DataBaseException {
        StringBuilder result = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                result.append(scanner.nextLine());
            }
            scanner.close();
            return result.toString();
        } catch (Exception e) {
            throw new DataBaseException();
        }
    }

    public static<T> void writeObjectToJsonFileWithName(T object, String fileName) throws DataBaseException {
        try (PrintWriter writer = new PrintWriter(getJsonFileAddress(fileName))) {
            Gson gson = new Gson();
            writer.println(gson.toJson(object));
        } catch (Exception e) {
            throw new DataBaseException();
        }
    }

    public static String getJsonFileAddress(String fileName) {
        return "data/" + fileName + ".json";
    }
}
