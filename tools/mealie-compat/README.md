# Mealie compatibility check

`probe_mealie.py` replays the requests this app makes (same paths, same payload shapes as
`MealieServiceKtor`) against a running Mealie server and checks every response against the
required fields of the Kotlin models in `datasource/.../models`.

```sh
python3 -I tools/mealie-compat/probe_mealie.py http://127.0.0.1:9925
```

It expects a fresh Mealie instance with the default `changeme@example.com` / `MyPassword` user and
exits non-zero if any check fails. The response fixtures in
`datasource/src/test/resources/mealie-v3` were recorded from Mealie 3.29.0, and
`MealieV3ResponsesTest` decodes them in CI.

Server-side changes this fork adapts to. The right column is what Mealie 3.29.0 does; the
left is what Mealient's code expects, which matches Mealie v1:

| Mealient expects (Mealie v1) | Mealie 3.29.0 |
|---|---|
| `/api/groups/shopping/lists`, `/api/groups/shopping/items` | `/api/households/shopping/lists`, `/api/households/shopping/items` |
| `POST /api/recipes/create-url` | `POST /api/recipes/create/url` |
| `favoriteRecipes` (slugs) on `GET /api/users/self` | `GET /api/users/self/favorites` (recipe ids) |
| ingredient `isFood` / `disableAmount`, recipe setting `disableAmount` | removed |

## End-to-end check of the app's request code

`e2e/` compiles the app's real `MealieServiceKtor`, its interface and its models (copied unchanged by
`sync.sh`) in a plain JVM project, builds the HTTP client the way `KtorClientBuilderImpl` does, and runs
the app's own calls against a live Mealie server: login, recipes with ingredients, favorites, shopping
lists with food and unit items, and the recipe-import route. It needs a fresh Mealie with the default
`changeme@example.com` / `MyPassword` user.

```sh
tools/mealie-compat/e2e/sync.sh
cd tools/mealie-compat/e2e && gradle run -Dmealie.url=http://127.0.0.1:9925
```

Last run: Mealie 3.29.0, 21 of 21 checks pass with Kotlin 2.2.21, Ktor 2.3.13, OkHttp 4.12.1 and
kotlinx-serialization 1.9.0. The URL import check only proves that the route accepts the request (Mealie
answers 400 when the page has no recipe); it does not import a real recipe.
