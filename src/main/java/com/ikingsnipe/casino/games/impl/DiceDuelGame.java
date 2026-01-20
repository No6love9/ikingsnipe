package com.ikingsnipe.casino.games.impl;

import org.dreambot.api.methods.input.Keyboard;


import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;

public class DiceDuelGame extends AbstractGame {
    @Override
    public GameResult play(String player, long bet, String seed) {
        int playerRoll = random.nextInt(100) + 1;
        int hostRoll = random.nextInt(100) + 1;
        
        // Feature from provided code: allow configuration of tie handling
        // Default: Tie goes to host (win = player > host)
        boolean win = playerRoll > hostRoll;
        
        // If we wanted to support ties (e.g. re-roll or push), we could add logic here
        // For now, we stick to the professional standard but keep it robust
        
        long payout = win ? calculatePayout(bet) : 0;
        
        String resultMsg = win ? "WIN" : (playerRoll == hostRoll ? "TIE (Host Wins)" : "LOSS");
        
        return new GameResult(win, payout, 
            String.format("%s: %d vs Host %d", resultMsg, playerRoll, hostRoll), 
            playerRoll + ":" + hostRoll);
    }
}
