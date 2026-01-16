package com.ikingsnipe.casino.games.impl;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import java.util.*;

public class BlackjackGame extends AbstractGame {
    
    @Override
    public GameResult play(String player, long bet, String seed) {
        List<Integer> playerHand = new ArrayList<>();
        List<Integer> hostHand = new ArrayList<>();
        
        // Initial deal
        playerHand.add(drawCard());
        playerHand.add(drawCard());
        hostHand.add(drawCard());
        hostHand.add(drawCard());
        
        int playerTotal = calculateTotal(playerHand);
        int hostTotal = calculateTotal(hostHand);
        
        // Player simulation (basic AI for automated casino)
        while (playerTotal < 16) {
            playerHand.add(drawCard());
            playerTotal = calculateTotal(playerHand);
        }
        
        // Host logic (Dealer hits on soft 17)
        while (hostTotal < 17) {
            hostHand.add(drawCard());
            hostTotal = calculateTotal(hostHand);
        }
        
        boolean win;
        String resultMsg;
        
        if (playerTotal > 21) {
            win = false;
            resultMsg = "Bust! (" + playerTotal + ")";
        } else if (hostTotal > 21) {
            win = true;
            resultMsg = "Host Busts! (" + hostTotal + ")";
        } else if (playerTotal > hostTotal) {
            win = true;
            resultMsg = playerTotal + " vs " + hostTotal;
        } else if (playerTotal == hostTotal) {
            win = false; // House wins ties
            resultMsg = "Tie (House Wins) " + playerTotal;
        } else {
            win = false;
            resultMsg = playerTotal + " vs " + hostTotal;
        }

        long payout = win ? calculatePayout(bet) : 0;

        return new GameResult(win, payout, resultMsg, playerTotal + ":" + hostTotal);
    }

    private int drawCard() {
        int card = random.nextInt(13) + 1;
        return Math.min(card, 10); // Face cards are 10
    }

    private int calculateTotal(List<Integer> hand) {
        int total = 0;
        int aces = 0;
        for (int card : hand) {
            if (card == 1) {
                aces++;
                total += 11;
            } else {
                total += card;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }
}
