package br.com.sd;

import br.com.sd.client.BoardCanvas;
import br.com.sd.client.NotificationServiceImpl;
import br.com.sd.entitys.*;
import br.com.sd.remote.BoardService;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Cliente do Quadro Colaborativo.
 *
 * Usa apenas RMI para invocar métodos remotos no servidor e receber callbacks.
 */
public class BoardClient {

    private static final String SERVER_HOST = "10.10.228.93";
    private static final int    RMI_PORT    = 1099;
    private static final String OBJECT_NAME = "BoardService";

    public static void main(String[] args) throws Exception {
        String clientId = "client-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("=== Quadro Colaborativo - Cliente: " + clientId + " ===\n");

        BoardService boardService = (BoardService)
                Naming.lookup("rmi://" + SERVER_HOST + ":" + RMI_PORT + "/" + OBJECT_NAME);

        System.out.println("[Client] Conectado ao servidor RMI: rmi://" + SERVER_HOST + ":" + RMI_PORT + "/" + OBJECT_NAME);

        System.out.println("[Client] Buscando estado inicial do quadro...");
        Board initialBoard = boardService.getBoard();
        System.out.println("[Client] Board recebido: " +
                initialBoard.getWidth() + "x" + initialBoard.getHeight());

        BoardCanvas canvas = new BoardCanvas(initialBoard);
        canvas.print();

        NotificationServiceImpl notifService = new NotificationServiceImpl(clientId, canvas);
        String regResult = boardService.registerClient(notifService);
        System.out.println("[Client] Registro: " + regResult);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\n[Client] Encerrando cliente...");

                String result = boardService.unregisterClient(clientId);

                System.out.println("[Client] Cliente removido do servidor: " + result);

            } catch (Exception e) {
                System.out.println("[Client] Erro ao remover cliente: " + e.getMessage());
            }
        }));

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nComandos:");
        System.out.println("  draw x y R G B   — desenha pixel");
        System.out.println("  clear             — limpa o quadro");
        System.out.println("  board             — exibe o quadro atual");
        System.out.println("  size              — tamanho do quadro");
        System.out.println("  exit              — sair\n");

        boolean running = true;
        while (running && scanner.hasNextLine()) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "draw": {
                    if (parts.length < 6) {
                        System.out.println("Uso: draw x y R G B");
                        break;
                    }
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int r = Integer.parseInt(parts[3]);
                    int g = Integer.parseInt(parts[4]);
                    int b = Integer.parseInt(parts[5]);

                    List<Pixel> pixels = new ArrayList<>();
                    pixels.add(new Pixel(x, y, new Color(r, g, b)));
                    DrawEvent event = new DrawEvent(clientId, pixels);

                    String result = boardService.drawPixels(event);
                    System.out.println("[Client] drawPixels: " + result);
                    break;
                }

                case "clear": {
                    String result = boardService.clearBoard();
                    System.out.println("[Client] clearBoard: " + result);
                    break;
                }

                case "board": {
                    Board board = boardService.getBoard();
                    canvas.setBoard(board);
                    canvas.print();
                    break;
                }

                case "size": {
                    int[] size = boardService.getBoardSize();
                    System.out.println("[Client] Tamanho: " + size[0] + "x" + size[1]);
                    break;
                }

                case "exit": {
                    String result = boardService.unregisterClient(clientId);
                    System.out.println("[Client] Desconectado: " + result);
                    running = false;
                    break;
                }

                default:
                    System.out.println("Comando desconhecido: " + cmd);
            }
        }

        System.exit(0);
    }
}
