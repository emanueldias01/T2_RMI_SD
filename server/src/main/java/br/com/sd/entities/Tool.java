package br.com.sd.entities;

public abstract class Tool {
    protected String name;

    public Tool(String name) {
        this.name = name;
    }

    public abstract void apply(Board board, int x, int y, int color);
}
