package com.ikingsnipe.casino.utils;

import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.utilities.Sleep;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatAI {
    private final Map<String, String[]> responses = new HashMap<>();
    private final Random random = new Random();

    public ChatAI() {
        responses.put("legit", new String[]{"Always legit, check my history!", "100% fair, use !hash to verify", "Trusted host here."});
        responses.put("how to play", new String[]{"Trade me and type !c for Craps or !dw for Dice War.", "Just trade me to start, commands are in the welcome message."});
        responses.put("scam", new String[]{"No scams here, provably fair only.", "Check the hash, I'm legit."});
        responses.put("luck", new String[]{"Good luck!", "May the RNG be with you.", "Big wins coming?"});
    }

    public void handleChat(String message) {
        String lower = message.toLowerCase();
        for (String key : responses.keySet()) {
            if (lower.contains(key)) {
                String[] options = responses.get(key);
                String reply = options[random.nextInt(options.length)];
                Sleep.sleep(1500, 3000); // Human-like delay
                Keyboard.type(reply, true);
                break;
            }
        }
    }
}
