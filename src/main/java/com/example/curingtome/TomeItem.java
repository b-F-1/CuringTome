package com.example.curingtome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.util.List;

/**
 * Responsible for creating the Curing Tome item, recognising it, and
 * registering / removing its crafting recipe.
 */
public final class TomeItem {

    private final CuringTomePlugin plugin;
    private final NamespacedKey tomeKey;   // PDC marker stored on the item
    private final NamespacedKey recipeKey;  // recipe identifier

    public TomeItem(CuringTomePlugin plugin) {
        this.plugin = plugin;
        this.tomeKey = new NamespacedKey(plugin, "curing_tome");
        this.recipeKey = new NamespacedKey(plugin, "curing_tome_recipe");
    }

    /** Build a fresh Curing Tome stack. */
    public ItemStack create(int amount) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Curing Tome", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("Right-click a villager to cure them", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Sometimes Dead is Better.", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, true),
                Component.text("(Not this time)", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, true)
        ));

        // Glint without a real enchantment (Paper 1.20.5+ / 26.x).
        meta.setEnchantmentGlintOverride(true);

        meta.getPersistentDataContainer().set(tomeKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /** True if the given stack is one of our Curing Tomes. */
    public boolean isTome(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK || !item.hasItemMeta()) {
            return false;
        }
        Byte flag = item.getItemMeta().getPersistentDataContainer()
                .get(tomeKey, PersistentDataType.BYTE);
        return flag != null && flag == (byte) 1;
    }

    /** Register the shaped recipe (2 weakness splash potions + 2 golden apples + 1 book). */
    public void registerRecipe() {
        removeRecipe(); // avoid duplicate-key errors on reload

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, create(1));
        recipe.shape(
                "RPR",
                "GBG",
                "RPR"
        );
        recipe.setIngredient('R', new RecipeChoice.MaterialChoice(Material.ROTTEN_FLESH));
        recipe.setIngredient('P', weaknessChoice());
        recipe.setIngredient('G', new RecipeChoice.MaterialChoice(Material.GOLDEN_APPLE));
        recipe.setIngredient('B', new RecipeChoice.MaterialChoice(Material.BOOK));

        Bukkit.addRecipe(recipe);
    }

    public void removeRecipe() {
        Bukkit.removeRecipe(recipeKey);
    }

    public NamespacedKey recipeKey() {
        return recipeKey;
    }

    /** Chat-friendly description of the recipe shape and ingredients. */
    public List<Component> recipeDescription() {
        return List.of(
                Component.text("Curing Tome Recipe:", NamedTextColor.GOLD),
                Component.text("R P R", NamedTextColor.GRAY),
                Component.text("G B G", NamedTextColor.GRAY),
                Component.text("R P R", NamedTextColor.GRAY),
                Component.text("R = Rotten Flesh", NamedTextColor.GRAY),
                Component.text("P = Splash Potion of Weakness (or Long)", NamedTextColor.GRAY),
                Component.text("G = Golden Apple", NamedTextColor.GRAY),
                Component.text("B = Book", NamedTextColor.GRAY)
        );
    }

    /** Accept either normal or extended (long) Splash Potion of Weakness. */
    private RecipeChoice weaknessChoice() {
        return new RecipeChoice.ExactChoice(
                splashPotion(PotionType.WEAKNESS),
                splashPotion(PotionType.LONG_WEAKNESS)
        );
    }

    private ItemStack splashPotion(PotionType type) {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(type);
        potion.setItemMeta(meta);
        return potion;
    }
}
