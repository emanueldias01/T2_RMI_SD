package br.com.sd.services;

import br.com.sd.entitys.Board;
import br.com.sd.entitys.DrawEvent;
import br.com.sd.entitys.Pixel;
import br.com.sd.remote.BoardService;
import br.com.sd.remote.NotificationService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação do serviço remoto de quadro colaborativo.
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

    @Override
    public Board getBoard() throws RemoteException {
        System.out.println("[BoardService] getBoard() chamado");
        synchronized (board) {
            return new Board(board.getGrid());
        }
    }

    @Override
    public String drawPixels(DrawEvent event) throws RemoteException {
        System.out.println("[BoardService] drawPixels() - " + event);

        synchronized (board) {
            for (Pixel p : event.getPixels()) {
                board.setPixel(p);
            }
        }

        broadcastDrawEvent(event);
        return "OK";
    }

    @Override
    public String clearBoard() throws RemoteException {
        System.out.println("[BoardService] clearBoard() chamado");
        synchronized (board) {
            board.clear();
        }
        broadcastClear();
        return "OK";
    }

    @Override
    public int[] getBoardSize() throws RemoteException {
        System.out.println("[BoardService] getBoardSize() chamado");
        return new int[]{board.getWidth(), board.getHeight()};
    }

    @Override
    public String registerClient(NotificationService client) throws RemoteException {
        String clientId = client.getClientId();
        System.out.println("[BoardService] registerClient() - clientId: " + clientId);

        registeredClients.put(clientId, client);
        System.out.println("[BoardService] Cliente registrado: " + clientId +
                           " | Total: " + registeredClients.size());

        return "REGISTERED:" + clientId;
    }

    @Override
    public String unregisterClient(String clientId) throws RemoteException {
        System.out.println("[BoardService] unregisterClient() - clientId: " + clientId);
        registeredClients.remove(clientId);
        System.out.println("[BoardService] Clientes registrados: " + registeredClients.size());
        return "UNREGISTERED:" + clientId;
    }

    // Broadcast privado: notifica clientes via REFERÊNCIA REMOTA
    private void broadcastDrawEvent(DrawEvent event) {
        for (Map.Entry<String, NotificationService> entry : registeredClients.entrySet()) {
            try {
                entry.getValue().onDrawEvent(event);
            } catch (RemoteException e) {
                System.err.println("[BoardService] Falha ao notificar cliente " +
                        entry.getKey() + ": " + e.getMessage());
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
