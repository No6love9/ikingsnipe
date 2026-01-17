package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.managers.SecureDataManager;
import com.ikingsnipe.casino.models.UserModel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

public class DiscordFlowerPokerGame {
    private final long playerId;
    private final long bet;
    private final SecureDataManager dataManager;
    private final long xpReward;

    public DiscordFlowerPokerGame(long playerId, long bet, SecureDataManager dataManager) {
        this.playerId = playerId;
        this.bet = bet;
        this.dataManager = dataManager;
        this.xpReward = bet / 1_000_000L;
    }

    public MessageEditData start() {
        // Simplified Flower Poker logic for Java rewrite
        // In the Python version, this was a complex hand-ranking game.
        // Here, we simulate a 50/50 chance for simplicity.
        boolean win = ThreadLocalRandom.current().nextBoolean();
        long winnings = 0;
        String outcome;
        String reason = "Flower Poker game";

        if (win) {
            outcome = "YOU WIN";
            winnings = bet * 2;
            reason += " win";
        } else {
            outcome = "YOU LOSE";
            reason += " loss";
        }

        try {
            if (winnings > 0) {
                dataManager.updateBalance(playerId, winnings, reason);
            }
            addXp(playerId, xpReward);
        } catch (IllegalArgumentException e) {
            System.err.println("Error in Flower Poker endGame: " + e.getMessage());
        }

        return MessageEditData.fromEmbeds(createEmbed(outcome, winnings));
    }

    private MessageEmbed createEmbed(String outcome, long winnings) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌸 Flower Poker Result: " + outcome)
                .setDescription(outcome.equals("YOU WIN") ? String.format("You won %,d GP!", winnings) : "Better luck next time!")
                .setColor(outcome.equals("YOU WIN") ? Color.decode("#2ECC71") : Color.decode("#E74C3C"));

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
