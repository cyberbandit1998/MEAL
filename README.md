# Copperpot

An Android client for [Mealie](https://github.com/mealie-recipes/mealie), the self-hosted recipe
manager. Browse and search your recipes, keep them available offline, add new ones (including from a
shared link), and manage shopping lists.

Copperpot is a fork of [Mealient](https://github.com/kirmanak/Mealient) by Kirill Kamakin, which is no
longer maintained. It is released under the same MIT license, see [LICENSE](LICENSE).

## DISCLAIMER

This project is developed independently from the core Mealie project. It is NOT associated with the
core Mealie developers. Report issues in this repository, NOT in the Mealie repository.

## What changed from Mealient

- **Works with current Mealie.** Mealient was written for Mealie v1 and fails against Mealie
  v3.29.0: recipes with ingredients do not load, and favorites, URL import and shopping lists are
  broken. This fork targets **Mealie v3** and was tested against v3.29.0. Mealie v2 has not been
  tested, and Mealie v1 is not supported any more. See
  [tools/mealie-compat](tools/mealie-compat/README.md) for the API changes.
- **Material 3 theme.** Material You dynamic color on Android 12+, with a warm palette generated
  from Mealie's orange as the fallback. *Appearance* in the menu lets you pick light, dark or system
  and turn dynamic color off.
- **Keep screen on.** The recipe screen already kept the display awake with no way to turn that off.
  A "Keep screen on" chip under the recipe header now switches it, on by default as before.
- **Its own identity.** The package is `io.github.cyberbandit1998.copperpot`, so it installs next to
  Mealient, and it has its own launcher icon (a copper pot). The log email no longer goes to the
  Mealient author.
- **Updated dependencies.** Kotlin 2.2, Gradle 8.14, AGP 8.13, Room 2.8, DataStore 1.3, Compose BOM
  2025.04 and more. The libraries are held at what builds against compileSdk 34; see below.

## Status

- CI compiles the app, compiles the instrumented tests, runs the unit tests and checks the
  built APK's package id, label, provider authorities and icon on every push.
- **It has not been run on a device yet.** Nothing in this app is verified to work on Android until
  someone has tried it on a phone; the checks above are build and unit-test checks.
- The Mealie API layer was also run against a live Mealie 3.29.0 server outside Android, see
  [tools/mealie-compat](tools/mealie-compat/README.md). Importing a recipe from a real web page has
  not been tried.

### Not done yet

Left for after the app has been tested on a device:

- Android Gradle Plugin 9 and Gradle 9, Ktor 3, OkHttp 5, Compose Destinations 2
- target SDK 36 (needed for Google Play) and compileSdk 36, which also unlocks newer AndroidX
  (core 1.17, Lifecycle 2.9+, Paging 3.4+, Compose 1.8+, Material 3 1.4) and edge-to-edge layouts
- new strings (theme, keep screen on) are English only

## Requirements

- Android 8.0 (API 26) or newer
- A Mealie v3 server

## Building

```sh
./gradlew assembleDebug        # app/build/outputs/apk/debug
./gradlew testDebugUnitTest
```

CI builds the debug APK on every push and attaches it to the run as the `debug-apk` artifact.

## Contribution

Bug reports and pull requests are welcome.
