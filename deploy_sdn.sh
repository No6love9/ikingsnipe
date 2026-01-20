#!/bin/bash
# Build
./gradlew clean build -x test

# Create release directory if it doesn't exist
mkdir -p sdn_release

# Copy artifact into SDN repo structure
cp "build/libs/ikingsnipe-14.0.0-GOATGANG.jar" "sdn_release/iKingsnipe.jar"

# Commit + push to SDN
git add sdn_release/iKingsnipe.jar
git commit -m "Deploy: $(date '+%Y-%m-%d %H:%M:%S')"
git push sdn main
