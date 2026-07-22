package com.example.curingtome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CuringCommand implements CommandExecutor, TabCompleter {

    private final CuringTomePlugin plugin;
    private final TomeItem tomeItem;

    public CuringCommand(CuringTomePlugin plugin, TomeItem tomeItem) {
        this.plugin = plugin;
        this.tomeItem = tomeItem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/curingtome <give|reload> [player] [amount]", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("curingtome.reload")) {
                    sender.sendMessage(plugin.config().noPermission());
                    return true;
                }
                plugin.reload();
                sender.sendMessage(plugin.config().reloaded());
                return true;
            }
            case "give" -> {
                if (!sender.hasPermission("curingtome.give")) {
                    sender.sendMessage(plugin.config().noPermission());
                    return true;
                }
                return handleGive(sender, args);
            }
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
                return true;
            }
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[2])));
            } catch (NumberFormatException ex) {
                sender.sendMessage(Component.text("Invalid amount: " + args[2], NamedTextColor.RED));
                return true;
            }
        }

        ItemStack tomes = tomeItem.create(amount);
        target.getInventory().addItem(tomes).values()
                .forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));

        sender.sendMessage(plugin.config().given(amount, target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("give", "reload")) {
                if (sub.startsWith(args[0].toLowerCase())) out.add(sub);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            out.add("1");
        }
        return out;
    }
}
