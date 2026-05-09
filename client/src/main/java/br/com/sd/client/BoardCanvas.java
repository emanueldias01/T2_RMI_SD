package br.com.sd.client;

import br.com.sd.entitys.Board;
import br.com.sd.entitys.Pixel;

/**
 * Representação local do quadro no cliente.
 * Mantém uma cópia do estado atual e é atualizada pelos callbacks do servidor.
 */
public class BoardCanvas {

    private Board localBoard;

    public BoardCanvas(Board initialBoard) {
        this.localBoard = initialBoard;
    }

    /** Aplica um pixel ao board local */
    public synchronized void applyPixel(Pixel p) {
        localBoard.setPixel(p);
    }

    /** Limpa o board local */
    public synchronized void clear() {
        localBoard.clear();
    }

    /** Imprime o estado atual do board no terminal */
    public synchronized void print() {
        System.out.println(localBoard.toString());
    }

    public synchronized Board getBoard() {
        return localBoard;
    }

    public synchronized void setBoard(Board board) {
        this.localBoard = board;
    }
}
