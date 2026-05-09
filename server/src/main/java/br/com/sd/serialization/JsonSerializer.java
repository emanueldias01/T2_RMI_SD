package br.com.sd.serialization;

import br.com.sd.entitys.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representação externa de dados via JSON (sem dependências externas).
 * Entidades passadas por VALOR usam esta classe.
 */
public class JsonSerializer {

    // ─── Color ───

    public static byte[] serializeColor(Color c) {
        return String.format("{\"r\":%d,\"g\":%d,\"b\":%d}", c.getR(), c.getG(), c.getB())
                     .getBytes(StandardCharsets.UTF_8);
    }

    public static Color deserializeColor(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        return new Color(getInt(json,"r"), getInt(json,"g"), getInt(json,"b"));
    }

    // ─── Pixel ───

    public static byte[] serializePixel(Pixel p) {
        return String.format("{\"x\":%d,\"y\":%d,\"color\":{\"r\":%d,\"g\":%d,\"b\":%d}}",
                p.getX(), p.getY(), p.getColor().getR(), p.getColor().getG(), p.getColor().getB())
                .getBytes(StandardCharsets.UTF_8);
    }

    public static Pixel deserializePixel(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        int x = getInt(json, "x"), y = getInt(json, "y");
        Color c = deserializeColor(getObj(json, "color").getBytes(StandardCharsets.UTF_8));
        return new Pixel(x, y, c);
    }

    // ─── Pixel List ───

    public static byte[] serializePixelList(List<Pixel> pixels) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < pixels.size(); i++) {
            Pixel p = pixels.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"x\":%d,\"y\":%d,\"color\":{\"r\":%d,\"g\":%d,\"b\":%d}}",
                    p.getX(), p.getY(), p.getColor().getR(), p.getColor().getG(), p.getColor().getB()));
        }
        return sb.append("]").toString().getBytes(StandardCharsets.UTF_8);
    }

    public static List<Pixel> deserializePixelList(byte[] data) {
        return parsePixels(new String(data, StandardCharsets.UTF_8));
    }

    // ─── DrawEvent ───

    public static byte[] serializeDrawEvent(DrawEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"authorId\":\"").append(esc(event.getAuthorId())).append("\",");
        sb.append("\"timestamp\":").append(event.getTimestamp()).append(",");
        sb.append("\"pixels\":[");
        List<Pixel> pixels = event.getPixels();
        for (int i = 0; i < pixels.size(); i++) {
            Pixel p = pixels.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"x\":%d,\"y\":%d,\"color\":{\"r\":%d,\"g\":%d,\"b\":%d}}",
                    p.getX(), p.getY(), p.getColor().getR(), p.getColor().getG(), p.getColor().getB()));
        }
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static DrawEvent deserializeDrawEvent(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        String authorId = getStr(json, "authorId");
        long timestamp  = getLong(json, "timestamp");
        int pixStart = json.indexOf("\"pixels\":[") + "\"pixels\":".length();
        List<Pixel> pixels = parsePixels(json.substring(pixStart).trim());
        DrawEvent e = new DrawEvent(authorId, pixels);
        e.setTimestamp(timestamp);
        return e;
    }

    // ─── Board ───

    public static byte[] serializeBoard(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"height\":").append(board.getHeight())
          .append(",\"width\":").append(board.getWidth()).append(",\"grid\":[");
        int[][] grid = board.getGrid();
        for (int i = 0; i < board.getHeight(); i++) {
            if (i > 0) sb.append(",");
            sb.append("[");
            for (int j = 0; j < board.getWidth(); j++) {
                if (j > 0) sb.append(",");
                sb.append(grid[i][j]);
            }
            sb.append("]");
        }
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Board deserializeBoard(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        int height = getInt(json, "height");
        int width  = getInt(json, "width");
        int gridIdx = json.indexOf("\"grid\":[") + "\"grid\":".length();
        String gridStr = json.substring(gridIdx).trim();
        // Strip outer []
        gridStr = gridStr.substring(1, gridStr.lastIndexOf("]"));
        List<String> rows = splitArrays(gridStr);
        int[][] grid = new int[height][width];
        for (int i = 0; i < rows.size() && i < height; i++) {
            String row = rows.get(i).trim();
            row = row.substring(1, row.length()-1);
            String[] vals = row.split(",");
            for (int j = 0; j < vals.length && j < width; j++)
                grid[i][j] = Integer.parseInt(vals[j].trim());
        }
        Board b = new Board(height, width);
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                b.setPixel(j, i, grid[i][j]);
        return b;
    }

    // ─── String ───

    public static byte[] serializeString(String s) {
        if (s == null) return "{\"value\":null}".getBytes(StandardCharsets.UTF_8);
        return ("{\"value\":\"" + esc(s) + "\"}").getBytes(StandardCharsets.UTF_8);
    }

    public static String deserializeString(byte[] data) {
        return getStr(new String(data, StandardCharsets.UTF_8), "value");
    }

    // ─── Int ───

    public static byte[] serializeInt(int v) {
        return ("{\"value\":" + v + "}").getBytes(StandardCharsets.UTF_8);
    }

    public static int deserializeInt(byte[] data) {
        return getInt(new String(data, StandardCharsets.UTF_8), "value");
    }

    // ─── Empty ───

    public static byte[] serializeEmpty() {
        return "{}".getBytes(StandardCharsets.UTF_8);
    }

    // ═══ Helpers internos ═══

    private static int getInt(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (m.find()) return Integer.parseInt(m.group(1));
        throw new RuntimeException("Campo int '" + key + "' não encontrado");
    }

    private static long getLong(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (m.find()) return Long.parseLong(m.group(1));
        throw new RuntimeException("Campo long '" + key + "' não encontrado");
    }

    private static String getStr(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (m.find()) return unescape(m.group(1));
        if (json.contains("\"" + key + "\":null")) return null;
        throw new RuntimeException("Campo string '" + key + "' não encontrado");
    }

    private static String getObj(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) throw new RuntimeException("Objeto '" + key + "' não encontrado");
        int start = json.indexOf("{", idx);
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') { depth--; if (depth == 0) return json.substring(start, i+1); }
        }
        throw new RuntimeException("Objeto mal formado");
    }

    private static List<Pixel> parsePixels(String json) {
        List<Pixel> result = new ArrayList<>();
        if (!json.startsWith("[")) return result;
        json = json.substring(1);
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { depth++; cur.append(c); }
            else if (c == '}') {
                cur.append(c); depth--;
                if (depth == 0) {
                    String pj = cur.toString();
                    int x = getInt(pj, "x"), y = getInt(pj, "y");
                    Color col = deserializeColor(getObj(pj, "color").getBytes(StandardCharsets.UTF_8));
                    result.add(new Pixel(x, y, col));
                    cur = new StringBuilder();
                }
            } else if (c == ']' && depth == 0) break;
            else if (depth > 0) cur.append(c);
        }
        return result;
    }

    private static List<String> splitArrays(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') { depth++; cur.append(c); }
            else if (c == ']') {
                cur.append(c); depth--;
                if (depth == 0) { result.add(cur.toString()); cur = new StringBuilder(); }
            } else if (depth > 0) cur.append(c);
        }
        return result;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }

    private static String unescape(String s) {
        if (s == null) return null;
        return s.replace("\\\"","\"").replace("\\\\","\\").replace("\\n","\n");
    }
}
