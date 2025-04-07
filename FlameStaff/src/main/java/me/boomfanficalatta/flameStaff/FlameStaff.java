package me.boomfanficalatta.flameStaff;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Objects;

public class FlameStaff extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        registerRecipe();
    }

    // Create the Flame Staff item with Diamond Hoe as the base item
    public static ItemStack createFlameStaff() {
        ItemStack staff = new ItemStack(Material.DIAMOND_HOE, 1); // Diamond Hoe as base item
        ItemMeta meta = staff.getItemMeta();

        meta.displayName(Component.text("§cFlame Staff")); // Custom red name
        meta.setCustomModelData(1704); // Custom Model Data for your 3D model (set this as appropriate)
        meta.lore(Collections.singletonList(Component.text("Harness the power of fire."))); // Lore text
        staff.setItemMeta(meta);

        return staff;
    }

    // Register the crafting recipe for Flame Staff
    public void registerRecipe() {
        ItemStack flameStaff = createFlameStaff();
        NamespacedKey key = new NamespacedKey(this, "flame_staff");

        ShapedRecipe recipe = new ShapedRecipe(key, flameStaff);
        recipe.shape(" F ", " B ", " R ");
        recipe.setIngredient('F', Material.FIRE_CHARGE);  // Flame Charge at the top
        recipe.setIngredient('B', Material.NETHER_STAR);     // Nether Star in the middle
        recipe.setIngredient('R', Material.BLAZE_ROD);       // Blaze Rod at the bottom

        getServer().addRecipe(recipe);
    }

    // Listener for the Flame Staff use event
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if player is using the Flame Staff
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && Objects.equals(item.getItemMeta().displayName(), Component.text("§cFlame Staff"))) {
                // Launch a Small Fireball
                SmallFireball smallFireball = player.launchProjectile(SmallFireball.class);

                // Add a persistent data tag to identify that this Fireball is from the Flame Staff
                smallFireball.getPersistentDataContainer().set(new NamespacedKey(this, "from_flame_staff"), PersistentDataType.BYTE, (byte) 1);
            }
        }
    }
}
