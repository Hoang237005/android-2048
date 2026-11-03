# Android 2048 — Java minigame

A native Android 2048 game for a group puzzle collection. All application code is Java; screen layout, strings and themes use XML. No Kotlin, Compose, third-party runtime dependencies, network permission or backend is needed.

## Features

- 4 × 4 board, four-direction swipes and accessible direction buttons.
- Standard merge rules: a tile merges once per move; 2 + 2 + 2 + 2 becomes 4 + 4.
- A new tile appears only after a move changes the board (90% 2, 10% 4).
- Current score, persistent best score, and automatic saved-board restoration.
- Win dialog at 2048, continued play, and game-over status.
- Confirmed restart and optional Back to games button (hidden in standalone demo).
- Scrollable layout for small screens and landscape; system-bar insets.
- Separate reusable library module with prefixed resources and isolated preferences.

## Open and run

1. Extract the ZIP and open the **android-2048** folder in Android Studio (the folder containing `settings.gradle`).
2. Set **Gradle JDK to the bundled JBR 25** in Android Studio's Gradle settings. Android Studio Quail 4 supports Android Gradle Plugin 9.4.0. Java source/target compatibility remains 17.
3. Install **Android SDK Platform 35** and **Build Tools 36.0.0** in SDK Manager if prompted. The wrapper downloads Gradle 9.6.0 on first sync; internet is needed for initial setup.
4. Let Gradle sync, select the `app` configuration, and press Run on an emulator or connected phone running **Android 6.0 / API 23 or newer**.

Windows command line:

```bat
gradlew.bat :app:assembleDebug
```

macOS/Linux:

```sh
chmod +x gradlew
./gradlew :app:assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
Android Studio normally creates `local.properties` with your SDK path. Do not commit this machine-specific file.

Build-version reference: https://developer.android.com/build/releases/agp-9-4-0-release-notes

## Integrate into the group's app (for the group leader)

Copy only the **game2048** directory into the root of the group's project, next to its `app` directory. Keep its Java package unchanged; it does not have to match the host application's package.

1. Add to the group's `settings.gradle`:

```groovy
include ':game2048'
```

For `settings.gradle.kts`, use `include(":game2048")`.

2. Ensure the root project declares the Android library plugin at the **same AGP version already used by the group's app**. For this sample, the root declarations are:

```groovy
plugins {
    id 'com.android.application' version '9.4.0' apply false
    id 'com.android.library' version '9.4.0' apply false
}
```

Do not duplicate an existing plugin declaration or replace the group's whole root build file. If the team uses a version catalog, add the matching Android library plugin alias instead.

3. In the group's app module, add:

```groovy
dependencies {
    implementation project(':game2048')
}
```

Kotlin DSL equivalent: `implementation(project(":game2048"))`.

4. The host app needs `minSdk` 23 or higher. This module uses `compileSdk` 35 and Java 17; align build settings with the group before merging.

5. Launch from a menu button inside the group's Activity:

```java
import android.content.Intent;
import com.example.puzzle.game2048.Game2048Activity;

// Inside your Activity's onCreate, after setContentView:
findViewById(R.id.button2048).setOnClickListener(view ->
    startActivity(new Intent(this, Game2048Activity.class))
);
```

`button2048` is an example ID: replace it with the actual menu button ID. From a Fragment use `new Intent(requireContext(), Game2048Activity.class)`.

The library manifest automatically registers the game Activity. It has no launcher intent and is not exported. Do not copy `DemoActivity` or the demo app's launcher manifest into the group app. Back closes the game and returns to the previous Activity. The game uses its own theme and SharedPreferences file `puzzle_game2048_v1`; no host Application class or database changes are needed.

## Main files to explain during presentation

| File | Responsibility |
| --- | --- |
| `Game2048.java` | Pure Java board, directional merging, score, spawning, win and loss rules |
| `BoardView.java` | Canvas board drawing, square sizing, swipe interpretation, board accessibility description |
| `Game2048Activity.java` | Screen controls, lifecycle, saved state, score and dialogs |
| `res/layout/g2048_activity.xml` | XML screen layout |
| `res/values/strings.xml` | Visible text; add a `values-vi` translation if needed |
| `Game2048Test.java` | Executable deterministic rules tests |
| `app/.../DemoActivity.java` | Standalone test launcher only |

The engine reads every row/column in movement order, removes empty spaces, merges neighboring equal values once, writes the result back, and spawns a tile only if the board changed. The Activity saves after every successful move and on pause, so a recreated screen restores the previous board and score.

## Tests and validation

Run the dependency-free rules tests with a JDK installed:

```bat
scripts\test-engine.bat
```

or:

```sh
sh scripts/test-engine.sh
```

With the Android build environment configured, `./gradlew :game2048:engineTest` runs the same tests. The checked-in GitHub Actions workflow runs engine tests, builds a debug APK and runs Android lint on pushes and pull requests.

Delivery validation: **43,614 engine and animation-mapping assertions passed**, covering exhaustive combinations of 0/2/4/8 in a line in all directions, no-op moves, score, initial spawn, dead boards, wins, restart, invalid saves and seeded random games. XML documents were parsed. See `VALIDATION.md` for exact limitations. Automated game rules tests do not replace on-device interaction tests.

Before the group submits:

- Play with swipe and direction controls; confirm one merge per tile per move.
- Rotate mid-game and return after closing/reopening the screen; verify board and score.
- Confirm New game cancels/resets correctly and preserves best score.
- Check 2048 dialog, continued play and a full board with no moves.
- Test small screen/landscape, large font size, and TalkBack direction buttons.
- Launch from the group's menu; confirm Back returns to it.

## Upload to GitHub

If uploading as a separate new repository, create an empty repository on GitHub and run these commands from this folder, replacing the URL with your own:

```sh
git init -b main
git add .
git commit -m "Add reusable Java Android 2048 game"
git remote add origin https://github.com/YOUR_USERNAME/android-2048.git
git push -u origin main
```

If contributing to an existing group repository, clone that repository, create a `feature/2048` branch, copy the `game2048` module, apply the integration changes above, and open a pull request. Do not overwrite the existing group's app/build settings with this demo project.

The GitHub workflow is for this standalone two-module project. When integrating, adapt the team's existing workflow instead of replacing it.

## Attribution

2048 is the puzzle popularized by Gabriele Cirulli: https://github.com/gabrielecirulli/2048 . This project is a new Java/Android implementation of the rules and does not bundle the original game's JavaScript or assets.


## Smooth-movement update

Tiles slide for about 170 ms, then merged tiles pop and new tiles fade/scale in.
Moves are ignored during the 260 ms animation to prevent overlapping turns.
Direction buttons show arrows with spoken accessibility labels. How to play opens
an Android dialog whose text scrolls when necessary.

The standalone demo hides Back to games. To show it when launching from the group menu:

```java
Intent intent = new Intent(this, Game2048Activity.class);
intent.putExtra(Game2048Activity.EXTRA_SHOW_BACK_TO_GAMES, true);
startActivity(intent);
```

### Install this update
Close the project and extract the updated ZIP into a fresh folder, then open
its android-2048 folder, sync and run app on the existing emulator. Keep the
same application ID and signing key to retain installed scores. Alternatively,
replace only the game2048/src directory in your existing project with the one
from this ZIP, then sync/run. No build-tool configuration changes are needed.

### Animation checks on a device
Check slides and double merges in all directions, quick repeated swipes, restart,
rotation or backgrounding during a move, arrows and TalkBack labels, and the help
dialog with large text and in landscape. The help text should remain scrollable.
