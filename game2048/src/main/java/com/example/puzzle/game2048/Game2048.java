package com.example.puzzle.game2048;

import java.util.Arrays;
import java.util.Random;

/** Pure Java rules: no Android dependencies. Indices are row-major. */
public final class Game2048 {
    public enum Direction { LEFT, RIGHT, UP, DOWN }
    private final Random random;
    private int[] cells = new int[16];
    private long score;
    private int[] moveTargets = new int[16];
    private boolean[] mergedCells = new boolean[16];
    private int spawnedIndex = -1;
    public int[] getMoveTargets() { return moveTargets.clone(); }
    public boolean[] getMergedCells() { return mergedCells.clone(); }
    public int getSpawnedIndex() { return spawnedIndex; }

    public Game2048() { this(new Random()); }
    public Game2048(Random random) { this.random = random; restart(); }
    public void restart() { Arrays.fill(cells, 0); score = 0; spawn(); spawn(); }
    public int[] getCells() { return cells.clone(); }
    public long getScore() { return score; }
    public boolean hasWon() {
        for (int cell : cells) if (cell >= 2048) return true;
        return false;
    }
    public void restore(int[] board, long score) {
        if (board == null || board.length != 16 || score < 0)
            throw new IllegalArgumentException("Invalid saved game");
        boolean any = false;
        for (int value : board) {
            if (value < 0 || value == 1 || (value != 0 && (value & (value - 1)) != 0))
                throw new IllegalArgumentException("Tiles must be powers of two");
            any |= value != 0;
        }
        if (!any) throw new IllegalArgumentException("Empty saved game");
        this.cells = board.clone(); this.score = score;
    }
    /** Returns false for a no-op; those moves never spawn a tile. */
    public boolean move(Direction direction) {
        int[] next = cells.clone();
        Arrays.fill(moveTargets, -1);
        Arrays.fill(mergedCells, false);
        spawnedIndex = -1;
        long gained = 0;
        for (int line = 0; line < 4; line++) {
            int[] packed = new int[4]; int[] sources = new int[4]; int count = 0;
            for (int pos = 0; pos < 4; pos++) {
                int value = cells[index(direction, line, pos)];
                if (value != 0) { sources[count] = index(direction, line, pos); packed[count++] = value; }
            }
            int[] merged = new int[4]; int out = 0;
            for (int p = 0; p < count; p++) {
                int value = packed[p];
                int target = index(direction, line, out);
                moveTargets[sources[p]] = target;
                if (p + 1 < count && value == packed[p + 1] && value <= Integer.MAX_VALUE / 2) {
                    moveTargets[sources[p + 1]] = target;
                    mergedCells[target] = true;
                    value *= 2; gained += value; p++;
                }
                merged[out++] = value;
            }
            for (int pos = 0; pos < 4; pos++) next[index(direction, line, pos)] = merged[pos];
        }
        if (Arrays.equals(next, cells)) return false;
        cells = next; score += gained; spawn(); return true;
    }
    private static int index(Direction d, int line, int pos) {
        switch (d) {
            case RIGHT: return line * 4 + 3 - pos;
            case UP: return pos * 4 + line;
            case DOWN: return (3 - pos) * 4 + line;
            default: return line * 4 + pos;
        }
    }
    public boolean canMove() {
        for (int i = 0; i < 16; i++) {
            if (cells[i] == 0) return true;
            if (cells[i] <= Integer.MAX_VALUE / 2 &&
                ((i % 4 < 3 && cells[i] == cells[i + 1]) ||
                 (i < 12 && cells[i] == cells[i + 4]))) return true;
        }
        return false;
    }
    private void spawn() {
        int[] empty = new int[16]; int count = 0;
        for (int i = 0; i < 16; i++) if (cells[i] == 0) empty[count++] = i;
        if (count > 0) {
            spawnedIndex = empty[random.nextInt(count)];
            cells[spawnedIndex] = random.nextDouble() < 0.9 ? 2 : 4;
        }
    }
}
