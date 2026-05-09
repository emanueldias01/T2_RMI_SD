package br.com.sd.entitys;

import java.io.Serializable;

/**
 * Representa o quadro colaborativo (canvas).
 * Composição tipo AGREGAÇÃO ("tem-um") com Pixel (grid).
 * Passada por VALOR nas chamadas remotas ao retornar snapshot (serializada via JSON).
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private int height;
    private int width;
    private int[][] grid; // grade de cores como inteiros RGB

    public Board() {}

    public Board(int height, int width) {
        this.height = height;
        this.width = width;
        this.grid = new int[height][width];
        // inicializa tudo com branco
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                this.grid[i][j] = 0xFFFFFF;
    }

    public Board(int[][] initGrid) {
        this.height = initGrid.length;
        this.width = initGrid[0].length;
        this.grid = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                this.grid[i][j] = initGrid[i][j];
    }

    /** Aplica um pixel ao quadro */
    public void setPixel(Pixel p) {
        if (p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height)
            this.grid[p.getY()][p.getX()] = p.getColor().toInt();
    }

    /** Aplica um pixel diretamente pelo inteiro RGB */
    public void setPixel(int x, int y, int colorInt) {
        if (x >= 0 && x < width && y >= 0 && y < height)
            this.grid[y][x] = colorInt;
    }

    /** Retorna cópia defensiva da grade */
    public int[][] getGrid() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                copy[i][j] = grid[i][j];
        return copy;
    }

    public int getHeight() { return height; }
    public int getWidth()  { return width;  }

    public void setHeight(int height) { this.height = height; }
    public void setWidth(int width)   { this.width = width;   }
    public void setGrid(int[][] grid) { this.grid = grid;     }

    /** Limpa o quadro (tudo branco) */
    public void clear() {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                grid[i][j] = 0xFFFFFF;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Board (" + width + "x" + height + "):\n");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                String hex = String.format("%06X", grid[i][j]);
                sb.append(hex.startsWith("F") ? "." : hex.charAt(0));
                sb.append("  ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
