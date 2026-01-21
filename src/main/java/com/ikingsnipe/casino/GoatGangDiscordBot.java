package com.ikingsnipe.casino;

import com.ikingsnipe.casino.managers.SecureDataManager;
import com.ikingsnipe.casino.models.UserModel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * GOAT-GANG CASINO BOT - JAVA EDITION
 * Discord bot implementation of the ChasingCraps Python bot.
 * Features: Craps, Blackjack, Flower Poker, Leveling, Daily Bonus, Audit Logs.
 */
public class GoatGangDiscordBot extends ListenerAdapter {

    private static JDA jda;
    private static SecureDataManager dataManager;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public static void main(String[] args) {
        dataManager = new SecureDataManager();
        String token = (String) dataManager.getConfig().get("bot_token");

        if (token == null || token.equals("YOUR_BOT_TOKEN_HERE")) {
            System.err.println("FATAL ERROR: Bot token not set! Please update config.json.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("Craps & Blackjack"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(new GoatGangDiscordBot())
                    .build();
            jda.awaitReady();
            System.out.println("GOAT-GANG MASTER BOT ONLINE (Java Edition)");

            // Register Slash Commands
            jda.updateCommands().addCommands(
                    new CommandData("balance", "Check your current GP balance, level, and XP."),
                    new CommandData("rank", "Show your current level and XP status."),
                    new CommandData("daily", "Claim your daily GP bonus.")
                    // Game commands will be added later
            ).queue();

            // Schedule daily backup
            scheduler.scheduleAtFixedRate(dataManager::createBackup, 0, 24, TimeUnit.HOURS);

        } catch (LoginException e) {
            System.err.println("Invalid bot token. Please check the config.");
        } catch (InterruptedException e) {
            System.err.println("Bot startup interrupted.");
        }
    }

    public void onSlashCommandEvent(SlashCommandEvent event) {
        if (event.getUser().isBot()) return;

        switch (event.getName()) {
            case "balance":
                handleBalanceCommand(event);
                break;
            case "rank":
                handleRankCommand(event);
                break;
            case "daily":
                handleDailyCommand(event);
                break;
            default:
                event.reply("Unknown command.").setEphemeral(true).queue();
        }
    }

    private void handleBalanceCommand(SlashCommandEvent event) {
        long userId = event.getUser().getIdLong();
        UserModel user = dataManager.getUserData(userId);
        
        String response = String.format(
                "🐐 **%s's Balance** 🐐\n" +
                "**GP Balance**: %,d GP\n" +
                "**Level**: %d\n" +
                "**XP**: %,d",
                event.getUser().getName(),
                user.getBalance(),
                user.getLevel(),
                user.getXp()
        );
        event.reply(response).setEphemeral(true).queue();
    }

    private void handleRankCommand(SlashCommandEvent event) {
        long userId = event.getUser().getIdLong();
        UserModel user = dataManager.getUserData(userId);

        int currentLevel = user.getLevel();
        long currentXp = user.getXp();
        int nextLevel = currentLevel + 1;
        // XP needed for next level: 1000 * (Level)^2
        long xpForNextLevel = 1000L * (nextLevel * nextLevel);
        long xpNeeded = xpForNextLevel - currentXp;

        String response = String.format(
                "🐐 **%s's Rank Status** 🐐\n" +
                "**Current Level**: %d\n" +
                "**Total XP**: %,d\n" +
                "**XP to Level %d**: %,d",
                event.getUser().getName(),
                currentLevel,
                currentXp,
                nextLevel,
                xpNeeded
        );
        event.reply(response).setEphemeral(true).queue();
    }

    private void handleDailyCommand(SlashCommandEvent event) {
        long userId = event.getUser().getIdLong();
        UserModel user = dataManager.getUserData(userId);
        long currentTime = System.currentTimeMillis() / 1000L;
        long cooldown = 24 * 60 * 60; // 24 hours in seconds
        long timeSinceLast = currentTime - user.getLastDaily();

        if (timeSinceLast < cooldown) {
            long remaining = cooldown - timeSinceLast;
            long hours = remaining / 3600;
            long minutes = (remaining % 3600) / 60;
            event.replyFormat("⏳ You can claim your next daily bonus in **%dh %dm**.", hours, minutes).setEphemeral(true).queue();
            return;
        }

        // Daily bonus scales with level
        long bonusAmount = 5_000_000L + (user.getLevel() * 1_000_000L);

        try {
            dataManager.updateBalance(userId, bonusAmount, "Daily Bonus Claim");
            user.setLastDaily(currentTime);
            dataManager.saveUserCache(); // Save the updated lastDaily time

            event.replyFormat("💰 **Daily Bonus Claimed!** You received **%,d GP**!", bonusAmount).setEphemeral(true).queue();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Error claiming daily bonus: " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
