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
import so.ifsc.View.Publish;

public class Rx implements Runnable{
    private final BufferedReader leitor;
    private final Client client;

    public Rx(InputStream in, Client client) {
//        baseado em json, le linha a linha -> converte de bytes para txt
        this.leitor = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.client = client;
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
                        if (client.getPublishView() != null) {
                            client.getPublishView().addMessage(msg);
                        }
                    }
                    case "TOPICS_LIST" -> {
                        List<String> topics = new Gson().fromJson(msg.payload, new TypeToken<List<String>>() {}.getType());

                        Client.latestTopics = topics;
                        Client.topicsUpdated = true;
                        
                        if (client.getPublishView() != null) {
                            client.getPublishView().updateTopics(topics);
                        }

                        // NOVO: Atualiza a tela de Subscribe se estiver aberta
                        if (client.getSubscribeView() != null) {
                            client.getSubscribeView().updateTopics(topics);
                        }
                        
                        if (client.getUnsubscribeView() != null) {
                            client.getUnsubscribeView().updateTopics(topics);
                        }
                    }
                    case "MY_TOPICS_LIST" -> { // Mude para o nome exato do tipo que seu servidor retorna para "LIST_MY_TOPICS"
                        List<String> myTopics = new Gson().fromJson(msg.payload, new TypeToken<List<String>>() {}.getType());

                        // Se a tela de Unsubscribe estiver aberta, atualiza ela dinamicamente!
                        if (client.getUnsubscribeView() != null) {
                            client.getUnsubscribeView().updateTopics(myTopics);
                        }
                    }
                    case "CHALLENGE" -> {
                        try {
                            // Carrega o certificado assinado e transforma em Hex
                            String certificadoHex = client.carregarCertificadoParaEnvio();

                            Message authResp = new Message();
                            authResp.type = "AUTH_RESPONSE";
                            authResp.clientId = client.getClientId();
                            authResp.payload = certificadoHex; // Transmite o certificado assinado via JSON

                            client.send(authResp);
                        } catch (Exception e) {
                            System.out.println("Erro ao ler credenciais assinadas: " + e.getMessage());
                        }
                    }
                    case "AUTH_SUCCESS" -> {
                        // Abre a tela de Menu e fecha a de Init (Login efetuado com sucesso!)
                        System.out.println("Autenticado com sucesso pelo Broker!");
                    }
                    default -> System.out.println("Não reconhecido: " + msg.type);
                }
            }
        } catch (IOException e) {
            System.out.println("Desconectado do broker (Rx): " + e.getMessage());
        }
    }
}
