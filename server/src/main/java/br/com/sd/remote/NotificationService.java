package br.com.sd.remote;

import br.com.sd.entitys.DrawEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota de CALLBACK: o servidor usa essa referência para
 * notificar clientes sobre mudanças no quadro (passagem por referência).
 */
public interface NotificationService extends Remote {

    void onDrawEvent(DrawEvent event) throws RemoteException;
    void onBoardCleared() throws RemoteException;
    String getClientId() throws RemoteException;
}
