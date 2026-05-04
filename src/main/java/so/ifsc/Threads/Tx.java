package so.ifsc.Threads;

import com.google.gson.Gson;
import so.ifsc.Client;
import so.ifsc.Model.Message;


import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tx implements Runnable{
//    envia p/ socket
    private final PrintWriter writer;
    private final Scanner scanner;

    public Tx(OutputStream out) {
        this.writer = new PrintWriter(out, true);
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            showMenu();
            String type = scanner.nextLine().trim();

            switch (type) {
                case "1" -> handleSubscribe();
                case "2" -> handlePublish();
                case "3" -> {
                    System.out.println("Saindo...!");
                    writer.close();
                    return;
                }
                default -> System.out.println("Comando invalido. Tente novamente.");
            }
        }
    }
    private void showMenu() {
        System.out.println("\n=======================================================");
        System.out.println("1. SUBSCRIBE - Inscreva-se em um topico");
        System.out.println("2. PUBLISH   - Envie uma mensagem à um topico");
        System.out.println("3. EXIT      - Desconectar");
        System.out.println("=======================================================");
        System.out.print("> ");
    }

    private void handleSubscribe() {
        List<String> topics = requestAndSelectTopic("LIST_ALL_TOPICS", "SUBSCRIBE");
        if (topics == null) return;

        String topic = chooseTopic("LIST_ALL_TOPICS", topics, "SUBSCRIBE");
        if (topic == null) return;

        send(createMessage("SUBSCRIBE", topic, null));
    }

    private void handlePublish() {
        List<String> topics = requestAndSelectTopic("LIST_MY_TOPICS", "PUBLISH");
        if (topics == null) return;

        String topic = chooseTopic("LIST_MY_TOPICS", topics, "PUBLISH");
        if (topic == null) return;

        System.out.println("[Topico]: " + topic);
        System.out.print("Digite uma mensagem: ");
        String payload = scanner.nextLine().trim();

        send(createMessage("PUBLISH", topic, payload));
    }

    private List<String> requestAndSelectTopic(String type, String action){
        requestTopics(type);
        pause();

        List<String> topics = new ArrayList<>(Client.latestTopics);
        String topic = "";

        if (topics.isEmpty()) {
            System.out.print("Nenhum topico disponivel. Crie um novo "+action+": ");
            topic = scanner.nextLine().trim();

            if (action.equalsIgnoreCase("PUBLISH")) {
                send(createMessage("SUBSCRIBE", topic, null));
            }

            if (topic.isEmpty()) {
                System.out.println("Topico nao pode ser vazio");
                return null;
            }
            Client.latestTopics.add(topic);
        }
        return topics;
    }

    private String chooseTopic(String type, List<String> topics, String action) {
//        requestTopics(type);
        System.out.print("Selecione um topico para "+action+" ou digite -1 para criar um novo: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index == -1) {
            System.out.print("Crie um novo topico: ");
            String topic = scanner.nextLine().trim();
            if (topic.isEmpty()) {
                System.out.println("Topico nao pode ser vazio.");
                return null;
            }

            if (action.equalsIgnoreCase("PUBLISH")) {
                send(createMessage("SUBSCRIBE", topic, null));
            }

            return topic;
        }

        if (index >= 0 && index < topics.size()) {
            return topics.get(index);
        }

        System.out.println("Numero invaido");
        return null;
    }

    private Message createMessage(String type, String topic, String payload) {
        Message msg = new Message();
        msg.type = type;
        msg.topic = topic;
        msg.payload = payload;
        msg.date = LocalDate.now().toString();
        msg.time = LocalTime.now().toString();
        return msg;
    }

    private void send(Message msg) {
        String json = new Gson().toJson(msg);
        writer.println(json);
//        System.out.println("Message sent: " + json);
    }

    private void requestTopics(String type) {
        send(createMessage(type, null, null));
        Client.topicsUpdated = false;
    }

    private void pause() {
        int attempts = 0;

        while (!Client.topicsUpdated && attempts < 20) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            attempts++;
        }
    }
}
