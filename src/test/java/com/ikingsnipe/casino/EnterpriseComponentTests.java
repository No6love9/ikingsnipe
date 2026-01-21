package com.ikingsnipe.casino;

import com.ikingsnipe.casino.input.ModernInputHandler;
import com.ikingsnipe.casino.listeners.EnterpriseTradeListener;
import com.ikingsnipe.casino.managers.EnterpriseTradeManager;
import com.ikingsnipe.casino.messaging.EnterpriseMessageHandler;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.TradeConfig;
import org.dreambot.api.wrappers.widgets.message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enterprise Component Test Suite
 * 
 * Tests all critical components:
 * - ModernInputHandler
 * - EnterpriseMessageHandler
 * - EnterpriseTradeListener
 * - EnterpriseTradeManager
 */
@DisplayName("Enterprise Component Tests")
public class EnterpriseComponentTests {
    
    private EnterpriseMessageHandler messageHandler;
    private EnterpriseTradeListener tradeListener;
    private TradeConfig tradeConfig;
    
    @BeforeEach
    public void setUp() {
        messageHandler = new EnterpriseMessageHandler();
        tradeConfig = new TradeConfig();
        tradeConfig.minBet = 100000;
        tradeConfig.verboseLogging = true;
        tradeListener = new EnterpriseTradeListener(tradeConfig);
    }
    
    /**
     * Test ModernInputHandler - Player name validation
     */
    @Test
    @DisplayName("ModernInputHandler: Valid player names")
    public void testValidPlayerNames() {
        assertTrue(ModernInputHandler.isValidPlayerName("Player123"));
        assertTrue(ModernInputHandler.isValidPlayerName("Test_Player"));
        assertTrue(ModernInputHandler.isValidPlayerName("P1"));
        assertTrue(ModernInputHandler.isValidPlayerName("Player Name"));
    }
    
    /**
     * Test ModernInputHandler - Invalid player names
     */
    @Test
    @DisplayName("ModernInputHandler: Invalid player names")
    public void testInvalidPlayerNames() {
        assertFalse(ModernInputHandler.isValidPlayerName(null));
        assertFalse(ModernInputHandler.isValidPlayerName(""));
        assertFalse(ModernInputHandler.isValidPlayerName("VeryLongPlayerNameThatExceedsLimit"));
        assertFalse(ModernInputHandler.isValidPlayerName("Player@123"));
        assertFalse(ModernInputHandler.isValidPlayerName("Player#123"));
    }
    
    /**
     * Test EnterpriseMessageHandler - Message queue
     */
    @Test
    @DisplayName("EnterpriseMessageHandler: Message queue operations")
    public void testMessageQueue() {
        assertFalse(messageHandler.hasPendingMessages());
        assertEquals(0, messageHandler.getPendingMessageCount());
        
        // Simulate message (would normally come from DreamBot)
        // Note: In real tests, we'd mock the Message object
        assertFalse(messageHandler.hasPendingMessages());
    }
    
    /**
     * Test EnterpriseMessageHandler - Statistics
     */
    @Test
    @DisplayName("EnterpriseMessageHandler: Statistics tracking")
    public void testMessageStatistics() {
        EnterpriseMessageHandler.MessageStatistics stats = messageHandler.getStatistics();
        assertNotNull(stats);
        assertEquals(0, stats.processed);
        assertEquals(0, stats.dropped);
        assertEquals(0, stats.duplicates);
    }
    
    /**
     * Test EnterpriseTradeListener - Trade event creation
     */
    @Test
    @DisplayName("EnterpriseTradeListener: Trade event handling")
    public void testTradeEventHandling() {
        assertFalse(tradeListener.hasPendingTrades());
        assertEquals(0, tradeListener.getPendingTradeCount());
        
        // Simulate trade request
        tradeListener.onTradeRequest("TestPlayer", System.currentTimeMillis());
        
        assertTrue(tradeListener.hasPendingTrades());
        assertEquals(1, tradeListener.getPendingTradeCount());
        
        // Get event
        EnterpriseTradeListener.TradeEvent event = tradeListener.getNextTradeEvent();
        assertNotNull(event);
        assertEquals("TestPlayer", event.playerName);
        assertEquals(EnterpriseTradeListener.TradeEventType.TRADE_REQUEST, event.type);
    }
    
    /**
     * Test EnterpriseTradeListener - Duplicate detection
     */
    @Test
    @DisplayName("EnterpriseTradeListener: Duplicate trade detection")
    public void testDuplicateTradeDetection() {
        long now = System.currentTimeMillis();
        
        // First request
        tradeListener.onTradeRequest("Player1", now);
        assertEquals(1, tradeListener.getPendingTradeCount());
        
        // Duplicate request (should be ignored)
        tradeListener.onTradeRequest("Player1", now + 100);
        assertEquals(1, tradeListener.getPendingTradeCount());
        
        // Different player
        tradeListener.onTradeRequest("Player2", now + 200);
        assertEquals(2, tradeListener.getPendingTradeCount());
    }
    
    /**
     * Test EnterpriseTradeListener - Trade phase tracking
     */
    @Test
    @DisplayName("EnterpriseTradeListener: Trade phase management")
    public void testTradePhaseTracking() {
        assertEquals(EnterpriseTradeListener.TradePhase.IDLE, tradeListener.getCurrentPhase());
        
        tradeListener.onTradeRequest("TestPlayer", System.currentTimeMillis());
        assertEquals(EnterpriseTradeListener.TradePhase.REQUEST_RECEIVED, tradeListener.getCurrentPhase());
        
        tradeListener.resetTradeState();
        assertEquals(EnterpriseTradeListener.TradePhase.IDLE, tradeListener.getCurrentPhase());
    }
    
    /**
     * Test EnterpriseTradeListener - Statistics
     */
    @Test
    @DisplayName("EnterpriseTradeListener: Statistics tracking")
    public void testTradeListenerStatistics() {
        EnterpriseTradeListener.TradeStatistics stats = tradeListener.getStatistics();
        assertNotNull(stats);
        assertEquals(0, stats.processed);
        assertEquals(0, stats.accepted);
        assertEquals(0, stats.declined);
        assertEquals(0, stats.scamsDetected);
        
        // Simulate trade
        tradeListener.onTradeRequest("Player1", System.currentTimeMillis());
        
        stats = tradeListener.getStatistics();
        assertEquals(1, stats.processed);
    }
    
    /**
     * Test EnterpriseTradeListener - Player name validation
     */
    @Test
    @DisplayName("EnterpriseTradeListener: Player name validation")
    public void testPlayerNameValidation() {
        // Valid names should be accepted
        tradeListener.onTradeRequest("ValidPlayer", System.currentTimeMillis());
        assertEquals(1, tradeListener.getPendingTradeCount());
        
        // Invalid names should be rejected
        tradeListener.onTradeRequest(null, System.currentTimeMillis());
        assertEquals(1, tradeListener.getPendingTradeCount());
        
        tradeListener.onTradeRequest("", System.currentTimeMillis());
        assertEquals(1, tradeListener.getPendingTradeCount());
    }
    
    /**
     * Test EnterpriseTradeListener - Trade declined handling
     */
    @Test
    @DisplayName("EnterpriseTradeListener: Trade declined handling")
    public void testTradeDeclinedHandling() {
        tradeListener.onTradeRequest("Player1", System.currentTimeMillis());
        assertEquals(1, tradeListener.getPendingTradeCount());
        
        tradeListener.onTradeDeclined("Player declined", System.currentTimeMillis());
        
        // Should have both request and declined events
        EnterpriseTradeListener.TradeEvent event1 = tradeListener.getNextTradeEvent();
        assertNotNull(event1);
        assertEquals(EnterpriseTradeListener.TradeEventType.TRADE_REQUEST, event1.type);
        
        EnterpriseTradeListener.TradeEvent event2 = tradeListener.getNextTradeEvent();
        assertNotNull(event2);
        assertEquals(EnterpriseTradeListener.TradeEventType.TRADE_DECLINED, event2.type);
    }
    
    /**
     * Test EnterpriseTradeManager - Initialization
     */
    @Test
    @DisplayName("EnterpriseTradeManager: Initialization")
    public void testTradeManagerInitialization() {
        CasinoConfig config = new CasinoConfig();
        config.minBet = 100000;
        config.tradeConfig = tradeConfig;
        
        EnterpriseTradeManager manager = new EnterpriseTradeManager(config, null);
        assertNotNull(manager);
        assertNotNull(manager.getTradeListener());
    }
    
    /**
     * Test EnterpriseTradeManager - Statistics
     */
    @Test
    @DisplayName("EnterpriseTradeManager: Statistics tracking")
    public void testTradeManagerStatistics() {
        CasinoConfig config = new CasinoConfig();
        config.minBet = 100000;
        config.tradeConfig = tradeConfig;
        
        EnterpriseTradeManager manager = new EnterpriseTradeManager(config, null);
        EnterpriseTradeManager.TradeManagerStatistics stats = manager.getStatistics();
        
        assertNotNull(stats);
        assertEquals(0, stats.processed);
        assertEquals(0, stats.accepted);
        assertEquals(0, stats.declined);
        assertEquals(0, stats.scamsDetected);
        assertEquals(0, stats.gpProcessed);
    }
    
    /**
     * Test concurrent trade requests
     */
    @Test
    @DisplayName("Concurrent: Multiple trade requests")
    public void testConcurrentTradeRequests() throws InterruptedException {
        long now = System.currentTimeMillis();
        
        // Simulate concurrent requests
        for (int i = 0; i < 10; i++) {
            tradeListener.onTradeRequest("Player" + i, now + i);
        }
        
        assertEquals(10, tradeListener.getPendingTradeCount());
        
        // Process all
        int count = 0;
        while (tradeListener.hasPendingTrades()) {
            EnterpriseTradeListener.TradeEvent event = tradeListener.getNextTradeEvent();
            if (event != null) count++;
        }
        
        assertEquals(10, count);
    }
    
    /**
     * Test message handler listener registration
     */
    @Test
    @DisplayName("EnterpriseMessageHandler: Listener management")
    public void testListenerManagement() {
        EnterpriseMessageHandler.MessageListener listener = new EnterpriseMessageHandler.MessageListener() {
            @Override
            public void onMessage(String text, MessageType type, long timestamp) {}
            
            @Override
            public void onTradeRequest(String playerName, long timestamp) {}
            
            @Override
            public void onTradeDeclined(String reason, long timestamp) {}
            
            @Override
            public void onGameResult(String playerName, String result, long timestamp) {}
        };
        
        messageHandler.addListener(listener);
        
        EnterpriseMessageHandler.MessageStatistics stats = messageHandler.getStatistics();
        assertEquals(1, stats.listenerCount);
        
        messageHandler.removeListener(listener);
        
        stats = messageHandler.getStatistics();
        assertEquals(0, stats.listenerCount);
    }
}
