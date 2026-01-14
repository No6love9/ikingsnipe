package com.ikingsnipe.casino.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowerPokerGame extends AbstractGame {
    private static final String[] FLOWERS = {"Red", "Blue", "Yellow", "Purple", "Orange", "White", "Black"};

    @Override
    public GameResult play(int betAmount, Map<String, Object> config) {
        List<String> playerHand = generateHand();
        List<String> hostHand = generateHand();
        
        int playerRank = getHandRank(playerHand);
        int hostRank = getHandRank(hostHand);
        
        boolean win = playerRank > hostRank;
        int payout = win ? betAmount * 2 : 0;
        
        return new GameResult(win, playerRank, payout);
    }

    private List<String> generateHand() {
        List<String> hand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            hand.add(FLOWERS[random.nextInt(FLOWERS.length)]);
        }
        return hand;
    }

    private int getHandRank(List<String> hand) {
        Map<String, Integer> counts = new HashMap<>();
        for (String flower : hand) {
            counts.put(flower, counts.getOrDefault(flower, 0) + 1);
        }
        List<Integer> values = new ArrayList<>(counts.values());
        Collections.sort(values, Collections.reverseOrder());
        
        if (values.get(0) == 5) return 6; // 5 OaK
        if (values.get(0) == 4) return 5; // 4 OaK
        if (values.get(0) == 3 && values.size() > 1 && values.get(1) == 2) return 4; // Full House
        if (values.get(0) == 3) return 3; // 3 OaK
        if (values.get(0) == 2 && values.size() > 1 && values.get(1) == 2) return 2; // 2 Pairs
        if (values.get(0) == 2) return 1; // 1 Pair
        return 0; // Bust
    }

    @Override
    public String getRules() {
        return "Plant 5 flowers. Higher rank wins. Payout 2x.";
    }

    @Override
    public boolean validateBet(int amount) {
        return amount >= 1000;
    }

    @Override
    public int calculatePayout(boolean win, int betAmount) {
        return win ? betAmount * 2 : 0;
    }
}
