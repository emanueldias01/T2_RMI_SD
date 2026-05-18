# T2_RMI_SD

**Equipe:**  
- Emanuel Araujo Brasileiro Dias - 569532  
- Victor Farias da Silva - 567680

## Como Rodar

### Pré-requisitos
- **Java JDK 8+ instalado**
- **Compilar e executar em ambiente Linux ou Windows**
- **Ambos cliente e servidor são aplicações de terminal (não há interface gráfica)**

### 1. Compilar o Projeto

No diretório raiz do projeto:

```sh
# Compile todas as classes Java
javac -d bin src/**/*.java
```

### 2. Iniciar o Servidor RMI

- A classe Main do server está em server/server/BoardServer.java
- Execute:
```sh
    java BoardServer.java
```

### 3. Executar o Cliente

- A classe Main do client está em client/client/BoardClient.java
- Execute:
```sh
    java BoardClient.java
```

Se o servidor e o cliente estiverem em máquinas diferentes, lembre-se de ajustar o host/IP no cliente conforme instruções no código.


## Relatório do Projeto

### PixelHub utilizando RMI

Universidade Federal do Ceará - Campus de Quixadá


### 1. Objetivo

Este repositório apresenta a implementação de um serviço remoto com RMI para um Hub de PixelArt, conforme solicitado no Trabalho 2 da disciplina de Sistemas Distribuídos. O objetivo principal foi reimplementar a solução do trabalho anterior, migrando para uma arquitetura baseada em comunicação cliente-servidor via RMI, seguindo o protocolo requisição-resposta descrito no livro texto.


### 2. Descrição Geral do Sistema

O sistema implementa um “Hub de PixelArt” (**PixelHub**), que permite aos usuários desenhar em um mural compartilhado em tempo real. Todas as interações de edição do _board_ são realizadas via invocação remota de métodos, sem interface gráfica, apenas usando linha de comando.

A solução segue o padrão da seção 5.2 do livro, utilizando métodos `dispatch`(`doOperation`), `getRequest` e `sendReply` no para Java RMI. As mensagens de requisição e resposta são empacotadas no servidor utilizando o `JsonSerializer.java` que serializa as mensagens para o formato JSON.

### 3. Arquitetura e Componentes

#### 3.1 Entidades

O sistema possui as seguintes classes do tipo entidade:

- `Usuario`: Representa um usuário do sistema.
- `QuadroPixelArt`: Representa um quadro de pixel art criado pelo usuário.
- `Pixel`: Representa um pixel individual dentro do quadro.
- `Color`: Representa uma cor no formato RGB com representação de inteiro ou string.
- `DrawEvent`: Representa um evento de pintura dentro do quadro.

Exemplo:
```java name=server/src/main/java/br/sd/entitys
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private int height;
    private int width;
    private int[][] grid;
    // getters, setters, e construtor
}
```

#### 3.2 Composição Agregação ("tem-um")

Exemplo de duas composições de agregação ("tem-um")

- `DrawEvent` tem uma lista de `Pixel`.

```java name=server/src/main/java/br/sd/entitys
private List<Pixel> pixels;
```

- `Pixel` tem uma cor de `Color`.

```java name=server/src/main/java/br/sd/entitys
public class Pixel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private Color color;
```

#### 3.3 Composição Extensão ("é-um")

- `BoardService` estende `Remote`

```java name=server/src/main/java/br/sd/remote
public interface BoardService extends Remote {

    Board getBoard() throws RemoteException;
    String drawPixels(DrawEvent event) throws RemoteException;
    String clearBoard() throws RemoteException;
    int[] getBoardSize() throws RemoteException;
    String registerClient(NotificationService client) throws RemoteException;
    String unregisterClient(String clientId) throws RemoteException;
}
```

- `NotificationService` estende `Remote`

```java name=server/src/main/java/br/sd/remote
public interface NotificationService extends Remote {

    void onDrawEvent(DrawEvent event) throws RemoteException;
    void onBoardCleared() throws RemoteException;
    String getClientId() throws RemoteException;
}
```

#### 3.4 Métodos para Invocação Remota

Exemplos de métodos definidos na interface remota:

- Interface

```java name=server/src/main/java/br/sd/remote
public interface BoardService extends Remote {
    Board getBoard() throws RemoteException;
    String drawPixels(DrawEvent event) throws RemoteException;
    String clearBoard() throws RemoteException;
    int[] getBoardSize() throws RemoteException;
    String registerClient(NotificationService client) throws RemoteException;
    String unregisterClient(String clientId) throws RemoteException;
}
```

- Implementação

```java name=server/src/main/java/br/sd/services/BoardServiceImpl.java
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
```

#### 3.5 Uso de passagem por referência para objetos remotos

```java name=server/src/main/java/br/sd/services/BoardServiceImpl.java
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

    //...

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

    // ...

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

    // ...
}
```

#### 3.6 Uso de passagem por valor para objetos locais

```java
/**
 * Interface remota do serviço de quadro colaborativo.
 */
public interface BoardService extends Remote {

    Board getBoard() throws RemoteException;
    String drawPixels(DrawEvent event) throws RemoteException;
    String clearBoard() throws RemoteException;
    int[] getBoardSize() throws RemoteException;
    String registerClient(NotificationService client) throws RemoteException;
    String unregisterClient(String clientId) throws RemoteException;
}
```

### 5. Serialização e Representação Externa

Todos os dados transmitidos nos métodos remotos são serializados como JSON. Assim, parâmetros e respostas seguem um padrão externo simples, facilitando interoperabilidade e debugging.


