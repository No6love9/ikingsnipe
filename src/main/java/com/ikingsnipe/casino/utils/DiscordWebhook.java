package com.ikingsnipe.casino.utils;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.utilities.Logger;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * SnipesScripts Enterprise Discord Integration v9.0
 * Features:
 * - Rich Embeds with dynamic colors
 * - Win/Loss specific styling
 * - Balance and Provably Fair data inclusion
 */
public class DiscordWebhook {
    private final String url;

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void send(String content) {
        if (url == null || url.isEmpty()) return;
        sendRaw("{\"content\": \"" + content.replace("\"", "\\\"") + "\"}");
    }

    public void sendGameResult(String player, String gameType, com.ikingsnipe.casino.games.GameResult result, String seed) {
        sendGameResult(player, result.isWin(), result.getPayout(), result.getPayout(), result.getDescription(), seed, 0, new CasinoConfig());
    }

    public void sendGameResult(String player, boolean win, long bet, long payout, String desc, String seed, long balance, CasinoConfig config) {
        if (!config.discordEnabled || url == null || url.isEmpty()) return;
        
        String color = win ? "3066993" : "15158332"; // Green vs Red
        String title = win ? "🏆 BIG WIN ALERT" : "💀 LOSS REPORT";
        String emoji = win ? "💰" : "📉";

        String json = "{"
            + "\"embeds\": [{"
            + "\"title\": \"" + emoji + " " + title + "\","
            + "\"color\": " + color + ","
            + "\"fields\": ["
            + "{\"name\": \"Player\", \"value\": \"" + player + "\", \"inline\": true},"
            + "{\"name\": \"Bet\", \"value\": \"" + formatGP(bet) + "\", \"inline\": true},"
            + "{\"name\": \"Result\", \"value\": \"" + desc + "\", \"inline\": false},"
            + "{\"name\": \"Payout\", \"value\": \"" + formatGP(payout) + "\", \"inline\": true},"
            + "{\"name\": \"New Balance\", \"value\": \"" + formatGP(balance) + "\", \"inline\": true},"
            + "{\"name\": \"Server Seed\", \"value\": \"`" + seed + "`\", \"inline\": false}"
            + "],"
            + "\"footer\": {\"text\": \"SnipesScripts Enterprise v9.0 | 2026 Grade\"},"
            + "\"timestamp\": \"" + java.time.Instant.now().toString() + "\""
            + "}]"
            + "}";
        
        sendRaw(json);
    }

    private void sendRaw(String json) {
        new Thread(() -> {
            try {
                URL webhookUrl = new URL(url);
                HttpsURLConnection conn = (HttpsURLConnection) webhookUrl.openConnection();
                conn.addRequestProperty("Content-Type", "application/json");
                conn.addRequestProperty("User-Agent", "SnipesScripts-Enterprise");
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                Logger.error("[Discord] Failed to send webhook: " + e.getMessage());
            }
        }).start();
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a) + " GP";
    }
}
