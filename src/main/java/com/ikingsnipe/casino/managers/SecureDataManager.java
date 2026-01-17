package com.ikingsnipe.casino.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ikingsnipe.casino.models.UserModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// NOTE: The original Python version used Fernet encryption. For simplicity in this Java rewrite,
// we are using plain JSON files. For a production environment, a proper Java encryption
// mechanism (e.g., AES) should be implemented.

public class SecureDataManager {
    private static final String CONFIG_FILE = "config.json";
    private static final String USER_DATA_FILE = "user_data.json";
    private static final String POOL_STATE_FILE = "poolstate.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ReentrantLock configLock = new ReentrantLock();
    private final ReentrantLock userLock = new ReentrantLock();
    private final ReentrantLock poolLock = new ReentrantLock();

    // In-memory data stores
    private Map<String, Object> config;
    private ConcurrentHashMap<Long, UserModel> userCache;
    private Map<String, Object> gangPool;

    public SecureDataManager() {
        this.config = new HashMap<>();
        this.userCache = new ConcurrentHashMap<>();
        this.gangPool = new HashMap<>();
        loadAllData();
    }

    private void loadAllData() {
        loadConfig();
        loadUserCache();
        loadGangPool();
    }

    // --- Config Management ---
    private void loadConfig() {
        configLock.lock();
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            this.config = gson.fromJson(reader, type);
            if (this.config == null) this.config = createDefaultConfig();
        } catch (IOException e) {
            System.err.println("Config file not found or error reading. Creating default.");
            this.config = createDefaultConfig();
            saveConfig();
        } finally {
            configLock.unlock();
        }
    }

    public void saveConfig() {
        configLock.lock();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(this.config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            configLock.unlock();
        }
    }

    private Map<String, Object> createDefaultConfig() {
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("bot_token", "YOUR_BOT_TOKEN_HERE");
        defaultConfig.put("guild_id", "1189439097345941585");
        defaultConfig.put("admin_role_id", "1293643009505628206");
        defaultConfig.put("admin_log_channel_id", "1404625168319516712");
        defaultConfig.put("api_secret_key", java.util.UUID.randomUUID().toString());
        // Simplified security config
        Map<String, Long> security = new HashMap<>();
        security.put("max_balance", 1000000000000L);
        security.put("transaction_limit", 10000000000L);
        defaultConfig.put("security", security);
        return defaultConfig;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    // --- User Data Management ---
    private void loadUserCache() {
        userLock.lock();
        try (FileReader reader = new FileReader(USER_DATA_FILE)) {
            Type type = new TypeToken<ConcurrentHashMap<Long, UserModel>>() {}.getType();
            this.userCache = gson.fromJson(reader, type);
            if (this.userCache == null) this.userCache = new ConcurrentHashMap<>();
        } catch (IOException e) {
            System.err.println("User data file not found or error reading. Starting fresh.");
            this.userCache = new ConcurrentHashMap<>();
            saveUserCache();
        } finally {
            userLock.unlock();
        }
    }

    public void saveUserCache() {
        userLock.lock();
        try (FileWriter writer = new FileWriter(USER_DATA_FILE)) {
            gson.toJson(this.userCache, writer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            userLock.unlock();
        }
    }

    public UserModel getUserData(long userId) {
        return userCache.computeIfAbsent(userId, k -> new UserModel());
    }

    public long updateBalance(long userId, long amount, String reason) throws IllegalArgumentException {
        userLock.lock();
        try {
            UserModel user = getUserData(userId);
            long newBalance = user.getBalance() + amount;

            // Security checks (simplified from Python version)
            if (newBalance < 0) {
                throw new IllegalArgumentException("Resulting balance would be negative.");
            }
            // Assuming security limits are in the config map
            long maxBalance = ((Map<String, Long>) config.get("security")).get("max_balance");
            if (newBalance > maxBalance) {
                throw new IllegalArgumentException("Resulting balance exceeds maximum allowed.");
            }

            user.setBalance(newBalance);
            user.addAuditEntry(amount, reason, newBalance);
            saveUserCache();
            return newBalance;
        } finally {
            userLock.unlock();
        }
    }

    // --- Gang Pool Management ---
    private void loadGangPool() {
        poolLock.lock();
        try (FileReader reader = new FileReader(POOL_STATE_FILE)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            this.gangPool = gson.fromJson(reader, type);
            if (this.gangPool == null) this.gangPool = createDefaultPool();
        } catch (IOException e) {
            System.err.println("Pool state file not found or error reading. Starting fresh.");
            this.gangPool = createDefaultPool();
            saveGangPool();
        } finally {
            poolLock.unlock();
        }
    }

    public void saveGangPool() {
        poolLock.lock();
        try (FileWriter writer = new FileWriter(POOL_STATE_FILE)) {
            gson.toJson(this.gangPool, writer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            poolLock.unlock();
        }
    }

    private Map<String, Object> createDefaultPool() {
        Map<String, Object> defaultPool = new HashMap<>();
        defaultPool.put("total_pool", 0L);
        defaultPool.put("hourly_contributions", new HashMap<String, Long>());
        defaultPool.put("last_distribution", System.currentTimeMillis() / 1000L);
        return defaultPool;
    }

    public Map<String, Object> getGangPool() {
        return gangPool;
    }
    
    // Simplified backup function for robustness
    public void createBackup() {
        File backupDir = new File("backups");
        if (!backupDir.exists()) backupDir.mkdirs();
        
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        File currentBackupDir = new File(backupDir, "backup_" + timestamp);
        currentBackupDir.mkdirs();
        
        try {
            java.nio.file.Files.copy(new File(CONFIG_FILE).toPath(), new File(currentBackupDir, CONFIG_FILE).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            java.nio.file.Files.copy(new File(USER_DATA_FILE).toPath(), new File(currentBackupDir, USER_DATA_FILE).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            java.nio.file.Files.copy(new File(POOL_STATE_FILE).toPath(), new File(currentBackupDir, POOL_STATE_FILE).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup created successfully at " + currentBackupDir.getName());
        } catch (IOException e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }
}
