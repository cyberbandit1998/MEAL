# MEAL

An Android client for [Mealie](https://github.com/mealie-recipes/mealie), the self-hosted recipe
manager. Browse and search your recipes, keep them available offline, add new ones (including from a
shared link), and manage shopping lists.

MEAL is a fork of [Mealient](https://github.com/kirmanak/Mealient) by Kirill Kamakin, which is no
longer maintained. It is released under the same MIT license, see [LICENSE](LICENSE).

## DISCLAIMER

This project is developed independently from the core Mealie project. It is NOT associated with the
core Mealie developers. Report issues here, NOT in the Mealie repository.

## What changed from Mealient

- **Works with current Mealie.** Mealient was written for Mealie v1 and cannot read recipes from
  Mealie v2 or v3. This fork targets Mealie **v2.0 and newer** (verified against v3.29.0) and does not
  support v1 any more. See [tools/mealie-compat](tools/mealie-compat/README.md) for the API changes.
- **Material 3 theme.** Material You dynamic color on Android 12+, with a warm palette generated
  from Mealie's orange as the fallback. *Appearance* in the menu lets you pick light, dark or system
  and turn dynamic color off.
- **Different identity.** The package is `io.github.cyberbandit1998.meal`, so it installs next to
  Mealient instead of replacing it. The log email no longer goes to the Mealient author.

## Requirements

- Android 8.0 (API 26) or newer
- A Mealie v2.0+ server

## Building

```sh
./gradlew assembleDebug        # app/build/outputs/apk/debug
./gradlew testDebugUnitTest
```

CI builds the debug APK on every push and attaches it to the run as the `debug-apk` artifact.

## Contribution

Bug reports and pull requests are welcome.
