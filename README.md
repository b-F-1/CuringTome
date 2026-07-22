# CuringTome

A Paper **26.2** plugin that adds a craftable **Curing Tome**. Right-click a villager
with it and that villager instantly grants the player the permanent zombie-cure trade
discount — no potions, golden apples, or 3–5 minute wait required.

## How it works

Under the hood, a "cured" villager isn't flagged with a boolean. The permanent discount is
a `major_positive` **gossip/reputation** entry keyed to the curing player's UUID (value 20,
which is the vanilla cap from a single cure). This plugin writes that reputation directly
through Paper's villager reputation API — reading `villager.getReputations()`, adjusting the
player's `Reputation` object, and writing it back with `villager.setReputation(uuid, rep)` —
so no NMS is involved.

Because one cure already maxes the permanent discount, using the tome again on the **same**
villager as the **same** player does nothing extra for the permanent bonus (it only refreshes
the temporary `minor_positive`). A **different** player gets their own discount, since the
reputation is per-UUID.

### Modes (`mode` in config.yml)

- **INSTANT** (default): keeps the same villager entity — trades are never touched — and
  grants the reputation directly. Optionally spreads a small temporary discount to nearby
  villagers, mimicking the vanilla cure aura.
- **AUTHENTIC**: literally calls `villager.zombify()`, sets you as the conversion player,
  and gives it a short conversion time so vanilla runs the real zombie→villager cure with
  the shaking animation and awards the gossip itself.

## Crafting

```
 P       P = Splash Potion of Weakness (normal or long)
GBG      G = Golden Apple   B = Book
 P
```

## Build

Requires JDK 25 and Maven.

```bash
mvn clean package
```

The compiled jar lands in `target/CuringTome-1.0.0.jar`. Drop it into your server's
`plugins/` folder.

> Note: the dependency version is the range `[26.2.build,)`, which resolves to the latest
> 26.2 build (per PaperMC's Maven instructions). To pin an exact build instead, replace it
> with a specific version such as `26.2.build.60-beta` from https://repo.papermc.io.

## Commands

- `/curingtome give [player] [amount]` — give tomes (perm: `curingtome.give`, default op)
- `/curingtome reload` — reload config (perm: `curingtome.reload`, default op)

Alias: `/ctome`.

## Permissions

| Node | Default | Purpose |
|------|---------|---------|
| `curingtome.use` | true | Use the tome on a villager |
| `curingtome.craft` | true | Craft the tome |
| `curingtome.give` | op | `/curingtome give` |
| `curingtome.reload` | op | `/curingtome reload` |

## Key config options

`mode`, `major-positive`, `minor-positive`, `affect-nearby` / `nearby-radius` /
`nearby-minor-positive`, `require-profession`, `consume-on-use`, `cooldown-seconds`,
`recipe-enabled`, `authentic.conversion-ticks`, the `effects` block (`particles`, `sound`,
`lightning` — the lightning is cosmetic only), and the `messages` block (MiniMessage
formatting).
