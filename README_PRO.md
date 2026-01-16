# Elite Titan Casino Pro v5.0

Professional Enterprise Casino System for DreamBot, merged and enhanced by Manus AI.

## Key Features
- **Multi-Game Support**: Includes Craps, Dice Duel, Flower Poker, Blackjack, Hot/Cold, 55x2, and Dice War.
- **Robust Trade Handling**: Advanced anti-scam verification, trade queuing, and customizable trade presets (Fast, Balanced, Maximum Security).
- **Professional GUI**: Modern, organized control panel with real-time configuration updates.
- **Enterprise Features**: Integrated Jackpot system, automated muling, and humanization (micro-breaks, camera jitter).
- **JDK Compatibility**: Fully compatible with JDK 11, 17, 21, and JDK 25.
- **Discord Integration**: Real-time notifications for wins, losses, and system status.

## Installation
1. Place `snipes_scripts_enterprise_pro.jar` in your DreamBot scripts folder.
2. Ensure `dreambot-api.jar` is available in the `libs` folder if you are building from source.

## Development
This project uses Gradle for builds.
- To build: `./gradlew shadowJar`
- The output will be in `build/libs/snipes_scripts_enterprise_pro.jar`

## Merged Features from Provided Code
- **Enhanced Flower Poker**: Added escalating payouts based on hand strength (up to x10 for 5-of-a-kind).
- **Improved Dice Duel**: Robust tie handling and professional result messaging.
- **Professional Defaults**: Updated advertising messages and win/loss notifications for a more "Elite" feel.
- **Modern Build System**: Updated to support the latest Java versions (JDK 25) while maintaining LTS compatibility.

---
*Maintained and Enhanced by ikingsnipe & Manus AI*
