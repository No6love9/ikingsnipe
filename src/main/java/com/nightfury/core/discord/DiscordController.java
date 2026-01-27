package com.nightfury.core.discord;

import com.nightfury.core.logic.TradeManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * The Discord C2: DiscordController.java
 * Refactored for Java 8/11 and JDA 4.x compatibility.
 */
public class DiscordController extends ListenerAdapter {
    private static JDA jda;
    private static final String BOT_TOKEN = System.getenv("DISCORD_BOT_TOKEN");
    private static final String COMMAND_PREFIX = "!";
    private static TradeManager tradeManager;

    public static void init() {
        if (BOT_TOKEN == null || BOT_TOKEN.isEmpty()) {
            System.err.println("[DiscordController] DISCORD_BOT_TOKEN not set.");
            return;
        }

        try {
            // JDA 4.x uses different intent handling
            jda = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.watching("iKingSnipe C2"))
                .addEventListeners(new DiscordController())
                .build();
            jda.awaitReady();
            System.out.println("[DiscordController] JDA C2 is online.");
        } catch (Exception e) {
            System.err.println("[DiscordController] Initialization failed: " + e.getMessage());
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

        if ("status".equals(command)) {
            handleStatus(event);
        } else if ("set".equals(command)) {
            handleSet(event, args);
        } else {
            event.getChannel().sendMessage("Unknown command. Available: `!status`, `!set`").queue();
        }
    }

    private void handleStatus(MessageReceivedEvent event) {
        String status = "**iKingSnipe Status**\n" +
            "• State: `" + tradeManager.getCurrentState() + "`\n" +
            "• Base Delay: `" + tradeManager.getBaseDelay() + "`ms\n" +
            "• Auto Accept: `" + tradeManager.isAutoAccept() + "`";
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
            if ("delay".equals(variable)) {
                double delay = Double.parseDouble(valueStr);
                tradeManager.setBaseDelay(delay);
                event.getChannel().sendMessage("✅ Base Delay set to: `" + delay + "ms`").queue();
            } else if ("autoaccept".equals(variable)) {
                boolean accept = Boolean.parseBoolean(valueStr);
                tradeManager.setAutoAccept(accept);
                event.getChannel().sendMessage("✅ Auto Accept set to: `" + accept + "`").queue();
            } else {
                event.getChannel().sendMessage("Unknown variable: `" + variable + "`. Try `delay` or `autoaccept`.").queue();
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Invalid value for " + variable + ": `" + valueStr + "`").queue();
        }
    }
}
