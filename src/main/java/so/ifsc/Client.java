package so.ifsc;

import com.google.gson.Gson;
import so.ifsc.Threads.Rx;
import so.ifsc.Threads.Tx;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import so.ifsc.Model.Message;
import so.ifsc.View.Publish;
import so.ifsc.View.Subscribe;
import so.ifsc.View.Unsubscribe;

public class Client {
//    servidor e porta:
    public final String host;
    private final int port;
    public String clientId;
    private PrintWriter writer;
    
    private Socket socket;
    private Rx rx;
    
    private Publish publishView;
    private Subscribe subscribeView;
    private Unsubscribe unsubscribeView;

    public static List<String> latestTopics = new ArrayList<>();
    public static volatile boolean topicsUpdated = false;

        public Client(String host, int port, String clientId) {
            this.host = host;
            this.port = port;
            this.clientId = clientId;
        }

        public void connect() throws IOException {

            socket = new Socket(host, port);
            
            writer = new PrintWriter(
                socket.getOutputStream(),
                true
            );
            
            Message connect = new Message();
            connect.type = "CONNECT";
            connect.clientId = clientId;

            send(connect);

            new Thread(new Rx(socket.getInputStream(), this)).start();
//            new Thread(new Tx(socket.getOutputStream(), clientId)).start();
        }
        
        public void setPublishView(Publish publishView) {
            this.publishView = publishView;
        }

        public Publish getPublishView() {
            return this.publishView;
        }
        public Socket getSocket() {
            return socket;
        }
        public void send(Message msg) {

            String json = new Gson().toJson(msg);

            writer.println(json);
        }
        public void disconnect() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }

        public String getClientId() {
            return clientId;
        }
        
        public void setSubscribeView(Subscribe subscribeView) {
            this.subscribeView = subscribeView;
        }

        public Subscribe getSubscribeView() {
            return this.subscribeView;
        }

    public Unsubscribe getUnsubscribeView() {
        return unsubscribeView;
    }

    public void setUnsubscribeView(Unsubscribe unsubscribeView) {
        this.unsubscribeView = unsubscribeView;
    }

   
        
    }