import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    public static void main(String[] args) {
        int MAX_CLIENTS = 100;
        int curret_clients = 0;
        ServerSocket serverSocket = null;
        Socket newClient;
        Thread[] clients = new Thread[MAX_CLIENTS];
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
                    outputStream.close();
                    newClient.close();
                } else {
                    for (int i = 0; i < clients.length; i++) {
                        synchronized (clients) {
                            if (clients[i] == null) {
                                clients[i] = new Thread(new ClientHandler(newClient, clients, i, stopCondition));
                                clients[i].start();
                                curret_clients++;
                                newClient.close();
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
