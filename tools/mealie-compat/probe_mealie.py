"""Replay the Android app's Mealie requests against a live server and check
responses against the Kotlin models' required (no-default) keys.

Usage: python3 -I probe_mealie.py <base_url> [--paths old|new]
"""
import json
import sys
import urllib.error
import urllib.parse
import urllib.request

BASE = sys.argv[1].rstrip("/")
RESULTS = []


def call(method, path, body=None, form=None, token=None, expect=None):
    url = BASE + path
    data = None
    headers = {}
    if form is not None:
        data = urllib.parse.urlencode(form).encode()
        headers["Content-Type"] = "application/x-www-form-urlencoded"
    elif body is not None:
        data = json.dumps(body).encode()
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            raw = r.read().decode()
            status = r.status
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        status = e.code
    try:
        parsed = json.loads(raw) if raw else None
    except json.JSONDecodeError:
        parsed = raw
    return status, parsed


def record(name, ok, detail=""):
    RESULTS.append((name, ok, detail))
    print(f"[{'PASS' if ok else 'FAIL'}] {name}" + (f"  -- {detail}" if detail else ""))


def check_keys(label, obj, required, nullable_ok=()):
    """required: keys that have no default in the Kotlin model.
    A required key must be present; if its Kotlin type is non-null it must not be null."""
    problems = []
    if not isinstance(obj, dict):
        record(label, False, f"expected object, got {type(obj).__name__}")
        return
    for key, can_be_null in required.items():
        if key not in obj:
            problems.append(f"missing '{key}'")
        elif obj[key] is None and not can_be_null:
            problems.append(f"'{key}' is null but Kotlin type is non-null")
    record(label, not problems, "; ".join(problems))


# ---- auth (getToken) -------------------------------------------------------
st, tok = call("POST", "/api/auth/token",
               form={"username": "changeme@example.com", "password": "MyPassword"})
record("POST /api/auth/token (form login)", st == 200 and "access_token" in (tok or {}), f"status={st}")
token = tok["access_token"]
check_keys("GetTokenResponse", tok, {"access_token": False})

# ---- version ---------------------------------------------------------------
st, about = call("GET", "/api/app/about")
check_keys("GET /api/app/about -> VersionResponse", about, {"version": False})

# ---- api token (createApiToken) -------------------------------------------
st, apitok = call("POST", "/api/users/api-tokens", body={"name": "MealientProbe"}, token=token)
record("POST /api/users/api-tokens", st in (200, 201), f"status={st}")
check_keys("CreateApiTokenResponse", apitok, {"token": False})

# ---- user self (getUserSelfInfo) ------------------------------------------
st, me = call("GET", "/api/users/self", token=token)
check_keys("GET /api/users/self -> GetUserInfoResponse", me, {"id": False})
fav_in_self = isinstance(me, dict) and "favoriteRecipes" in me
record("users/self still exposes 'favoriteRecipes' (app reads favorites from it)", fav_in_self,
       "app's favorites list would always be EMPTY" if not fav_in_self else "")
user_id = me["id"]

# ---- recipe create / update ------------------------------------------------
st, slug = call("POST", "/api/recipes", body={"name": "Probe Soup"}, token=token)
record("POST /api/recipes (createRecipe) returns slug string", st == 201 and isinstance(slug, str), f"status={st} body={slug!r}")

update_body = {
    "description": "A probe recipe",
    "recipeYield": "4 servings",
    "recipeIngredient": [
        {"referenceId": "11111111-1111-4111-8111-111111111111", "note": "2 cups water"},
        {"referenceId": "22222222-2222-4222-8222-222222222222", "note": "1 tsp salt"},
    ],
    "recipeInstructions": [
        {"id": "33333333-3333-4333-8333-333333333333", "text": "Boil water", "ingredientReferences": []},
    ],
    "settings": {"disableComments": True, "public": False},
}
st, upd = call("PATCH", f"/api/recipes/{slug}", body=update_body, token=token)
record("PATCH /api/recipes/{slug} (updateRecipe) with app payload", st == 200, f"status={st} {str(upd)[:200] if st != 200 else ''}")

# ---- recipe detail (getRecipe -> GetRecipeResponse) -----------------------
st, rec = call("GET", f"/api/recipes/{slug}", token=token)
check_keys("GetRecipeResponse (top level)", rec, {"id": False, "name": False})
settings = rec.get("settings")
if settings is not None:
    check_keys("GetRecipeSettingsResponse", settings, {"disableAmount": False})
for i, ing in enumerate(rec.get("recipeIngredient", [])):
    check_keys(f"GetRecipeIngredientResponse[{i}]", ing, {
        "unit": True, "food": True, "quantity": True, "display": False,
        "referenceId": False, "title": True, "isFood": False, "disableAmount": False,
    })
for i, ins in enumerate(rec.get("recipeInstructions", [])):
    check_keys(f"GetRecipeInstructionResponse[{i}]", ins, {"id": False, "text": False})

# ---- recipe list (getRecipeSummary -> GetRecipesResponse) -----------------
st, lst = call("GET", "/api/recipes?page=1&perPage=15", token=token)
check_keys("GetRecipesResponse", lst, {"items": False})
for i, it in enumerate((lst or {}).get("items", [])[:3]):
    check_keys(f"GetRecipeSummaryResponse[{i}]", it,
               {"id": False, "name": False, "slug": False, "dateAdded": False})

# ---- favorites -------------------------------------------------------------
st, _ = call("POST", f"/api/users/{user_id}/favorites/{slug}", token=token)
record("POST /api/users/{id}/favorites/{slug} (addFavoriteRecipe)", st in (200, 201, 204), f"status={st}")
st, me2 = call("GET", "/api/users/self", token=token)
record("After favoriting, users/self.favoriteRecipes contains slug",
       isinstance(me2, dict) and slug in (me2.get("favoriteRecipes") or []),
       "app would not show the recipe as favorite")
st, favs = call("GET", "/api/users/self/favorites", token=token)
record("GET /api/users/self/favorites (new source of truth)", st == 200, f"status={st} body={json.dumps(favs)[:300]}")
st, _ = call("DELETE", f"/api/users/{user_id}/favorites/{slug}", token=token)
record("DELETE /api/users/{id}/favorites/{slug} (removeFavoriteRecipe)", st in (200, 204), f"status={st}")

# ---- URL import path -------------------------------------------------------
st_old, _ = call("POST", "/api/recipes/create-url", body={"url": "https://example.com", "includeTags": False}, token=token)
record("POST /api/recipes/create-url (old path) is served", st_old not in (404, 405), f"status={st_old}")
st_new, _ = call("POST", "/api/recipes/create/url", body={"url": "https://example.com", "includeTags": False}, token=token)
record("POST /api/recipes/create/url (new path) exists", st_new not in (404, 405), f"status={st_new}")

# ---- shopping lists: old (groups) vs new (households) ---------------------
for prefix in ("/api/groups/shopping", "/api/households/shopping"):
    st, _ = call("GET", f"{prefix}/lists?page=1&perPage=50", token=token)
    record(f"GET {prefix}/lists exists", st == 200, f"status={st}")

SH = "/api/households/shopping"
st, _ = call("POST", f"{SH}/lists", body={"name": "Probe list"}, token=token)
record("POST shopping/lists (createShoppingList) [households path]", st in (200, 201), f"status={st}")
st, lists = call("GET", f"{SH}/lists?page=1&perPage=50", token=token)
check_keys("GetShoppingListsResponse", lists, {"page": False, "per_page": False, "total": False, "total_pages": False, "items": False})
for i, it in enumerate((lists or {}).get("items", [])[:2]):
    check_keys(f"GetShoppingListsSummaryResponse[{i}]", it, {"id": False, "name": True})
list_id = lists["items"][0]["id"]

st, food = call("GET", "/api/foods?perPage=5", token=token)
check_keys("GetFoodsResponse", food, {"items": False})
st, units = call("GET", "/api/units?perPage=5", token=token)
check_keys("GetUnitsResponse", units, {"items": False})

item_req = {
    "shopping_list_id": list_id, "checked": False, "position": 0, "is_food": False,
    "note": "Milk", "quantity": 1.0, "food_id": None, "unit_id": None,
}
st, created = call("POST", f"{SH}/items", body=item_req, token=token)
record("POST shopping/items with app's snake_case payload", st in (200, 201), f"status={st} {str(created)[:200] if st not in (200,201) else ''}")

st, sl = call("GET", f"{SH}/lists/{list_id}", token=token)
check_keys("GetShoppingListResponse", sl, {"id": False, "groupId": False, "recipeReferences": False})
items = (sl or {}).get("listItems", [])
record("shopping list contains created item", len(items) == 1, f"items={len(items)}")
for i, it in enumerate(items):
    check_keys(f"GetShoppingListItemResponse[{i}]", it, {"shoppingListId": False, "id": False})
if items:
    item_id = items[0]["id"]
    st, item_json = call("GET", f"{SH}/items/{item_id}", token=token)
    record("GET shopping/items/{id}", st == 200, f"status={st}")
    item_json["checked"] = True
    st, _ = call("PUT", f"{SH}/items/{item_id}", body=item_json, token=token)
    record("PUT shopping/items/{id} with round-tripped JSON (check item)", st == 200, f"status={st}")
    st, _ = call("DELETE", f"{SH}/items/{item_id}", token=token)
    record("DELETE shopping/items/{id}", st == 200, f"status={st}")
st, list_json = call("GET", f"{SH}/lists/{list_id}", token=token)
st, _ = call("PUT", f"{SH}/lists/{list_id}", body=list_json, token=token)
record("PUT shopping/lists/{id} with round-tripped JSON", st == 200, f"status={st}")
st, _ = call("DELETE", f"{SH}/lists/{list_id}", token=token)
record("DELETE shopping/lists/{id}", st == 200, f"status={st}")

# ---- cleanup ---------------------------------------------------------------
st, _ = call("DELETE", f"/api/recipes/{slug}", token=token)
record("DELETE /api/recipes/{slug}", st == 200, f"status={st}")

fails = [r for r in RESULTS if not r[1]]
print(f"\n{len(RESULTS) - len(fails)} passed, {len(fails)} failed")
sys.exit(1 if fails else 0)
