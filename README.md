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

O sistema implementa um “Hub de PixelArt” (**PixelHub**), que permite aos usuários criar e compartilhar quadros de pixel art de modo distribuído. Todas as interações (criação, edição, consulta, listagem de quadros) são realizadas via invocação remota de métodos, sem interface gráfica, apenas modo texto (linha de comando).

A solução segue o padrão da seção 5.2 do livro, utilizando métodos `doOperation`, `getRequest` e `sendReply` adaptados para Java RMI. As mensagens de requisição e resposta são empacotadas utilizando JSON.


### 3. Arquitetura e Componentes

#### 3.1 Entidades

O sistema possui as seguintes classes do tipo entidade:

- `Usuario`: Representa um usuário do sistema.
- `QuadroPixelArt`: Representa um quadro de pixel art criado pelo usuário.
- `Pixel`: Representa um pixel individual dentro do quadro.
- `Comentario`: Representa um comentário feito em um quadro.

**Exemplo de entidade:**

```java name=src/model/QuadroPixelArt.java
public class QuadroPixelArt implements Serializable {
    private String id;
    private String titulo;
    private Usuario autor;
    private List<Pixel> pixels;
    private List<Comentario> comentarios;
    // getters, setters, e construtor
}
```

#### 3.2 Composição Agregação ("tem-um")

- `QuadroPixelArt` tem uma lista de `Pixel`.
- `QuadroPixelArt` tem uma lista de `Comentario`.

```java name=src/model/QuadroPixelArt.java
private List<Pixel> pixels;
private List<Comentario> comentarios;
```

#### 3.3 Composição Extensão ("é-um")

- `QuadroColorido` estende `QuadroPixelArt` (suporte a múltiplas cores).
- `QuadroPretoBranco` estende `QuadroPixelArt` (apenas duas cores).

```java name=src/model/QuadroColorido.java
public class QuadroColorido extends QuadroPixelArt {
    // campos e comportamentos extras
}
```

#### 3.4 Métodos Remotos

Exemplos de métodos definidos na interface remota:

```java name=src/remote/IPixelHub.java
public interface IPixelHub extends Remote {
    QuadroPixelArt criarQuadro(Usuario usuario, String titulo, int largura, int altura) throws RemoteException;
    void editarPixel(String idQuadro, int x, int y, String cor) throws RemoteException;
    List<QuadroPixelArt> listarQuadros() throws RemoteException;
    void comentarQuadro(String idQuadro, String comentario, Usuario usuario) throws RemoteException;
}
```

Todos os métodos acima estão disponíveis no serviço RMI e aceitam/passam objetos serializáveis. Nos métodos adequados, o servidor trata passagem por referência/remota, conforme solicitado.

---

### 4. Protocolo de Comunicação

A comunicação segue o modelo requisição-resposta como no livro texto:

- `doOperation(RemoteObjectRef o, int methodId, byte[] arguments)`: empacota e realiza chamada remota.
- `getRequest()`: no servidor, recebe requisições dos clientes.
- `sendReply(byte[] reply, InetAddress clientHost, int clientPort)`: envia resposta ao cliente.

**Exemplo ilustrativo de empacotamento com JSON:**

```java name=src/remote/PixelHubServer.java
import com.google.gson.Gson;

Gson gson = new Gson();
String jsonArgs = gson.toJson(argsObjeto);
byte[] serializedArgs = jsonArgs.getBytes(StandardCharsets.UTF_8);
```

---

### 5. Serialização e Representação Externa

Todos os dados transmitidos nos métodos remotos são serializados como JSON. Assim, parâmetros e respostas seguem um padrão externo simples, facilitando interoperabilidade e debugging.

**Exemplo de serialização:**

```java name=src/model/Usuario.java
Gson gson = new Gson();
Usuario usuario = new Usuario("fulano", "senhaforte");
String usuarioJson = gson.toJson(usuario);
```

> Observação: O sistema não possui interface gráfica (GUI); todas as operações são feitas por linha de comando.
