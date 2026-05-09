package br.com.sd.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota de CALLBACK: o servidor usa essa referência para
 * notificar clientes sobre mudanças no quadro (passagem por referência).
 *
 * EXTENSÃO ("é-um"): implementações concretas estendem UnicastRemoteObject
 * e implementam esta interface.
 */
public interface NotificationService extends Remote {

    /**
     * Chamado pelo servidor quando há novos pixels desenhados.
     * O cliente recebe o DrawEvent serializado em JSON.
     */
    void onDrawEvent(byte[] drawEventJson) throws RemoteException;

    /**
     * Chamado pelo servidor quando o quadro é limpo.
     */
    void onBoardCleared() throws RemoteException;

    /**
     * Retorna o ID único deste cliente.
     */
    String getClientId() throws RemoteException;
}
