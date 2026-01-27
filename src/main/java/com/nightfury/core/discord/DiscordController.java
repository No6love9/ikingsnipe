package com.nightfury.core.discord;

import com.nightfury.core.logic.TradeManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import javax.security.auth.login.LoginException;
import java.util.Arrays;

/**
 * The Discord C2: DiscordController.java
 * Implements a command listener to allow remote variable adjustment (e.g., !set delay 4500).
 */
public class DiscordController extends ListenerAdapter {
    private static JDA jda;
    private static final String BOT_TOKEN = System.getenv("DISCORD_BOT_TOKEN"); // Token from environment variable
    private static final String COMMAND_PREFIX = "!";
    private static TradeManager tradeManager;

    public static void init() {
        if (BOT_TOKEN == null || BOT_TOKEN.isEmpty()) {
            System.err.println("[DiscordController] DISCORD_BOT_TOKEN environment variable not set. C2 disabled.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(BOT_TOKEN, 
                    GatewayIntent.GUILD_MESSAGES, 
                    GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.watching("iKingSnipe C2"))
                .addEventListeners(new DiscordController())
                .build();
            jda.awaitReady();
            System.out.println("[DiscordController] JDA C2 is online and ready.");
        } catch (LoginException e) {
            System.err.println("[DiscordController] Login failed. Check BOT_TOKEN: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[DiscordController] Initialization interrupted.");
        }
    }
    
    public static void setTradeManager(TradeManager manager) {
        tradeManager = manager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith(COMMAND_PREFIX)) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().substring(COMMAND_PREFIX.length()).split("\\s+");
        String command = args[0].toLowerCase();

        if (tradeManager == null) {
            event.getChannel().sendMessage("Error: TradeManager not initialized.").queue();
            return;
        }

        switch (command) {
            case "status":
                handleStatus(event);
                break;
            case "set":
                handleSet(event, args);
                break;
            default:
                event.getChannel().sendMessage("Unknown command. Available: `!status`, `!set`").queue();
        }
    }

    private void handleStatus(MessageReceivedEvent event) {
        String status = String.format(
            "**iKingSnipe Status**\n" +
            "• State: `%s`\n" +
            "• Base Delay: `%.0f`ms\n" +
            "• Auto Accept: `%s`",
            tradeManager.getCurrentState(),
            tradeManager.baseDelay.get(),
            tradeManager.autoAccept.get()
        );
        event.getChannel().sendMessage(status).queue();
    }

    private void handleSet(MessageReceivedEvent event, String[] args) {
        if (args.length < 3) {
            event.getChannel().sendMessage("Usage: `!set <variable> <value>`").queue();
            return;
        }

        String variable = args[1].toLowerCase();
        String valueStr = args[2];

        try {
            switch (variable) {
                case "delay":
                    double delay = Double.parseDouble(valueStr);
                    tradeManager.setBaseDelay(delay);
                    event.getChannel().sendMessage("✅ Base Delay set to: `" + delay + "ms`").queue();
                    break;
                case "autoaccept":
                    boolean accept = Boolean.parseBoolean(valueStr);
                    tradeManager.setAutoAccept(accept);
                    event.getChannel().sendMessage("✅ Auto Accept set to: `" + accept + "`").queue();
                    break;
                default:
                    event.getChannel().sendMessage("Unknown variable: `" + variable + "`. Try `delay` or `autoaccept`.").queue();
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Invalid value for " + variable + ": `" + valueStr + "`").queue();
        }
    }
}
