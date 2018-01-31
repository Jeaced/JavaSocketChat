import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static int curret_clients = 0;
    private static ClientHandler[] clients;

    public static void main(String[] args) {
        int MAX_CLIENTS = 100;
        ServerSocket serverSocket = null;
        Socket newClient = null;
        clients = new ClientHandler[MAX_CLIENTS];
        try {
            serverSocket = new ServerSocket(9099);
        } catch (IOException e) {
            System.out.println("Could not make a ServerSocket. Stopping the program.");
            System.exit(1);
        }
        AtomicBoolean stopCondition = new AtomicBoolean();
        stopCondition.set(false);
        while (!stopCondition.get()) {
            try {
                newClient = serverSocket.accept();
                if (curret_clients == MAX_CLIENTS) {
                    DataOutputStream outputStream = new DataOutputStream(newClient.getOutputStream());
                    outputStream.writeUTF("Max amount of clients are connected to the server at" +
                                            "the moment. Retry later.");
                    outputStream.flush();
                    //outputStream.close();
                    //newClient.close();
                } else {
                    for (int i = 0; i < clients.length; i++) {
                        synchronized (clients) {
                            if (clients[i] == null) {
                                clients[i] = new ClientHandler(newClient, clients, i, stopCondition);
                                clients[i].start();
                                curret_clients++;
                                //newClient.close();
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
        /*
        try {
            newClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    public static void removeClient(int clientID) {
        if (clients != null) {
            synchronized (clients) {
                if (clients[clientID] != null) {
                    clients[clientID] = null;
                    curret_clients--;
                }
            }
        }
    }
}
