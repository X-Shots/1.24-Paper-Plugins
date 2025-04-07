package me.boomfanficalatta.windStaff;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
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

public class WindStaff extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        registerRecipe();
    }

    // Create the Wind Staff item with Golden Hoe as the base item
    public static ItemStack createWindStaff() {
        ItemStack staff = new ItemStack(Material.GOLDEN_HOE, 1); // Golden Hoe as base item
        ItemMeta meta = staff.getItemMeta();

        meta.displayName(Component.text("§bWind Staff")); // Custom blue name
        meta.setCustomModelData(1703); // Custom Model Data for your 3D model
        meta.lore(Collections.singletonList(Component.text("Harness the power of the wind."))); // Lore text
        staff.setItemMeta(meta);

        return staff;
    }

    // Register the crafting recipe for Wind Staff
    public void registerRecipe() {
        ItemStack windStaff = createWindStaff();
        NamespacedKey key = new NamespacedKey(this, "wind_staff");

        ShapedRecipe recipe = new ShapedRecipe(key, windStaff);
        recipe.shape(" W ", " B ", " R ");
        recipe.setIngredient('W', Material.WIND_CHARGE);  // Wind Charge at the top
        recipe.setIngredient('B', Material.NETHER_STAR); // Nether Star in the middle
        recipe.setIngredient('R', Material.BREEZE_ROD);   // Breeze Rod at the bottom

        getServer().addRecipe(recipe);
    }

    // Listener for the Wind Staff use event
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if player is using the Wind Staff
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && Objects.equals(item.getItemMeta().displayName(), Component.text("§bWind Staff"))) {
                // Launch a Wind Charge (directly using WindCharge)
                WindCharge windCharge = player.launchProjectile(WindCharge.class);

                // Add a persistent data tag to identify that this Wind Charge is from the Wind Staff
                windCharge.getPersistentDataContainer().set(new NamespacedKey(this, "from_wind_staff"), PersistentDataType.BYTE, (byte) 1);
            }
        }
    }
}
