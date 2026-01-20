package com.ikingsnipe.casino.managers;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;

/**
 * Enterprise Balance Manager for GoatGang Edition
 * Handles Gold Coins and Platinum Tokens with unified balance logic.
 */
public class BalanceManager {
    private static final int COINS_ID = 995;
    private static final int PLATINUM_TOKEN_ID = 13204;
    private static final long TOKEN_VALUE = 1000L;

    /**
     * Gets the total balance in GP (Coins + Tokens * 1000)
     */
    public static long getTotalInventoryBalance() {
        long coins = Inventory.count(COINS_ID);
        long tokens = Inventory.count(PLATINUM_TOKEN_ID);
        return coins + (tokens * TOKEN_VALUE);
    }

    /**
     * Formats GP into a readable string (e.g., 1.5M, 2.3B)
     */
    public static String formatGP(long amount) {
        if (amount >= 1_000_000_000L) return String.format("%.2fB", amount / 1_000_000_000.0);
        if (amount >= 1_000_000L) return String.format("%.2fM", amount / 1_000_000.0);
        if (amount >= 1_000L) return String.format("%.1fK", amount / 1_000.0);
        return String.valueOf(amount);
    }

    /**
     * Validates if the bot has enough liquidity for a payout
     */
    public static boolean hasLiquidity(long amount) {
        return getTotalInventoryBalance() >= amount;
    }
}
