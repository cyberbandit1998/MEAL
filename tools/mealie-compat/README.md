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

Server-side changes this fork adapts to (Mealie v2 and later):

| Mealie v1 | Mealie v2+ |
|---|---|
| `/api/groups/shopping/lists`, `/api/groups/shopping/items` | `/api/households/shopping/lists`, `/api/households/shopping/items` |
| `POST /api/recipes/create-url` | `POST /api/recipes/create/url` |
| `favoriteRecipes` (slugs) on `GET /api/users/self` | `GET /api/users/self/favorites` (recipe ids) |
| ingredient `isFood` / `disableAmount`, recipe setting `disableAmount` | removed |
