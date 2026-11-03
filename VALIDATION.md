# Smooth movement update validation

- 43,614 assertions passed using the dependency-free Java harness: existing rules,
  directional merges, scoring, seeded games, plus source-to-destination animation
  reconstruction, merge flags, and spawn location for exhaustive line patterns.
- Resource XML parses successfully.
- All Android Java sources compile against API 35 using ECJ and temporary R declarations; only five existing WindowInsets deprecation warnings remain. This is not a full APK build.
- Build versions are unchanged from the Quail4 package that ran on your machine.
- No emulator or full Android SDK is available here. APK build, resource linking,
  and visual/lifecycle interaction tests must be completed in Android Studio.
