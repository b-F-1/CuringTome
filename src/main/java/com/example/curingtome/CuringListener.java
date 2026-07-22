package com.example.curingtome;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CuringListener implements Listener {

    private final CuringTomePlugin plugin;
    private final TomeItem tomeItem;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    /** Vanilla cap for minor_positive gossip. */
    private static final int MINOR_POSITIVE_MAX = 200;

    public CuringListener(CuringTomePlugin plugin, TomeItem tomeItem) {
        this.plugin = plugin;
        this.tomeItem = tomeItem;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        // Only react to the main hand to avoid the event firing twice.
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!tomeItem.isTome(held)) {
            return;
        }

        // From here on the tome is involved, so stop the default trade GUI.
        event.setCancelled(true);

        PluginConfig cfg = plugin.config();

        if (!player.hasPermission("curingtome.use")) {
            player.sendMessage(cfg.noPermission());
            return;
        }

        if (!(event.getRightClicked() instanceof Villager villager)) {
            player.sendMessage(cfg.notAVillager());
            return;
        }

        if (cfg.requireProfession()
                && villager.getProfession().equals(Villager.Profession.NONE)) {
            player.sendMessage(cfg.needsProfession());
            return;
        }

        // Cooldown check.
        if (cfg.cooldownSeconds() > 0) {
            long now = System.currentTimeMillis();
            long readyAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (now < readyAt) {
                long remaining = (readyAt - now + 999) / 1000; // round up
                player.sendMessage(cfg.onCooldown(remaining));
                return;
            }
            cooldowns.put(player.getUniqueId(), now + cfg.cooldownSeconds() * 1000L);
        }

        applyCure(cfg, player, villager);
        playEffects(cfg, villager);

        if (cfg.consumeOnUse()) {
            held.setAmount(held.getAmount() - 1);
        }

        player.sendMessage(cfg.cured());
    }

    private void applyCure(PluginConfig cfg, Player player, Villager villager) {
        UUID id = player.getUniqueId();

        if (cfg.mode() == PluginConfig.Mode.AUTHENTIC) {
            // Real cure: zombify, then let vanilla convert it back and award the gossip.
            ZombieVillager zombie = villager.zombify();
            if (zombie != null) {
                zombie.setConversionPlayer(player);
                zombie.setConversionTime(cfg.authenticConversionTicks());
            }
            return;
        }

        // INSTANT mode: write the reputation directly on the existing villager.
        // getReputations() returns a copy, so we mutate a Reputation and write it back.
        Reputation rep = reputationFor(villager, id);
        if (cfg.majorPositive() > 0) {
            // Permanent cure discount: never lower an existing value, cap at the configured amount.
            rep.setReputation(ReputationType.MAJOR_POSITIVE,
                    Math.max(current(rep, ReputationType.MAJOR_POSITIVE), cfg.majorPositive()));
        }
        if (cfg.minorPositive() > 0) {
            // Temporary discount: add on top of any existing value, capped at the vanilla max.
            rep.setReputation(ReputationType.MINOR_POSITIVE,
                    Math.min(MINOR_POSITIVE_MAX, current(rep, ReputationType.MINOR_POSITIVE) + cfg.minorPositive()));
        }
        villager.setReputation(id, rep);

        if (cfg.affectNearby() && cfg.nearbyMinorPositive() > 0) {
            double r = cfg.nearbyRadius();
            for (Entity entity : villager.getNearbyEntities(r, r, r)) {
                if (entity instanceof Villager nearby && !nearby.equals(villager)) {
                    Reputation nearbyRep = reputationFor(nearby, id);
                    nearbyRep.setReputation(ReputationType.MINOR_POSITIVE,
                            Math.min(MINOR_POSITIVE_MAX,
                                    current(nearbyRep, ReputationType.MINOR_POSITIVE) + cfg.nearbyMinorPositive()));
                    nearby.setReputation(id, nearbyRep);
                }
            }
        }
    }

    /** Existing reputation for the player, or a fresh empty one. */
    private Reputation reputationFor(Villager villager, UUID id) {
        Reputation existing = villager.getReputations().get(id);
        return existing != null ? existing : new Reputation();
    }

    /** Current value of a reputation type, treating "unset" as 0. */
    private int current(Reputation rep, ReputationType type) {
        return rep.hasReputationSet(type) ? rep.getReputation(type) : 0;
    }

    private void playEffects(PluginConfig cfg, Villager villager) {
        if (cfg.lightning()) {
            // Effect-only strike: no entity damage, no block fire.
            villager.getWorld().strikeLightningEffect(villager.getLocation());
        }
        if (cfg.particles()) {
            Location loc = villager.getLocation().add(0, 1, 0);
            villager.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 20, 0.4, 0.6, 0.4);
        }
        if (cfg.sound()) {
            villager.getWorld().playSound(villager.getLocation(),
                    Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
        }
    }

    /** Prevent players without curingtome.craft from taking the crafted result. */
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (!tomeItem.isTome(result)) {
            return;
        }
        if (event.getView().getPlayer() instanceof Player player
                && !player.hasPermission("curingtome.craft")) {
            event.getInventory().setResult(null);
        }
    }
}
