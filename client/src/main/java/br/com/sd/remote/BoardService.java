package br.com.sd.remote;

import br.com.sd.entitys.Board;
import br.com.sd.entitys.DrawEvent;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BoardService extends Remote {

    Board getBoard() throws RemoteException;
    String drawPixels(DrawEvent event) throws RemoteException;
    String clearBoard() throws RemoteException;
    int[] getBoardSize() throws RemoteException;
    String registerClient(NotificationService client) throws RemoteException;
    String unregisterClient(String clientId) throws RemoteException;
}
