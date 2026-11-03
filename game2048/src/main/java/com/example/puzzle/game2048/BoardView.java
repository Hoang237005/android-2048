package com.example.puzzle.game2048;

import android.content.Context;
import android.animation.ValueAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.LinearInterpolator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** Square, resolution-independent board. Swipes resolve along their dominant axis. */
public final class BoardView extends View {
    public interface OnMoveListener { void onMove(Game2048.Direction direction); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] cells = new int[16];
    private float startX, startY;
    private ValueAnimator animator;
    private float progress;
    private int[] previous, targets;
    private boolean[] merged;
    private int spawned;
    public boolean isAnimating() { return animator != null; }
    public void finishAnimation() {
        if (animator != null) animator.end();
    }
    public void animateMove(int[] before, int[] after, int[] destinations,
                            boolean[] merges, int spawn, Runnable complete) {
        finishAnimation();
        setCells(after);
        previous = before.clone(); targets = destinations.clone();
        merged = merges.clone(); spawned = spawn; progress = 0;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(260);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(value -> {
            progress = (float) value.getAnimatedValue(); invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                animator = null; previous = null; invalidate(); complete.run();
            }
        });
        animator.start();
    }
    @Override protected void onDetachedFromWindow() {
        finishAnimation(); super.onDetachedFromWindow();
    }
    private OnMoveListener listener;
    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs); setClickable(true);
    }
    public void setOnMoveListener(OnMoveListener listener) { this.listener = listener; }
    public void setCells(int[] values) {
        cells = values.clone();
        StringBuilder description = new StringBuilder("2048 board. ");
        for (int i = 0; i < 16; i++) {
            if (i % 4 == 0) description.append("Row ").append(i / 4 + 1).append(": ");
            description.append(cells[i] == 0 ? "empty" : String.valueOf(cells[i])).append(", ");
        }
        setContentDescription(description.toString()); invalidate();
    }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = resolveSize((int)(340 * getResources().getDisplayMetrics().density), widthSpec);
        setMeasuredDimension(width, width);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float gap = getWidth() * .025f, side = (getWidth() - gap * 5) / 4;
        paint.setColor(Color.rgb(185, 173, 159));
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), gap, gap, paint);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        for (int i = 0; i < 16; i++) drawTile(canvas, i % 4, i / 4, 0, 1, 255, gap, side);
        if (isAnimating() && progress < .65f) {
            float t = progress / .65f;
            t = 1 - (1 - t) * (1 - t); // Smooth deceleration into each destination.
            for (int i = 0; i < 16; i++) {
                if (previous[i] == 0) continue;
                int target = targets[i];
                drawTile(canvas, i % 4 + (target % 4 - i % 4) * t,
                        i / 4 + (target / 4 - i / 4) * t, previous[i], 1, 255, gap, side);
            }
        } else {
            float t = isAnimating() ? Math.min(1f, (progress - .65f) / .35f) : 1f;
            for (int i = 0; i < 16; i++) {
                if (cells[i] == 0) continue;
                float scale = 1; int alpha = 255;
                if (isAnimating()) {
                    if (merged[i]) scale += .13f * (float)Math.sin(Math.PI * t);
                    if (i == spawned) { scale = .65f + .35f * t; alpha = (int)(255 * t); }
                }
                drawTile(canvas, i % 4, i / 4, cells[i], scale, alpha, gap, side);
            }
        }
    }
    private void drawTile(Canvas canvas, float col, float row, int value,
                          float scale, int alpha, float gap, float side) {
        float x = gap + col * (side + gap), y = gap + row * (side + gap);
        canvas.save();
        canvas.scale(scale, scale, x + side / 2, y + side / 2);
        paint.setColor(tileColor(value)); paint.setAlpha(alpha);
        canvas.drawRoundRect(x, y, x + side, y + side, gap * .55f, gap * .55f, paint);
        if (value != 0) {
            String label = String.valueOf(value);
            paint.setColor(value <= 4 ? Color.rgb(93, 83, 73) : Color.WHITE);
            paint.setAlpha(alpha); paint.setTextSize(side * .44f);
            if (paint.measureText(label) > side * .84f)
                paint.setTextSize(paint.getTextSize() * side * .84f / paint.measureText(label));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(label, x + side / 2, y + side / 2 - (paint.ascent() + paint.descent()) / 2, paint);
        }
        paint.setAlpha(255); canvas.restore();
    }
    private int tileColor(int value) {
        switch (value) {
            case 0: return 0xffcdc1b4; case 2: return 0xffeee4da;
            case 4: return 0xffede0c8; case 8: return 0xffedaf70;
            case 16: return 0xffec9458; case 32: return 0xffec795b;
            case 64: return 0xffe95937; case 128: return 0xffdfbc55;
            case 256: return 0xffd8ad36; case 512: return 0xffcaa024;
            case 1024: return 0xffb78e13; case 2048: return 0xffa77e08;
            default: return 0xff403b36;
        }
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX(); startY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true); return true;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                float dx = event.getX() - startX, dy = event.getY() - startY;
                float threshold = 24 * getResources().getDisplayMetrics().density;
                if (Math.max(Math.abs(dx), Math.abs(dy)) < threshold) { performClick(); return true; }
                if (listener != null) listener.onMove(Math.abs(dx) > Math.abs(dy)
                    ? (dx > 0 ? Game2048.Direction.RIGHT : Game2048.Direction.LEFT)
                    : (dy > 0 ? Game2048.Direction.DOWN : Game2048.Direction.UP));
                return true;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false); return true;
            default: return true;
        }
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
