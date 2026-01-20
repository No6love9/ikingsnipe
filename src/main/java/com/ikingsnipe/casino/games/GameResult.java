package com.ikingsnipe.casino.games;

import org.dreambot.api.methods.input.Keyboard;


public class GameResult {
    private final boolean win;
    private final long payout;
    private final String description;
    private final String rawOutcome;

    public GameResult(boolean win, long payout, String description, String rawOutcome) {
        this.win = win; this.payout = payout; this.description = description; this.rawOutcome = rawOutcome;
    }

    public boolean isWin() { return win; }
    public long getPayout() { return payout; }
    public String getDescription() { return description; }
    public String getRawOutcome() { return rawOutcome; }
    public String getRoll() { return rawOutcome; }
}
