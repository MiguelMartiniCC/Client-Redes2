package so.ifsc;

import so.ifsc.Threads.Rx;
import so.ifsc.Threads.Tx;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
//    servidor e porta:
    public static final String HOST = "192.168.3.24";
    private static final int PORT = 5000;

    public static List<String> latestTopics = new ArrayList<>();
    public static volatile boolean topicsUpdated = false;

    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket(HOST, PORT);
            System.out.println("Conectado ao broker: " + HOST + ":" + PORT);

//            trhead para receber e enviar
            new Thread(new Rx(socket.getInputStream())).start();
            new Thread(new Tx(socket.getOutputStream())).start();
        }
        catch (IOException e) {
            System.out.println("Falha na conexão: " + e.getMessage());
        } finally {
            if (socket != null && socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Erro ao finalizar socket: " + e.getMessage());
                }
            }
        }

    }
}