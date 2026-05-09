package br.com.sd.client;

import br.com.sd.entitys.DrawEvent;
import br.com.sd.entitys.Pixel;
import br.com.sd.remote.NotificationService;
import br.com.sd.serialization.JsonSerializer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementação do serviço de notificação no CLIENTE.
 *
 * EXTENSÃO ("é-um"): extends UnicastRemoteObject → é um objeto remoto RMI.
 * EXTENSÃO ("é-um"): implements NotificationService → é um NotificationService.
 *
 * Esta referência é passada ao servidor via PASSAGEM POR REFERÊNCIA (RMI).
 * O servidor a usa para invocar callbacks remotamente quando o quadro muda.
 */
public class NotificationServiceImpl extends UnicastRemoteObject implements NotificationService {
    private static final long serialVersionUID = 1L;

    private final String clientId;
    private final BoardCanvas canvas; // referência ao canvas local para atualizar

    public NotificationServiceImpl(String clientId, BoardCanvas canvas) throws RemoteException {
        super();
        this.clientId = clientId;
        this.canvas   = canvas;
    }

    /**
     * Chamado pelo SERVIDOR (via RMI) quando novos pixels foram desenhados.
     * Recebe DrawEvent por VALOR (JSON desserializado).
     */
    @Override
    public void onDrawEvent(byte[] drawEventJson) throws RemoteException {
        DrawEvent event = JsonSerializer.deserializeDrawEvent(drawEventJson);
        System.out.println("[Client-" + clientId + "] onDrawEvent() recebido: " + event);

        // Aplica os pixels ao canvas local (passagem por valor já desserializada)
        for (Pixel p : event.getPixels()) {
            canvas.applyPixel(p);
        }
        canvas.print();
    }

    /**
     * Chamado pelo SERVIDOR quando o quadro foi limpo.
     */
    @Override
    public void onBoardCleared() throws RemoteException {
        System.out.println("[Client-" + clientId + "] Quadro foi limpo pelo servidor.");
        canvas.clear();
        canvas.print();
    }

    /**
     * Retorna o ID único deste cliente (passagem por valor).
     */
    @Override
    public String getClientId() throws RemoteException {
        return clientId;
    }
}
