package br.com.sd.entitys;

import java.io.Serializable;

/**
 * Representa uma cor RGB.
 * Passada por VALOR nas chamadas remotas (serializada via JSON).
 */
public class Color implements Serializable {
    private static final long serialVersionUID = 1L;

    private int r;
    private int g;
    private int b;

    public Color() {}

    public Color(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("Componentes RGB devem estar entre 0 e 255");
        }
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /** Cria Color a partir de um inteiro RGB (0xRRGGBB) */
    public static Color fromInt(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new Color(r, g, b);
    }

    /** Converte para inteiro RGB */
    public int toInt() {
        return (r << 16) | (g << 8) | b;
    }

    public int getR() { 
        return r; 
    }
    public int getG() { 
        return g; 
    }
    public int getB() { 
        return b; 
    }

    public void setR(int r) { 
        this.r = r; 
    }
    public void setG(int g) { 
        this.g = g; 
    }
    public void setB(int b) { 
        this.b = b; 
    }

    @Override
    public String toString() {
        return String.format("Color(r=%d, g=%d, b=%d, hex=#%06X)", r, g, b, toInt());
    }
}
