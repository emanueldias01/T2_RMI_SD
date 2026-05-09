package br.com.sd.services;

import br.com.sd.entitys.Board;
import br.com.sd.entitys.DrawEvent;
import br.com.sd.entitys.Pixel;
import br.com.sd.remote.BoardService;
import br.com.sd.remote.NotificationService;
import br.com.sd.serialization.JsonSerializer;

import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação do serviço remoto de quadro colaborativo.
 *
 * EXTENSÃO ("é-um"): extends UnicastRemoteObject → é um objeto remoto RMI.
 * EXTENSÃO ("é-um"): implements BoardService → é um BoardService remoto.
 *
 * Mantém o estado do Board no servidor e notifica todos os clientes
 * registrados via callbacks (passagem por REFERÊNCIA de NotificationService).
 */
public class BoardServiceImpl extends UnicastRemoteObject implements BoardService {
    private static final long serialVersionUID = 1L;

    // Estado do quadro — mantido no servidor
    private final Board board;

    // Registro de clientes para broadcast — passagem por REFERÊNCIA (objetos remotos)
    private final Map<String, NotificationService> registeredClients = new ConcurrentHashMap<>();

    public BoardServiceImpl(int height, int width) throws RemoteException {
        super();
        this.board = new Board(height, width);
        System.out.println("[BoardService] Quadro criado: " + width + "x" + height);
    }

    // ───────────────────────────────────────────────
    // Método 1: getBoard
    // Retorna snapshot do Board por VALOR (serializado em JSON)
    // ───────────────────────────────────────────────
    @Override
    public byte[] getBoard() throws RemoteException {
        System.out.println("[BoardService] getBoard() chamado");
        synchronized (board) {
            return JsonSerializer.serializeBoard(board);
        }
    }

    // ───────────────────────────────────────────────
    // Método 2: drawPixels
    // Recebe DrawEvent por VALOR, aplica ao board, e notifica clientes por REFERÊNCIA
    // ───────────────────────────────────────────────
    @Override
    public byte[] drawPixels(byte[] drawEventJson) throws RemoteException {
        DrawEvent event = JsonSerializer.deserializeDrawEvent(drawEventJson);
        System.out.println("[BoardService] drawPixels() - " + event);

        synchronized (board) {
            for (Pixel p : event.getPixels()) {
                board.setPixel(p);
            }
        }

        // Broadcast para todos os clientes registrados (referência remota)
        broadcastDrawEvent(event);

        return JsonSerializer.serializeString("OK");
    }

    // ───────────────────────────────────────────────
    // Método 3: clearBoard
    // Limpa o quadro e notifica todos os clientes
    // ───────────────────────────────────────────────
    @Override
    public byte[] clearBoard() throws RemoteException {
        System.out.println("[BoardService] clearBoard() chamado");
        synchronized (board) {
            board.clear();
        }
        broadcastClear();
        return JsonSerializer.serializeString("OK");
    }

    // ───────────────────────────────────────────────
    // Método 4: getBoardSize
    // Retorna dimensões do quadro por VALOR (JSON)
    // ───────────────────────────────────────────────
    @Override
    public byte[] getBoardSize() throws RemoteException {
        System.out.println("[BoardService] getBoardSize() chamado");
        String json = String.format("{\"width\":%d,\"height\":%d}", board.getWidth(), board.getHeight());
        return json.getBytes(StandardCharsets.UTF_8);
    }

    // ───────────────────────────────────────────────
    // Método 5: registerClient
    // O clientRefJson contém o ID + endereço do cliente para callback
    // O servidor busca a referência remota do NotificationService no RMI registry
    // ───────────────────────────────────────────────
    @Override
    public byte[] registerClient(byte[] clientRefJson) throws RemoteException {
        String clientId = JsonSerializer.deserializeString(clientRefJson);
        System.out.println("[BoardService] registerClient() - clientId: " + clientId);

        // A referência remota (NotificationService) é buscada pelo servidor no RMI registry
        try {
            NotificationService notif = (NotificationService)
                java.rmi.Naming.lookup("rmi://localhost/" + clientId);
            registeredClients.put(clientId, notif);
            System.out.println("[BoardService] Cliente registrado: " + clientId +
                               " | Total: " + registeredClients.size());
        } catch (Exception e) {
            System.err.println("[BoardService] Falha ao localizar callback do cliente " + clientId + ": " + e.getMessage());
            return JsonSerializer.serializeString("ERROR: " + e.getMessage());
        }

        return JsonSerializer.serializeString("REGISTERED:" + clientId);
    }

    // ───────────────────────────────────────────────
    // Método 6: unregisterClient
    // Remove o cliente do mapa de notificações
    // ───────────────────────────────────────────────
    @Override
    public byte[] unregisterClient(byte[] clientIdJson) throws RemoteException {
        String clientId = JsonSerializer.deserializeString(clientIdJson);
        System.out.println("[BoardService] unregisterClient() - clientId: " + clientId);
        registeredClients.remove(clientId);
        System.out.println("[BoardService] Clientes registrados: " + registeredClients.size());
        return JsonSerializer.serializeString("UNREGISTERED:" + clientId);
    }

    // ───────────────────────────────────────────────
    // Broadcast privado: notifica clientes via REFERÊNCIA REMOTA
    // ───────────────────────────────────────────────
    private void broadcastDrawEvent(DrawEvent event) {
        byte[] eventJson = JsonSerializer.serializeDrawEvent(event);
        for (Map.Entry<String, NotificationService> entry : registeredClients.entrySet()) {
            try {
                // Invocação remota do callback no cliente (passagem por referência)
                entry.getValue().onDrawEvent(eventJson);
            } catch (RemoteException e) {
                System.err.println("[BoardService] Falha ao notificar cliente " +
                        entry.getKey() + ": " + e.getMessage());
                // Remove cliente inacessível
                registeredClients.remove(entry.getKey());
            }
        }
    }

    private void broadcastClear() {
        for (Map.Entry<String, NotificationService> entry : registeredClients.entrySet()) {
            try {
                entry.getValue().onBoardCleared();
            } catch (RemoteException e) {
                System.err.println("[BoardService] Falha ao notificar limpeza ao cliente " +
                        entry.getKey() + ": " + e.getMessage());
                registeredClients.remove(entry.getKey());
            }
        }
    }

    /** Para testes/debug */
    public int getRegisteredClientCount() {
        return registeredClients.size();
    }
}
