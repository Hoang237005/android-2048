#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/engine-tests
javac -d build/engine-tests game2048/src/main/java/com/example/puzzle/game2048/Game2048.java game2048/src/test/java/com/example/puzzle/game2048/Game2048Test.java
java -cp build/engine-tests com.example.puzzle.game2048.Game2048Test
