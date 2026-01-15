#!/bin/bash
echo "Testing compilation..."

# Find DreamBot JAR
DREAMBOT_JAR=$(find libs -name "*.jar" 2>/dev/null | head -1)

if [ -z "$DREAMBOT_JAR" ]; then
    echo "No DreamBot JAR found in libs/"
    exit 1
fi

echo "Using: $DREAMBOT_JAR"

# Clean
rm -rf build_test
mkdir -p build_test

# Compile
javac -encoding UTF-8 \
      -source 1.8 \
      -target 1.8 \
      -cp "$DREAMBOT_JAR" \
      -d build_test \
      $(find src/main/java -name "*.java") 2>&1

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    ls -lh build_test/com/ikingsnipe/
else
    echo "✗ Compilation failed"
fi
