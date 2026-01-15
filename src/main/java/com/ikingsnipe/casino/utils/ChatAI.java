package com.ikingsnipe.casino.utils;

import com.ikingsnipe.casino.managers.ProfitTracker;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.utilities.Sleep;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatAI {
    private final Map<String, String[]> responses = new HashMap<>();
    private final Random random = new Random();
    private ProfitTracker profitTracker;

    public ChatAI() {
        responses.put("legit", new String[]{"Always legit, check my history!", "100% fair, use !hash to verify", "Trusted host here."});
        responses.put("how to play", new String[]{"Trade me and type !c for Craps or !dw for Dice War.", "Just trade me to start, commands are in the welcome message."});
        responses.put("scam", new String[]{"No scams here, provably fair only.", "Check the hash, I'm legit."});
        responses.put("luck", new String[]{"Good luck!", "May the RNG be with you.", "Big wins coming?"});
        responses.put("payout", new String[]{"I've paid out over %s today!", "Total payouts so far: %s. Join the winners!"});
        responses.put("winner", new String[]{"Last winner was %s!", "Check the overlay for recent winners like %s."});
    }

    public void setProfitTracker(ProfitTracker tracker) {
        this.profitTracker = tracker;
    }

    public void handleChat(String message) {
        String lower = message.toLowerCase();
        for (String key : responses.keySet()) {
            if (lower.contains(key)) {
                String[] options = responses.get(key);
                String reply = options[random.nextInt(options.length)];
                
                // Dynamic Data Injection
                if (key.equals("payout") && profitTracker != null) {
                    reply = String.format(reply, formatGP(Math.abs(profitTracker.getNetProfit())));
                } else if (key.equals("winner") && profitTracker != null && !profitTracker.getRecentWinners().isEmpty()) {
                    reply = String.format(reply, profitTracker.getRecentWinners().get(0));
                }

                Sleep.sleep(1500, 3000); // Human-like delay
                Keyboard.type(reply, true);
                break;
            }
        }
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }
}
