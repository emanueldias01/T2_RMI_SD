package br.com.sd.client;


import javax.swing.*;

public class PixelHubClient {
    public static void main(String[] args) {
        System.out.println("oi");

        JFrame frame = new JFrame();
        startScreen(frame);
    }

    private static void startScreen(JFrame f) {
        f.setTitle("PixelHub");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(500, 500);
        f.setVisible(true);
    }
}
