package com.example.puzzle.demo;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import com.example.puzzle.game2048.Game2048Activity;

/** Only a demo launcher; the actual game lives in the library module. */
public class DemoActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        startActivity(new Intent(this, Game2048Activity.class));
        finish();
    }
}
