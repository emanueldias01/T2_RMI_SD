package br.com.sd.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota do serviço de quadro colaborativo.
 * Define os métodos disponíveis para invocação remota.
 *
 * Cada método corresponde a um methodId nas mensagens de requisição.
 */
public interface BoardService extends Remote {

    /** methodId: "getBoard" — retorna o snapshot atual do quadro (Board serializado por valor) */
    byte[] getBoard() throws RemoteException;

    /** methodId: "drawPixels" — aplica um DrawEvent (lista de pixels) ao quadro */
    byte[] drawPixels(byte[] drawEventJson) throws RemoteException;

    /** methodId: "clearBoard" — limpa o quadro inteiro */
    byte[] clearBoard() throws RemoteException;

    /** methodId: "getBoardSize" — retorna as dimensões do quadro como JSON */
    byte[] getBoardSize() throws RemoteException;

    /** methodId: "registerClient" — registra um cliente para receber broadcasts */
    byte[] registerClient(byte[] clientRefJson) throws RemoteException;

    /** methodId: "unregisterClient" — remove o cliente do registro de broadcasts */
    byte[] unregisterClient(byte[] clientIdJson) throws RemoteException;
}
