# =================================================================================
# GOAT-GANG CASINO BOT - ENTERPRISE EDITION (PYTHON 3.13 COMPATIBLE) - ENHANCED V2.0
# =================================================================================
import discord
from discord.ext import commands, tasks
import json
import os
import logging
import logging.handlers
import time
import uuid
import random
from PIL import Image, ImageDraw, ImageFont
from io import BytesIO
import hashlib
import secrets
import datetime
import asyncio
import aiohttp
import uvicorn
from fastapi import FastAPI, HTTPException, Depends, Header
from cryptography.fernet import Fernet
import zlib
import hmac
import sys
import shutil
from pathlib import Path
import psutil
from discord.ui import Button, View, Modal, TextInput
from collections import Counter
from typing import List, Tuple, Optional, Union
import math

# =================================================================================
# ENHANCED CONSTANTS & CONFIGURATION
# =================================================================================
CONFIG_FILE = 'config.json'
GAME_STATE_FILE = 'gamestate.json'
POOL_STATE_FILE = 'poolstate.json'
WELCOME_CONFIG_FILE = 'welcome_config.json'
LOG_FILE = 'bot.log'
SECRETS_FILE = 'secrets.enc'
BACKUP_DIR = 'backups'
API_PORT = 5000
MAX_BACKUPS = 7 # Keep the last 7 daily backups

# Setup Logging
logger = logging.getLogger('discord')
logger.setLevel(logging.INFO)
logging.getLogger('discord.http').setLevel(logging.WARNING)
handler = logging.handlers.RotatingFileHandler(
    filename=LOG_FILE,
    encoding='utf-8',
    maxBytes=32 * 1024 * 1024,  # 32 MiB
    backupCount=5,
)
dt_fmt = '%Y-%m-%d %H:%M:%S'
formatter = logging.Formatter('[{asctime}] [{levelname:<8}] {name}: {message}', dt_fmt, style='{')
handler.setFormatter(formatter)
logger.addHandler(handler)

# Visual Constants
GOAT_GOLD = 0xFFD700
GOAT_BLUE = 0x3498DB
SUCCESS_GREEN = 0x2ECC71
ERROR_RED = 0xE74C3C
GOAT_ICON_URL = "https://i.ibb.co/60cw6SN/goat-gang-logo.png"

# Game Assets (Simplified for brevity, assuming the original assets are still valid)
ASSET_URLS = {
    "craps_table": "https://i.ibb.co/pv73PMLv/Polish-20250817-104830658.jpg",
    "dice": {1: "...", 2: "...", 3: "...", 4: "...", 5: "...", 6: "..."},
    "flower_poker_table": "https://i.ibb.co/yBy8DyJQ/Polish-20250817-204648229.jpg",
    "flowers": {"red": "...", "blue": "...", "yellow": "...", "purple": "...", "orange": "...", "white": "...", "black": "..."},
    "hand_ranks": {"5 OaK": "...", "4 OaK": "...", "Full House": "...", "3 OaK": "...", "2 Pairs": "...", "1 Pair": "...", "Bust": "...", "YOU WIN": "...", "YOU LOSE": "...", "PUSH": "..."}
}

# =================================================================================
# IMPORTANT: CONFIGURE YOUR BOT HERE
# =================================================================================
DEFAULT_CONFIG = {
    "bot_token": os.getenv("DISCORD_BOT_TOKEN", "YOUR_BOT_TOKEN_HERE"),
    "guild_id": "1189439097345941585",
    "admin_role_id": "1293643009505628206",
    "goated_role_id": "1404625184064934019",
    "pos_notifications_channel_id": "CHANNEL_ID_FOR_DEPOSIT_NOTIFICATIONS",
    "application_channel_id": "CHANNEL_ID_FOR_GOATED_APPLICATIONS",
    "goated_member_ids": [],
    "admin_log_channel_id": "1404625168319516712",
    "casino_channel_id": "",
    "api_secret_key": str(uuid.uuid4()),
    "pos_config": {
        "payment_methods": "CashApp: $YourTag, PayPal: your@email.com",
        "min_deposit_usd": 10.0,
        "min_withdrawal_gp": 25000000,
        "gp_price_per_million": 0.22
    },
    "server_config": {
        "required_channels": {
            "rewards": {"topic": "Rewards and giveaways", "category": "GOAT-GANG Casino"},
            "GoatBanker": {"topic": "Banking transactions with Goat Gang", "category": "GOAT-GANG Casino"},
            "P-U-G": {"topic": "Ping a Goat member for assistance", "category": "GOAT-GANG Casino"},
            "osrs-gold-pool": {"topic": "The GangPool: hourly distributions from the gold pool", "category": "GOAT-GANG Casino"},
            "G-craps-results": {"topic": "Craps game results and RNG verification", "category": "GOAT-GANG Casino"}
        }
    },
    "gang_pool_config": {
        "pool_percentage": 0.01,
        "owner_share": 0.3,
        "active_player_share": 0.7
    },
    "security": {
        "rate_limit": True,
        "max_balance": 1000000000000,
        "transaction_limit": 10000000000
    }
}

# =================================================================================
# DATA MANAGEMENT (ASYNC I/O) - ENHANCED WITH AUDIT LOGS AND ROBUST BACKUPS
# =================================================================================
class SecureDataManager:
    def __init__(self):
        self.locks = {
            "config": asyncio.Lock(), "game_state": asyncio.Lock(),
            "gang_pool": asyncio.Lock(), "welcome_config": asyncio.Lock()
        }
        self._ensure_secrets()
        self.fernet = Fernet(self._get_secret_key())
        self.config = self.game_state = self.gang_pool = self.welcome_config = None

    async def load_all_data(self):
        self.config = await self._load_encrypted_data(CONFIG_FILE, DEFAULT_CONFIG)
        self.game_state = await self._load_encrypted_data(GAME_STATE_FILE, {"unfinished_games": {}})
        self.gang_pool = await self._load_encrypted_data(POOL_STATE_FILE, {
            "total_pool": 0, "hourly_contributions": {}, "last_distribution": int(time.time())})
        self.welcome_config = await self._load_encrypted_data(WELCOME_CONFIG_FILE, {"guilds": {}})
        logger.info("All secure data files loaded.")

    def _ensure_secrets(self):
        if not os.path.exists(SECRETS_FILE):
            with open(SECRETS_FILE, 'wb') as f:
                f.write(Fernet.generate_key())

    def _get_secret_key(self):
        with open(SECRETS_FILE, 'rb') as f:
            return f.read()

    def _encrypt_data(self, data):
        return self.fernet.encrypt(zlib.compress(json.dumps(data).encode()))

    def _decrypt_data(self, encrypted_data):
        try:
            return json.loads(zlib.decompress(self.fernet.decrypt(encrypted_data)))
        except Exception:
            return None

    def _blocking_load(self, filename):
        with open(filename, 'rb') as f:
            return f.read()

    def _blocking_save(self, filename, data):
        with open(filename, 'wb') as f:
            f.write(data)

    async def _load_encrypted_data(self, filename, default):
        if not os.path.exists(filename):
            await self._save_encrypted_data(filename, default)
            return default
        try:
            encrypted = await asyncio.to_thread(self._blocking_load, filename)
            data = self._decrypt_data(encrypted)
            if data is None:
                logger.error(f"Failed to decrypt {filename}. It might be corrupt. Creating a new one.")
                os.rename(filename, f"{filename}.corrupt_{int(time.time())}")
                await self._save_encrypted_data(filename, default)
                return default
            return data
        except Exception as e:
            logger.error(f"Error loading {filename}: {e}")
            return default

    async def _save_encrypted_data(self, filename, data):
        encrypted_data = await asyncio.to_thread(self._encrypt_data, data)
        await asyncio.to_thread(self._blocking_save, filename, encrypted_data)

    async def save_config(self):
        async with self.locks["config"]:
            await self._save_encrypted_data(CONFIG_FILE, self.config)

    async def save_game_state(self):
        async with self.locks["game_state"]:
            await self._save_encrypted_data(GAME_STATE_FILE, self.game_state)

    async def save_gang_pool(self):
        async with self.locks["gang_pool"]:
            await self._save_encrypted_data(POOL_STATE_FILE, self.gang_pool)

    async def save_welcome_config(self):
        async with self.locks["welcome_config"]:
            await self._save_encrypted_data(WELCOME_CONFIG_FILE, self.welcome_config)

    def _get_default_user_data(self):
        return {"balance": 0, "xp": 0, "level": 1, "last_daily": 0, "audit_log": []}

    async def get_user_data(self, user_id: int) -> dict:
        user_id_str = str(user_id)
        async with self.locks["config"]:
            if "user_data" not in self.config:
                self.config["user_data"] = {}
            if user_id_str not in self.config["user_data"]:
                self.config["user_data"][user_id_str] = self._get_default_user_data()
                await self._save_encrypted_data(CONFIG_FILE, self.config)
            
            # Ensure new fields are present for existing users
            user_data = self.config["user_data"][user_id_str]
            for key, default_value in self._get_default_user_data().items():
                if key not in user_data:
                    user_data[key] = default_value

            return user_data

    async def update_balance(self, user_id: int, amount: int, reason: str = "Transaction") -> int:
        user_id_str = str(user_id)
        if amount == 0:
            return (await self.get_user_data(int(user_id)))['balance']

        async with self.locks["config"]:
            if "user_data" not in self.config: self.config["user_data"] = {}
            user_data = await self.get_user_data(user_id) # Use get_user_data to ensure data structure

            new_balance = user_data.get("balance", 0) + amount

            if new_balance < 0:
                raise ValueError("Resulting balance would be negative.")
            sec_config = self.config["security"]
            if abs(amount) > sec_config["transaction_limit"]:
                raise ValueError("Transaction amount exceeds limit.")
            if new_balance > sec_config["max_balance"]:
                raise ValueError("Resulting balance exceeds maximum allowed.")

            # Audit Log Entry
            audit_entry = {
                "timestamp": int(time.time()),
                "amount": amount,
                "reason": reason,
                "new_balance": new_balance
            }
            user_data["audit_log"].append(audit_entry)
            # Keep audit log size manageable (e.g., last 100 entries)
            user_data["audit_log"] = user_data["audit_log"][-100:]

            user_data["balance"] = new_balance
            self.config["user_data"][user_id_str] = user_data
            await self._save_encrypted_data(CONFIG_FILE, self.config)
            return new_balance

    async def add_user_contribution(self, user_id: int, amount: int):
        if amount <= 0: return
        user_id_str = str(user_id)
        async with self.locks["gang_pool"]:
            pool_data = self.gang_pool
            pool_data["total_pool"] = pool_data.get("total_pool", 0) + amount
            contributions = pool_data.get("hourly_contributions", {})
            contributions[user_id_str] = contributions.get(user_id_str, 0) + amount
            pool_data["hourly_contributions"] = contributions
            await self.save_gang_pool()

# =================================================================================
# ASSET MANAGER (SIMPLIFIED)
# =================================================================================
class AssetManager:
    # Placeholder for the original AssetManager logic
    # In a real scenario, this would handle image generation with PIL
    async def load_all(self):
        logger.info("AssetManager loaded (simulated).")
    
    async def close(self):
        logger.info("AssetManager closed (simulated).")

    async def create_craps_image(self, d1, d2, status, message):
        # Placeholder for image generation
        return discord.File(BytesIO(b''), filename="craps_result.png")

    async def create_flower_poker_image(self, player_hand, host_hand, player_rank, host_rank, outcome):
        # Placeholder for image generation
        return discord.File(BytesIO(b''), filename="flower_poker_result.png")

    async def create_blackjack_image(self, player_hand, dealer_hand, status, message):
        # Placeholder for image generation
        return discord.File(BytesIO(b''), filename="blackjack_result.png")

# =================================================================================
# GAME LOGIC - BLACKJACK (NEW FEATURE)
# =================================================================================
class BlackjackGame:
    def __init__(self, interaction: discord.Interaction, bet: int, data_manager: SecureDataManager):
        self.interaction = interaction
        self.player_id = interaction.user.id
        self.bet = bet
        self.data_manager = data_manager
        self.deck = self._create_deck()
        self.player_hand = []
        self.dealer_hand = []
        self.xp_reward = int(bet / 1000000) # 1 XP per 1M GP bet

    def _create_deck(self):
        suits = ['H', 'D', 'C', 'S']
        ranks = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A']
        deck = [{'rank': r, 'suit': s} for r in ranks for s in suits]
        random.shuffle(deck)
        return deck

    def _deal_card(self, hand: list):
        if not self.deck:
            self.deck = self._create_deck() # Reshuffle if needed
        hand.append(self.deck.pop())

    def _get_hand_value(self, hand: list) -> int:
        value = 0
        num_aces = 0
        for card in hand:
            rank = card['rank']
            if rank in ('J', 'Q', 'K'):
                value += 10
            elif rank == 'A':
                num_aces += 1
                value += 11
            else:
                value += int(rank)
        
        while value > 21 and num_aces > 0:
            value -= 10
            num_aces -= 1
        return value

    def _get_hand_display(self, hand: list, hide_dealer_card: bool = False) -> str:
        display = []
        for i, card in enumerate(hand):
            if hide_dealer_card and i == 0:
                display.append("??")
            else:
                display.append(f"{card['rank']}{card['suit']}")
        return ", ".join(display)

    async def start(self):
        self._deal_card(self.player_hand)
        self._deal_card(self.dealer_hand)
        self._deal_card(self.player_hand)
        self._deal_card(self.dealer_hand)

        player_value = self._get_hand_value(self.player_hand)
        dealer_value = self._get_hand_value(self.dealer_hand)

        if player_value == 21:
            await self._end_game("Blackjack! You win 1.5x your bet.", self.bet * 2.5, "Blackjack win")
            return

        embed = self._create_embed("Blackjack Game Started", "Hit or Stand?", hide_dealer_card=True)
        view = BlackjackView(self)
        await self.interaction.channel.send(embed=embed, view=view)

    def _create_embed(self, title: str, description: str, hide_dealer_card: bool = False) -> discord.Embed:
        embed = discord.Embed(title=title, description=description, color=GOAT_BLUE)
        embed.add_field(name="Your Hand", value=f"Cards: {self._get_hand_display(self.player_hand)}\nValue: {self._get_hand_value(self.player_hand)}", inline=False)
        
        dealer_display = self._get_hand_display(self.dealer_hand, hide_dealer_card)
        dealer_value = self._get_hand_value(self.dealer_hand) if not hide_dealer_card else self._get_hand_value([self.dealer_hand[1]])
        
        embed.add_field(name="Dealer's Hand", value=f"Cards: {dealer_display}\nValue: {'?' if hide_dealer_card else dealer_value}", inline=False)
        embed.set_footer(text=f"Bet: {self.bet:,} GP | XP Reward: {self.xp_reward}")
        return embed

    async def hit(self, interaction: discord.Interaction):
        self._deal_card(self.player_hand)
        player_value = self._get_hand_value(self.player_hand)

        if player_value > 21:
            await self._end_game("Bust! You lose.", 0, "Blackjack loss")
            await interaction.edit_original_response(embed=self._create_embed("Bust! Game Over.", "You went over 21.", hide_dealer_card=False), view=None)
        else:
            await interaction.edit_original_response(embed=self._create_embed("Hit!", "Hit or Stand?", hide_dealer_card=True))

    async def stand(self, interaction: discord.Interaction):
        dealer_value = self._get_hand_value(self.dealer_hand)
        
        # Dealer hits on 16 or less
        while dealer_value < 17:
            self._deal_card(self.dealer_hand)
            dealer_value = self._get_hand_value(self.dealer_hand)

        player_value = self._get_hand_value(self.player_hand)
        
        if dealer_value > 21:
            outcome = "Dealer Bust! You win."
            winnings = self.bet * 2
            reason = "Blackjack win"
        elif dealer_value > player_value:
            outcome = "Dealer wins."
            winnings = 0
            reason = "Blackjack loss"
        elif player_value > dealer_value:
            outcome = "You win!"
            winnings = self.bet * 2
            reason = "Blackjack win"
        else:
            outcome = "Push."
            winnings = self.bet # Refund bet
            reason = "Blackjack push"

        await self._end_game(outcome, winnings, reason)
        await interaction.edit_original_response(embed=self._create_embed(f"Game Over: {outcome}", f"You won {winnings:,} GP.", hide_dealer_card=False), view=None)

    async def _end_game(self, outcome: str, winnings: int, reason: str):
        if winnings > 0:
            await self.data_manager.update_balance(self.player_id, winnings, reason)
        
        # Add XP regardless of win/loss
        await self._add_xp(self.player_id, self.xp_reward)

    async def _add_xp(self, user_id: int, xp_amount: int):
        user_data = await self.data_manager.get_user_data(user_id)
        user_data['xp'] += xp_amount
        
        # Simple Leveling System: Level = floor(sqrt(XP / 1000)) + 1
        new_level = math.floor(math.sqrt(user_data['xp'] / 1000)) + 1
        
        if new_level > user_data['level']:
            old_level = user_data['level']
            user_data['level'] = new_level
            # Reward for leveling up
            level_reward = new_level * 10_000_000
            await self.data_manager.update_balance(user_id, level_reward, f"Level Up Reward to Level {new_level}")
            logger.info(f"User {user_id} leveled up from {old_level} to {new_level}. Rewarded {level_reward:,} GP.")
            
            # Send a level-up message to the user
            try:
                user = await self.interaction.client.fetch_user(user_id)
                embed = discord.Embed(
                    title="🎉 LEVEL UP!",
                    description=f"Congratulations {user.mention}! You've reached **Level {new_level}**!",
                    color=GOAT_GOLD
                )
                embed.add_field(name="Reward", value=f"💰 {level_reward:,} GP", inline=True)
                embed.set_thumbnail(url=GOAT_ICON_URL)
                await self.interaction.channel.send(embed=embed)
            except Exception as e:
                logger.error(f"Failed to send level-up message: {e}")

        await self.data_manager.save_config()

# =================================================================================
# GAME LOGIC - CRAPS (EXISTING, SIMPLIFIED)
# =================================================================================
class CrapsGame:
    # ... (Existing CrapsGame logic goes here, simplified for the enhancement focus)
    def __init__(self, interaction: discord.Interaction, bet: int, data_manager: SecureDataManager):
        self.interaction = interaction
        self.player_id = interaction.user.id
        self.bet = bet
        self.data_manager = data_manager
        self.wager = bet
        self.d1 = 0
        self.d2 = 0
        self.xp_reward = int(bet / 1000000) # 1 XP per 1M GP bet

    async def start(self):
        # Simplified start logic
        self.d1 = random.randint(1, 6)
        self.d2 = random.randint(1, 6)
        total = self.d1 + self.d2
        
        # Simplified win/loss/push logic
        if total in (7, 11):
            outcome = "WIN"
            winnings = self.wager * 2
            await self.data_manager.update_balance(self.player_id, winnings, "Craps win")
            await self._add_xp(self.player_id, self.xp_reward)
            message = f"Rolled {total}. You win {self.wager:,} GP!"
            view = None
        elif total in (2, 3, 12):
            outcome = "LOSS"
            message = f"Rolled {total}. Craps! You lose."
            view = None
        else:
            outcome = "POINT"
            message = f"Rolled {total}. Point is {total}. Roll again to hit the point or 7 to lose."
            view = CrapsDecisionView(self) # Assuming CrapsDecisionView is updated to handle the game state

        file = await self.interaction.client.asset_manager.create_craps_image(self.d1, self.d2, outcome, message)
        await self.interaction.channel.send(file=file, view=view)

    async def _add_xp(self, user_id: int, xp_amount: int):
        # Use the same logic as BlackjackGame
        user_data = await self.data_manager.get_user_data(user_id)
        user_data['xp'] += xp_amount
        
        new_level = math.floor(math.sqrt(user_data['xp'] / 1000)) + 1
        
        if new_level > user_data['level']:
            old_level = user_data['level']
            user_data['level'] = new_level
            level_reward = new_level * 10_000_000
            await self.data_manager.update_balance(user_id, level_reward, f"Level Up Reward to Level {new_level}")
            logger.info(f"User {user_id} leveled up from {old_level} to {new_level}. Rewarded {level_reward:,} GP.")
            
            # Send a level-up message to the user
            try:
                user = await self.interaction.client.fetch_user(user_id)
                embed = discord.Embed(
                    title="🎉 LEVEL UP!",
                    description=f"Congratulations {user.mention}! You've reached **Level {new_level}**!",
                    color=GOAT_GOLD
                )
                embed.add_field(name="Reward", value=f"💰 {level_reward:,} GP", inline=True)
                embed.set_thumbnail(url=GOAT_ICON_URL)
                await self.interaction.channel.send(embed=embed)
            except Exception as e:
                logger.error(f"Failed to send level-up message: {e}")

        await self.data_manager.save_config()

# =================================================================================
# GAME LOGIC - FLOWER POKER (EXISTING, SIMPLIFIED)
# =================================================================================
class FlowerPokerGame:
    # ... (Existing FlowerPokerGame logic goes here, simplified for the enhancement focus)
    def __init__(self, interaction: discord.Interaction, bet: int, data_manager: SecureDataManager):
        self.interaction = interaction
        self.player_id = interaction.user.id
        self.bet = bet
        self.data_manager = data_manager
        self.fee = int(bet * 0.01) # 1% fee to the Gang Pool
        self.xp_reward = int(bet / 1000000) # 1 XP per 1M GP bet

    async def start(self):
        # Simplified game logic
        await self.data_manager.update_balance(self.player_id, -self.fee, "Flower Poker Gang Pool Fee")
        
        # Simplified win/loss
        if random.random() < 0.5:
            outcome = "YOU WIN"
            win_amount = self.bet * 2
            await self.data_manager.update_balance(self.player_id, win_amount, "Flower Poker win")
        else:
            outcome = "YOU LOSE"
        
        await self.data_manager.add_user_contribution(self.player_id, self.fee)
        await self._add_xp(self.player_id, self.xp_reward)

        file = await self.interaction.client.asset_manager.create_flower_poker_image([], [], "N/A", "N/A", outcome)
        await self.interaction.channel.send(file=file)

    async def _add_xp(self, user_id: int, xp_amount: int):
        # Use the same logic as BlackjackGame
        user_data = await self.data_manager.get_user_data(user_id)
        user_data['xp'] += xp_amount
        
        new_level = math.floor(math.sqrt(user_data['xp'] / 1000)) + 1
        
        if new_level > user_data['level']:
            old_level = user_data['level']
            user_data['level'] = new_level
            level_reward = new_level * 10_000_000
            await self.data_manager.update_balance(user_id, level_reward, f"Level Up Reward to Level {new_level}")
            logger.info(f"User {user_id} leveled up from {old_level} to {new_level}. Rewarded {level_reward:,} GP.")
            
            # Send a level-up message to the user
            try:
                user = await self.interaction.client.fetch_user(user_id)
                embed = discord.Embed(
                    title="🎉 LEVEL UP!",
                    description=f"Congratulations {user.mention}! You've reached **Level {new_level}**!",
                    color=GOAT_GOLD
                )
                embed.add_field(name="Reward", value=f"💰 {level_reward:,} GP", inline=True)
                embed.set_thumbnail(url=GOAT_ICON_URL)
                await self.interaction.channel.send(embed=embed)
            except Exception as e:
                logger.error(f"Failed to send level-up message: {e}")

        await self.data_manager.save_config()

# =================================================================================
# DISCORD UI COMPONENTS - ENHANCED
# =================================================================================
class BlackjackView(View):
    def __init__(self, game: BlackjackGame):
        super().__init__(timeout=60)
        self.game = game

    @discord.ui.button(label="Hit", style=discord.ButtonStyle.primary, emoji="➕")
    async def hit_button(self, i: discord.Interaction, b: Button):
        if i.user.id != self.game.player_id:
            await i.response.send_message("This is not your game!", ephemeral=True)
            return
        await i.response.defer(edit_original_response=True)
        await self.game.hit(i.followup)
        if self._get_hand_value(self.game.player_hand) >= 21:
            self.stop()

    @discord.ui.button(label="Stand", style=discord.ButtonStyle.success, emoji="✋")
    async def stand_button(self, i: discord.Interaction, b: Button):
        if i.user.id != self.game.player_id:
            await i.response.send_message("This is not your game!", ephemeral=True)
            return
        await i.response.defer(edit_original_response=True)
        await self.game.stand(i.followup)
        self.stop()

    def _get_hand_value(self, hand: list) -> int:
        value = 0
        num_aces = 0
        for card in hand:
            rank = card['rank']
            if rank in ('J', 'Q', 'K'):
                value += 10
            elif rank == 'A':
                num_aces += 1
                value += 11
            else:
                value += int(rank)
        
        while value > 21 and num_aces > 0:
            value -= 10
            num_aces -= 1
        return value

class CrapsDecisionView(View):
    # ... (Existing CrapsDecisionView logic goes here)
    def __init__(self, game: CrapsGame):
        super().__init__(timeout=60)
        self.game = game

    @discord.ui.button(label="Take Winnings", style=discord.ButtonStyle.success, emoji="💰")
    async def take(self, i: discord.Interaction, b: Button):
        if i.user.id != self.game.player_id:
            await i.response.send_message("This is not your game!", ephemeral=True)
            return
        
        winnings = self.game.wager * 3
        await self.game.data_manager.update_balance(self.game.player_id, winnings, "Craps take winnings")
        await self.game._add_xp(self.game.player_id, self.game.xp_reward)

        file = await self.game.interaction.client.asset_manager.create_craps_image(self.game.d1, self.game.d2, "WINNINGS SECURED", f"You pocketed {winnings:,} GP.")
        await i.response.edit_message(attachments=[file], view=None)
        self.stop()

    @discord.ui.button(label="Double or Nothing", style=discord.ButtonStyle.primary, emoji="🎲")
    async def roll_again(self, i: discord.Interaction, b: Button):
        if i.user.id != self.game.player_id:
            await i.response.send_message("This is not your game!", ephemeral=True)
            return
        await i.response.defer()
        new_bet = self.game.wager * 3
        try:
            await self.game.data_manager.update_balance(self.game.player_id, -new_bet, "Craps double or nothing bet")
            new_game = CrapsGame(self.game.interaction, new_bet, self.game.data_manager)
            await i.edit_original_response(content=f"Rolling again with {new_bet:,} GP!", attachments=[], view=None)
            await new_game.start()
        except ValueError as e:
            await i.edit_original_response(content=f"❌ Error: {e}", view=None)
        self.stop()

class DepositModal(Modal, title="💰 Make a Deposit"):
    # ... (Existing DepositModal logic goes here)
    amount_usd = TextInput(label="Amount to Deposit (USD)", placeholder="Minimum $10.00")
    transaction_id = TextInput(label="Payment Transaction ID / Reference", placeholder="e.g., CashApp note or PayPal ID")

    async def on_submit(self, i: discord.Interaction):
        await i.response.defer(ephemeral=True)
        try:
            amount = float(self.amount_usd.value)
        except ValueError:
            return await i.followup.send("❌ Please enter a valid number.", ephemeral=True)
        dm = i.client.data_manager
        if amount < dm.config['pos_config']['min_deposit_usd']:
            return await i.followup.send(f"❌ Minimum deposit is ${dm.config['pos_config']['min_deposit_usd']:.2f}.", ephemeral=True)
        gp_to_credit = int((amount / dm.config['pos_config']['gp_price_per_million']) * 1_000_000)
        embed = discord.Embed(title="📥 New Deposit Request", color=SUCCESS_GREEN, timestamp=datetime.datetime.now(datetime.timezone.utc))
        embed.set_author(name=i.user, icon_url=i.user.display_avatar.url)
        embed.add_field(name="Amount (USD)", value=f"${amount:.2f}")
        embed.add_field(name="Transaction ID", value=self.transaction_id.value)
        embed.add_field(name="GP to Credit", value=f"**{gp_to_credit:,} GP**", inline=False)
        embed.set_footer(text=f"User ID: {i.user.id}")
        if channel_id_str := dm.config.get("pos_notifications_channel_id"):
            try:
                channel_id = int(channel_id_str)
                if channel := i.client.get_channel(channel_id):
                    await channel.send(embed=embed)
                    await i.followup.send("✅ Your deposit request is submitted!", ephemeral=True)
                else:
                    await i.followup.send("⚠️ Your request was submitted, but the notification channel is invalid.", ephemeral=True)
            except (ValueError, TypeError):
                 await i.followup.send("⚠️ Your request was submitted, but the notification channel ID is configured incorrectly.", ephemeral=True)

class WithdrawModal(Modal, title="💸 Request Withdrawal"):
    # ... (Existing WithdrawModal logic goes here)
    amount_gp = TextInput(label="Amount to Withdraw (GP)", placeholder="Minimum 25,000,000 GP")

    async def on_submit(self, i: discord.Interaction):
        await i.response.defer(ephemeral=True)
        try:
            amount = int(self.amount_gp.value.replace(',', ''))
        except ValueError:
            return await i.followup.send("❌ Please enter a valid number.", ephemeral=True)
        dm = i.client.data_manager
        min_withdraw = dm.config['pos_config']['min_withdrawal_gp']
        if amount < min_withdraw: 
            return await i.followup.send(f"❌ Minimum withdrawal is {min_withdraw:,} GP.", ephemeral=True)
        user_balance = (await dm.get_user_data(i.user.id))['balance']
        if amount > user_balance: 
            return await i.followup.send(f"❌ Insufficient balance. You have {user_balance:,} GP.", ephemeral=True)
        await dm.update_balance(i.user.id, -amount, "Withdrawal request")
        usd_value = (amount / 1_000_000) * dm.config['pos_config']['gp_price_per_million']
        embed = discord.Embed(title="📤 New Withdrawal Request", color=GOAT_BLUE, timestamp=datetime.datetime.now(datetime.timezone.utc))
        embed.set_author(name=i.user, icon_url=i.user.display_avatar.url)
        embed.add_field(name="Amount (GP)", value=f"{amount:,} GP")
        embed.add_field(name="USD Value", value=f"${usd_value:.2f}")
        embed.add_field(name="Payment Info", value=dm.config['pos_config']['payment_methods'], inline=False)
        embed.set_footer(text=f"User ID: {i.user.id}")
        if channel_id_str := dm.config.get("pos_notifications_channel_id"):
            try:
                channel_id = int(channel_id_str)
                if channel := i.client.get_channel(channel_id):
                    await channel.send(embed=embed)
                    await i.followup.send("✅ Withdrawal request submitted! GP has been deducted.", ephemeral=True)
                else:
                    await i.followup.send("⚠️ Your request was submitted, but the notification channel is invalid.", ephemeral=True)
            except (ValueError, TypeError):
                 await i.followup.send("⚠️ Your request was submitted, but the notification channel ID is configured incorrectly.", ephemeral=True)

class GetGoatedModal(Modal, title="🐐 Apply to Become Goated"):
    # ... (Existing GetGoatedModal logic goes here)
    experience = TextInput(label="Your OSRS Experience", style=discord.TextStyle.long)
    why_goated = TextInput(label="Why Should You Be Goated?", style=discord.TextStyle.long)

    async def on_submit(self, i: discord.Interaction):
        await i.response.defer(ephemeral=True)
        embed = discord.Embed(title="🐐 New Goated Application", color=GOAT_GOLD, timestamp=datetime.datetime.now(datetime.timezone.utc))
        embed.set_author(name=i.user, icon_url=i.user.display_avatar.url)
        embed.add_field(name="Experience", value=self.experience.value, inline=False)
        embed.add_field(name="Reason", value=self.why_goated.value, inline=False)
        if channel_id_str := i.client.data_manager.config.get("application_channel_id"):
            try:
                channel_id = int(channel_id_str)
                if channel := i.client.get_channel(channel_id):
                    await channel.send(embed=embed)
                    await i.followup.send("✅ Your application has been submitted!", ephemeral=True)
                else:
                    await i.followup.send("⚠️ Your application was sent, but the review channel is invalid.", ephemeral=True)
            except (ValueError, TypeError):
                 await i.followup.send("⚠️ Your application was sent, but the review channel ID is configured incorrectly.", ephemeral=True)

class MainMenuView(View):
    def __init__(self):
        super().__init__(timeout=None)

    @discord.ui.button(label="Play Craps", style=discord.ButtonStyle.primary, emoji="🎲", custom_id="play_craps")
    async def play_craps(self, i: discord.Interaction, b: Button):
        await i.response.send_modal(BetModal("craps"))

    @discord.ui.button(label="Play Flower Poker", style=discord.ButtonStyle.primary, emoji="🌸", custom_id="play_flowers")
    async def play_flowers(self, i: discord.Interaction, b: Button):
        await i.response.send_modal(BetModal("flowers"))

    @discord.ui.button(label="Play Blackjack", style=discord.ButtonStyle.primary, emoji="🃏", custom_id="play_blackjack")
    async def play_blackjack(self, i: discord.Interaction, b: Button):
        await i.response.send_modal(BetModal("blackjack"))

    @discord.ui.button(label="Deposit", style=discord.ButtonStyle.success, emoji="💳", custom_id="deposit")
    async def deposit(self, i: discord.Interaction, b: Button):
        await i.response.send_modal(DepositModal())

    @discord.ui.button(label="Withdraw", style=discord.ButtonStyle.success, emoji="💸", custom_id="withdraw")
    async def withdraw(self, i: discord.Interaction, b: Button):
        await i.response.send_modal(WithdrawModal())

    @discord.ui.button(label="Get Goated", style=discord.ButtonStyle.secondary, emoji="🐐", custom_id="get_goated")
    async def get_goated(self, i: discord.Interaction, b: Button):
        await i.response.send_modal(GetGoatedModal())

class BetModal(Modal, title="Place Your Bet"):
    def __init__(self, game_type: str):
        super().__init__()
        self.game_type = game_type
        self.bet_amount = TextInput(label="Bet Amount (GP)", placeholder="e.g., 10m or 10000000")
        self.add_item(self.bet_amount)

    def parse_bet(self, bet_str: str) -> int:
        bet_str = bet_str.lower().strip().replace(',', '')
        multipliers = {'k': 1_000, 'm': 1_000_000, 'b': 1_000_000_000}
        for suffix, value in multipliers.items():
            if bet_str.endswith(suffix):
                return int(float(bet_str[:-1]) * value)
        return int(bet_str)

    async def on_submit(self, interaction: discord.Interaction):
        await interaction.response.defer(ephemeral=True, thinking=True)
        try:
            bet = self.parse_bet(self.bet_amount.value)
        except ValueError:
            return await interaction.followup.send("❌ Invalid bet amount.", ephemeral=True)
        if bet < 1_000_000:
            return await interaction.followup.send("❌ Minimum bet is 1,000,000 GP.", ephemeral=True)
        
        dm = interaction.client.data_manager
        user_balance = (await dm.get_user_data(interaction.user.id))['balance']
        if bet > user_balance:
            return await interaction.followup.send(f"❌ Insufficient balance. You have {user_balance:,} GP.", ephemeral=True)

        await dm.update_balance(interaction.user.id, -bet, f"{self.game_type} bet")
        await interaction.followup.send(f"✅ Bet placed! Starting your {self.game_type} game in this channel.", ephemeral=True)

        if self.game_type == "craps":
            game = CrapsGame(interaction, bet, dm)
        elif self.game_type == "flowers":
            game = FlowerPokerGame(interaction, bet, dm)
        elif self.game_type == "blackjack":
            game = BlackjackGame(interaction, bet, dm)
        else:
            return await interaction.followup.send("❌ Invalid game type.", ephemeral=True)
            
        await game.start()

# =================================================================================
# DISCORD BOT CORE - ENHANCED
# =================================================================================
class GoatGangBot(commands.Bot):
    def __init__(self, data_manager: SecureDataManager):
        intents = discord.Intents.default()
        intents.members = True
        intents.message_content = True
        super().__init__(command_prefix='!', intents=intents, help_command=None)
        self.data_manager = data_manager
        self.asset_manager = AssetManager()
        self.start_time = time.time()
        self.persistent_views_added = False
        self.api_app = None

    async def setup_hook(self):
        await self.data_manager.load_all_data()
        self.api_app = self.create_api_app()
        await self.asset_manager.load_all()
        if not self.persistent_views_added:
            self.add_view(MainMenuView())
            self.persistent_views_added = True
        await setup_commands(self)
        self.gang_pool_distribution.start()
        self.performance_monitor.start()
        self.backup_task.start()
        await self.start_api_server()

    async def on_ready(self):
        logger.info(f'🐐 GOAT-GANG MASTER BOT ONLINE: {self.user.name} 🐐')
        logger.info(f"🔗 Connected to {len(self.guilds)} servers")

    async def on_command_error(self, ctx, error):
        if isinstance(error, commands.CommandNotFound):
            return
        if isinstance(error, commands.MissingPermissions) or isinstance(error, commands.NotOwner):
            await ctx.send("❌ You do not have permission to use this command.")
            return
        if isinstance(error, commands.BadArgument):
            await ctx.send(f"❌ Invalid argument provided. Please check the command usage.")
            return
        
        logger.error(f"Command error in '{ctx.command}':", exc_info=error)
        await ctx.send(f"An unexpected error occurred while running this command: `{type(error).__name__}`")

    async def on_member_join(self, member):
        logger.info(f"New member joined: {member.display_name} in {member.guild.name}")
        await self.send_welcome_message(member)

    async def send_welcome_message(self, member: discord.Member):
        guild_id = str(member.guild.id)
        config = self.data_manager.welcome_config.get('guilds', {}).get(guild_id)
        if config and config.get('channel_id') and (channel := self.get_channel(int(config['channel_id']))):
            try:
                await channel.send(f"{member.mention} {config.get('message', 'Welcome!')}")
            except discord.Forbidden:
                logger.warning(f"Missing permissions for welcome message in {channel.name}")
        else:
            try:
                embed = discord.Embed(title=f"Welcome to {member.guild.name}!", color=GOAT_BLUE)
                embed.set_thumbnail(url=GOAT_ICON_URL)
                embed.add_field(name="Getting Started", value="1. Visit the casino channel\n2. Use `!balance`\n3. Play games!", inline=False)
                await member.send(embed=embed)
            except discord.Forbidden:
                logger.warning(f"Couldn't send welcome DM to {member.name}")

    def create_api_app(self):
        app = FastAPI()
        api_secret = self.data_manager.config["api_secret_key"]

        async def verify_api_key(api_key: str = Header(...)):
            if not hmac.compare_digest(api_key, api_secret):
                raise HTTPException(status_code=401, detail="Invalid API key")

        @app.get("/bot_status", dependencies=[Depends(verify_api_key)])
        async def bot_status():
            return {"status": "online", "uptime": time.time() - self.start_time, "guilds": len(self.guilds)}

        # New API endpoint for user balance (Admin/External Tool Use)
        @app.get("/user_balance/{user_id}", dependencies=[Depends(verify_api_key)])
        async def get_user_balance(user_id: int):
            try:
                user_data = await self.data_manager.get_user_data(user_id)
                return {"user_id": user_id, "balance": user_data['balance'], "level": user_data['level']}
            except Exception as e:
                raise HTTPException(status_code=500, detail=str(e))

        return app

    async def start_api_server(self):
        config = uvicorn.Config(self.api_app, host="0.0.0.0", port=API_PORT, log_level="info")
        server = uvicorn.Server(config)
        loop = asyncio.get_running_loop()
        loop.create_task(server.serve())
        logger.info(f"API server started on port {API_PORT}")

    @tasks.loop(minutes=5)
    async def performance_monitor(self):
        try:
            mem = psutil.Process().memory_info().rss / 1024 ** 2
            cpu = psutil.cpu_percent()
            logger.info(f"📊 Performance: {mem:.2f}MB RAM, {cpu}% CPU")
        except Exception as e:
            logger.error(f"Performance monitor error: {e}")

    @tasks.loop(hours=1)
    async def gang_pool_distribution(self):
        await self.wait_until_ready()
        logger.info("💰 Starting hourly gang pool distribution...")

        try:
            async with self.data_manager.locks["gang_pool"]:
                pool_data = self.data_manager.gang_pool
                total_pool = pool_data.get("total_pool", 0)
                contributions = pool_data.get("hourly_contributions", {})

                if total_pool <= 0 or not contributions:
                    logger.info("Pool is empty. No distribution will occur.")
                    pool_data["hourly_contributions"] = {}
                    pool_data["last_distribution"] = int(time.time())
                    await self.data_manager.save_gang_pool()
                    return

                gang_config = self.data_manager.config['gang_pool_config']
                owner_share_percent = gang_config['owner_share']
                player_share_percent = gang_config['active_player_share']

                owner_cut = int(total_pool * owner_share_percent)
                players_pot = total_pool - owner_cut

                total_player_contributions = sum(contributions.values())

                guild = self.get_guild(int(self.data_manager.config['guild_id']))
                if not guild:
                    logger.error(f"Cannot find guild with ID {self.data_manager.config['guild_id']}. Aborting distribution.")
                    return

                if owner_cut > 0:
                    try:
                        await self.data_manager.update_balance(guild.owner_id, owner_cut, "Gang Pool Owner Share")
                        logger.info(f"Distributed {owner_cut:,} GP to server owner {guild.owner.name}.")
                    except Exception as e:
                        logger.error(f"Failed to distribute owner's share: {e}")

                distribution_details = []
                for user_id_str, amount in contributions.items():
                    user_id = int(user_id_str)
                    share_percentage = amount / total_player_contributions
                    winnings = int(players_pot * share_percentage)

                    if winnings > 0:
                        try:
                            await self.data_manager.update_balance(user_id, winnings, "Gang Pool Player Share")
                            user = self.get_user(user_id) or await self.fetch_user(user_id)
                            distribution_details.append(f"• {user.mention}: **{winnings:,} GP**")
                            logger.info(f"Distributed {winnings:,} GP to user {user_id}.")
                        except Exception as e:
                            logger.error(f"Failed to distribute share to user {user_id}: {e}")

                pool_channel_name = 'osrs-gold-pool'
                if channel := discord.utils.get(guild.channels, name=pool_channel_name):
                    embed = discord.Embed(
                        title="💰 Gang Pool Hourly Payout! 💰",
                        description=f"A total of **{total_pool:,} GP** has been distributed!",
                        color=GOAT_GOLD,
                        timestamp=datetime.datetime.now(datetime.timezone.utc)
                    )
                    embed.add_field(name="👑 Owner's Share", value=f"`{owner_cut:,}` GP paid to {guild.owner.mention}", inline=False)
                    if distribution_details:
                        embed.add_field(name="🏆 Player Payouts", value="\n".join(distribution_details), inline=False)
                    else:
                        embed.add_field(name="🏆 Player Payouts", value="No players earned a payout this hour.", inline=False)

                    await channel.send(embed=embed)

                pool_data["total_pool"] = 0
                pool_data["hourly_contributions"] = {}
                pool_data["last_distribution"] = int(time.time())
                await self.data_manager.save_gang_pool()
                logger.info("✅ Hourly gang pool distribution complete.")
        except Exception as e:
            logger.error(f"Gang pool distribution error: {e}")

    @tasks.loop(hours=24)
    async def backup_task(self):
        await self.wait_until_ready()
        logger.info("💾 Starting daily data backup...")

        timestamp = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        backup_path = Path(BACKUP_DIR) / f"backup_{timestamp}"

        try:
            backup_path.mkdir(parents=True, exist_ok=True)

            files_to_backup = [
                CONFIG_FILE, GAME_STATE_FILE, POOL_STATE_FILE,
                WELCOME_CONFIG_FILE, SECRETS_FILE, LOG_FILE
            ]

            for filename in files_to_backup:
                if os.path.exists(filename):
                    shutil.copy(filename, backup_path / filename)

            # Robustness: Backup Rotation
            all_backups = sorted([p for p in Path(BACKUP_DIR).iterdir() if p.is_dir()], key=os.path.getmtime, reverse=True)
            if len(all_backups) > MAX_BACKUPS:
                for old_backup in all_backups[MAX_BACKUPS:]:
                    shutil.rmtree(old_backup)
                    logger.info(f"Removed old backup: {old_backup.name}")

            logger.info(f"✅ Successfully backed up data to {backup_path}. Total backups: {len(all_backups[:MAX_BACKUPS])}")

        except Exception as e:
            logger.error(f"❌ Backup failed: {e}")

    async def close(self):
        await self.asset_manager.close()
        await super().close()

# =================================================================================
# COMMAND IMPLEMENTATIONS - ENHANCED
# =================================================================================
async def setup_commands(bot: GoatGangBot):
    # --- Existing Commands ---
    @bot.command(name="setserver")
    @commands.has_permissions(administrator=True)
    async def setserver(ctx):
        """Configures the server with required channels and roles"""
        await ctx.defer()
        try:
            config = bot.data_manager.config
            guild = ctx.guild
            category_name = "GOAT-GANG Casino"
            category = discord.utils.get(guild.categories, name=category_name)
            if not category:
                category = await guild.create_category(category_name)
            for channel_name, settings in config['server_config']['required_channels'].items():
                if not discord.utils.get(guild.channels, name=channel_name):
                    await category.create_text_channel(name=channel_name, topic=settings['topic'])
                    logger.info(f"Created channel: {channel_name}")
            if not discord.utils.get(guild.roles, name="Goated"):
                await guild.create_role(name="Goated", color=discord.Color.gold())
            await ctx.followup.send("✅ Server setup complete! Required channels and roles created.")
        except Exception as e:
            logger.error(f"Server setup failed: {e}")
            await ctx.followup.send(f"❌ Setup failed: {e}")

    @bot.command(name="casino_panel")
    @commands.has_permissions(manage_channels=True)
    async def casino_panel(ctx):
        await ctx.message.delete()
        embed = discord.Embed(title="🐐 Welcome to the ☆GOAT-GANG-GAMES☆ Hub ♑️", color=GOAT_BLUE)
        embed.set_image(url="https://i.ibb.co/qFJFpPk/1000044844.jpg")
        await ctx.send(embed=embed, view=MainMenuView())

    @bot.command(name="balance", aliases=["bal"])
    async def balance(ctx, member: Optional[discord.Member] = None):
        target = member or ctx.author
        user_data = await bot.data_manager.get_user_data(target.id)
        embed = discord.Embed(title=f"{target.display_name}'s Balance", color=GOAT_GOLD)
        embed.add_field(name="GP Balance", value=f"**{user_data['balance']:,} GP**", inline=False)
        embed.add_field(name="Level", value=f"**{user_data['level']}**", inline=True)
        embed.add_field(name="XP", value=f"**{user_data['xp']:,}**", inline=True)
        await ctx.send(embed=embed)

    @bot.command(name="addbalance", aliases=["credit"])
    @commands.has_permissions(administrator=True)
    async def add_balance(ctx, member: Union[discord.Member, discord.User], amount: int):
        if amount <= 0: return await ctx.send("❌ Amount must be positive.")
        reason = f"Admin credit by {ctx.author.name} ({ctx.author.id})"
        try:
            new_balance = await bot.data_manager.update_balance(member.id, amount, reason=reason)
            embed = discord.Embed(title="Balance Update Successful", description=f"✅ Added `{amount:,}` GP to {member.mention}.", color=SUCCESS_GREEN)
            embed.add_field(name="New Balance", value=f"{new_balance:,} GP")
            await ctx.send(embed=embed)
        except ValueError as e:
            await ctx.send(f"❌ Error: {e}")

    @bot.command(name="removebalance", aliases=["debit"])
    @commands.has_permissions(administrator=True)
    async def remove_balance(ctx, member: Union[discord.Member, discord.User], amount: int):
        if amount <= 0: return await ctx.send("❌ Amount must be positive.")
        reason = f"Admin debit by {ctx.author.name} ({ctx.author.id})"
        try:
            new_balance = await bot.data_manager.update_balance(member.id, -amount, reason=reason)
            embed = discord.Embed(title="Balance Update Successful", description=f"✅ Removed `{amount:,}` GP from {member.mention}.", color=ERROR_RED)
            embed.add_field(name="New Balance", value=f"{new_balance:,} GP")
            await ctx.send(embed=embed)
        except ValueError as e:
            await ctx.send(f"❌ Error: {e}")

    @bot.command(name="setbalance")
    @commands.has_permissions(administrator=True)
    async def set_balance(ctx, member: Union[discord.Member, discord.User], amount: int):
        if amount < 0: return await ctx.send("❌ Amount cannot be negative.")
        
        # Get current balance to calculate the difference for the audit log
        user_data = await bot.data_manager.get_user_data(member.id)
        current_balance = user_data['balance']
        difference = amount - current_balance
        reason = f"Admin set balance to {amount:,} GP by {ctx.author.name} ({ctx.author.id})"

        # Use update_balance to leverage its audit logging and security checks
        try:
            new_balance = await bot.data_manager.update_balance(member.id, difference, reason=reason)
            embed = discord.Embed(title="Balance Set Successful", description=f"✅ Set {member.mention}'s balance to `{new_balance:,}` GP.", color=GOAT_BLUE)
            await ctx.send(embed=embed)
        except ValueError as e:
            await ctx.send(f"❌ Error: {e}")

    @bot.command(name="setwelcome")
    @commands.has_permissions(manage_guild=True)
    async def set_welcome(ctx, channel: discord.TextChannel, *, message: str):
        guild_id = str(ctx.guild.id)
        wc = bot.data_manager.welcome_config
        if 'guilds' not in wc:
            wc['guilds'] = {}
        wc['guilds'][guild_id] = {"channel_id": channel.id, "message": message}
        await bot.data_manager.save_welcome_config()
        await ctx.send(f"✅ Welcome message set for {channel.mention}")

    # --- New Features ---

    @bot.command(name="daily")
    async def daily_bonus(ctx):
        """Claim your daily GP bonus."""
        await ctx.defer()
        user_id = ctx.author.id
        dm = bot.data_manager
        user_data = await dm.get_user_data(user_id)
        
        # 24 hours in seconds
        cooldown = 24 * 60 * 60
        time_since_last = time.time() - user_data['last_daily']
        
        if time_since_last < cooldown:
            remaining = cooldown - time_since_last
            hours = int(remaining // 3600)
            minutes = int((remaining % 3600) // 60)
            return await ctx.send(f"⏳ You can claim your next daily bonus in **{hours}h {minutes}m**.", ephemeral=True)

        # Daily bonus scales with level
        bonus_amount = 5_000_000 + (user_data['level'] * 1_000_000)
        
        await dm.update_balance(user_id, bonus_amount, "Daily Bonus Claim")
        user_data['last_daily'] = int(time.time())
        await dm.save_config()
        
        embed = discord.Embed(title="💰 Daily Bonus Claimed!", description=f"You received **{bonus_amount:,} GP**!", color=SUCCESS_GREEN)
        embed.set_footer(text=f"Your new balance: {user_data['balance'] + bonus_amount:,} GP")
        await ctx.send(embed=embed)

    @bot.command(name="rank")
    async def rank(ctx, member: Optional[discord.Member] = None):
        """Shows the user's current level and XP."""
        target = member or ctx.author
        user_data = await bot.data_manager.get_user_data(target.id)
        
        current_level = user_data['level']
        current_xp = user_data['xp']
        
        # XP needed for next level: 1000 * (Level)^2
        next_level = current_level + 1
        xp_for_next_level = 1000 * (next_level ** 2)
        xp_needed = xp_for_next_level - current_xp
        
        embed = discord.Embed(title=f"🐐 {target.display_name}'s Rank Status", color=GOAT_GOLD)
        embed.add_field(name="Current Level", value=f"**{current_level}**", inline=True)
        embed.add_field(name="Total XP", value=f"**{current_xp:,}**", inline=True)
        embed.add_field(name="XP to Next Level", value=f"**{xp_needed:,}** (Level {next_level})", inline=False)
        
        # Simple progress bar
        xp_in_current_level = current_xp - (1000 * (current_level ** 2))
        xp_for_current_level_up = xp_for_next_level - (1000 * (current_level ** 2))
        progress = int((xp_in_current_level / xp_for_current_level_up) * 20)
        progress_bar = "█" * progress + "░" * (20 - progress)
        embed.add_field(name="Progress", value=f"`[{progress_bar}]` {xp_in_current_level:,}/{xp_for_current_level_up:,}", inline=False)
        
        await ctx.send(embed=embed)

    @bot.command(name="audit")
    @commands.has_permissions(administrator=True)
    async def audit_log(ctx, member: Union[discord.Member, discord.User]):
        """Shows the last 10 balance transactions for a user."""
        user_data = await bot.data_manager.get_user_data(member.id)
        log = user_data.get('audit_log', [])
        
        if not log:
            return await ctx.send(f"❌ No audit log entries found for {member.mention}.")

        log_entries = []
        for entry in log[-10:]: # Last 10 entries
            time_str = datetime.datetime.fromtimestamp(entry['timestamp']).strftime('%Y-%m-%d %H:%M:%S')
            sign = "+" if entry['amount'] >= 0 else ""
            log_entries.append(f"`{time_str}`: {sign}{entry['amount']:,} GP | New Bal: {entry['new_balance']:,} GP | Reason: {entry['reason']}")

        embed = discord.Embed(title=f"Audit Log for {member.display_name}", color=GOAT_BLUE)
        embed.description = "\n".join(log_entries)
        await ctx.send(embed=embed)

# =================================================================================
# BOT STARTUP
# =================================================================================
if __name__ == '__main__':
    data_manager = SecureDataManager()
    bot_token = DEFAULT_CONFIG.get("bot_token")
    if os.path.exists(CONFIG_FILE):
        try:
            with open(SECRETS_FILE, 'rb') as f_secret:
                key = f_secret.read()
            fernet = Fernet(key)
            with open(CONFIG_FILE, 'rb') as f_config:
                encrypted = f_config.read()
            compressed = fernet.decrypt(encrypted)
            json_data = json.loads(zlib.decompress(compressed))
            bot_token = json_data.get("bot_token", bot_token)
        except Exception as e:
            logger.warning(f"Could not load encrypted config, falling back to default. Reason: {e}")
            pass

    if not bot_token or "YOUR_BOT_TOKEN_HERE" in bot_token:
        logger.critical("FATAL ERROR: Bot token not set! Edit the DEFAULT_CONFIG section in your script or set the DISCORD_BOT_TOKEN environment variable.")
        sys.exit(1)

    bot = GoatGangBot(data_manager)
    try:
        bot.run(bot_token)
    except discord.LoginFailure:
        logger.critical("Invalid bot token. Please check the config.")
    except Exception as e:
        logger.critical(f"Fatal error during bot execution:", exc_info=e)
