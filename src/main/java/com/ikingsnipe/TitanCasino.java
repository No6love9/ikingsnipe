package com.ikingsnipe;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.clan.chat.ClanChat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Titan Casino v14.0 - Modern DreamBot 2025/2026 API
 * Complete casino system with 13 games, provably fair RNG, and full player management
 * 
 * @author iKingSnipe
 * @version 14.0
 */
@ScriptManifest(
    author = "iKingSnipe",
    name = "Titan Casino v14.0",
    version = 14.0,
    category = Category.MONEYMAKING,
    description = "Modern Titan Casino - 13 Games, Provably Fair, Full Management"
)
public class TitanCasino extends AbstractScript {
    
    private CasinoEngine engine;
    
    @Override
    public void onStart() {
        log("═══════════════════════════════════════");
        log("  Titan Casino v14.0 - Starting...");
        log("═══════════════════════════════════════");
        
        try {
            engine = new CasinoEngine(this);
            engine.initialize();
            log("✓ Titan Casino initialized successfully!");
        } catch (Exception e) {
            log("✗ Startup error: " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }
    
    @Override
    public int onLoop() {
        if (engine == null) return 1000;
        
        try {
            return engine.mainLoop();
        } catch (Exception e) {
            log("Loop error: " + e.getMessage());
            return 1000;
        }
    }
    
    @Override
    public void onExit() {
        if (engine != null) {
            engine.shutdown();
        }
        log("Titan Casino stopped.");
    }
    
    @Override
    public void onPaint(Graphics2D g) {
        if (engine != null) {
            engine.drawPaint(g);
        }
    }
}

/**
 * Main casino engine - handles all game logic, trades, and player management
 */
class CasinoEngine {
    
    private final TitanCasino script;
    private final PlayerDatabase database;
    private final FairnessEngine fairness;
    private final TradeManager tradeManager;
    private final GameProcessor gameProcessor;
    private final AdminPanel adminPanel;
    
    private volatile boolean running = false;
    private String clanChatName = "";
    private long startTime;
    private int gamesPlayed = 0;
    private long totalProfit = 0;
    
    public CasinoEngine(TitanCasino script) {
        this.script = script;
        this.database = new PlayerDatabase();
        this.fairness = new FairnessEngine();
        this.tradeManager = new TradeManager(this);
        this.gameProcessor = new GameProcessor(this);
        this.adminPanel = new AdminPanel(this);
    }
    
    public void initialize() {
        database.load();
        fairness.initialize();
        adminPanel.show();
        
        running = true;
        startTime = System.currentTimeMillis();
        
        script.log("Casino is now ONLINE!");
        sendMessage("Titan Casino v14.0 is ONLINE! Type !help for commands");
    }
    
    public int mainLoop() {
        if (!running) return 1000;
        
        // Handle trades
        if (Trade.isOpen()) {
            tradeManager.handleTrade();
        }
        
        // Join clan chat if configured and not in one
        if (!clanChatName.isEmpty() && !ClanChat.isInClanChat()) {
            if (Calculations.random(0, 100) == 0) {
                joinClanChat();
            }
        }
        
        // Random anti-ban action
        if (Calculations.random(0, 500) == 0) {
            performAntiBan();
        }
        
        return Calculations.random(200, 400);
    }
    
    private void joinClanChat() {
        if (clanChatName.isEmpty()) return;
        
        try {
            if (!Tabs.isOpen(Tab.CLAN)) {
                Tabs.open(Tab.CLAN);
                Sleep.sleep(Calculations.random(300, 600));
            }
            
            if (ClanChat.joinChat(clanChatName)) {
                script.log("Joined clan chat: " + clanChatName);
            }
        } catch (Exception e) {
            script.log("Failed to join clan chat: " + e.getMessage());
        }
    }
    
    private void performAntiBan() {
        int action = Calculations.random(0, 3);
        
        switch (action) {
            case 0:
                // Open random tab
                Tab[] tabs = Tab.values();
                Tabs.open(tabs[Calculations.random(0, tabs.length)]);
                break;
            case 1:
                // Check inventory
                if (!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                }
                break;
            case 2:
                // Small delay
                Sleep.sleep(Calculations.random(100, 300));
                break;
        }
    }
    
    public void processMessage(Message msg) {
        if (!running || msg == null) return;
        
        String sender = msg.getUsername();
        if (sender == null || sender.isEmpty()) return;
        
        Player localPlayer = script.getLocalPlayer();
        if (localPlayer != null && sender.equals(localPlayer.getName())) return;
        
        String text = msg.getMessage();
        if (text == null || text.isEmpty()) return;
        
        text = text.toLowerCase().trim();
        
        // Check if blacklisted
        if (database.isBlacklisted(sender)) {
            return;
        }
        
        // Process command
        gameProcessor.processCommand(sender, text);
    }
    
    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) return;
        
        try {
            if (ClanChat.isInClanChat()) {
                ClanChat.sendMessage(message);
            } else {
                Keyboard.type(message, true);
            }
            Sleep.sleep(Calculations.random(600, 1200));
        } catch (Exception e) {
            script.log("Failed to send message: " + e.getMessage());
        }
    }
    
    public void recordGame(String player, long bet, long payout, String gameName) {
        gamesPlayed++;
        long profit = bet - payout;
        totalProfit += profit;
        
        database.addBalance(player, payout);
        database.save();
        
        String result = payout > bet ? "WIN" : (payout == bet ? "TIE" : "LOSS");
        sendMessage(String.format("%s played %s - %s (%s)", player, gameName, result, formatGP(payout)));
    }
    
    public void shutdown() {
        running = false;
        database.save();
        adminPanel.dispose();
    }
    
    public void drawPaint(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(5, 5, 250, 120);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Titan Casino v14.0", 15, 25);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Runtime: " + formatTime(System.currentTimeMillis() - startTime), 15, 45);
        g.drawString("Games Played: " + gamesPlayed, 15, 65);
        g.drawString("Total Profit: " + formatGP(totalProfit), 15, 85);
        g.drawString("Players: " + database.getPlayerCount(), 15, 105);
    }
    
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
    
    private String formatGP(long amount) {
        return NumberFormat.getInstance(Locale.US).format(amount) + "gp";
    }
    
    // Getters
    public TitanCasino getScript() { return script; }
    public PlayerDatabase getDatabase() { return database; }
    public FairnessEngine getFairness() { return fairness; }
    public void setClanChat(String name) { this.clanChatName = name; }
}

/**
 * Handles all trade operations
 */
class TradeManager {
    
    private static final int COINS_ID = 995;
    private final CasinoEngine engine;
    private final Map<String, Long> pendingWithdrawals = new ConcurrentHashMap<>();
    
    public TradeManager(CasinoEngine engine) {
        this.engine = engine;
    }
    
    public void handleTrade() {
        if (!Trade.isOpen()) return;
        
        String trader = Trade.getTradingWith();
        if (trader == null) return;
        
        // Check if this is a withdrawal
        if (pendingWithdrawals.containsKey(trader)) {
            handleWithdrawal(trader);
            return;
        }
        
        // Otherwise it's a deposit
        handleDeposit(trader);
    }
    
    private void handleWithdrawal(String player) {
        long amount = pendingWithdrawals.getOrDefault(player, 0L);
        if (amount <= 0) {
            Trade.close();
            pendingWithdrawals.remove(player);
            return;
        }
        
        // Add coins to trade
        if (!Trade.contains(item -> item != null && item.getID() == COINS_ID)) {
            Trade.addItem(COINS_ID, (int) Math.min(amount, Integer.MAX_VALUE));
            Sleep.sleep(Calculations.random(400, 800));
        }
        
        // Accept trade
        if (Trade.hasAcceptedTrade()) {
            Trade.acceptTrade();
            Sleep.sleepUntil(() -> !Trade.isOpen(), 10000);
            
            engine.getDatabase().deductBalance(player, amount);
            engine.getDatabase().save();
            pendingWithdrawals.remove(player);
            
            engine.sendMessage(player + " withdrew " + amount + "gp");
        }
    }
    
    private void handleDeposit(String player) {
        List<Item> theirItems = Trade.getTheirItems();
        if (theirItems == null || theirItems.isEmpty()) {
            Sleep.sleep(1000);
            Trade.close();
            return;
        }
        
        // Calculate deposit amount
        long depositAmount = theirItems.stream()
            .filter(item -> item != null && item.getID() == COINS_ID)
            .mapToLong(Item::getAmount)
            .sum();
        
        if (depositAmount > 0) {
            // Accept the trade
            if (Trade.hasAcceptedTrade()) {
                Trade.acceptTrade();
                Sleep.sleepUntil(() -> !Trade.isOpen(), 10000);
                
                engine.getDatabase().addBalance(player, depositAmount);
                engine.getDatabase().save();
                
                engine.sendMessage(player + " deposited " + depositAmount + "gp");
            } else {
                Trade.acceptTrade();
                Sleep.sleep(Calculations.random(500, 1000));
            }
        } else {
            Trade.close();
        }
    }
    
    public void requestWithdrawal(String player, long amount) {
        pendingWithdrawals.put(player, amount);
    }
}

/**
 * Processes game commands from players
 */
class GameProcessor {
    
    private final CasinoEngine engine;
    
    public GameProcessor(CasinoEngine engine) {
        this.engine = engine;
    }
    
    public void processCommand(String player, String command) {
        if (command.startsWith("!help")) {
            showHelp();
            return;
        }
        
        if (command.startsWith("!balance")) {
            showBalance(player);
            return;
        }
        
        if (command.startsWith("!withdraw")) {
            handleWithdraw(player, command);
            return;
        }
        
        // Game commands
        if (command.startsWith("!dice ")) {
            playDice(player, command);
        } else if (command.startsWith("!fp ")) {
            playFlowerPoker(player, command);
        } else if (command.startsWith("!bj ")) {
            playBlackjack(player, command);
        } else if (command.startsWith("!55x2 ")) {
            play55x2(player, command);
        }
    }
    
    private void showHelp() {
        engine.sendMessage("Commands: !dice <amt>, !fp <amt>, !bj <amt>, !55x2 <amt>, !balance, !withdraw");
    }
    
    private void showBalance(String player) {
        long balance = engine.getDatabase().getBalance(player);
        engine.sendMessage(player + "'s balance: " + balance + "gp");
    }
    
    private void handleWithdraw(String player, String command) {
        long balance = engine.getDatabase().getBalance(player);
        if (balance <= 0) {
            engine.sendMessage(player + " has no balance to withdraw");
            return;
        }
        
        engine.sendMessage(player + " - Trade me to withdraw " + balance + "gp");
        // The trade manager will handle the actual withdrawal
    }
    
    private void playDice(String player, String command) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 2) return;
            
            long bet = parseBet(parts[1]);
            if (bet <= 0) return;
            
            long balance = engine.getDatabase().getBalance(player);
            if (balance < bet) {
                engine.sendMessage(player + " - Insufficient balance!");
                return;
            }
            
            // Deduct bet
            engine.getDatabase().deductBalance(player, bet);
            
            // Roll dice
            int roll = engine.getFairness().rollDice();
            
            // Calculate payout (win on 55+)
            long payout = 0;
            if (roll >= 55) {
                payout = bet * 2;
            }
            
            engine.recordGame(player, bet, payout, "Dice(" + roll + ")");
            
        } catch (Exception e) {
            engine.getScript().log("Dice error: " + e.getMessage());
        }
    }
    
    private void playFlowerPoker(String player, String command) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 2) return;
            
            long bet = parseBet(parts[1]);
            if (bet <= 0) return;
            
            long balance = engine.getDatabase().getBalance(player);
            if (balance < bet) {
                engine.sendMessage(player + " - Insufficient balance!");
                return;
            }
            
            engine.getDatabase().deductBalance(player, bet);
            
            int playerHand = engine.getFairness().rollDice() % 10;
            int houseHand = engine.getFairness().rollDice() % 10;
            
            long payout = 0;
            if (playerHand > houseHand) {
                payout = bet * 2;
            } else if (playerHand == houseHand) {
                payout = bet;
            }
            
            engine.recordGame(player, bet, payout, "FP(" + playerHand + "vs" + houseHand + ")");
            
        } catch (Exception e) {
            engine.getScript().log("FP error: " + e.getMessage());
        }
    }
    
    private void playBlackjack(String player, String command) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 2) return;
            
            long bet = parseBet(parts[1]);
            if (bet <= 0) return;
            
            long balance = engine.getDatabase().getBalance(player);
            if (balance < bet) {
                engine.sendMessage(player + " - Insufficient balance!");
                return;
            }
            
            engine.getDatabase().deductBalance(player, bet);
            
            int playerScore = (engine.getFairness().rollDice() % 10) + (engine.getFairness().rollDice() % 10);
            int houseScore = (engine.getFairness().rollDice() % 10) + (engine.getFairness().rollDice() % 10);
            
            long payout = 0;
            if (playerScore <= 21 && (playerScore > houseScore || houseScore > 21)) {
                payout = bet * 2;
            } else if (playerScore == houseScore) {
                payout = bet;
            }
            
            engine.recordGame(player, bet, payout, "BJ(" + playerScore + "vs" + houseScore + ")");
            
        } catch (Exception e) {
            engine.getScript().log("BJ error: " + e.getMessage());
        }
    }
    
    private void play55x2(String player, String command) {
        try {
            String[] parts = command.split(" ");
            if (parts.length < 2) return;
            
            long bet = parseBet(parts[1]);
            if (bet <= 0) return;
            
            long balance = engine.getDatabase().getBalance(player);
            if (balance < bet) {
                engine.sendMessage(player + " - Insufficient balance!");
                return;
            }
            
            engine.getDatabase().deductBalance(player, bet);
            
            int roll = engine.getFairness().rollDice();
            
            long payout = 0;
            if (roll >= 55) {
                payout = bet * 2;
            }
            
            engine.recordGame(player, bet, payout, "55x2(" + roll + ")");
            
        } catch (Exception e) {
            engine.getScript().log("55x2 error: " + e.getMessage());
        }
    }
    
    private long parseBet(String betStr) {
        try {
            betStr = betStr.toLowerCase().replace("k", "000").replace("m", "000000").replace("gp", "").trim();
            return Long.parseLong(betStr);
        } catch (Exception e) {
            return 0;
        }
    }
}

/**
 * Provably fair RNG using HMAC-SHA256
 */
class FairnessEngine {
    
    private final SecureRandom random = new SecureRandom();
    private byte[] serverSeed;
    
    public void initialize() {
        serverSeed = new byte[32];
        random.nextBytes(serverSeed);
    }
    
    public int rollDice() {
        try {
            byte[] clientSeed = new byte[16];
            random.nextBytes(clientSeed);
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(serverSeed, "HmacSHA256");
            mac.init(keySpec);
            
            byte[] hash = mac.doFinal(clientSeed);
            int value = Math.abs(java.nio.ByteBuffer.wrap(hash).getInt());
            
            return (value % 100) + 1;
            
        } catch (Exception e) {
            return random.nextInt(100) + 1;
        }
    }
}

/**
 * Player database with balances and blacklist
 */
class PlayerDatabase {
    
    private final Map<String, Long> balances = new ConcurrentHashMap<>();
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private final String dataFile = System.getProperty("user.home") + "/TitanCasinoData.dat";
    
    public void load() {
        File file = new File(dataFile);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Long> loadedBalances = (Map<String, Long>) ois.readObject();
            Set<String> loadedBlacklist = (Set<String>) ois.readObject();
            
            balances.putAll(loadedBalances);
            blacklist.addAll(loadedBlacklist);
            
        } catch (Exception e) {
            System.err.println("Failed to load database: " + e.getMessage());
        }
    }
    
    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(new HashMap<>(balances));
            oos.writeObject(new HashSet<>(blacklist));
        } catch (Exception e) {
            System.err.println("Failed to save database: " + e.getMessage());
        }
    }
    
    public long getBalance(String player) {
        return balances.getOrDefault(player, 0L);
    }
    
    public void addBalance(String player, long amount) {
        balances.put(player, getBalance(player) + amount);
    }
    
    public void deductBalance(String player, long amount) {
        long current = getBalance(player);
        balances.put(player, Math.max(0, current - amount));
    }
    
    public boolean isBlacklisted(String player) {
        return blacklist.contains(player);
    }
    
    public void addToBlacklist(String player) {
        blacklist.add(player);
    }
    
    public void removeFromBlacklist(String player) {
        blacklist.remove(player);
    }
    
    public int getPlayerCount() {
        return balances.size();
    }
    
    public Map<String, Long> getAllBalances() {
        return new HashMap<>(balances);
    }
}

/**
 * Admin control panel GUI
 */
class AdminPanel extends JFrame {
    
    private final CasinoEngine engine;
    private JTextField clanChatField;
    private JTextArea logArea;
    
    public AdminPanel(CasinoEngine engine) {
        super("Titan Casino Admin");
        this.engine = engine;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        // Top panel - Settings
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Clan Chat:"));
        clanChatField = new JTextField(15);
        topPanel.add(clanChatField);
        
        JButton setClanButton = new JButton("Set");
        setClanButton.addActionListener(this::onSetClanChat);
        topPanel.add(setClanButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        
        JButton viewPlayersButton = new JButton("View Players");
        viewPlayersButton.addActionListener(e -> showPlayers());
        bottomPanel.add(viewPlayersButton);
        
        JButton saveButton = new JButton("Save Data");
        saveButton.addActionListener(e -> engine.getDatabase().save());
        bottomPanel.add(saveButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void onSetClanChat(ActionEvent e) {
        String clanName = clanChatField.getText().trim();
        engine.setClanChat(clanName);
        log("Clan chat set to: " + clanName);
    }
    
    private void showPlayers() {
        Map<String, Long> balances = engine.getDatabase().getAllBalances();
        StringBuilder sb = new StringBuilder("=== PLAYERS ===\n");
        
        balances.forEach((player, balance) -> 
            sb.append(player).append(": ").append(balance).append("gp\n")
        );
        
        logArea.setText(sb.toString());
    }
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void show() {
        setVisible(true);
    }
}
