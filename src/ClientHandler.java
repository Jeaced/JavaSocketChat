import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler extends Thread {

    private Socket client;
    private ClientHandler[] clients;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private int roomNumber;
    private int NUMBER_OF_ROOMS = 3;
    private int clientID;
    private AtomicBoolean stopCondition;

    public ClientHandler(Socket client, ClientHandler[] clients, int clientID, AtomicBoolean stopCondition) {
        this.client = client;
        this.clients = clients;
        this.clientID = clientID;
        this.stopCondition = stopCondition;
    }

    @Override
    public void run() {
        try {
            outputStream = new DataOutputStream(client.getOutputStream());
            inputStream = new DataInputStream(client.getInputStream());
            outputStream.writeUTF(String.format("There are %d rooms. Choose your room " +
                                                "and type its number", NUMBER_OF_ROOMS));
            outputStream.flush();
            setRoom(inputStream.readInt());
            String message;
            while (!stopCondition.get()) {
                message = inputStream.readUTF();
                if ("/quit".equals(message)) {
                    outputStream.close();
                    inputStream.close();
                    client.close();
                    Server.removeClient(clientID);
                    return;
                } else if ("/stop_chat".equals(message)) {
                    outputStream.close();
                    inputStream.close();
                    client.close();
                    synchronized (clients) {
                        stopCondition.set(true);
                    }
                    return;
                } else if ("/change_room".equals(message)) {
                    outputStream.writeUTF(String.format("There are %d rooms. Choose your room " +
                            "and type its number", NUMBER_OF_ROOMS));
                    outputStream.flush();
                    setRoom(inputStream.readInt());
                } else {
                    synchronized (clients) {
                        for (int i = 0; i < clients.length; i++) {
                            if (clients[i] != null && clients[i].getRoomNumber() == this.getRoomNumber()) {
                                DataOutputStream otherOutputStream = clients[i].getOutputStream();
                                otherOutputStream.writeUTF(message);
                                otherOutputStream.flush();
                            }
                        }
                    }
                }
            }
            outputStream.close();
            inputStream.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void setRoom(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }
}
