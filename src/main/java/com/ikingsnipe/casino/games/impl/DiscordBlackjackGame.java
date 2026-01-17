package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.managers.SecureDataManager;
import com.ikingsnipe.casino.models.UserModel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DiscordBlackjackGame {

    private final long playerId;
    private final long bet;
    private final SecureDataManager dataManager;
    private final List<Map<String, String>> deck;
    private final List<Map<String, String>> playerHand;
    private final List<Map<String, String>> dealerHand;
    private final long xpReward;

    public DiscordBlackjackGame(long playerId, long bet, SecureDataManager dataManager) {
        this.playerId = playerId;
        this.bet = bet;
        this.dataManager = dataManager;
        this.deck = createDeck();
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.xpReward = bet / 1_000_000L; // 1 XP per 1M GP bet
    }

    private List<Map<String, String>> createDeck() {
        String[] suits = {"H", "D", "C", "S"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        List<Map<String, String>> newDeck = new ArrayList<>();
        for (String suit : suits) {
            for (String rank : ranks) {
                Map<String, String> card = new HashMap<>();
                card.put("rank", rank);
                card.put("suit", suit);
                newDeck.add(card);
            }
        }
        Collections.shuffle(newDeck, ThreadLocalRandom.current());
        return newDeck;
    }

    private void dealCard(List<Map<String, String>> hand) {
        if (deck.isEmpty()) {
            // Reshuffle if needed
            deck.addAll(createDeck());
        }
        hand.add(deck.remove(0));
    }

    private int getHandValue(List<Map<String, String>> hand) {
        int value = 0;
        int numAces = 0;
        for (Map<String, String> card : hand) {
            String rank = card.get("rank");
            if (rank.matches("10|[JQK]")) {
                value += 10;
            } else if (rank.equals("A")) {
                numAces++;
                value += 11;
            } else {
                try {
                    value += Integer.parseInt(rank);
                } catch (NumberFormatException e) {
                    // Should not happen
                }
            }
        }

        while (value > 21 && numAces > 0) {
            value -= 10;
            numAces--;
        }
        return value;
    }

    private String getHandDisplay(List<Map<String, String>> hand, boolean hideDealerCard) {
        List<String> display = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hideDealerCard && i == 0) {
                display.add("??");
            } else {
                display.add(hand.get(i).get("rank") + hand.get(i).get("suit"));
            }
        }
        return String.join(", ", display);
    }

    public MessageEmbed createEmbed(String title, String description, boolean hideDealerCard) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.decode("#3498DB")); // GOAT_BLUE

        int playerValue = getHandValue(playerHand);
        embed.addField("Your Hand", String.format("Cards: %s\nValue: %d", getHandDisplay(playerHand, false), playerValue), false);

        String dealerDisplay = getHandDisplay(dealerHand, hideDealerCard);
        String dealerValue = hideDealerCard ? "?" : String.valueOf(getHandValue(dealerHand));

        embed.addField("Dealer's Hand", String.format("Cards: %s\nValue: %s", dealerDisplay, dealerValue), false);
        embed.setFooter(String.format("Bet: %,d GP | XP Reward: %,d", bet, xpReward));
        return embed.build();
    }

    public MessageEditData start() {
        dealCard(playerHand);
        dealCard(dealerHand);
        dealCard(playerHand);
        dealCard(dealerHand);

        if (getHandValue(playerHand) == 21) {
            // Immediate Blackjack
            long winnings = (long) (bet * 2.5);
            endGame("Blackjack! You win 1.5x your bet.", winnings, "Blackjack win");
            return MessageEditData.fromEmbeds(createEmbed("Blackjack! Game Over.", String.format("You won %,d GP.", winnings), false));
        }

        return MessageEditData.newBuilder()
                .setEmbeds(createEmbed("Blackjack Game Started", "Hit or Stand?", true))
                .setComponents(ActionRow.of(
                        Button.primary("blackjack_hit_" + playerId, "Hit"),
                        Button.success("blackjack_stand_" + playerId, "Stand")
                ))
                .build();
    }

    public MessageEditData hit() {
        dealCard(playerHand);
        int playerValue = getHandValue(playerHand);

        if (playerValue > 21) {
            endGame("Bust! You lose.", 0, "Blackjack loss");
            return MessageEditData.fromEmbeds(createEmbed("Bust! Game Over.", "You went over 21.", false));
        }

        return MessageEditData.newBuilder()
                .setEmbeds(createEmbed("Hit!", "Hit or Stand?", true))
                .setComponents(ActionRow.of(
                        Button.primary("blackjack_hit_" + playerId, "Hit"),
                        Button.success("blackjack_stand_" + playerId, "Stand")
                ))
                .build();
    }

    public MessageEditData stand() {
        int dealerValue = getHandValue(dealerHand);

        // Dealer hits on 16 or less
        while (dealerValue < 17) {
            dealCard(dealerHand);
            dealerValue = getHandValue(dealerHand);
        }

        int playerValue = getHandValue(playerHand);
        String outcome;
        long winnings;
        String reason;

        if (dealerValue > 21) {
            outcome = "Dealer Bust! You win.";
            winnings = bet * 2;
            reason = "Blackjack win";
        } else if (dealerValue > playerValue) {
            outcome = "Dealer wins.";
            winnings = 0;
            reason = "Blackjack loss";
        } else if (playerValue > dealerValue) {
            outcome = "You win!";
            winnings = bet * 2;
            reason = "Blackjack win";
        } else {
            outcome = "Push.";
            winnings = bet; // Refund bet
            reason = "Blackjack push";
        }

        endGame(outcome, winnings, reason);
        return MessageEditData.fromEmbeds(createEmbed(String.format("Game Over: %s", outcome), String.format("You won %,d GP.", winnings), false));
    }

    private void endGame(String outcome, long winnings, String reason) {
        try {
            if (winnings > 0) {
                dataManager.updateBalance(playerId, winnings, reason);
            }
            addXp(playerId, xpReward);
        } catch (IllegalArgumentException e) {
            System.err.println("Error in Blackjack endGame: " + e.getMessage());
        }
    }

    private void addXp(long userId, long xpAmount) {
        UserModel user = dataManager.getUserData(userId);
        user.setXp(user.getXp() + xpAmount);

        // Simple Leveling System: Level = floor(sqrt(XP / 1000)) + 1
        int newLevel = (int) Math.floor(Math.sqrt(user.getXp() / 1000.0)) + 1;

        if (newLevel > user.getLevel()) {
            int oldLevel = user.getLevel();
            user.setLevel(newLevel);
            // Reward for leveling up
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
