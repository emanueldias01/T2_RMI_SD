package br.com.sd.entities;

public class Pencil extends Tool {
    public Pencil() {
        super("Lápis Comum");
    }

    @Override
    public void apply(Board board, int x, int y, int color) {
        // Agregação implícita: O Board recebe um novo Pixel baseado na Tool
        board.setPixel(x, y, color);
    }
}
