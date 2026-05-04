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
    private final BufferedReader leitor;

    public Rx(InputStream in) {
//        baseado em json, le linha a linha -> converte de bytes para txt
        this.leitor = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = leitor.readLine()) != null){
//                JSON -> Obj - Message
                Message msg = new Gson().fromJson(line, Message.class);
//                System.out.println("Tudo do msg: "+msg);

                switch (msg.type.toUpperCase()) {
                    case "MESSAGE" -> {
                        System.out.println("\n-------------------Mensagem Recebida!-------------------");
                        System.out.println("Topico:      "+msg.topic);
                        System.out.println("Data:       "+msg.date);
                        System.out.println("Horario:       "+msg.time);
                        System.out.println("Mensagem:    "+msg.payload);
                        System.out.println("-------------------------------------------------------");
                    }
                    case "TOPICS_LIST" -> {
                        List<String> topics = new Gson().fromJson(msg.payload, new TypeToken<List<String>>() {
                        }.getType());

                        Client.latestTopics = topics;
                        Client.topicsUpdated = true;

                        System.out.print("[Topicos disponiveis: " + topics.toString() + "]\n");
                        System.out.println("-1. 'Crie um novo tópico'");
                        for (int i = 0; i < topics.size(); i++) {
                            System.out.println(" "+i + ". " + topics.get(i));
                        }
                        System.out.print("\n>");
                    }
                    default -> System.out.println("Não reconhecido: " + msg.type);
                }
            }
        } catch (IOException e) {
            System.out.println("Desconectado do broker (Rx): " + e.getMessage());
        }
    }
}
