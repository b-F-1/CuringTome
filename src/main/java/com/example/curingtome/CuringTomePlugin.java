package com.example.curingtome;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CuringTomePlugin extends JavaPlugin {

    private PluginConfig config;
    private TomeItem tomeItem;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = new PluginConfig(getConfig());
        this.tomeItem = new TomeItem(this);

        if (config.recipeEnabled()) {
            tomeItem.registerRecipe();
        }

        getServer().getPluginManager().registerEvents(new CuringListener(this, tomeItem), this);

        PluginCommand cmd = getCommand("curingtome");
        if (cmd != null) {
            CuringCommand handler = new CuringCommand(this, tomeItem);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("CuringTome enabled (mode: " + config.mode() + ").");
    }

    @Override
    public void onDisable() {
        if (tomeItem != null) {
            tomeItem.removeRecipe();
        }
    }

    /** Reload config from disk and re-register the recipe to reflect new settings. */
    public void reload() {
        reloadConfig();
        this.config = new PluginConfig(getConfig());
        tomeItem.removeRecipe();
        if (config.recipeEnabled()) {
            tomeItem.registerRecipe();
        }
    }

    public PluginConfig config() {
        return config;
    }

    public TomeItem tomeItem() {
        return tomeItem;
    }
}
