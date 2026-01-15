package com.ikingsnipe.casino.utils;

import com.ikingsnipe.casino.models.CasinoConfig;
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

    public void sendGameResult(String player, boolean win, long bet, long payout, String result, String seed, CasinoConfig config) {
        if (!config.discordEnabled || url == null || url.isEmpty()) return;
        if (win && !config.discordNotifyWins) return;
        if (!win && !config.discordNotifyLosses) return;

        StringBuilder sb = new StringBuilder();
        sb.append(win ? "✅ **WINNER**" : "❌ **LOSS**").append("\\n");
        sb.append("**Player:** ").append(player).append("\\n");
        sb.append("**Bet:** ").append(formatGP(bet)).append("\\n");
        if (win) sb.append("**Payout:** ").append(formatGP(payout)).append("\\n");
        sb.append("**Result:** ").append(result).append("\\n");
        
        if (config.discordShowSeeds && seed != null) {
            sb.append("**Verification Seed:** `").append(seed).append("`\\n");
            sb.append("_Verify this roll in our Discord server using the seed pairing system._");
        }

        send(sb.toString());
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }
}
