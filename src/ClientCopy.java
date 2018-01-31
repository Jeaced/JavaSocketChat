import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientCopy implements Runnable {
    private DataInputStream dataInputStream;
    private static boolean stop;

    private ClientCopy(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public static void main(String[] args) {
        try {
            stop = false;
            Socket socket = new Socket("127.0.0.1", 9099);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            Thread readingThread = new Thread(new ClientCopy(inputStream));
            readingThread.start();
            Scanner scanner = new Scanner(System.in);
            String message = "";
            while (!"/quit".equals(message) && !"/stop_chat".equals(message)) {
                message = scanner.nextLine();
                outputStream.writeUTF(message);
                outputStream.flush();
            }
            stop = true;
            System.exit(0);
            //inputStream.close();
            //outputStream.close();
            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        while (!stop) {
            try {
                message = dataInputStream.readUTF();
                if ("/quit".equals(message) || "/stop_chat".equals(message)) {
                    return;
                } else {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
