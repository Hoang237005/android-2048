package com.example.puzzle.game2048;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;

public final class Game2048Activity extends Activity {
    public static final String EXTRA_SHOW_BACK_TO_GAMES = "com.example.puzzle.game2048.SHOW_BACK_TO_GAMES";
    private Game2048 game;
    private BoardView board;
    private TextView scoreView, bestView, statusView;
    private SharedPreferences preferences;
    private long best;
    private boolean winAcknowledged;
    private boolean resumed;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.g2048_activity);
        View root = findViewById(R.id.g2048_root);
        // System-bar insets also support hosts targeting Android 15 edge-to-edge.
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        root.requestApplyInsets();
        preferences = getSharedPreferences("puzzle_game2048_v1", MODE_PRIVATE);
        game = new Game2048(); best = preferences.getLong("best", 0);
        try {
            String saved = preferences.getString("board", "");
            if (!saved.isEmpty()) {
                String[] parts = saved.split(",");
                if (parts.length != 16) throw new IllegalArgumentException();
                int[] cells = new int[16];
                for (int i = 0; i < 16; i++) cells[i] = Integer.parseInt(parts[i]);
                game.restore(cells, preferences.getLong("score", 0));
                winAcknowledged = preferences.getBoolean("won", false);
            }
        } catch (IllegalArgumentException error) { game.restart(); winAcknowledged = false; }
        board = findViewById(R.id.g2048_board);
        scoreView = findViewById(R.id.g2048_score);
        bestView = findViewById(R.id.g2048_best);
        statusView = findViewById(R.id.g2048_status);
        board.setOnMoveListener(this::move);
        findViewById(R.id.g2048_left).setOnClickListener(v -> move(Game2048.Direction.LEFT));
        findViewById(R.id.g2048_right).setOnClickListener(v -> move(Game2048.Direction.RIGHT));
        findViewById(R.id.g2048_up).setOnClickListener(v -> move(Game2048.Direction.UP));
        findViewById(R.id.g2048_down).setOnClickListener(v -> move(Game2048.Direction.DOWN));
        findViewById(R.id.g2048_exit).setVisibility(
                getIntent().getBooleanExtra(EXTRA_SHOW_BACK_TO_GAMES, false) ? View.VISIBLE : View.GONE);
        findViewById(R.id.g2048_help).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.g2048_rules).setMessage(R.string.g2048_rules_text)
                .setPositiveButton(android.R.string.ok, null).show());
        findViewById(R.id.g2048_exit).setOnClickListener(v -> finish());
        findViewById(R.id.g2048_restart).setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(R.string.g2048_restart_title).setMessage(R.string.g2048_restart_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.g2048_new_game, (dialog, which) -> {
                board.finishAnimation(); game.restart(); winAcknowledged = false; render(); save();
            }).show());
        render(); save();
    }
    private void move(Game2048.Direction direction) {
        if (board.isAnimating()) return;
        int[] before = game.getCells();
        if (!game.move(direction)) return;
        best = Math.max(best, game.getScore());
        save();
        board.animateMove(before, game.getCells(), game.getMoveTargets(),
                game.getMergedCells(), game.getSpawnedIndex(), () -> {
                    render();
                    if (resumed && !isFinishing() && !isDestroyed()) checkWin();
                });
    }
    private void checkWin() {
        if (game.hasWon() && !winAcknowledged) {
            new AlertDialog.Builder(this).setTitle(R.string.g2048_win_title)
                .setMessage(R.string.g2048_win_message).setCancelable(false)
                .setPositiveButton(R.string.g2048_continue, (dialog, which) -> {
                    winAcknowledged = true; save();
                }).show();
        }
    }
    private void render() {
        best = Math.max(best, game.getScore());
        board.setCells(game.getCells());
        scoreView.setText(String.format(Locale.getDefault(), "%d", game.getScore()));
        bestView.setText(String.format(Locale.getDefault(), "%d", best));
        statusView.setText(!game.canMove() ? R.string.g2048_game_over
                : game.hasWon() ? R.string.g2048_keep_going : R.string.g2048_hint);
    }
    private void save() {
        StringBuilder values = new StringBuilder();
        for (int value : game.getCells()) { if (values.length() > 0) values.append(','); values.append(value); }
        preferences.edit().putString("board", values.toString()).putLong("score", game.getScore())
            .putLong("best", best).putBoolean("won", winAcknowledged).apply();
    }
    @Override protected void onResume() {
        super.onResume(); resumed = true; checkWin();
    }
    @Override protected void onPause() { resumed = false; super.onPause();
        if (board != null) board.finishAnimation();
        if (game != null) save(); }
}
