package br.com.sd.middleware;

import br.com.sd.remote.BoardService;
import br.com.sd.serialization.JsonSerializer;

import java.io.*;
import java.net.InetAddress;

/**
 * Despachante de requisições no lado do SERVIDOR.
 *
 * Implementa o protocolo requisição-resposta:
 * - getRequest(): lê a RequestMessage chegando
 * - dispatch():   roteia para o método correto do objeto remoto
 * - sendReply():  envia a ReplyMessage de volta
 *
 * EXTENSÃO ("é-um"): esta classe É UM componente de middleware do servidor.
 */
public class RequestDispatcher {

    private final BoardService service;

    public RequestDispatcher(BoardService service) {
        this.service = service;
    }

    /**
     * Obtém uma requisição de um cliente a partir de um InputStream.
     * Corresponde ao método getRequest().
     */
    public RequestMessage getRequest(InputStream in) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(in);
        try {
            return (RequestMessage) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao desserializar RequestMessage: " + e.getMessage(), e);
        }
    }

    /**
     * Envia a mensagem de resposta para o cliente.
     * Corresponde ao método sendReply().
     *
     * @param reply      a ReplyMessage a ser enviada
     * @param clientHost endereço IP do cliente
     * @param clientPort porta do cliente
     */
    public void sendReply(ReplyMessage reply, InetAddress clientHost, int clientPort,
                          OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(reply);
        oos.flush();
    }

    /**
     * Despacha a requisição para o método correto do serviço remoto.
     * Retorna a ReplyMessage com o resultado serializado.
     */
    public ReplyMessage dispatch(RequestMessage request) {
        String methodId = request.getMethodId();
        byte[] args     = request.getArguments();
        int    reqId    = request.getRequestId();

        System.out.println("[Dispatcher] Despachando: " + request);

        try {
            byte[] result;

            switch (methodId) {
                case "getBoard":
                    result = service.getBoard();
                    break;

                case "drawPixels":
                    result = service.drawPixels(args);
                    break;

                case "clearBoard":
                    result = service.clearBoard();
                    break;

                case "getBoardSize":
                    result = service.getBoardSize();
                    break;

                case "registerClient":
                    result = service.registerClient(args);
                    break;

                case "unregisterClient":
                    result = service.unregisterClient(args);
                    break;

                default:
                    return new ReplyMessage(reqId, "Método desconhecido: " + methodId);
            }

            return new ReplyMessage(reqId, result);

        } catch (Exception e) {
            System.err.println("[Dispatcher] Erro ao executar " + methodId + ": " + e.getMessage());
            return new ReplyMessage(reqId, "Erro interno: " + e.getMessage());
        }
    }
}
