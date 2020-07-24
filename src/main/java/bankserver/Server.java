package bankserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private ArrayList<Client> clients;
    private ServerImpl server;

    public Server(int port, boolean debug) throws IOException {
        server = new ServerImpl(port, debug);
        clients = new ArrayList<>();
    }

    public void start() {
        server.start();
    }

    private void removeClient(Client client, boolean debug) {
        clients.remove(client);
        if (debug)
            writeToConsole(clients.size());
    }

    private <T> void writeToConsole(T message) {
        System.out.println(message);
    }

    private String createAccount(String query) {
        Matcher matcher = getMatcher("create_account (\\w+) (\\w+) (\\w+) (.+) (.+)", query);
        if (matcher.find()) {
            String firstName = matcher.group(1);
            String lastName = matcher.group(2);
            String username = matcher.group(3);
            String password = matcher.group(4);
            String repeatPassword = matcher.group(5);
            if (!password.equals(repeatPassword)) {
                return "passwords do not match";
            }
            try {
                if (Account.accountExists(username)) {
                    return "username is not available";
                }
                return String.valueOf(new Account(firstName, lastName, username, password).getId());
            } catch (DataBaseException e) {
                return e.getMessage();
            }
        } else {
            return "invalid input";
        }
    }

    private String getToken(String query) {
        Matcher matcher = getMatcher("get_token (\\w+) (.+)", query);
        if (matcher.find()) {
            String username = matcher.group(1);
            String password = matcher.group(2);
            try {
                if (!Account.accountExists(username)) return "invalid username or password";
                return Account.getAccount(username, password).getToken();
            } catch (DataBaseException e) {
                return e.getMessage();
            } catch (Exception e) {
                return "invalid username or password";
            }
        } else {
            return "invalid input";
        }
    }

    private String createReceipt(String query) {
        Matcher matcher = getMatcher("create_receipt (\\w+) (\\w+) (\\w+) (-?\\w+) (-?\\w+) ?(.*)", query);
        if (matcher.find()) {
            String token = matcher.group(1);
            String receiptTypeString = matcher.group(2);
            String moneyString = matcher.group(3);
            String sourceId = matcher.group(4);
            String destId = matcher.group(5);
            String description = matcher.group(6);

            ReceiptType receiptType;
            long money;
            Account currentAccount, source, dest;
            try {
                receiptType = ReceiptType.getTypeFromString(receiptTypeString);
                money = Long.parseLong(moneyString);
                currentAccount = Token.getAccount(token);
                source = Account.getAccount(sourceId);
                dest = Account.getAccount(destId);
                if (source != null && dest != null && source.getUsername().equals(dest.getUsername()))
                    return "equal source and dest account";
                if (description.contains("*") || description.contains("{") || description.contains("}"))
                    return "your input contains invalid characters";
//                if (!description.matches("[\\w\\s\\-.?!]"))
//                    return "your input contains invalid characters";
                if (receiptType == ReceiptType.DEPOSIT) {
                    if (!sourceId.equals("-1"))
                        return "source account id is invalid";
                    if (destId.equals("-1"))
                        return "invalid account id";
                    if (dest == null)
                        return "dest account id is invalid";
                    if (!dest.getUsername().equals(currentAccount.getUsername()))
                        return "token is invalid";
                }
                if (receiptType == ReceiptType.WITHDRAW) {
                    if (!destId.equals("-1"))
                        return "dest account id is invalid";
                    if (sourceId.equals("-1"))
                        return "invalid account id";
                    if (source == null)
                        return "source account id is invalid";
                    if (!source.getUsername().equals(currentAccount.getUsername()))
                        return "token is invalid";
                }
                if (receiptType == ReceiptType.MOVE) {
                    if (destId.equals("-1") || sourceId.equals("-1"))
                        return "invalid account id";
                    if (source == null)
                        return "source account id is invalid";
                    if (dest == null)
                        return "dest account id is invalid";
                    if (!source.getUsername().equals(currentAccount.getUsername()))
                        return "token is invalid";
                }
                if (money <= 0) return "invalid money";
                Receipt receipt = new Receipt(receiptType, money, sourceId, destId, description);
                if (!sourceId.equals("-1")) Account.getAccount(sourceId).acceptReceipt(receipt);
                if (!destId.equals("-1")) Account.getAccount(destId).acceptReceipt(receipt);
                return String.valueOf(receipt.getId());
            } catch (NumberFormatException e) {
                return "invalid money";
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "invalid parameters passed";
        }
    }

    private String getTransactions(String query) {
        Matcher matcher = getMatcher("get_transactions (\\w+) (.+)", query);
        if (matcher.find()) {
            String token = matcher.group(1);
            String type = matcher.group(2);
            try {
                Account tempAccount = Token.getAccount(token);
                Account account = FileManager.getAccount(tempAccount.getUsername());
                return account.getTransactions(type);
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "invalid input";
        }
    }

    private String pay(String query) {
        Matcher matcher = getMatcher("pay (\\w+)", query);
        if (matcher.find()) {
            String receiptID = matcher.group(1);
            try {
                Receipt receipt = Objects.requireNonNull(Receipt.getReceipt(receiptID));
                if (receipt.isPaid())
                    return "receipt is paid before";
                receipt.execute();
                return "done successfully";
            } catch (NumberFormatException | NullPointerException e) {
                return "invalid receipt id";
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "invalid input";
        }
    }

    private String getBalance(String query) {
        Matcher matcher = getMatcher("get_balance (\\w+)", query);
        if (matcher.find()) {
            String token = matcher.group(1);
            try {
                Account tempAccount = Token.getAccount(token);
                Account account = FileManager.getAccount(tempAccount.getUsername());
                return String.valueOf(account.getBalance());
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "invalid input";
        }
    }

    private Matcher getMatcher(String regex, String query) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(query);
    }

    private class ServerImpl extends Thread {
        private ServerSocket serverSocket;
        private boolean debug;

        public ServerImpl(int port, boolean debug) throws IOException {
            this.serverSocket = new ServerSocket(port);
            this.debug = debug;
        }

        @Override
        public void run() {
            Socket clientSocket;
            Client client;
            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    client = new Client(clientSocket);
                    clients.add(client);
                    DataInputStream inputStream =
                            new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    DataOutputStream outputStream =
                            new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                    new ClientHandler(client, clientSocket, inputStream, outputStream, debug).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientHandler extends Thread {
        Client client;
        Socket clientSocket;
        DataInputStream inputStream;
        DataOutputStream outputStream;
        boolean debug;

        public ClientHandler(Client client, Socket clientSocket,
                             DataInputStream inputStream, DataOutputStream outputStream, boolean debug) {
            this.client = client;
            this.clientSocket = clientSocket;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.debug = debug;
        }

        private void handleClient() {
            if (debug)
                writeToOutputStream("hello " + client.getId());
            String query, result;
            boolean exit = false;
            while (!exit) {
                try {
                    result = "";
                    query = inputStream.readUTF();
                    if (debug)
                        writeToConsole(query);
                    if (query.equals("exit"))
                        exit = true;
                    else if (query.startsWith("create_account")) {
                        result = createAccount(query);
                    } else if (query.startsWith("get_token")) {
                        result = getToken(query);
                    } else if (query.startsWith("create_receipt")) {
                        result = createReceipt(query);
                    } else if (query.startsWith("get_transactions")) {
                        result = getTransactions(query);
                    } else if (query.startsWith("pay")) {
                        result = pay(query);
                    } else if (query.startsWith("get_balance")) {
                        result = getBalance(query);
                    } else {
                        writeToOutputStream("invalid input");
                    }
                    if (exit)
                        removeClient(client, debug);
                    writeToOutputStream(result);
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }

        private void writeToOutputStream(String message) {
            if (message == null) {
                System.out.println("Want to print null message!!");
                return;
            }
            try {
                outputStream.writeUTF(message);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            handleClient();
        }
    }
}
