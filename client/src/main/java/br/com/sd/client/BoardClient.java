package br.com.sd.client;

import br.com.sd.entitys.*;
import br.com.sd.middleware.RMIClient;
import br.com.sd.middleware.RemoteObjectRef;
import br.com.sd.serialization.JsonSerializer;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Cliente do Quadro Colaborativo.
 *
 * Demonstra:
 *  - Uso de doOperation() para invocar métodos remotos
 *  - Registro de callback (NotificationServiceImpl) via RMI — passagem por REFERÊNCIA
 *  - Recebimento de DrawEvents do servidor via callback
 *  - Passagem de entidades por VALOR (serialização JSON)
 */
public class BoardClient {

    private static final String SERVER_HOST    = "localhost";
    private static final int    PROTOCOL_PORT  = 5001;
    private static final int    RMI_PORT       = 1099;
    private static final String OBJECT_NAME    = "BoardService";

    public static void main(String[] args) throws Exception {
        // ID único para este cliente
        String clientId = "client-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("=== Quadro Colaborativo - Cliente: " + clientId + " ===\n");

        // ── Referência ao objeto remoto ──────────────────────────────
        RemoteObjectRef remoteRef = new RemoteObjectRef(SERVER_HOST, PROTOCOL_PORT, OBJECT_NAME);

        // ── Middleware do cliente: doOperation() ─────────────────────────────────
        RMIClient rmiClient = new RMIClient(SERVER_HOST, PROTOCOL_PORT);

        // ── 1. Busca o board inicial via doOperation("getBoard") ─────────────────
        System.out.println("[Client] Buscando estado inicial do quadro...");
        byte[] boardData = rmiClient.doOperation(remoteRef, "getBoard",
                JsonSerializer.serializeEmpty());
        Board initialBoard = JsonSerializer.deserializeBoard(boardData);
        System.out.println("[Client] Board recebido: " +
                initialBoard.getWidth() + "x" + initialBoard.getHeight());

        // ── 2. Cria canvas local e registra callback RMI ─────────────────────────
        BoardCanvas canvas = new BoardCanvas(initialBoard);
        canvas.print();

        // Registra NotificationServiceImpl no RMI registry local para callbacks
        // O servidor fará lookup e chamará métodos nesse objeto (PASSAGEM POR REFERÊNCIA)
        NotificationServiceImpl notifService = new NotificationServiceImpl(clientId, canvas);
        Naming.rebind("rmi://localhost:" + RMI_PORT + "/" + clientId, notifService);
        System.out.println("[Client] Serviço de notificação registrado: rmi://localhost/" + clientId);

        // ── 3. Registra-se no servidor via doOperation("registerClient") ──────────
        byte[] regResult = rmiClient.doOperation(remoteRef, "registerClient",
                JsonSerializer.serializeString(clientId));
        System.out.println("[Client] Registro: " + JsonSerializer.deserializeString(regResult));

        // ── 4. Loop interativo para desenhar ──────────────────────────────────────
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

                    // Monta DrawEvent com Pixel (entidades por VALOR)
                    List<Pixel> pixels = new ArrayList<>();
                    pixels.add(new Pixel(x, y, new Color(r, g, b)));
                    DrawEvent event = new DrawEvent(clientId, pixels);

                    // doOperation("drawPixels", drawEventJson)
                    byte[] result = rmiClient.doOperation(remoteRef, "drawPixels",
                            JsonSerializer.serializeDrawEvent(event));
                    System.out.println("[Client] drawPixels: " + JsonSerializer.deserializeString(result));
                    break;
                }

                case "clear": {
                    // doOperation("clearBoard")
                    byte[] result = rmiClient.doOperation(remoteRef, "clearBoard",
                            JsonSerializer.serializeEmpty());
                    System.out.println("[Client] clearBoard: " + JsonSerializer.deserializeString(result));
                    break;
                }

                case "board": {
                    // doOperation("getBoard")
                    byte[] boardBytes = rmiClient.doOperation(remoteRef, "getBoard",
                            JsonSerializer.serializeEmpty());
                    Board board = JsonSerializer.deserializeBoard(boardBytes);
                    canvas.setBoard(board);
                    canvas.print();
                    break;
                }

                case "size": {
                    // doOperation("getBoardSize")
                    byte[] sizeBytes = rmiClient.doOperation(remoteRef, "getBoardSize",
                            JsonSerializer.serializeEmpty());
                    System.out.println("[Client] Tamanho: " +
                            new String(sizeBytes, java.nio.charset.StandardCharsets.UTF_8));
                    break;
                }

                case "exit": {
                    // doOperation("unregisterClient")
                    rmiClient.doOperation(remoteRef, "unregisterClient",
                            JsonSerializer.serializeString(clientId));
                    System.out.println("[Client] Desconectado.");
                    running = false;
                    break;
                }

                default:
                    System.out.println("Comando desconhecido: " + cmd);
            }
        }

        // Remove do RMI registry local
        try {
            Naming.unbind("rmi://localhost:" + RMI_PORT + "/" + clientId);
        } catch (Exception ignored) {}
        System.exit(0);
    }
}
