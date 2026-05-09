package br.com.sd.middleware;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proxy do lado do CLIENTE que implementa o protocolo requisição-resposta.
 *
 * Implementa o método doOperation():
 *   public byte[] doOperation(RemoteObjectRef o, int methodId, byte[] arguments)
 *
 * Encapsula a comunicação com o servidor: empacota a RequestMessage,
 * envia via RMI (TCP), e aguarda a ReplyMessage.
 *
 * EXTENSÃO ("é-um"): este proxy É UM componente de middleware do cliente.
 */
public class RMIClient {

    private static final AtomicInteger requestCounter = new AtomicInteger(0);

    private final String serverHost;
    private final int serverPort;

    public RMIClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    /**
     * Envia uma mensagem de requisição para o objeto remoto e retorna a resposta.
     * Corresponde ao método doOperation().
     *
     * @param remoteRef  referência para o objeto remoto
     * @param methodId   nome do método a ser invocado
     * @param arguments  argumentos serializados em JSON (byte[])
     * @return resultado serializado em JSON (byte[])
     */
    public byte[] doOperation(RemoteObjectRef remoteRef, String methodId, byte[] arguments)
            throws IOException {

        int requestId = requestCounter.incrementAndGet();

        // Empacota a RequestMessage.
        RequestMessage request = new RequestMessage(
                requestId,
                remoteRef.getObjectName(),
                methodId,
                arguments
        );

        System.out.println("[RMIClient] " + request + " → " + remoteRef);

        // Envia ao servidor e aguarda resposta (via TCP, gerenciado pelo RMI runtime)
        try (Socket socket = new Socket(remoteRef.getHost(), remoteRef.getPort())) {
            // Envia requisição
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(request);
            oos.flush();

            // Recebe resposta
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ReplyMessage reply = (ReplyMessage) ois.readObject();

            System.out.println("[RMIClient] " + reply);

            if (!reply.isSuccess()) {
                throw new IOException("Erro remoto: " + reply.getErrorMessage());
            }

            return reply.getResult();

        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao desserializar ReplyMessage: " + e.getMessage(), e);
        }
    }

    public String getServerHost() { 
        return serverHost; 
    }
    public int getServerPort() { 
        return serverPort;  
    }
}
