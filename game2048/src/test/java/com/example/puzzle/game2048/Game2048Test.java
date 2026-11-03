package com.example.puzzle.game2048;

import java.util.Arrays;
import java.util.Random;

/** Dependency-free executable tests; run with the instructions in README.md. */
public final class Game2048Test {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    private static final class LastEmptyRandom extends Random {
        private static final long serialVersionUID = 1L;
        @Override public int nextInt(int bound) { return bound - 1; }
        @Override public double nextDouble() { return 0; }
    }
    private static int index(Game2048.Direction d, int line, int pos) {
        if (d == Game2048.Direction.LEFT) return line * 4 + pos;
        if (d == Game2048.Direction.RIGHT) return line * 4 + 3 - pos;
        if (d == Game2048.Direction.UP) return pos * 4 + line;
        return (3 - pos) * 4 + line;
    }
    public static void main(String[] args) {
        // Exhaust every four-cell line made of 0, 2, 4 and 8 in all four directions.
        // Reference rule processes incoming tiles and locks the last merged tile.
        for (Game2048.Direction d : Game2048.Direction.values()) {
            for (int code = 1; code < 256; code++) {
                int n = code; int[] input = new int[16];
                int[] output = new int[4]; int used = 0; boolean lastMerged = false;
                long score = 0;
                for (int p = 0; p < 4; p++) {
                    int digit = n % 4; n /= 4;
                    int v = digit == 0 ? 0 : 1 << digit;
                    input[index(d, 0, p)] = v;
                    if (v == 0) continue;
                    if (used > 0 && output[used - 1] == v && !lastMerged) {
                        output[used - 1] += v; score += 2L * v; lastMerged = true;
                    } else { output[used++] = v; lastMerged = false; }
                }
                int[] expected = new int[16];
                for (int p = 0; p < 4; p++) expected[index(d, 0, p)] = output[p];
                boolean changed = !Arrays.equals(input, expected);
                if (changed) for (int p = 15; p >= 0; p--) if (expected[p] == 0) { expected[p] = 2; break; }
                Game2048 game = new Game2048(new LastEmptyRandom()); game.restore(input, 0);
                check(game.move(d) == changed, "Changed flag " + d + " " + code);
                check(Arrays.equals(game.getCells(), expected), "Merge/spawn " + d + " " + code);
                check(game.getScore() == score, "Score " + d + " " + code);
                if (changed) {
                    int[] targets = game.getMoveTargets(), reconstructed = new int[16], counts = new int[16];
                    for (int source = 0; source < 16; source++) if (input[source] != 0) {
                        check(targets[source] >= 0 && targets[source] < 16, "Every tile has destination");
                        reconstructed[targets[source]] += input[source]; counts[targets[source]]++;
                    }
                    boolean[] merges = game.getMergedCells();
                    for (int target = 0; target < 16; target++) {
                        check(counts[target] <= 2, "At most two tiles per destination");
                        check(merges[target] == (counts[target] == 2), "Merge highlight matches arrivals");
                    }
                    int spawn = game.getSpawnedIndex();
                    check(spawn >= 0 && reconstructed[spawn] == 0, "Spawn occupies empty destination");
                    reconstructed[spawn] = game.getCells()[spawn];
                    check(Arrays.equals(reconstructed, game.getCells()), "Animation reconstructs final board");
                } else check(game.getSpawnedIndex() == -1, "No-op has no spawn animation");
            }
        }
        Game2048 game = new Game2048(new Random(42));
        int occupied = 0; for (int v : game.getCells()) if (v != 0) occupied++;
        check(occupied == 2 && game.getScore() == 0, "Initial board");
        int[] dead = {2,4,2,4,4,2,4,2,2,4,2,4,4,2,4,2};
        game.restore(dead, 32); check(!game.canMove(), "Game over");
        for (Game2048.Direction d : Game2048.Direction.values()) check(!game.move(d), "Dead board no-op");
        check(game.getScore() == 32, "No-op score");
        dead[0] = 4; game.restore(dead, 0); check(game.canMove(), "Full board merge possible");
        int[] win = new int[16]; win[0] = 1024; win[1] = 1024;
        game.restore(win, 0); game.move(Game2048.Direction.LEFT);
        check(game.hasWon() && game.getScore() == 2048, "Win");
        int[] copy = game.getCells(); copy[0] = 0; check(game.getCells()[0] == 2048, "Defensive copy");
        game.restart(); check(!game.hasWon() && game.getScore() == 0, "Restart");
        boolean rejected = false;
        try { game.restore(new int[16], 0); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, "Reject invalid save");
        Random choices = new Random(13);
        for (int trial = 0; trial < 30; trial++) {
            game.restart();
            for (int move = 0; move < 1000 && game.canMove(); move++) {
                int[] before = game.getCells(); long sumBefore = sum(before), scoreBefore = game.getScore();
                boolean changed = game.move(Game2048.Direction.values()[choices.nextInt(4)]);
                long delta = sum(game.getCells()) - sumBefore;
                check(changed ? delta == 2 || delta == 4 : delta == 0, "Tile sum conservation plus spawn");
                check(game.getScore() >= scoreBefore, "Monotonic score");
                if (!changed) check(Arrays.equals(before, game.getCells()), "No-op unchanged");
            }
        }
        System.out.println("PASS: " + checks + " checks (exhaustive directional merges and random games)");
    }
    private static long sum(int[] board) { long sum = 0; for (int v : board) sum += v; return sum; }
}
