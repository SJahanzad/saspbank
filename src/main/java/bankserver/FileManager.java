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

    public static void updateAccount(Account account) {
        String address = "data/" + account.getUsername();
        writeObjectToFileInAddress(account, address);
    }

    public static boolean fileExists(String username) {
        String address = "data/" + username;
        try {
            File file = new File(address);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Account getAccount(String username) {
        try {
            File file = new File("data/" + username);
            Gson gson = new Gson();
            return gson.fromJson(readWholeFile(file), Account.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readWholeFile(File file) {
        StringBuilder result = new StringBuilder();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                result.append(scanner.nextLine());
            }
            scanner.close();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static<T> void writeObjectToFileInAddress(T object, String address) {
        try (PrintWriter writer = new PrintWriter(address)) {
            Gson gson = new Gson();
            writer.println(gson.toJson(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
