# 🔗 Linking Your Discord Bot to the Webhook

This guide explains how to link your standalone Python Discord Bot to your server's webhook for real-time notifications.

---

## 1. Pre-Configured Webhook
I have already pre-configured the bot with your provided webhook URL:
`https://discord.com/api/webhooks/1462544141874499638/PsIKk3oEr_4xYA1DbPG-mTzDx1RecIgGNm6Ck5-5dwzXMgSgEc80w16kbFU5fhSKWMMz`

---

## 2. Steps to Verify or Update the Link

### **Step 2.1: Open the Bot Script**
Open the file `discord_bot/casino_bot.py` in any text editor.

### **Step 2.2: Locate the Webhook Variable**
Find line 10 in the script:
```python
WEBHOOK_URL = "https://discord.com/api/webhooks/1462544141874499638/PsIKk3oEr_4xYA1DbPG-mTzDx1RecIgGNm6Ck5-5dwzXMgSgEc80w16kbFU5fhSKWMMz"
```
If you ever need to change the destination channel, simply replace this URL with a new one from your Discord Server Settings (Integrations -> Webhooks).

### **Step 2.3: Install Required Library**
The bot uses `aiohttp` to send asynchronous requests to the webhook. Ensure it is installed:
```bash
pip install aiohttp
```

---

## 3. How the Link Works

The bot is now programmed to use this link in two ways:

1.  **Status Alerts**: Every time you start the bot, it sends a "🟢 Casino Management Bot Online" message to your Discord channel via the webhook.
2.  **Event Broadcasting**: You can now use the `send_webhook_notification()` function anywhere in the bot's code to push real-time data (like big wins or system alerts) directly to that channel without needing a user to trigger a command.

---

## 4. Testing the Link

1.  Ensure your MySQL database is running (run `./setup.sh` if you haven't).
2.  Add your `BOT_TOKEN` to line 9 of `casino_bot.py`.
3.  Run the bot:
    ```bash
    python3 discord_bot/casino_bot.py
    ```
4.  Check your Discord channel. You should immediately see a green embed notification confirming the bot is online.

---

## 🛠️ Troubleshooting
- **No Notification?**: Check your console for "Failed to send webhook" errors. This usually means the URL is invalid or the bot has no internet access.
- **Permission Error**: Ensure the Webhook in Discord has "Send Messages" permissions (this is enabled by default).
