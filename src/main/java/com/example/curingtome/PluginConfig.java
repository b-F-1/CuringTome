package com.example.curingtome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Reads and holds all tunable values from config.yml.
 * Rebuilt whenever the config is (re)loaded.
 */
public final class PluginConfig {

    public enum Mode { INSTANT, AUTHENTIC }

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Mode mode;
    private final int majorPositive;
    private final int minorPositive;
    private final boolean affectNearby;
    private final int nearbyRadius;
    private final int nearbyMinorPositive;
    private final boolean requireProfession;
    private final boolean consumeOnUse;
    private final int cooldownSeconds;
    private final boolean recipeEnabled;
    private final int authenticConversionTicks;
    private final boolean particles;
    private final boolean sound;
    private final boolean lightning;

    private final String msgCured;
    private final String msgNoPermission;
    private final String msgNoCraftPermission;
    private final String msgOnCooldown;
    private final String msgNotAVillager;
    private final String msgNeedsProfession;
    private final String msgGiven;
    private final String msgReloaded;

    public PluginConfig(FileConfiguration c) {
        Mode parsed;
        try {
            parsed = Mode.valueOf(c.getString("mode", "INSTANT").trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            parsed = Mode.INSTANT;
        }
        this.mode = parsed;

        this.majorPositive = c.getInt("major-positive", 20);
        this.minorPositive = c.getInt("minor-positive", 25);
        this.affectNearby = c.getBoolean("affect-nearby", true);
        this.nearbyRadius = c.getInt("nearby-radius", 16);
        this.nearbyMinorPositive = c.getInt("nearby-minor-positive", 5);
        this.requireProfession = c.getBoolean("require-profession", false);
        this.consumeOnUse = c.getBoolean("consume-on-use", true);
        this.cooldownSeconds = Math.max(0, c.getInt("cooldown-seconds", 0));
        this.recipeEnabled = c.getBoolean("recipe-enabled", true);
        this.authenticConversionTicks = Math.max(1, c.getInt("authentic.conversion-ticks", 60));
        this.particles = c.getBoolean("effects.particles", true);
        this.sound = c.getBoolean("effects.sound", true);
        this.lightning = c.getBoolean("effects.lightning", true);

        this.msgCured = c.getString("messages.cured", "<green>The villager has been cured.");
        this.msgNoPermission = c.getString("messages.no-permission", "<red>No permission.");
        this.msgNoCraftPermission = c.getString("messages.no-craft-permission", "<red>No permission to craft.");
        this.msgOnCooldown = c.getString("messages.on-cooldown", "<red>Wait <seconds>s.");
        this.msgNotAVillager = c.getString("messages.not-a-villager", "<red>Not a villager.");
        this.msgNeedsProfession = c.getString("messages.needs-profession", "<red>No profession.");
        this.msgGiven = c.getString("messages.given", "<green>Gave <amount> to <player>.");
        this.msgReloaded = c.getString("messages.reloaded", "<green>Reloaded.");
    }

    public Mode mode() { return mode; }
    public int majorPositive() { return majorPositive; }
    public int minorPositive() { return minorPositive; }
    public boolean affectNearby() { return affectNearby; }
    public int nearbyRadius() { return nearbyRadius; }
    public int nearbyMinorPositive() { return nearbyMinorPositive; }
    public boolean requireProfession() { return requireProfession; }
    public boolean consumeOnUse() { return consumeOnUse; }
    public int cooldownSeconds() { return cooldownSeconds; }
    public boolean recipeEnabled() { return recipeEnabled; }
    public int authenticConversionTicks() { return authenticConversionTicks; }
    public boolean particles() { return particles; }
    public boolean sound() { return sound; }
    public boolean lightning() { return lightning; }

    public Component cured() { return mm(msgCured); }
    public Component noPermission() { return mm(msgNoPermission); }
    public Component noCraftPermission() { return mm(msgNoCraftPermission); }
    public Component onCooldown(long seconds) {
        return MM.deserialize(msgOnCooldown, Placeholder.unparsed("seconds", String.valueOf(seconds)));
    }
    public Component notAVillager() { return mm(msgNotAVillager); }
    public Component needsProfession() { return mm(msgNeedsProfession); }
    public Component given(int amount, String player) {
        return MM.deserialize(msgGiven,
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.unparsed("player", player));
    }
    public Component reloaded() { return mm(msgReloaded); }

    private static Component mm(String s) { return MM.deserialize(s); }
}
