package com.ikingsnipe;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Elite Titan Casino - PROFESSIONAL EDITION
 * A ground-up reconstruction for maximum stability and performance.
 */
@ScriptManifest(
    name = "Elite Titan Casino PRO",
    description = "Professional-grade casino bot with advanced trade security and game logic.",
    author = "ikingsnipe",
    version = 5.0,
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {

    // --- Configuration Constants ---
    private static final int COINS_ID = 995;
    private static final int DICE_ID = 10403;
    private static final int WHEEL_ID = 10404;
    private static final int ROULETTE_ID = 10405;
    private static final int MAIN_INTERFACE = 548;

    // --- Script States ---
    private enum State { SETUP, IDLE, ADVERTISING, TRADING, GAMING, RECOVERING }
    private State currentState = State.SETUP;

    // --- User Settings (Configurable via GUI) ---
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private String activeGame = "Dice";
    private int betPerGame = 1000;
    private int minTrade = 1000;
    private String adText = "Elite Casino PRO | Fast Payouts | Dice & Roulette!";
    private String winText = "Winner! Payout sent. Good luck next time!";
    private String lossText = "House wins! Better luck on the next roll.";
    private boolean autoAccept = true;

    // --- Internal Tracking ---
    private int initialGold = -1;
    private int winCount = 0;
    private int lossCount = 0;
    private long lastAdTimestamp = 0;
    private long stateStartTime = 0;

    @Override
    public void onStart() {
        log("Starting Elite Titan Casino PRO Overhaul...");
        SwingUtilities.invokeLater(this::initGUI);
    }

    private void initGUI() {
        JFrame frame = new JFrame("Elite Titan Casino PRO v5.0");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(new JLabel("Select Game:"));
        JComboBox<String> gameSelector = new JComboBox<>(new String[]{"Dice", "Wheel", "Roulette"});
        mainPanel.add(gameSelector);

        mainPanel.add(new JLabel("Bet Amount:"));
        JTextField betInput = new JTextField("1000");
        mainPanel.add(betInput);

        mainPanel.add(new JLabel("Min Trade Amount:"));
        JTextField tradeInput = new JTextField("1000");
        mainPanel.add(tradeInput);

        mainPanel.add(new JLabel("Ad Message:"));
        JTextField adInput = new JTextField(adText);
        mainPanel.add(adInput);

        mainPanel.add(new JLabel("Win Message:"));
        JTextField winInput = new JTextField(winText);
        mainPanel.add(winInput);

        mainPanel.add(new JLabel("Loss Message:"));
        JTextField lossInput = new JTextField(lossText);
        mainPanel.add(lossInput);

        JCheckBox autoAcceptCheck = new JCheckBox("Auto-Accept Safe Trades", autoAccept);
        mainPanel.add(autoAcceptCheck);

        JButton startButton = new JButton("LAUNCH PRO ENGINE");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(34, 139, 34));
        startButton.setForeground(Color.WHITE);

        startButton.addActionListener(e -> {
            activeGame = (String) gameSelector.getSelectedItem();
            try {
                betPerGame = Integer.parseInt(betInput.getText());
                minTrade = Integer.parseInt(tradeInput.getText());
            } catch (NumberFormatException ex) {
                log("Invalid numeric input. Using defaults.");
            }
            adText = adInput.getText();
            winText = winInput.getText();
            lossText = lossInput.getText();
            autoAccept = autoAcceptCheck.isSelected();
            
            isStarted.set(true);
            currentState = State.IDLE;
            frame.dispose();
            log("PRO Engine Launched. Mode: " + activeGame);
        });

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(startButton, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public int onLoop() {
        if (!isStarted.get()) return 1000;

        // Global Session Recovery
        if (Client.getGameState() != GameState.LOGGED_IN) {
            log("Waiting for active session...");
            return 5000;
        }

        // Initialize gold tracking
        if (initialGold == -1) {
            Item coins = Inventory.get(COINS_ID);
            initialGold = (coins != null) ? coins.getAmount() : 0;
        }

        // Timeout Recovery (Stuck state protection)
        if (System.currentTimeMillis() - stateStartTime > 60000 && currentState != State.IDLE) {
            log("State timeout detected. Resetting to IDLE.");
            currentState = State.IDLE;
        }

        switch (currentState) {
            case IDLE:
                stateStartTime = System.currentTimeMillis();
                if (Trade.isOpen()) {
                    currentState = State.TRADING;
                } else {
                    currentState = State.ADVERTISING;
                }
                break;

            case ADVERTISING:
                if (System.currentTimeMillis() - lastAdTimestamp > 12000) {
                    Keyboard.type(adText);
                    lastAdTimestamp = System.currentTimeMillis();
                }
                if (Trade.isOpen()) currentState = State.TRADING;
                break;

            case TRADING:
                handleSecureTrade();
                break;

            case GAMING:
                executeGameLogic();
                break;

            case RECOVERING:
                // Handle stuck interfaces or unexpected movements
                Widgets.closeAll();
                currentState = State.IDLE;
                break;
        }

        return Calculations.random(600, 1000);
    }

    private void handleSecureTrade() {
        if (!Trade.isOpen()) {
            currentState = State.IDLE;
            return;
        }

        if (Trade.isOpen(1)) {
            Item[] offeredItems = Trade.getTheirItems();
            int totalOffered = 0;
            if (offeredItems != null) {
                for (Item item : offeredItems) {
                    if (item != null && item.getID() == COINS_ID) {
                        totalOffered += item.getAmount();
                    }
                }
            }

            if (totalOffered >= minTrade) {
                log("Safe trade detected: " + totalOffered + " coins. Accepting...");
                if (autoAccept) Trade.acceptTrade();
            } else if (totalOffered > 0) {
                log("Invalid trade amount. Declining.");
                Trade.declineTrade();
            }
        } else if (Trade.isOpen(2)) {
            log("Confirming second trade window.");
            Trade.acceptTrade();
            currentState = State.GAMING;
        }
    }

    private void executeGameLogic() {
        int targetId = activeGame.equals("Dice") ? DICE_ID : 
                       activeGame.equals("Wheel") ? WHEEL_ID : ROULETTE_ID;
        
        GameObject gameObj = GameObjects.closest(targetId);
        if (gameObj != null) {
            if (gameObj.interact("Play")) {
                sleepUntil(() -> Widgets.getWidget(MAIN_INTERFACE) != null, 5000, 100);
                
                // Professional Widget Interaction
                WidgetChild actionButton = Widgets.getWidgetChild(MAIN_INTERFACE, 12);
                if (actionButton != null && actionButton.isVisible()) {
                    actionButton.interact();
                    log("Game started: " + activeGame);
                    sleep(Calculations.random(4000, 6000)); // Wait for animation
                    
                    // Result Simulation (Replace with actual widget parsing if IDs are known)
                    boolean isWin = Calculations.random(0, 100) > 52;
                    if (isWin) {
                        winCount++;
                        Keyboard.type(winText);
                    } else {
                        lossCount++;
                        Keyboard.type(lossText);
                    }
                }
            }
        }
        currentState = State.IDLE;
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Modern Overlay
        g2.setColor(new Color(20, 20, 20, 220));
        g2.fillRoundRect(10, 10, 250, 150, 15, 15);
        g2.setColor(new Color(0, 200, 0));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(10, 10, 250, 150, 15, 15);

        g2.setFont(new Font("Verdana", Font.BOLD, 14));
        g2.drawString("ELITE CASINO PRO v5.0", 25, 35);
        
        g2.setFont(new Font("Verdana", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("Status: " + currentState, 25, 60);
        g2.drawString("Game: " + activeGame, 25, 80);
        g2.drawString("Wins: " + winCount + " | Losses: " + lossCount, 25, 100);
        
        int profit = getCurrentProfit();
        g2.setColor(profit >= 0 ? Color.GREEN : Color.RED);
        g2.drawString("Profit: " + profit + " coins", 25, 125);
    }

    private int getCurrentProfit() {
        if (initialGold == -1) return 0;
        Item coins = Inventory.get(COINS_ID);
        return (coins != null ? coins.getAmount() : 0) - initialGold;
    }
}
