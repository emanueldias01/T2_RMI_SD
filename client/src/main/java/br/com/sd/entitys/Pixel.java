package br.com.sd.entitys;

import java.io.Serializable;

/**
 * Representa um pixel no quadro colaborativo.
 */
public class Pixel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private Color color;

    public Pixel() {}

    public Pixel(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Pixel(int x, int y, int colorInt) {
        this.x = x;
        this.y = y;
        this.color = Color.fromInt(colorInt);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Color getColor() { return color; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setColor(Color color) { this.color = color; }

    @Override
    public String toString() {
        return String.format("Pixel(x=%d, y=%d, %s)", x, y, color);
    }
}
