package br.com.sd.entitys;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um evento de desenho: um conjunto de pixels alterados de uma vez.
 */
public class DrawEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String authorId;
    private List<Pixel> pixels;
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

    public String getAuthorId() { 
        return authorId; 
    }
    public List<Pixel> getPixels() { 
        return pixels;   
    }
    public long getTimestamp() { 
        return timestamp; 
    }

    public void setAuthorId(String authorId) { 
        this.authorId = authorId; 
    }
    public void setPixels(List<Pixel> pixels) { 
        this.pixels = pixels;    
    }
    public void setTimestamp(long timestamp)  { 
        this.timestamp = timestamp; 
    }

    public void addPixel(Pixel p) { 
        this.pixels.add(p); 
    }

    @Override
    public String toString() {
        return String.format("DrawEvent(author=%s, pixels=%d, ts=%d)",
                authorId, pixels.size(), timestamp);
    }
}
