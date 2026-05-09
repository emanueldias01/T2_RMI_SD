package br.com.sd.server;

import br.com.sd.middleware.RemoteObjectRef;
import br.com.sd.middleware.ReplyMessage;
import br.com.sd.middleware.RequestDispatcher;
import br.com.sd.middleware.RequestMessage;
import br.com.sd.services.BoardServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Servidor do Quadro Colaborativo.
 *
 * Duas camadas:
 *  1. RMI Registry: registra o BoardServiceImpl para que clientes RMI o localizem.
 *  2. ServerSocket: recebe RequestMessages, despacha via
 *     RequestDispatcher e envia ReplyMessages — implementando getRequest()/sendReply()
 *
 */
public class BoardServer {

    private static final int RMI_PORT      = 1099;
    private static final int PROTOCOL_PORT = 5001;
    private static final int BOARD_HEIGHT  = 50;
    private static final int BOARD_WIDTH   = 50;

    public static void main(String[] args) throws Exception {
        BoardServiceImpl boardService = new BoardServiceImpl(BOARD_HEIGHT, BOARD_WIDTH);

        try {
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("[Server] RMI Registry criado na porta " + RMI_PORT);
        } catch (Exception e) {
            System.out.println("[Server] RMI Registry já em execução.");
        }

        Naming.rebind("rmi://localhost:" + RMI_PORT + "/BoardService", boardService);
        System.out.println("[Server] BoardService registrado no RMI Registry.");
        System.out.println("[Server] RemoteObjectRef: " +
                new RemoteObjectRef("localhost", RMI_PORT, "BoardService"));

        RequestDispatcher dispatcher = new RequestDispatcher(boardService);

        ServerSocket serverSocket = new ServerSocket(PROTOCOL_PORT);
        System.out.println("[Server] Servidor de protocolo escutando na porta " + PROTOCOL_PORT);
        System.out.println("[Server] Aguardando clientes...\n");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            InetAddress clientHost = clientSocket.getInetAddress();
            int clientPort = clientSocket.getPort();
            System.out.println("[Server] Nova conexão: " + clientHost + ":" + clientPort);

            Thread.ofVirtual().start(() ->
                    handleClient(clientSocket, clientHost, clientPort, dispatcher));
        }
    }

    /**
     * Trata uma conexão de cliente executando o protocolo req-resposta:
     *  1. getRequest()  — lê a RequestMessage
     *  2. dispatch()    — invoca o método correto no objeto remoto
     *  3. sendReply()   — envia a ReplyMessage de volta
     */
    private static void handleClient(Socket socket, InetAddress clientHost,
                                     int clientPort, RequestDispatcher dispatcher) {
        try {
            // getRequest(): obtém requisição do cliente
            RequestMessage request = getRequest(socket.getInputStream());
            System.out.println("[Server] Recebido: " + request +
                               " de " + clientHost + ":" + clientPort);

            ReplyMessage reply = dispatcher.dispatch(request);

            sendReply(reply, clientHost, clientPort, socket.getOutputStream());
            System.out.println("[Server] Resposta enviada: " + reply);

        } catch (IOException e) {
            System.err.println("[Server] Erro com cliente " + clientHost + ":" + clientPort
                               + " — " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Obtém uma requisição de um cliente através de da porta do servidor.
     */
    private static RequestMessage getRequest(InputStream in) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(in);
        try {
            return (RequestMessage) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao desserializar RequestMessage", e);
        }
    }

    /**
     * Envia a mensagem de resposta para o cliente, endereçando-a a seu IP e porta.
     */
    private static void sendReply(ReplyMessage reply, InetAddress clientHost,
                                  int clientPort, OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(reply);
        oos.flush();
    }
}
