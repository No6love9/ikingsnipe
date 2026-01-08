#!/bin/bash
# DreamBot Script Deployment Script for Linux

# Configuration
SCRIPT_NAME="TitanCasino"
DREAMBOT_SCRIPTS_DIR="$HOME/DreamBot/Scripts"

# Build the project
echo "Building $SCRIPT_NAME..."
./gradlew clean build

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Create DreamBot scripts directory if it doesn't exist
    mkdir -p "$DREAMBOT_SCRIPTS_DIR"
    
    # Copy the JAR to DreamBot scripts folder
    echo "Deploying to $DREAMBOT_SCRIPTS_DIR..."
    cp output/TitanCasino.jar "$DREAMBOT_SCRIPTS_DIR/"
    
    echo "Deployment complete! You can now find '$SCRIPT_NAME' in your DreamBot client."
else
    echo "Build failed. Please check the errors above."
    exit 1
fi
