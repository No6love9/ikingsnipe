# 🐐 iKingSnipe: GoatGang Edition

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Framework: DreamBot 3](https://img.shields.io/badge/Framework-DreamBot%203-blue.svg)](https://dreambot.org/)

**iKingSnipe GoatGang Edition** is an enterprise-grade OSRS casino framework designed for professional hosts and scripters. Built on a modern **Tree-Branch-Leaf** architecture, it offers unparalleled stability, modularity, and human-like behavior.

## 🌟 Features

- **Modular Architecture**: Uses a hierarchical tree structure for clean, maintainable code.
- **Provably Fair**: Cryptographically secure game results using SHA-256.
- **Discord Integration**: Real-time notifications via Webhooks and interactive commands via a JDA-based bot.
- **Database Driven**: Full MySQL integration for tracking player balances, game history, and profit.
- **Advanced Humanization**: Implements micro-breaks, camera jitter, and randomized delays to mimic human play.
- **Automated Operations**: Built-in support for banking, restocking, and muling.

## 🏗️ Architecture

The framework is organized into three main components:

1.  **Root**: The entry point that manages the global state and initializes the tree.
2.  **Branches**: Decision-making nodes that evaluate conditions (e.g., `MaintenanceBranch`, `HostingBranch`).
3.  **Leaves**: Executable units that perform specific actions (e.g., `TradeLeaf`, `MulingLeaf`, `AutoChatLeaf`).

## 🚀 Getting Started

### Prerequisites

- **Java 11+**
- **DreamBot 3 Client**
- **MySQL Server**
- **Python 3.8+** (for the Discord management bot)

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/no6love9/ikingsnipe.git
    ```
2.  **Configure Environment**:
    Copy `.env.example` to `.env` and fill in your database and Discord credentials.
3.  **Setup Database**:
    Run the provided SQL scripts (if available) or allow the framework to auto-generate tables on first run.
4.  **Build the Project**:
    Use Gradle to build the JAR file:
    ```bash
    ./gradlew build
    ```
5.  **Run the Discord Bot**:
    ```bash
    pip install -r requirements.txt
    python discord_bot/casino_bot.py
    ```

## 🤝 Contributing

We welcome contributions! Please feel free to submit Pull Requests or open Issues for bugs and feature requests.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Part of the **GoatGang** ecosystem. Proliferate your OSRS experience.*
