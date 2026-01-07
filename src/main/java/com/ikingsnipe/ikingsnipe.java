package com.ikingsnipe.titan;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.clanchat.ClanChat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.trade.TradeUser;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.Client;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.Mouse;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

// =================================================================================
// MAIN SCRIPT - ENTRY POINT (FAIL-SAFE WRAPPER)
// =================================================================================
@ScriptManifest(
        author = "Snipe",
        name = "iKingSnipe TITAN v13.0 FINAL",
        version = 13.0,
        category = Category.MONEYMAKING,
        description = "Titan Final: Ultimate Robust Casino with 13 Games"
)
public class ikingsnipe extends AbstractScript {

    private CasinoEngine engine;

    @Override
    public void onStart() {
        try {
            log("╔══════════════════════════════════════════════════╗");
            log("║      iKingSnipe TITAN v13.0 FINAL - Loading      ║");
            log("╚══════════════════════════════════════════════════╝");
            engine = new CasinoEngine(this);
            engine.initialize();
            log("✅ Titan FINAL Initialized Successfully");
        } catch (Exception e) {
            log("❌ CRITICAL STARTUP ERROR: " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public int onLoop() {
        if (engine == null) return 1000;
        try {
            return engine.onLoop();
        } catch (Exception e) {
            log("⚠️ Loop Error: " + e.getMessage());
            sleep(1000);
            return 1000;
        }
    }

    @Override
    public void onExit() {
        if (engine != null) {
            engine.shutdown();
        }
        log("🔴 Titan FINAL Shutdown Complete");
    }

    @Override
    public void onMessage(Message msg) {
        if (engine != null && msg != null) {
            engine.onMessage(msg);
        }
    }
}

// =================================================================================
// CORE ENGINE - STATE MACHINE
// =================================================================================
class CasinoEngine {
    private final AbstractScript script;
    private final Database database;
    private final FairnessProvider fairness;
    private final MessageQueue messageQueue;
    private final GameManager gameManager;
    private final BankingManager bankingManager;
    private final AdminGUI gui;
    private final ScriptMonitor monitor;

    private volatile boolean running = false;
    private volatile boolean emergencyStop = false;
    private String clanOwner = "";
    private long lastTradeCheck = 0;
    private long startTime = System.currentTimeMillis();

    // Failover Tracking
    private int ccFailCount = 0;
    private boolean publicChatFallback = false;

    CasinoEngine(AbstractScript script) {
        this.script = script;
        this.database = new Database(script);
        this.fairness = new FairnessProvider();
        this.messageQueue = new MessageQueue(script);
        this.gameManager = new GameManager(this);
        this.bankingManager = new BankingManager(this);
        this.gui = new AdminGUI(this);
        this.monitor = new ScriptMonitor();
    }

    void initialize() {
        script.log("🚀 Titan FINAL: Booting...");

        try {
            database.load();
            fairness.initialize();
            messageQueue.start();
            gui.createAndShow();

            if (clanOwner != null && !clanOwner.isEmpty()) {
                joinClanChat();
            }

            running = true;
            emergencyStop = false;
            startTime = System.currentTimeMillis();

            messageQueue.send("🎰 iKingSnipe TITAN FINAL v13.0 is ONLINE! Type !help");
            script.log("✅ TITAN FINAL INITIALIZED");

        } catch (Exception e) {
            script.log("❌ INITIALIZATION FAILED: " + e.getMessage());
            throw e;
        }
    }

    int onLoop() {
        if (emergencyStop) {
            script.log("🛑 EMERGENCY STOP ACTIVATED");
            return 60000;
        }

        if (!running) return 1000;

        // Perform idle actions
        monitor.performIdleAction(script);

        // Session safety check
        monitor.checkSafety(script, startTime, messageQueue);

        // Trade Throttling
        long now = System.currentTimeMillis();
        if (now - lastTradeCheck > monitor.getHumanDelay(200, 800)) {
            bankingManager.handleTradeIfOpen();
            lastTradeCheck = now;
        }

        // Connection Maintenance
        if (!publicChatFallback && clanOwner != null && !clanOwner.isEmpty() && !ClanChat.isInClanChat()) {
            if (Calculations.random(0, 200) == 0) {
                joinClanChat();
            }
        }

        return monitor.getHumanDelay(150, 350);
    }

    private void joinClanChat() {
        if (clanOwner == null || clanOwner.isEmpty()) return;

        try {
            script.sleep(monitor.getHumanDelay(500, 1500));

            if (!Tabs.isOpen(Tab.CLAN)) {
                Tabs.open(Tab.CLAN);
                script.sleep(monitor.getHumanDelay(200, 600));
            }

            if (ClanChat.joinChat(clanOwner)) {
                ccFailCount = 0;
                script.log("✅ Joined Clan Chat: " + clanOwner);
                script.sleep(monitor.getHumanDelay(300, 800));
            } else {
                ccFailCount++;
                script.log("⚠️ Failed to join CC (" + ccFailCount + "/5)");

                if (ccFailCount >= 5) {
                    publicChatFallback = true;
                    messageQueue.send("⚠️ Clan Chat Unstable - Switched to Public Chat Mode.");
                    script.log("🔄 FAILOVER ACTIVATED: Public Chat Mode");
                }
            }
        } catch (Exception e) {
            script.log("❌ CC Join Error: " + e.getMessage());
        }
    }

    void shutdown() {
        running = false;
        emergencyStop = true;

        try {
            database.save();
            messageQueue.stop();
            gui.dispose();
            script.sleep(500);
        } catch (Exception e) {
            script.log("⚠️ Shutdown Error: " + e.getMessage());
        }

        script.log("🔴 TITAN FINAL Shutdown Complete.");
    }

    void onMessage(Message msg) {
        if (!running || emergencyStop || msg == null) return;

        String senderName = msg.getUsername();
        if (senderName == null || senderName.equals(Client.getLocalPlayer().getName())) return;

        try {
            String player = senderName.intern();
            if (database.isBlacklisted(player)) return;

            String text = msg.getMessage();
            if (text != null) {
                text = text.toLowerCase().trim();
                gameManager.processCommand(player, text);
            }
        } catch (Exception e) {
            // Silently ignore malformed messages
        }
    }

    void finishGame(String player, long bet, long payout, String desc) {
        if (payout > 0) {
            database.addBalance(player, payout);
        }

        long profit = bet - payout;
        database.addProfit(profit);

        gui.updateIfNeeded();
        database.saveAsync();

        String result;
        if (payout > bet) {
            result = "WIN";
        } else if (payout == bet) {
            result = "TIE";
        } else {
            result = "LOSS";
        }

        String message = monitor.addVariation("🎲 " + player + " " + desc + " → " + result + " (" + fmt(payout) + ")");
        messageQueue.send(message);

        sendWebhook(player, desc, result, bet, payout);
    }

    void sendWebhook(String player, String desc, String res, long bet, long payout) {
        String urlStr = gui.getWebhookUrl();
        if (urlStr == null || urlStr.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                int color = res.equals("WIN") ? 65280 : (res.equals("TIE") ? 16776960 : 16711680);
                String json = String.format(
                        "{\"embeds\":[{\"title\":\"Titan Final - %s\",\"description\":\"**Player:** %s\\n**Game:** %s\\n**Bet:** %s\\n**Payout:** %s\",\"color\":%d,\"timestamp\":\"%s\"}]}",
                        res, player, desc, fmt(bet), fmt(payout), color, new Date().toInstant().toString()
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200 && responseCode != 204) {
                    script.log("⚠️ Webhook error: " + responseCode);
                }
            } catch (Exception e) {
                // Silently fail webhook errors
            }
        });
    }

    String fmt(long n) {
        return NumberFormat.getInstance(Locale.US).format(n) + "gp";
    }

    // Getters
    AbstractScript getScript() { return script; }
    Database getDatabase() { return database; }
    FairnessProvider getFairness() { return fairness; }
    MessageQueue getMessageQueue() { return messageQueue; }
    BankingManager getBanking() { return bankingManager; }
    ScriptMonitor getMonitor() { return monitor; }

    // Setters
    void setClanOwner(String owner) {
        this.clanOwner = owner;
        this.publicChatFallback = false;
        this.ccFailCount = 0;
        if (running) {
            joinClanChat();
        }
    }

    void setRunning(boolean r) { this.running = r; }
    void emergencyStop() { this.emergencyStop = true; }
}

// =================================================================================
// SCRIPT MONITOR - ANTI-DETECTION & SAFETY
// =================================================================================
class ScriptMonitor {
    private final SecureRandom secureRandom = new SecureRandom();
    private long lastInteraction = System.currentTimeMillis();
    private int interactionCount = 0;
    private final List<String> emojiVariations = Arrays.asList("🎰", "💰", "🎲", "🃏", "✨", "⭐", "⚡", "🔥", "💎", "👑");
    private final List<String> idleMessages = Arrays.asList("...", "brb", "sec", "one sec", "hold on");

    int getHumanDelay(int min, int max) {
        double base = Calculations.random(min, max);
        double variation = secureRandom.nextGaussian() * (max - min) * 0.1;
        return (int) Math.max(min * 0.5, Math.min(max * 1.5, base + variation));
    }

    void performIdleAction(AbstractScript script) {
        if (secureRandom.nextInt(1000) < 2) {
            switch (secureRandom.nextInt(6)) {
                case 0:
                    if (!Tabs.isOpen(Tab.INVENTORY)) {
                        Tabs.open(Tab.INVENTORY);
                        script.sleep(getHumanDelay(200, 600));
                        Tabs.open(Tab.values()[secureRandom.nextInt(Tab.values().length)]);
                    }
                    break;
                case 1:
                    Camera.rotateTo(secureRandom.nextInt(360), secureRandom.nextInt(100));
                    break;
                case 2:
                    Mouse.move(secureRandom.nextInt(800), secureRandom.nextInt(500));
                    break;
                case 3:
                    if (secureRandom.nextInt(10) < 3) {
                        Keyboard.type(idleMessages.get(secureRandom.nextInt(idleMessages.size())));
                    }
                    break;
                case 4:
                    Tabs.open(Tab.values()[secureRandom.nextInt(Tab.values().length)]);
                    break;
                case 5:
                    Camera.rotateTo(Camera.getYaw() + secureRandom.nextInt(60) - 30);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + secureRandom.nextInt(6));
            }
            script.sleep(getHumanDelay(300, 1200));
        }
    }

    void checkSafety(AbstractScript script, long startTime, MessageQueue messageQueue) {
        long currentTime = System.currentTimeMillis();

        if (interactionCount > 50 && (currentTime - lastInteraction) < 60000) {
            script.log("⚠️ High activity rate - cooling down");
            script.sleep(getHumanDelay(5000, 15000));
            interactionCount = 0;
        }

        if ((currentTime - startTime) > 4 * 60 * 60 * 1000) {
            messageQueue.send("Session timeout - restarting soon");
            script.sleep(getHumanDelay(30000, 60000));
            script.log("🕒 Session limit reached");
        }

        if ((currentTime - lastInteraction) > 300000) {
            interactionCount = Math.max(0, interactionCount - 25);
        }
    }

    void recordInteraction() {
        interactionCount++;
        lastInteraction = System.currentTimeMillis();
    }

    String addVariation(String message) {
        if (secureRandom.nextInt(10) < 3) {
            String emoji = emojiVariations.get(secureRandom.nextInt(emojiVariations.size()));
            return emoji + " " + message;
        }
        return message;
    }
}

// =================================================================================
// DATABASE - ATOMIC SAVES & BACKUPS
// =================================================================================
class Database {
    private final AbstractScript script;
    private final ConcurrentHashMap<String, Long> wallets = new ConcurrentHashMap<>();
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private volatile long globalProfit = 0;

    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Titan-DB-Saver");
        t.setDaemon(true);
        return t;
    });

    Database(AbstractScript script) {
        this.script = script;
    }

    @SuppressWarnings("unchecked")
    void load() {
        File file = new File(System.getProperty("user.home") + "/TitanFinalData.bin");
        File backup = new File(System.getProperty("user.home") + "/TitanFinalData.bak");

        if (!loadFile(file)) {
            script.log("⚠️ Main DB load failed. Trying backup...");
            if (loadFile(backup)) {
                script.log("✅ Backup loaded successfully.");
                saveAsync();
            } else {
                script.log("ℹ️ No valid database found. Starting fresh.");
            }
        }
    }

    private boolean loadFile(File f) {
        if (!f.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            wallets.putAll((Map<String, Long>) ois.readObject());
            globalProfit = ois.readLong();
            script.log("✅ Loaded " + wallets.size() + " wallets, Profit: " + globalProfit);
            return true;
        } catch (Exception e) {
            script.log("❌ DB Read Error: " + e.getMessage());
            return false;
        }
    }

    void save() {
        File mainFile = new File(System.getProperty("user.home") + "/TitanFinalData.bin");
        File tempFile = new File(System.getProperty("user.home") + "/TitanFinalData.tmp");
        File backFile = new File(System.getProperty("user.home") + "/TitanFinalData.bak");

        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                Map<String, Long> copy = new HashMap<>(wallets);
                oos.writeObject(copy);
                oos.writeLong(globalProfit);
                oos.flush();
            }

            if (mainFile.exists()) {
                Files.copy(mainFile.toPath(), backFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(tempFile.toPath(), mainFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (tempFile.exists()) {
                tempFile.delete();
            }

        } catch (Exception e) {
            script.log("❌ CRITICAL SAVE ERROR: " + e.getMessage());
            emergencySave();
        }
    }

    private void emergencySave() {
        try {
            File emergencyFile = new File(System.getProperty("user.home") + "/TitanFinalData.emergency");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(emergencyFile))) {
                oos.writeObject(new HashMap<>(wallets));
                oos.writeLong(globalProfit);
            }
            script.log("⚠️ Emergency save created");
        } catch (Exception e2) {
            script.log("💀 EMERGENCY SAVE FAILED!");
        }
    }

    void saveAsync() {
        saveExecutor.submit(() -> {
            try {
                save();
            } catch (Exception e) {
                script.log("❌ Async Save Error: " + e.getMessage());
            }
        });
    }

    long getBalance(String p) {
        return wallets.getOrDefault(p, 0L);
    }

    void addBalance(String p, long amt) {
        wallets.merge(p, amt, Long::sum);
    }

    void deductBalance(String p, long amt) {
        wallets.computeIfPresent(p, (k, v) -> v >= amt ? v - amt : v);
    }

    boolean isBlacklisted(String p) {
        return blacklist.contains(p.toLowerCase());
    }

    void addProfit(long p) {
        globalProfit += p;
    }

    long getGlobalProfit() {
        return globalProfit;
    }

    Map<String, Long> getWallets() {
        return new HashMap<>(wallets);
    }

    int getWalletCount() {
        return wallets.size();
    }
}

// =================================================================================
// FAIRNESS PROVIDER - CRYPTOGRAPHIC RNG
// =================================================================================
class FairnessProvider {
    private String serverSeed = "TITAN_FINAL_SEED_" + System.currentTimeMillis();
    private final AtomicLong nonce = new AtomicLong(0);
    private final SecureRandom fallbackRandom = new SecureRandom();

    void initialize() {
        try {
            int test = fairInt("init_test", 1, 100);
            System.out.println("[Fairness] Initialized. Test roll: " + test);
        } catch (Exception e) {
            System.out.println("[Fairness] Crypto init failed, using fallback RNG");
        }
    }

    int fairInt(String clientSeed, int min, int max) {
        long currentNonce = nonce.incrementAndGet();
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(serverSeed.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            String input = clientSeed + "_" + currentNonce + "_" + System.currentTimeMillis();
            byte[] hash = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            long value = 0;
            for (int i = 0; i < 8 && i < hash.length; i++) {
                value = (value << 8) | (hash[i] & 0xff);
            }

            int range = max - min + 1;
            return min + (int)(Math.abs(value) % range);
        } catch (Exception e) {
            return min + fallbackRandom.nextInt(max - min + 1);
        }
    }

    double fairDouble(String seed) {
        return fairInt(seed, 0, 999999) / 1000000.0;
    }

    void setServerSeed(String s) {
        this.serverSeed = s;
        System.out.println("[Fairness] Server seed updated");
    }
}

// =================================================================================
// MESSAGE QUEUE - ANTI-SPAM PROTECTED
// =================================================================================
class MessageQueue {
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(100);
    private final AbstractScript script;
    private volatile boolean running = true;
    private final ScriptMonitor monitor;

    private final List<String> prefixes = Arrays.asList("", "> ", "~ ", "• ");
    private final List<String> suffixes = Arrays.asList("", ".", "!", "..");

    MessageQueue(AbstractScript script) {
        this.script = script;
        this.monitor = new ScriptMonitor();
    }

    void start() {
        Thread queueThread = new Thread(() -> {
            script.log("📨 Message Queue Started");
            while (running) {
                try {
                    String msg = queue.poll(500, TimeUnit.MILLISECONDS);
                    if (msg != null && !msg.trim().isEmpty()) {
                        sendMessageWithVariation(msg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    script.log("⚠️ MsgQueue Error: " + e.getMessage());
                }
            }
        }, "Titan-MessageQueue");

        queueThread.setDaemon(true);
        queueThread.start();
    }

    private void sendMessageWithVariation(String msg) {
        try {
            String finalMsg = msg;
            if (monitor != null) {
                finalMsg = monitor.addVariation(msg);
            }

            if (new Random().nextInt(10) < 3) {
                String prefix = prefixes.get(new Random().nextInt(prefixes.size()));
                String suffix = suffixes.get(new Random().nextInt(suffixes.size()));
                finalMsg = prefix + finalMsg + suffix;
            }

            if (ClanChat.isInClanChat()) {
                ClanChat.sendMessage(finalMsg);
            } else {
                Keyboard.type(finalMsg, true);
            }

            int delay = monitor.getHumanDelay(600, 1200);
            Thread.sleep(delay);

        } catch (Exception e) {
            script.log("❌ Send Message Error: " + e.getMessage());
        }
    }

    void send(String msg) {
        if (msg == null || msg.trim().isEmpty() || !running) return;

        if (!queue.offer(msg)) {
            queue.poll();
            queue.offer(msg);
        }
    }

    void stop() {
        running = false;
        queue.clear();
    }
}

// =================================================================================
// BANKING MANAGER - ROBUST TRADE HANDLING
// =================================================================================
class BankingManager {
    private final CasinoEngine engine;
    private final Set<String> withdrawalQueue = ConcurrentHashMap.newKeySet();
    private final Set<String> processingTrades = ConcurrentHashMap.newKeySet();
    private static final int COINS_ID = 995;
    private static final int PLAT_ID = 13204;

    private final Map<String, Long> lastTradeTime = new ConcurrentHashMap<>();

    BankingManager(CasinoEngine engine) {
        this.engine = engine;
    }

    void handleTradeIfOpen() {
        if (!Trade.isOpen()) return;

        try {
            String trader = Trade.getTradingWith();
            if (trader == null || trader.isEmpty()) {
                engine.getScript().sleep(engine.getMonitor().getHumanDelay(200, 600));
                return;
            }

            if (processingTrades.contains(trader)) {
                return;
            }

            processingTrades.add(trader);

            long lastTime = lastTradeTime.getOrDefault(trader, 0L);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime < 30000) {
                engine.getScript().log("⚠️ Trade cooldown for " + trader);
                Trade.close();
                processingTrades.remove(trader);
                return;
            }

            if (Trade.hasAcceptedTrade(TradeUser.valueOf(trader)) && Trade.hasAcceptedTrade(engine.getScript().getLocalPlayer().getName())) {
                engine.getScript().sleep(engine.getMonitor().getHumanDelay(300, 800));
                Trade.acceptTrade();
                processingTrades.remove(trader);
                lastTradeTime.put(trader, System.currentTimeMillis());
                return;
            }

            if (withdrawalQueue.contains(trader)) {
                handleWithdrawal(trader);
            } else {
                handleDeposit(trader);
            }

            processingTrades.remove(trader);

        } catch (Exception e) {
            engine.getScript().log("❌ Trade Error: " + e.getMessage());
            processingTrades.clear();
        }
    }

    private void handleWithdrawal(String trader) {
        long balance = engine.getDatabase().getBalance(trader);
        if (balance <= 0) {
            withdrawalQueue.remove(trader);
            Trade.close();
            return;
        }

        engine.getScript().sleep(engine.getMonitor().getHumanDelay(800, 2000));

        int inventoryCoins = Inventory.contains(COINS_ID) ? Inventory.count(COINS_ID) : 0;
        if (inventoryCoins < Math.min(balance, 1000000)) {
            engine.getMessageQueue().send(trader + " Error: Insufficient coins!");
            withdrawalQueue.remove(trader);
            Trade.close();
            return;
        }

        int amountToAdd = (int) Math.min(balance, Integer.MAX_VALUE);
        if (!Trade.contains(item -> item != null && item.getID() == COINS_ID && item.getAmount() >= amountToAdd)) {
            Trade.addItem(COINS_ID, amountToAdd);
            engine.getScript().sleep(engine.getMonitor().getHumanDelay(400, 1000));
        }

        if (Trade.hasAcceptedTrade(TradeUser.valueOf(trader))) {
            engine.getScript().sleep(engine.getMonitor().getHumanDelay(300, 700));
            Trade.acceptTrade();

            if (engine.getScript().sleepUntil(() -> {
                return !Trade.isOpen();
            }, 10000)) {
                engine.getDatabase().deductBalance(trader, balance);
                withdrawalQueue.remove(trader);
                engine.getDatabase().saveAsync();
                lastTradeTime.put(trader, System.currentTimeMillis());

                String message = engine.getMonitor().addVariation(trader + " Withdrawn: " + engine.fmt(balance));
                engine.getMessageQueue().send(message);
            }
        }
    }

    private void handleDeposit(String trader) {
        List<Item> theirItems = Trade.getTheirItems();
        if (theirItems == null || theirItems.isEmpty()) {
            engine.getScript().sleep(engine.getMonitor().getHumanDelay(1000, 3000));
            Trade.close();
            return;
        }

        long depositAmount = theirItems.stream()
                .filter(Objects::nonNull)
                .mapToLong(item -> {
                    if (item.getID() == COINS_ID) return item.getAmount();
                    if (item.getID() == PLAT_ID) return item.getAmount() * 1000L;
                    return 0;
                })
                .sum();

        if (depositAmount > 0 && Trade.hasAcceptedTrade(TradeUser.valueOf(trader))) {
            engine.getScript().sleep(engine.getMonitor().getHumanDelay(500, 1500));
            Trade.acceptTrade();

            if (engine.getScript().sleepUntil(() -> !Trade.isOpen(), 10000)) {
                engine.getDatabase().addBalance(trader, depositAmount);
                engine.getDatabase().saveAsync();
                lastTradeTime.put(trader, System.currentTimeMillis());

                String message = engine.getMonitor().addVariation(trader + " Deposited: " + engine.fmt(depositAmount));
                engine.getMessageQueue().send(message);
            }
        }
    }

    void queueWithdrawal(String player) {
        withdrawalQueue.add(player);
        engine.getMessageQueue().send(player + " Withdrawal queued. Please trade me.");
    }

    void clearQueue() {
        withdrawalQueue.clear();
        processingTrades.clear();
    }
}

// =================================================================================
// GAME MANAGER - ALL 13 GAMES
// =================================================================================
class GameManager {
    private final CasinoEngine engine;
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, BlackjackSession> blackjackSessions = new ConcurrentHashMap<>();

    GameManager(CasinoEngine engine) {
        this.engine = engine;
        registerGames();
    }

    private void registerGames() {
        games.put("!fp", new FlowerPokerGame(engine));
        games.put("!dd", new DiceDuelGame(engine));
        games.put("!55x2", new FiftyFiveXTwoGame(engine));
        games.put("!bj", new BlackjackGame(engine, blackjackSessions));
        games.put("!crash", new CrashGame(engine));
        games.put("!slots", new SlotsGame(engine));
        games.put("!coin", new CoinFlipGame(engine));
        games.put("!plinko", new PlinkoGame(engine));
        games.put("!rb", new RedBlackGame(engine));
        games.put("!ou", new OverUnderGame(engine));
        games.put("!h", new HotColdGame(engine, true));
        games.put("!c", new HotColdGame(engine, false));
        games.put("!craps", new CrapsGame(engine));

        engine.getScript().log("✅ Registered " + games.size() + " games");
    }

    void processCommand(String player, String text) {
        try {
            engine.getMonitor().recordInteraction();

            String[] parts = text.split("\\s+");
            if (parts.length == 0) return;

            String cmd = parts[0];

            if (cmd.equals("!help")) {
                sendHelp(player);
            } else if (cmd.equals("!bal")) {
                showBalance(player);
            } else if (cmd.equals("!withdraw")) {
                handleWithdrawal(player);
            } else if (cmd.equals("!hit") || cmd.equals("!stand")) {
                handleBlackjackAction(player, cmd);
            } else {
                handleGameCommand(player, parts, cmd);
            }
        } catch (Exception e) {
            engine.getScript().log("❌ Command Error: " + e.getMessage());
            engine.getMessageQueue().send(player + " Error processing command.");
        }
    }

    private void sendHelp(String player) {
        String helpMsg = player + " Games: !fp !dd !55x2 !bj !crash !slots !coin !plinko !rb !ou !h !c !craps | Commands: !bal !withdraw !help";
        engine.getMessageQueue().send(helpMsg);
    }

    private void showBalance(String player) {
        long balance = engine.getDatabase().getBalance(player);
        engine.getMessageQueue().send(player + " Balance: " + engine.fmt(balance));
    }

    private void handleWithdrawal(String player) {
        long balance = engine.getDatabase().getBalance(player);
        if (balance <= 0) {
            engine.getMessageQueue().send(player + " You have no balance to withdraw.");
            return;
        }
        engine.getBanking().queueWithdrawal(player);
    }

    private void handleBlackjackAction(String player, String action) {
        BlackjackGame bj = (BlackjackGame) games.get("!bj");
        if (bj != null) {
            bj.handleAction(player, action);
        } else {
            engine.getMessageQueue().send(player + " No active blackjack session.");
        }
    }

    private void handleGameCommand(String player, String[] parts, String cmd) {
        Game game = games.get(cmd);
        if (game == null) {
            engine.getMessageQueue().send(player + " Unknown command. Type !help");
            return;
        }

        if (parts.length < 2) {
            engine.getMessageQueue().send(player + " Usage: " + cmd + " [amount]");
            return;
        }

        long playerBalance = engine.getDatabase().getBalance(player);
        long bet = parseBet(parts[1], playerBalance);

        if (!isValidBet(player, bet)) {
            return;
        }

        engine.getDatabase().deductBalance(player, bet);
        game.execute(player, bet, parts);
    }

    private long parseBet(String s, long balance) {
        try {
            s = s.toLowerCase().trim();
            if (s.equals("all") || s.equals("max")) {
                return Math.min(balance, 1000000000);
            }

            long multiplier = 1;
            if (s.endsWith("k")) {
                multiplier = 1000;
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("m")) {
                multiplier = 1000000;
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("b")) {
                multiplier = 1000000000;
                s = s.substring(0, s.length() - 1);
            }

            long value = Long.parseLong(s.replaceAll("[^0-9]", ""));
            long result = value * multiplier;

            return Math.max(100000, Math.min(result, 1000000000));
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isValidBet(String player, long bet) {
        long minBet = 100000;
        long maxBet = 1000000000;
        long playerBalance = engine.getDatabase().getBalance(player);

        if (bet < minBet) {
            engine.getMessageQueue().send(player + " Minimum bet is " + engine.fmt(minBet));
            return false;
        }

        if (bet > maxBet) {
            engine.getMessageQueue().send(player + " Maximum bet is " + engine.fmt(maxBet));
            return false;
        }

        if (bet > playerBalance) {
            engine.getMessageQueue().send(player + " Insufficient funds. Balance: " + engine.fmt(playerBalance));
            return false;
        }

        return true;
    }
}

interface Game {
    String getCommand();
    void execute(String player, long bet, String[] args);
}

// =================================================================================
// GAME IMPLEMENTATIONS
// =================================================================================

// 1. Flower Poker
class FlowerPokerGame implements Game {
    private final CasinoEngine engine;
    FlowerPokerGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!fp"; }
    @Override public void execute(String player, long bet, String[] args) {
        String[] playerHand = generateHand(player);
        String[] houseHand = generateHand(player + "_house");
        int playerScore = evaluateHand(playerHand);
        int houseScore = evaluateHand(houseHand);
        boolean win = playerScore > houseScore;
        boolean tie = playerScore == houseScore;
        long payout = win ? (long)(bet * 1.96) : (tie ? bet : 0);
        String result = "Flower Poker: " + handToString(playerHand) + " (" + rankToString(playerScore) +
                ") vs " + handToString(houseHand) + " (" + rankToString(houseScore) + ")";
        engine.finishGame(player, bet, payout, result);
    }

    private String[] generateHand(String seed) {
        String[] flowers = {"R", "B", "Y", "P", "O", "M", "Pa", "W", "Bl"};
        String[] hand = new String[5];
        for (int i = 0; i < 5; i++) {
            int roll = engine.getFairness().fairInt(seed + i, 0, 100);
            hand[i] = flowers[Math.min(roll / 12, 8)];
        }
        return hand;
    }

    private int evaluateHand(String[] hand) {
        int[] counts = new int[9];
        for (String flower : hand) {
            counts[flowerIndex(flower)]++;
        }

        if (counts[8] > 0 || counts[7] > 0) return 0;

        boolean hasFive = false, hasFour = false, hasThree = false, hasTwo = false;
        int pairs = 0;
        for (int count : counts) {
            if (count == 5) hasFive = true;
            if (count == 4) hasFour = true;
            if (count == 3) hasThree = true;
            if (count == 2) { hasTwo = true; pairs++; }
        }

        if (hasFive) return 600;
        if (hasFour) return 500;
        if (hasThree && hasTwo) return 400;
        if (hasThree) return 300;
        if (pairs == 2) return 200;
        if (pairs == 1) return 100;
        return 0;
    }

    private int flowerIndex(String flower) {
        switch (flower) {
            case "R": return 0;
            case "B": return 1;
            case "Y": return 2;
            case "P": return 3;
            case "O": return 4;
            case "M": return 5;
            case "Pa": return 6;
            case "W": return 7;
            case "Bl": return 8;
            default: return 0;
        }
    }

    private String rankToString(int score) {
        switch (score) {
            case 600: return "5 of a Kind";
            case 500: return "4 of a Kind";
            case 400: return "Full House";
            case 300: return "3 of a Kind";
            case 200: return "Two Pair";
            case 100: return "One Pair";
            default: return "High Card";
        }
    }

    private String handToString(String[] hand) {
        return String.join("-", hand);
    }
}

// 2. Dice Duel
class DiceDuelGame implements Game {
    private final CasinoEngine engine;
    DiceDuelGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!dd"; }
    @Override public void execute(String player, long bet, String[] args) {
        int playerRoll = engine.getFairness().fairInt(player + "_p", 1, 100);
        int houseRoll = engine.getFairness().fairInt(player + "_h", 1, 100);
        long payout = playerRoll > houseRoll ? (long)(bet * 1.96) :
                (playerRoll == houseRoll ? bet : 0);
        engine.finishGame(player, bet, payout, "Dice Duel: " + playerRoll + " vs " + houseRoll);
    }
}

// 3. 55x2
class FiftyFiveXTwoGame implements Game {
    private final CasinoEngine engine;
    FiftyFiveXTwoGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!55x2"; }
    @Override public void execute(String player, long bet, String[] args) {
        int roll = engine.getFairness().fairInt(player, 1, 100);
        long payout = roll > 55 ? (long)(bet * 1.96) : 0;
        engine.finishGame(player, bet, payout, "55x2: Rolled " + roll);
    }
}

// 4. Blackjack
class BlackjackGame implements Game {
    private final CasinoEngine engine;
    private final Map<String, BlackjackSession> sessions;

    BlackjackGame(CasinoEngine engine, Map<String, BlackjackSession> sessions) {
        this.engine = engine;
        this.sessions = sessions;
    }

    @Override public String getCommand() { return "!bj"; }

    @Override public void execute(String player, long bet, String[] args) {
        if (sessions.containsKey(player)) {
            engine.getMessageQueue().send(player + " Finish current hand first!");
            return;
        }

        BlackjackSession session = new BlackjackSession(bet);
        session.deal(engine.getFairness(), player);
        sessions.put(player, session);

        if (session.isBlackjack()) {
            long payout = (long)(bet * 2.5);
            engine.finishGame(player, bet, payout, "Blackjack: Natural 21!");
            sessions.remove(player);
        } else {
            engine.getMessageQueue().send(player + " Blackjack: " + session.handToString(session.playerHand) +
                    " (Total: " + session.calculateHandValue(session.playerHand) +
                    ") vs Dealer: " + session.dealerHand.get(0));
        }
    }

    void handleAction(String player, String action) {
        BlackjackSession session = sessions.get(player);
        if (session == null) {
            engine.getMessageQueue().send(player + " No active blackjack session.");
            return;
        }

        if (action.equals("!hit")) {
            session.hit(engine.getFairness(), player);
            int playerValue = session.calculateHandValue(session.playerHand);

            if (playerValue > 21) {
                engine.finishGame(player, session.bet, 0, "Blackjack: Bust with " + playerValue);
                sessions.remove(player);
            } else {
                engine.getMessageQueue().send(player + " Hit: " + session.handToString(session.playerHand) +
                        " (Total: " + playerValue + ")");
            }

        } else if (action.equals("!stand")) {
            session.dealerTurn(engine.getFairness(), player);
            int playerValue = session.calculateHandValue(session.playerHand);
            int dealerValue = session.calculateHandValue(session.dealerHand);

            long payout;
            if (dealerValue > 21 || playerValue > dealerValue) {
                payout = (long)(session.bet * 1.96);
            } else if (playerValue == dealerValue) {
                payout = session.bet;
            } else {
                payout = 0;
            }

            engine.finishGame(player, session.bet, payout,
                    "Blackjack: " + playerValue + " vs Dealer: " + dealerValue);
            sessions.remove(player);
        }
    }
}

class BlackjackSession {
    long bet;
    List<Integer> playerHand = new ArrayList<>();
    List<Integer> dealerHand = new ArrayList<>();

    BlackjackSession(long bet) { this.bet = bet; }

    void deal(FairnessProvider fairness, String seed) {
        hit(fairness, seed, playerHand);
        hit(fairness, seed, playerHand);
        hit(fairness, seed + "_dealer", dealerHand);
        hit(fairness, seed + "_dealer", dealerHand);
    }

    void hit(FairnessProvider fairness, String seed) {
        hit(fairness, seed, playerHand);
    }

    void hit(FairnessProvider fairness, String seed, List<Integer> hand) {
        hand.add(fairness.fairInt(seed + "_" + System.nanoTime(), 1, 13));
    }

    void dealerTurn(FairnessProvider fairness, String seed) {
        while (calculateHandValue(dealerHand) < 17) {
            hit(fairness, seed + "_dealer", dealerHand);
        }
    }

    int calculateHandValue(List<Integer> hand) {
        int value = 0;
        int aces = 0;

        for (int card : hand) {
            int cardValue = Math.min(card, 10);
            if (card == 1) {
                aces++;
                cardValue = 11;
            }
            value += cardValue;
        }

        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }

    boolean isBlackjack() {
        return playerHand.size() == 2 && calculateHandValue(playerHand) == 21;
    }

    String handToString(List<Integer> hand) {
        return hand.stream()
                .map(card -> {
                    if (card == 1) return "A";
                    if (card == 11) return "J";
                    if (card == 12) return "Q";
                    if (card == 13) return "K";
                    return String.valueOf(card);
                })
                .collect(Collectors.joining(" "));
    }
}

// 5. Crash
class CrashGame implements Game {
    private final CasinoEngine engine;
    CrashGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!crash"; }
    @Override public void execute(String player, long bet, String[] args) {
        double target = 2.0;
        if (args.length > 2) {
            try {
                target = Double.parseDouble(args[2]);
                target = Math.max(1.1, Math.min(target, 10.0));
            } catch (Exception e) {}
        }

        double random = engine.getFairness().fairDouble(player);
        double crashPoint = 0.98 / (1.0 - random);

        long payout = crashPoint >= target ? (long)(bet * target) : 0;
        String result = String.format("Crash: Crashed at %.2fx", crashPoint);
        engine.finishGame(player, bet, payout, result);
    }
}

// 6. Slots
class SlotsGame implements Game {
    private final CasinoEngine engine;
    private final String[] symbols = {"🍒", "🍋", "🔔", "💎", "7️⃣"};

    SlotsGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!slots"; }

    @Override public void execute(String player, long bet, String[] args) {
        int reel1 = engine.getFairness().fairInt(player + "_1", 0, 4);
        int reel2 = engine.getFairness().fairInt(player + "_2", 0, 4);
        int reel3 = engine.getFairness().fairInt(player + "_3", 0, 4);

        long payout = calculatePayout(bet, reel1, reel2, reel3);
        String result = "Slots: " + symbols[reel1] + symbols[reel2] + symbols[reel3];
        engine.finishGame(player, bet, payout, result);
    }

    private long calculatePayout(long bet, int r1, int r2, int r3) {
        if (r1 == r2 && r2 == r3) {
            return r1 == 4 ? bet * 50 : bet * 20;
        } else if (r1 == r2 || r2 == r3 || r1 == r3) {
            return (long)(bet * 1.5);
        }
        return 0;
    }
}

// 7. Coin Flip
class CoinFlipGame implements Game {
    private final CasinoEngine engine;
    CoinFlipGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!coin"; }

    @Override public void execute(String player, long bet, String[] args) {
        boolean betOnHeads = true;
        if (args.length > 2) {
            String choice = args[2].toLowerCase();
            betOnHeads = choice.startsWith("h");
        }

        int flip = engine.getFairness().fairInt(player, 0, 1);
        boolean isHeads = flip == 0;

        long payout = (betOnHeads && isHeads) || (!betOnHeads && !isHeads) ?
                (long)(bet * 1.96) : 0;

        String result = "Coin Flip: " + (isHeads ? "Heads" : "Tails");
        engine.finishGame(player, bet, payout, result);
    }
}

// 8. Plinko
class PlinkoGame implements Game {
    private final CasinoEngine engine;
    private final double[] multipliers = {10, 5, 2, 1, 0.5, 1, 2, 5, 10};

    PlinkoGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!plinko"; }

    @Override public void execute(String player, long bet, String[] args) {
        int position = 0;
        for (int i = 0; i < 8; i++) {
            position += engine.getFairness().fairInt(player + "_" + i, 0, 1);
        }

        double multiplier = multipliers[Math.min(position, 8)];
        long payout = (long)(bet * multiplier * 0.95);

        engine.finishGame(player, bet, payout, "Plinko: Slot " + position + " (x" + multiplier + ")");
    }
}

// 9. Red/Black
class RedBlackGame implements Game {
    private final CasinoEngine engine;
    private final Set<Integer> redNumbers = new HashSet<>(Arrays.asList(
            1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
    ));

    RedBlackGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!rb"; }

    @Override public void execute(String player, long bet, String[] args) {
        boolean betOnRed = true;
        if (args.length > 2) {
            String choice = args[2].toLowerCase();
            betOnRed = choice.startsWith("r");
        }

        int number = engine.getFairness().fairInt(player, 0, 36);
        boolean isRed = redNumbers.contains(number);
        boolean isZero = number == 0;

        long payout = 0;
        if (!isZero) {
            if ((betOnRed && isRed) || (!betOnRed && !isRed)) {
                payout = (long)(bet * 1.96);
            }
        }

        String color = isZero ? "Green" : (isRed ? "Red" : "Black");
        engine.finishGame(player, bet, payout, "Red/Black: " + number + " (" + color + ")");
    }
}

// 10. Over/Under
class OverUnderGame implements Game {
    private final CasinoEngine engine;
    OverUnderGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!ou"; }

    @Override public void execute(String player, long bet, String[] args) {
        boolean betOver = true;
        if (args.length > 2) {
            String choice = args[2].toLowerCase();
            betOver = choice.startsWith("o");
        }

        int number = engine.getFairness().fairInt(player, 1, 100);
        boolean isOver = number > 50;

        long payout = (betOver && isOver) || (!betOver && !isOver) ?
                (long)(bet * 1.96) : 0;

        engine.finishGame(player, bet, payout, "Over/Under: " + number);
    }
}

// 11 & 12. Hot/Cold
class HotColdGame implements Game {
    private final CasinoEngine engine;
    private final boolean isHotGame;

    HotColdGame(CasinoEngine engine, boolean isHotGame) {
        this.engine = engine;
        this.isHotGame = isHotGame;
    }

    @Override public String getCommand() { return isHotGame ? "!h" : "!c"; }

    @Override public void execute(String player, long bet, String[] args) {
        int result = engine.getFairness().fairInt(player, 0, 2);

        long payout = 0;
        if (isHotGame && result == 0) {
            payout = (long)(bet * 1.96);
        } else if (!isHotGame && result == 1) {
            payout = (long)(bet * 1.96);
        }

        String[] outcomes = {"Hot", "Cold", "Rainbow"};
        engine.finishGame(player, bet, payout, (isHotGame ? "Hot" : "Cold") + ": " + outcomes[result]);
    }
}

// 13. Craps
class CrapsGame implements Game {
    private final CasinoEngine engine;
    CrapsGame(CasinoEngine engine) { this.engine = engine; }
    @Override public String getCommand() { return "!craps"; }

    @Override public void execute(String player, long bet, String[] args) {
        int die1 = engine.getFairness().fairInt(player + "_1", 1, 6);
        int die2 = engine.getFairness().fairInt(player + "_2", 1, 6);
        int total = die1 + die2;

        long payout;
        if (total == 7 || total == 11) {
            payout = (long)(bet * 1.96);
        } else if (total == 2 || total == 3 || total == 12) {
            payout = 0;
        } else {
            payout = bet;
        }

        engine.finishGame(player, bet, payout, "Craps: " + die1 + " + " + die2 + " = " + total);
    }
}

// =================================================================================
// ADMIN GUI
// =================================================================================
class AdminGUI {
    private final CasinoEngine engine;
    private JFrame frame;
    private JLabel profitLabel;
    private JLabel walletLabel;
    private JTextField clanField;
    private JTextField webhookField;
    private JButton startButton;
    private JButton stopButton;
    private JButton emergencyButton;
    private long lastUpdate = 0;

    AdminGUI(CasinoEngine engine) {
        this.engine = engine;
    }

    void createAndShow() {
        if (SwingUtilities.isEventDispatchThread()) {
            buildGUI();
        } else {
            SwingUtilities.invokeLater(this::buildGUI);
        }
    }

    private void buildGUI() {
        try {
            frame = new JFrame("TITAN FINAL v13.0 - Control Panel");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    int confirm = JOptionPane.showConfirmDialog(frame,
                            "Close Titan Final?",
                            "Confirm Exit", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        engine.shutdown();
                        frame.dispose();
                    }
                }
            });

            frame.setSize(600, 400);
            frame.setLayout(new BorderLayout(10, 10));

            // Top Panel - Title
            JPanel titlePanel = new JPanel();
            titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel titleLabel = new JLabel("🎰 TITAN FINAL CASINO v13.0", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setForeground(new Color(0, 100, 0));
            titlePanel.add(titleLabel);
            frame.add(titlePanel, BorderLayout.NORTH);

            // Center Panel - Stats
            JPanel statsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

            profitLabel = new JLabel("Total Profit: 0 gp", SwingConstants.CENTER);
            profitLabel.setFont(new Font("Arial", Font.BOLD, 16));

            walletLabel = new JLabel("Active Wallets: 0", SwingConstants.CENTER);
            walletLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            statsPanel.add(profitLabel);
            statsPanel.add(walletLabel);
            frame.add(statsPanel, BorderLayout.CENTER);

            // Right Panel - Controls
            JPanel controlPanel = new JPanel(new GridLayout(6, 1, 5, 5));
            controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

            // Clan Settings
            JPanel clanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            clanPanel.add(new JLabel("Clan:"));
            clanField = new JTextField(15);
            clanField.setText("iKingSnipe");
            clanPanel.add(clanField);

            // Webhook Settings
            JPanel webhookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            webhookPanel.add(new JLabel("Webhook:"));
            webhookField = new JTextField(20);
            webhookField.setText("");
            webhookPanel.add(webhookField);

            // Buttons
            JButton saveButton = new JButton("💾 Save & Connect");
            saveButton.addActionListener(e -> {
                engine.setClanOwner(clanField.getText().trim());
                JOptionPane.showMessageDialog(frame, "Settings saved!");
            });

            startButton = new JButton("🟢 START");
            startButton.setBackground(new Color(0, 150, 0));
            startButton.setForeground(Color.WHITE);
            startButton.addActionListener(e -> {
                engine.setRunning(true);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            });

            stopButton = new JButton("🔴 STOP");
            stopButton.setBackground(new Color(200, 0, 0));
            stopButton.setForeground(Color.WHITE);
            stopButton.addActionListener(e -> {
                engine.setRunning(false);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            });
            stopButton.setEnabled(false);

            emergencyButton = new JButton("🆘 EMERGENCY STOP");
            emergencyButton.setBackground(Color.RED);
            emergencyButton.setForeground(Color.WHITE);
            emergencyButton.setFont(new Font("Arial", Font.BOLD, 12));
            emergencyButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "EMERGENCY STOP: Immediately halt all operations?",
                        "CONFIRM",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    engine.emergencyStop();
                    startButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    emergencyButton.setEnabled(false);
                }
            });

            controlPanel.add(clanPanel);
            controlPanel.add(webhookPanel);
            controlPanel.add(saveButton);
            controlPanel.add(startButton);
            controlPanel.add(stopButton);
            controlPanel.add(emergencyButton);

            frame.add(controlPanel, BorderLayout.EAST);

            // Bottom Panel - Status
            JPanel statusPanel = new JPanel();
            statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
            JLabel statusLabel = new JLabel("🟢 Ready", SwingConstants.CENTER);
            statusPanel.add(statusLabel);
            frame.add(statusPanel, BorderLayout.SOUTH);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Start update timer
            Timer timer = new Timer(2000);
            timer.start();

        } catch (Exception e) {
            engine.getScript().log("❌ GUI Creation Error: " + e.getMessage());
        }
    }

    void updateIfNeeded() {
        if (frame == null || !frame.isVisible()) return;

        long now = System.currentTimeMillis();
        if (now - lastUpdate > 2000) {
            SwingUtilities.invokeLater(() -> {
                try {
                    long profit = engine.getDatabase().getGlobalProfit();
                    int wallets = engine.getDatabase().getWalletCount();

                    profitLabel.setText("Total Profit: " + engine.fmt(profit));
                    walletLabel.setText("Active Wallets: " + wallets);

                    if (profit > 0) {
                        profitLabel.setForeground(new Color(0, 150, 0));
                    } else if (profit < 0) {
                        profitLabel.setForeground(Color.RED);
                    } else {
                        profitLabel.setForeground(Color.BLACK);
                    }
                } catch (Exception e) {}
            });
            lastUpdate = now;
        }
    }

    String getWebhookUrl() {
        return webhookField != null ? webhookField.getText() : "";
    }

    void dispose() {
        if (frame != null) {
            frame.dispose();
        }
    }
}