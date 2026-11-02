#!/usr/bin/env bash
# Copies the app's real datasource sources, unchanged, so the harness compiles exactly what the app runs.
set -eu
ROOT=$(git rev-parse --show-toplevel)
R=$ROOT/datasource/src/main/kotlin/gq/kirmanak/mealient/datasource
D=$(dirname "$0")/src/main/kotlin/gq/kirmanak/mealient/datasource
mkdir -p "$D/impl" "$D/models"
cp "$R/MealieService.kt" "$R/ServerUrlProvider.kt" "$D/"
cp "$R/impl/MealieServiceKtor.kt" "$D/impl/"
cp "$R/models/"*.kt "$D/models/"
