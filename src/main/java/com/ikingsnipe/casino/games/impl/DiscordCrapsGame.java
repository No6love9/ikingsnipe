package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.managers.SecureDataManager;
import com.ikingsnipe.casino.models.UserModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public class DiscordCrapsGame {
    private final long playerId;
    private final long bet;
    private final SecureDataManager dataManager;
    private final long xpReward;

    public DiscordCrapsGame(long playerId, long bet, SecureDataManager dataManager) {
        this.playerId = playerId;
        this.bet = bet;
        this.dataManager = dataManager;
        this.xpReward = bet / 1_000_000L;
    }

    public MessageEditData start() {
        int d1 = ThreadLocalRandom.current().nextInt(6) + 1;
        int d2 = ThreadLocalRandom.current().nextInt(6) + 1;
        int total = d1 + d2;

        String outcome;
        long winnings = 0;
        String reason = "Craps game";
        MessageEditData response;

        if (total == 7 || total == 11) {
            outcome = "WIN";
            winnings = bet * 2;
            reason += " win";
            response = MessageEditData.fromEmbeds(createEmbed(d1, d2, outcome, String.format("You rolled %d and won %,d GP!", total, winnings)));
        } else if (total == 2 || total == 3 || total == 12) {
            outcome = "LOSS";
            reason += " loss";
            response = MessageEditData.fromEmbeds(createEmbed(d1, d2, outcome, String.format("You rolled %d. Craps! You lose.", total)));
        } else {
            outcome = "POINT";
            // For simplicity, we end the game here in the Java rewrite, as interactive Craps is complex
            // In a full implementation, this would start a new phase with a point to hit.
            response = MessageEditData.fromEmbeds(createEmbed(d1, d2, outcome, String.format("You rolled %d. Point is %d. Game over for now.", total, total)));
        }

        try {
            if (winnings > 0) {
                dataManager.updateBalance(playerId, winnings, reason);
            }
            addXp(playerId, xpReward);
        } catch (IllegalArgumentException e) {
            System.err.println("Error in Craps endGame: " + e.getMessage());
        }
        
        return response;
    }

    private MessageEmbed createEmbed(int d1, int d2, String outcome, String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎲 Craps Game Result: " + outcome)
                .setDescription(message)
                .setColor(outcome.equals("WIN") ? Color.decode("#2ECC71") : Color.decode("#E74C3C")); // SUCCESS_GREEN or ERROR_RED

        embed.addField("Dice Roll", String.format("%d + %d = %d", d1, d2, d1 + d2), false);
        embed.setFooter(String.format("Bet: %,d GP | XP Reward: %,d", bet, xpReward));
        return embed.build();
    }

    private void addXp(long userId, long xpAmount) {
        UserModel user = dataManager.getUserData(userId);
        user.setXp(user.getXp() + xpAmount);

        int newLevel = (int) Math.floor(Math.sqrt(user.getXp() / 1000.0)) + 1;

        if (newLevel > user.getLevel()) {
            int oldLevel = user.getLevel();
            user.setLevel(newLevel);
            long levelReward = (long) newLevel * 10_000_000L;
            try {
                dataManager.updateBalance(userId, levelReward, String.format("Level Up Reward to Level %d", newLevel));
                System.out.printf("User %d leveled up from %d to %d. Rewarded %,d GP.%n", userId, oldLevel, newLevel, levelReward);
            } catch (IllegalArgumentException e) {
                System.err.println("Error rewarding level up: " + e.getMessage());
            }
        }
        dataManager.saveUserCache();
    }
}
