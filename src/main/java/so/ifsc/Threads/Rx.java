package so.ifsc.Threads;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import so.ifsc.Client;
import so.ifsc.Model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Rx implements Runnable{
    private final BufferedReader reader;

    public Rx(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null){
                Message msg = new Gson().fromJson(line, Message.class);

                switch (msg.type.toUpperCase()) {
                    case "MESSAGE" -> {
                        System.out.println("\n-------------------Message Received!-------------------");
                        System.out.println("Topic:      "+msg.topic);
                        System.out.println("Date:       "+msg.date);
                        System.out.println("Time:       "+msg.time);
                        System.out.println("Message:    "+msg.payload);
                        System.out.println("-------------------------------------------------------");
                    }
                    case "TOPICS_LIST" -> {
                        List<String> topics = new Gson().fromJson(msg.payload, new TypeToken<List<String>>() {
                        }.getType());

                        Client.latestTopics = topics;
                        Client.topicsUpdated = true;

                        System.out.print("[Topics available: " + topics.toString() + "]\n");
                        System.out.println("-1. 'Create a new topic'");
                        for (int i = 0; i < topics.size(); i++) {
                            System.out.println(" "+i + ". " + topics.get(i));
                        }
                        System.out.print("\n>");
                    }
                    default -> System.out.println("Unknown type: " + msg.type);
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from the broker (Rx): " + e.getMessage());
        }
    }
}
