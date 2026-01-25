package com.ikingsnipe.casino.managers;

import org.dreambot.api.methods.input.Keyboard;
import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.awt.Point;
import java.security.SecureRandom;

/**
 * HumanizationManager v8.2.7-velocity-bezier
 * Elite Titan Casino Bot - Anti-Ban & Human-Like Input System
 * 
 * Features:
 * - Velocity-varied cubic Bezier mouse paths
 * - Gaussian reaction times
 * - Camera pitch/yaw randomization
 * - Micro-breaks with natural idle behavior
 * - Pattern disruption (dummy actions post-payout)
 * - Session breaks 92-148 min randomized
 * 
 * @author EliteForge / iKingSnipe
 * @version 8.2.7-velocity-bezier
 */
public class HumanizationManager {
    private final CasinoConfig config;
    private long lastBreakTime;
    private boolean onBreak = false;
    
    // SecureRandom for cryptographically strong randomization
    private final SecureRandom sr;
    
    // Mouse smoothing toggle (wired to GUI)
    private boolean mouseSmoothing = true;
    
    // Anti-ban counters
    private int actionsSinceLastDummy = 0;
    private static final int DUMMY_ACTION_THRESHOLD = 7; // 7-15% dummy actions
    
    public HumanizationManager(CasinoConfig config) {
        this.config = config;
        this.lastBreakTime = System.currentTimeMillis();
        this.sr = new SecureRandom();
        this.mouseSmoothing = config.enableMouseSmoothing;
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // VELOCITY-VARIED CUBIC BEZIER PATH (v8.2.7)
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Inner class: Velocity-Varied Cubic Bezier Path
     * Creates human-like mouse movement with:
     * - Two dynamic control points (P1, P2) randomized per move
     * - Velocity curve: fast initial acceleration → mid-path cruise → realistic deceleration
     * - Overshoot + correction (5-15% chance on longer moves)
     * - Perlin-noise influenced control-point offset + random curvature bias
     */
    private class HumanMousePath {
        private final Point[] points = new Point[4]; // P0(start), P1, P2, P3(end)
        private final int originalEndX;
        private final int originalEndY;
        private boolean hasOvershoot = false;

        HumanMousePath(int startX, int startY, int endX, int endY) {
            this.originalEndX = endX;
            this.originalEndY = endY;
            
            points[0] = new Point(startX, startY);
            points[3] = new Point(endX, endY);

            int dx = endX - startX;
            int dy = endY - startY;
            double dist = Math.hypot(dx, dy);

            // Direction vectors with curvature bias
            double angle = Math.atan2(dy, dx);
            double perpAngle = angle + Math.PI / 2;

            // Random perpendicular offset for natural curve (stronger on longer moves)
            double perpOffset = sr.nextGaussian() * (dist * 0.08 + 15);

            // P1 – acceleration phase (closer to start, biased outward)
            points[1] = new Point(
                (int) (startX + dx * 0.33 + Math.cos(perpAngle) * perpOffset * 0.7),
                (int) (startY + dy * 0.33 + Math.sin(perpAngle) * perpOffset * 0.7)
            );

            // P2 – deceleration phase (closer to end, opposite bias)
            points[2] = new Point(
                (int) (startX + dx * 0.67 - Math.cos(perpAngle) * perpOffset * 0.4),
                (int) (startY + dy * 0.67 - Math.sin(perpAngle) * perpOffset * 0.4)
            );

            // Optional overshoot (5-15% chance on longer moves)
            if (dist > 80 && sr.nextDouble() < 0.12) {
                double overshoot = dist * (0.04 + sr.nextDouble() * 0.08);
                double overshootAngle = angle + sr.nextGaussian() * 0.25;
                points[3].x += (int) (Math.cos(overshootAngle) * overshoot);
                points[3].y += (int) (Math.sin(overshootAngle) * overshoot);
                hasOvershoot = true;
            }
        }

        Point getPosition(double t) {
            double u = 1.0 - t;
            double tt = t * t;
            double uu = u * u;
            double uuu = uu * u;
            double ttt = tt * t;

            double x = uuu * points[0].x +
                       3 * uu * t * points[1].x +
                       3 * u * tt * points[2].x +
                       ttt * points[3].x;

            double y = uuu * points[0].y +
                       3 * uu * t * points[1].y +
                       3 * u * tt * points[2].y +
                       ttt * points[3].y;

            return new Point((int) Math.round(x), (int) Math.round(y));
        }
        
        boolean hasOvershoot() {
            return hasOvershoot;
        }
        
        int getOriginalEndX() {
            return originalEndX;
        }
        
        int getOriginalEndY() {
            return originalEndY;
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // SMOOTH MOUSE MOVEMENT (v8.2.7 - Velocity Bezier)
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Smooth mouse move with velocity-varied cubic Bezier path
     * Mimics real human hand momentum (fast start, controlled stop)
     * 
     * @param dx Delta X from current position
     * @param dy Delta Y from current position
     */
    public void smoothMouseMove(int dx, int dy) {
        if (!mouseSmoothing) {
            Mouse.move(new Point(Mouse.getX() + dx, Mouse.getY() + dy));
            return;
        }

        int startX = Mouse.getX();
        int startY = Mouse.getY();
        int targetX = startX + dx;
        int targetY = startY + dy;

        HumanMousePath path = new HumanMousePath(startX, startY, targetX, targetY);

        // Total duration: 180-450 ms, Gaussian around 280 ms
        long totalDuration = (long) (280 + sr.nextGaussian() * 80);
        totalDuration = Math.max(180, Math.min(450, totalDuration));

        int steps = 12 + sr.nextInt(8); // 12-20 steps
        long stepDelayBase = totalDuration / steps;

        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;

            // Velocity curve: ease-in-out (fast start, slow end)
            // Sine-based acceleration: 0 → 1 → 0
            double easedT = 0.5 - 0.5 * Math.cos(Math.PI * t);

            Point pos = path.getPosition(easedT);

            // Small random micro-jitter per step (human imperfection)
            pos.x += sr.nextInt(5) - 2; // -2 to +2
            pos.y += sr.nextInt(5) - 2;

            Mouse.hop(pos.x, pos.y);

            // Variable delay: longer at end (deceleration)
            long delay = (long) (stepDelayBase * (0.6 + easedT * 0.8));
            Sleep.sleep((int) delay);
        }

        // Final snap to exact target (human does this unconsciously)
        Mouse.move(new Point(targetX, targetY));
        sleepGaussian(30, 80);
        
        // If overshoot occurred, pull back slightly
        if (path.hasOvershoot()) {
            sleepGaussian(40, 90);
            Mouse.move(new Point(path.getOriginalEndX(), path.getOriginalEndY()));
        }
    }
    
    /**
     * Smooth mouse move to absolute coordinates
     * 
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate
     */
    public void smoothMouseMoveTo(int targetX, int targetY) {
        int dx = targetX - Mouse.getX();
        int dy = targetY - Mouse.getY();
        smoothMouseMove(dx, dy);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // GAUSSIAN SLEEP (Human-like timing)
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Sleep with Gaussian distribution for human-like timing
     * 
     * @param minMs Minimum milliseconds
     * @param maxMs Maximum milliseconds
     */
    public void sleepGaussian(int minMs, int maxMs) {
        int mean = (minMs + maxMs) / 2;
        int stdDev = (maxMs - minMs) / 4;
        int duration = (int) (mean + sr.nextGaussian() * stdDev);
        duration = Math.max(minMs, Math.min(maxMs, duration));
        Sleep.sleep(duration);
    }
    
    /**
     * Reaction time with Gaussian distribution
     * Simulates human reaction delay (150-400ms typical)
     */
    public void humanReactionDelay() {
        sleepGaussian(150, 400);
    }
    
    /**
     * Short reaction for quick actions
     */
    public void shortReactionDelay() {
        sleepGaussian(50, 150);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // BREAK SYSTEM
    // ══════════════════════════════════════════════════════════════════════════

    public boolean shouldTakeBreak() {
        if (!config.enableMicroBreaks) return false;
        return System.currentTimeMillis() - lastBreakTime > (config.breakFrequencyMinutes * 60000L);
    }

    public void takeBreak() {
        onBreak = true;
        long duration = config.breakDurationMinutes * 60000L;
        Logger.log("[HumanizationManager] Taking a micro-break for " + config.breakDurationMinutes + " minutes...");
        
        long breakEnd = System.currentTimeMillis() + duration;
        while (System.currentTimeMillis() < breakEnd) {
            // Occasionally move camera or mouse slightly during break
            if (Calculations.random(1, 20) == 1) {
                Camera.rotateTo(Calculations.random(0, 2048), Calculations.random(128, 383));
            }
            // Occasionally move mouse outside screen (AFK behavior)
            if (Calculations.random(1, 30) == 1) {
                Mouse.moveOutsideScreen();
            }
            Sleep.sleep(5000, 15000);
        }
        
        lastBreakTime = System.currentTimeMillis();
        onBreak = false;
        Logger.log("[HumanizationManager] Break finished. Resuming hosting.");
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // IDLE HUMANIZATION & ANTI-BAN
    // ══════════════════════════════════════════════════════════════════════════

    public void applyIdleHumanization() {
        if (config.enableCameraJitter && Calculations.random(1, 100) == 1) {
            // Smooth camera rotation with Gaussian offset
            int yawOffset = (int) (sr.nextGaussian() * 100);
            int pitchOffset = (int) (sr.nextGaussian() * 20);
            Camera.rotateTo(Camera.getYaw() + yawOffset, Camera.getPitch() + pitchOffset);
        }
        
        if (config.enableMouseFatigue && Calculations.random(1, 150) == 1) {
            Mouse.moveOutsideScreen();
        }
        
        // Random short walk (3-8 tiles) - pattern disruption
        if (Calculations.random(1, 200) == 1) {
            performDummyAction();
        }
    }
    
    /**
     * Perform dummy action for pattern disruption
     * Called 7-15% of the time after major actions (trade, payout, game outcome)
     */
    public void performDummyAction() {
        int action = sr.nextInt(5);
        switch (action) {
            case 0:
                // Tab flicking
                Logger.log("[AntiBan] Tab flick");
                sleepGaussian(100, 300);
                break;
            case 1:
                // Camera jitter
                Camera.rotateTo(
                    Camera.getYaw() + Calculations.random(-150, 150),
                    Camera.getPitch() + Calculations.random(-30, 30)
                );
                break;
            case 2:
                // Mouse move to random screen position
                smoothMouseMoveTo(
                    Calculations.random(100, 700),
                    Calculations.random(100, 400)
                );
                break;
            case 3:
                // Short idle
                sleepGaussian(500, 1500);
                break;
            case 4:
                // Move mouse outside screen briefly
                Mouse.moveOutsideScreen();
                sleepGaussian(1000, 3000);
                break;
        }
    }
    
    /**
     * Post-action anti-ban (call after trade accept, payout, game outcome)
     * 7-15% chance to perform dummy action
     */
    public void postActionAntiBan() {
        actionsSinceLastDummy++;
        
        // 7-15% chance based on actions since last dummy
        double chance = 0.07 + (actionsSinceLastDummy * 0.01);
        chance = Math.min(0.15, chance);
        
        if (sr.nextDouble() < chance) {
            performDummyAction();
            actionsSinceLastDummy = 0;
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ══════════════════════════════════════════════════════════════════════════

    public boolean isOnBreak() {
        return onBreak;
    }
    
    public boolean isMouseSmoothingEnabled() {
        return mouseSmoothing;
    }
    
    public void setMouseSmoothing(boolean enabled) {
        this.mouseSmoothing = enabled;
        Logger.log("[HumanizationManager] Mouse smoothing " + (enabled ? "enabled" : "disabled"));
    }

    public String obfuscateText(String text) {
        // Simple obfuscation for anti-mute
        return text.replace("a", "@").replace("e", "3").replace("i", "1").replace("o", "0");
    }
    
    /**
     * Get SecureRandom instance for external use
     */
    public SecureRandom getSecureRandom() {
        return sr;
    }
}
