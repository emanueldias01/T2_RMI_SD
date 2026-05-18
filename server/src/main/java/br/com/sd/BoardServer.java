package br.com.sd;

import br.com.sd.services.BoardServiceImpl;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

/**
 * Servidor do Quadro Colaborativo.
 *
 * Usa apenas RMI para expor o serviço BoardService e permitir callbacks.
 */
public class BoardServer {

    private static final int RMI_PORT     = 1099;
    private static final int BOARD_HEIGHT = 50;
    private static final int BOARD_WIDTH  = 50;

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
        System.out.println("[Server] URL: rmi://localhost:" + RMI_PORT + "/BoardService");

        System.out.println("[Server] Servidor aguardando invocações RMI...\n");

        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}
