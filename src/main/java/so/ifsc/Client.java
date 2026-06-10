package so.ifsc;

import com.google.gson.Gson;
import so.ifsc.Threads.Rx;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import so.ifsc.Model.Message;
import so.ifsc.View.Publish;
import so.ifsc.View.Subscribe;
import so.ifsc.View.Unsubscribe;

public class Client {
    public final String host;
    private final int port;
    public String clientId;
    private String certPath; // Passa a guardar o caminho para o arquivo .crt do cliente
    private PrintWriter writer;

    private Socket socket;

    private Publish publishView;
    private Subscribe subscribeView;
    private Unsubscribe unsubscribeView;

    public static List<String> latestTopics = new ArrayList<>();
    public static volatile boolean topicsUpdated = false;

    public Client(String host, int port, String clientId, String certPath) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.certPath = certPath;
    }

    public void connect() throws Exception {
        // 1. CONEXÃO DIRETA: Conecta de forma limpa usando Sockets padrão de Redes
        this.socket = new Socket(host, port);

        // 2. Cria o canal de escrita para mandar o JSON textualmente
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);

        // 3. Envia o comando inicial de conexão informando apenas o seu ID
        Message connect = new Message();
        connect.type = "CONNECT";
        connect.clientId = clientId;

        send(connect);

        // 4. Inicializa a thread de recepção (Rx) para ouvir a resposta ou o DESAFIO do Broker
        new Thread(new Rx(this.socket.getInputStream(), this)).start();
    }
    public String carregarCertificadoParaEnvio() throws Exception {
        // Lê os bytes puros do arquivo .crt do cliente (que foi assinado offline pelo servidor)
        byte[] certBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(this.certPath));

        // Converte para String Hexadecimal para trafegar com segurança dentro do campo 'payload' do JSON
        StringBuilder hexString = new StringBuilder();
        for (byte b : certBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    // Método que calcula localmente SHA256(Nonce + Certificado)
    public String calcularHashDesafio(String nonce, String caminhoCertificado) throws Exception {
        // Lê o conteúdo binário do arquivo .crt do usuário selecionado na interface
        String conteudoCertificado = new String(Files.readAllBytes(Paths.get(caminhoCertificado)), StandardCharsets.UTF_8);

        // Concatena o Nonce dinâmico descartável gerado pelo servidor com o segredo
        String combinacao = nonce + conteudoCertificado.trim();

        // Gera a assinatura SHA-256 no braço
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(combinacao.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void setPublishView(Publish publishView) { this.publishView = publishView; }
    public Publish getPublishView() { return this.publishView; }
    public Socket getSocket() { return socket; }

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

    public String getClientId() { return clientId; }
    public void setSubscribeView(Subscribe subscribeView) { this.subscribeView = subscribeView; }
    public Subscribe getSubscribeView() { return this.subscribeView; }
    public String getCertPath() { return certPath; }
    public Unsubscribe getUnsubscribeView() { return unsubscribeView; }
    public void setUnsubscribeView(Unsubscribe unsubscribeView) { this.unsubscribeView = unsubscribeView; }
}