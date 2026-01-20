import discord
from discord.ext import commands
import mysql.connector
import os
import datetime

# --- Configuration ---
# NOTE: Replace 'YOUR_BOT_TOKEN_HERE' with your actual Discord Bot Token
BOT_TOKEN = "YOUR_BOT_TOKEN_HERE" 
WEBHOOK_URL = "https://discord.com/api/webhooks/1462544141874499638/PsIKk3oEr_4xYA1DbPG-mTzDx1RecIgGNm6Ck5-5dwzXMgSgEc80w16kbFU5fhSKWMMz"
DB_HOST = "localhost"
DB_USER = "root" # Assuming default user for local setup
DB_PASSWORD = "YOUR_MYSQL_ROOT_PASSWORD" # MUST be set by user during setup
DB_NAME = "goatgang"
PREFIX = "!"

import aiohttp
import json

async def send_webhook_notification(content=None, embed=None):
    """Sends a notification to the configured Discord Webhook."""
    if not WEBHOOK_URL:
        return
    
    payload = {}
    if content:
        payload["content"] = content
    if embed:
        payload["embeds"] = [embed.to_dict()]
        
    async with aiohttp.ClientSession() as session:
        async with session.post(WEBHOOK_URL, json=payload) as response:
            if response.status not in [200, 204]:
                print(f"Failed to send webhook: {response.status}")

# --- Bot Setup ---
intents = discord.Intents.default()
intents.message_content = True
bot = commands.Bot(command_prefix=PREFIX, intents=intents)

# --- Database Connection ---
def get_db_connection():
    return mysql.connector.connect(
        host=DB_HOST,
        user=DB_PASSWORD,
        password=DB_PASSWORD,
        database=DB_NAME
    )

# --- Utility Functions ---
def format_gp(amount):
    if amount >= 1_000_000_000:
        return f"{amount / 1_000_000_000:.2f}B"
    if amount >= 1_000_000:
        return f"{amount / 1_000_000:.2f}M"
    if amount >= 1_000:
        return f"{amount / 1_000:.1f}K"
    return str(amount)

# --- Bot Events ---
@bot.event
async def on_ready():
    print(f'Bot connected as {bot.user}')
    await bot.change_presence(activity=discord.Game(name="Managing the GoatGang Casino"))
    
    # Notify via Webhook that the bot is online
    embed = discord.Embed(
        title="🟢 Casino Management Bot Online",
        description="The GoatGang Discord Bot has successfully connected and is monitoring the casino database.",
        color=0x00FF00,
        timestamp=datetime.datetime.utcnow()
    )
    await send_webhook_notification(embed=embed)

@bot.event
async def on_command_error(ctx, error):
    if isinstance(error, commands.CommandNotFound):
        return
    if isinstance(error, commands.MissingRequiredArgument):
        await ctx.send(f"Missing argument. Usage: `{PREFIX}{ctx.command.name} {ctx.command.signature}`")
        return
    print(f"An error occurred: {error}")
    await ctx.send("An internal error occurred while processing your command.")

# --- Bot Commands ---

@bot.command(name="stats", help="Shows real-time casino statistics.")
async def casino_stats(ctx):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Get total wagered and total paid out (approximation from game_history)
        cursor.execute("SELECT SUM(bet) as total_wagered, SUM(CASE WHEN result='WIN' THEN bet * 2 ELSE 0 END) as total_paid FROM game_history")
        stats = cursor.fetchone()
        
        # Get total profit (requires more complex logic, for now, we'll show wagered)
        total_wagered = stats['total_wagered'] if stats and stats['total_wagered'] else 0
        
        embed = discord.Embed(title="🐐 GoatGang Casino Statistics", color=0xD4AF37)
        embed.add_field(name="Total Wagered", value=f"💰 {format_gp(total_wagered)} GP", inline=False)
        embed.add_field(name="Total Players", value="SELECT COUNT(DISTINCT username) FROM players", inline=True)
        embed.add_field(name="Total Games", value="SELECT COUNT(*) FROM game_history", inline=True)
        embed.set_footer(text="Data is real-time from the MySQL database.")
        
        await ctx.send(embed=embed)
        
    except mysql.connector.Error as err:
        await ctx.send(f"Database Error: Could not connect to the casino database. Please ensure MySQL is running and configured correctly.")
    finally:
        if 'conn' in locals() and conn.is_connected():
            conn.close()

@bot.command(name="balance", help="Checks a player's balance. Usage: !balance <player_name>")
async def check_balance(ctx, player_name: str):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("SELECT balance FROM players WHERE username = %s", (player_name,))
        result = cursor.fetchone()
        
        if result:
            balance = result['balance']
            await ctx.send(f"💰 **{player_name}**'s balance is: **{format_gp(balance)} GP**.")
        else:
            await ctx.send(f"Player **{player_name}** not found in the database.")
            
    except mysql.connector.Error as err:
        await ctx.send("Database Error: Could not connect to the casino database.")
    finally:
        if 'conn' in locals() and conn.is_connected():
            conn.close()

@bot.command(name="setbalance", help="[ADMIN] Sets a player's balance. Usage: !setbalance <player_name> <amount>")
@commands.has_permissions(administrator=True)
async def set_balance(ctx, player_name: str, amount: int):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Check if player exists
        cursor.execute("SELECT username FROM players WHERE username = %s", (player_name,))
        if not cursor.fetchone():
            # Create player if they don't exist
            cursor.execute("INSERT INTO players (username, balance, total_wagered, total_won, games_played) VALUES (%s, %s, 0, 0, 0)", (player_name, amount))
            await ctx.send(f"✅ Player **{player_name}** created and balance set to **{format_gp(amount)} GP**.")
        else:
            # Update balance
            cursor.execute("UPDATE players SET balance = %s WHERE username = %s", (amount, player_name))
            await ctx.send(f"✅ **{player_name}**'s balance updated to **{format_gp(amount)} GP**.")
            
        conn.commit()
        
    except mysql.connector.Error as err:
        await ctx.send(f"Database Error: Could not update balance. {err}")
    finally:
        if 'conn' in locals() and conn.is_connected():
            conn.close()

@bot.command(name="recentwins", help="Shows the last 5 big wins.")
async def recent_wins(ctx):
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Assuming a 'big win' is a win where the payout was > 10M GP
        cursor.execute("SELECT username, bet, timestamp FROM game_history WHERE result = 'WIN' AND bet > 10000000 ORDER BY timestamp DESC LIMIT 5")
        wins = cursor.fetchall()
        
        if wins:
            embed = discord.Embed(title="🏆 Recent Big Wins (Payout > 10M GP)", color=0x00FF00)
            for win in wins:
                time_str = win['timestamp'].strftime("%Y-%m-%d %H:%M:%S")
                embed.add_field(name=f"Player: {win['username']}", value=f"Wager: {format_gp(win['bet'])} GP\nTime: {time_str}", inline=False)
            await ctx.send(embed=embed)
        else:
            await ctx.send("No recent big wins recorded yet.")
            
    except mysql.connector.Error as err:
        await ctx.send("Database Error: Could not connect to the casino database.")
    finally:
        if 'conn' in locals() and conn.is_connected():
            conn.close()

# --- Run Bot ---
if __name__ == "__main__":
    if BOT_TOKEN == "YOUR_BOT_TOKEN_HERE":
        print("ERROR: Please set your Discord Bot Token in casino_bot.py")
    else:
        bot.run(BOT_TOKEN)
