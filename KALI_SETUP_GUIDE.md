# iKingSnipe Kali Linux Setup Guide

This guide explains how to use the automated setup script for the `ikingsnipe` repository on Kali Linux.

## 🚀 Quick Start

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone https://github.com/No6love9/ikingsnipe.git
   cd ikingsnipe
   ```

2. **Run the setup script**:
   ```bash
   sudo ./kali_setup.sh
   ```

## 🛠️ What the Script Does

The `kali_setup.sh` script provides a prompted UI to:
- **Configure Proxies**: Set up HTTP/HTTPS/SOCKS5 proxies for the entire setup process and system (APT).
- **Install Dependencies**: Installs Java 11, MySQL, Python 3, and other required tools.
- **Download DreamBot**: Automatically downloads the latest DreamBot launcher via your configured proxy.
- **Initialize Database**: Sets up the MySQL database and schema for the casino bot.
- **Build iKingSnipe**: Compiles the latest version of the script and places it in the DreamBot scripts folder.

## 🌐 Proxy Usage

If you configured a proxy during setup, you should also run DreamBot with proxy settings to ensure it can connect:

```bash
java -Dhttp.proxyHost=YOUR_PROXY_IP -Dhttp.proxyPort=YOUR_PROXY_PORT -jar ~/DreamBot/DBLauncher.jar
```

## 📁 Directory Structure

- **DreamBot Launcher**: `~/DreamBot/DBLauncher.jar`
- **iKingSnipe Scripts**: `~/DreamBot/Scripts/`
- **Database Name**: `goatgang`

## 📞 Support

For issues related to the script, please check the `build_log.txt` or the console output during setup.
