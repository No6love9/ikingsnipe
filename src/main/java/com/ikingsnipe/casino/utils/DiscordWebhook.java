package com.ikingsnipe.casino.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    private final String url;

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void send(String content) {
        if (url == null || url.isEmpty()) return;
        
        new Thread(() -> {
            try {
                URL webhookUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) webhookUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String json = "{\"content\": \"" + content.replace("\"", "\\\"") + "\"}";
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                // Silently fail to avoid script interruption
            }
        }).start();
    }
}
