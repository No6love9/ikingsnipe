# snipes♧scripts Casino: Chasing Edition

The **Chasing Edition** is a specialized update focusing on the "Chasing Craps" game mode, robust banking, and chat-command-driven gameplay.

## 🎲 Chasing Craps (!c / !craps)

This is a simplified, high-stakes version of Craps with the following rules:
- **Winning Numbers**: 7, 9, and 12.
- **Standard Payout**: x3 the bet.
- **!b2b (Back-to-Back)**:
  - If a player wins a roll, they can immediately bet on a second roll.
  - **B2B Win**: x9 the payout.
  - **Predicted B2B Win**: If they call the number (e.g., `!b2b 7`) and hit it, the payout is **x12**.

## 💬 Chat Commands

The bot now listens for commands in the **Trade Window** to determine the game and bet:
- `!c [amount]` or `!craps [amount]`: Sets game to Chasing Craps.
- `!b2b [number] [amount]`: Sets game to B2B Craps with optional prediction.
- `!fp [amount]`: Sets game to Flower Poker.

## 💰 Balance & Currency Handling

- **Smart Betting**: The bot checks the player's session balance first. If they have enough from previous wins, it uses that balance.
- **Physical Fallback**: If no balance exists, the bot verifies GP or Platinum Tokens in the trade window.
- **Anti-Scam**: Trade values are verified in both windows. If a player changes the amount in Window 2, the bot declines immediately.

## 🏦 Robust Banking

- **Loop Fix**: The banking logic has been rewritten to ensure the bot deposits all items, withdraws necessary stock, and correctly closes the bank before returning to the IDLE state.
- **Thresholds**: Configurable in the GUI to ensure you never run out of payout stock.

## ⏱ Trade Timeouts

- **30s Threshold**: To prevent stalling, the bot will automatically decline any trade that stays open for more than 30 seconds without completion.

---
*Renamed to **snipes♧scripts** as requested. Happy hosting!*
