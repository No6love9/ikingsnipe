# 🚀 How to Run iKingSnipe: GoatGang Edition

Follow these steps to get your enterprise casino framework up and running.

## 1. Environment Setup
First, you need to configure your secrets and database settings.
1.  Locate the `.env.example` file in the root directory.
2.  Copy it and rename it to `.env`.
3.  Open `.env` and fill in your:
    *   `DISCORD_BOT_TOKEN`: From the Discord Developer Portal.
    *   `DB_PASS`: Your MySQL root password.
    *   `MASTER_PASSWORD`: Set to `sheba777` by default.

## 2. Build the Java Script
The script needs to be compiled into a JAR file that DreamBot can read.
1.  Open a terminal in the `ikingsnipe` folder.
2.  Run the build command:
    ```bash
    ./gradlew build
    ```
3.  Once finished, the compiled JAR will be in `build/libs/ikingsnipe-14.0.jar`.
4.  Copy this JAR to your DreamBot scripts folder (usually `C:\Users\YourName\DreamBot\Scripts\`).

## 3. Start the Discord Management Bot
The Discord bot handles your database and remote commands.
1.  Ensure you have Python installed.
2.  Install the required dependencies:
    ```bash
    pip install -r requirements.txt
    ```
3.  Run the bot:
    ```bash
    python discord_bot/casino_bot.py
    ```

## 4. Launch in DreamBot
1.  Open the **DreamBot 3** client.
2.  Log in to your OSRS account.
3.  Go to the **Scripts** tab and find **"GoatGang Edition"**.
4.  Click **Start**.
5.  The **Casino GUI** will appear. Configure your target location and settings, then click **Start Bot**.

## 🛠️ Troubleshooting
*   **Database Error**: Ensure your MySQL server is running and the credentials in `.env` match.
*   **Auth Failure**: Make sure you are using the correct `MASTER_PASSWORD` in the GUI.
*   **Bot Not Responding**: Check the terminal where `casino_bot.py` is running for error logs.

---
*Need help? Check the [DREAMBOT_FORUM_POST.md](DREAMBOT_FORUM_POST.md) for community support links.*
