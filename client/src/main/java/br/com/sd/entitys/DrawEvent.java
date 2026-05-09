package br.com.sd.entitys;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um evento de desenho: um conjunto de pixels alterados de uma vez.
 * Composição tipo AGREGAÇÃO ("tem-uma lista de Pixels").
 * Passada por VALOR nas chamadas remotas (serializada via JSON).
 */
public class DrawEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String authorId;       // ID do cliente que originou o evento
    private List<Pixel> pixels;    // AGREGAÇÃO: DrawEvent "tem-uma" lista de Pixel
    private long timestamp;

    public DrawEvent() {
        this.pixels = new ArrayList<>();
        this.timestamp = Instant.now().toEpochMilli();
    }

    public DrawEvent(String authorId, List<Pixel> pixels) {
        this.authorId = authorId;
        this.pixels = pixels != null ? pixels : new ArrayList<>();
        this.timestamp = Instant.now().toEpochMilli();
    }

    public String getAuthorId()      { return authorId; }
    public List<Pixel> getPixels()   { return pixels;   }
    public long getTimestamp()       { return timestamp; }

    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setPixels(List<Pixel> pixels) { this.pixels = pixels;    }
    public void setTimestamp(long timestamp)  { this.timestamp = timestamp; }

    public void addPixel(Pixel p) { this.pixels.add(p); }

    @Override
    public String toString() {
        return String.format("DrawEvent(author=%s, pixels=%d, ts=%d)",
                authorId, pixels.size(), timestamp);
    }
}
