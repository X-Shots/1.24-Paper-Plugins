package me.boomfanficalatta.enderStaff;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Location;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class EnderStaff extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        registerRecipe();
    }

    // Create the Ender Staff item with Netherite Hoe as the base item
    public static ItemStack createEnderStaff() {
        ItemStack staff = new ItemStack(Material.NETHERITE_HOE, 1); // Netherite Hoe as base item
        ItemMeta meta = staff.getItemMeta();

        meta.displayName(Component.text("§5Ender Staff")); // Custom purple name
        meta.setCustomModelData(1703); // Custom Model Data for your 3D model
        meta.lore(Collections.singletonList(Component.text("Harness the power of the End."))); // Lore text
        staff.setItemMeta(meta);

        return staff;
    }

    // Register the crafting recipe for Ender Staff
    public void registerRecipe() {
        ItemStack enderStaff = createEnderStaff();
        NamespacedKey key = new NamespacedKey(this, "ender_staff");

        ShapedRecipe recipe = new ShapedRecipe(key, enderStaff);
        recipe.shape(" E ", " S ", " H ");
        recipe.setIngredient('E', Material.ENDER_EYE);  // Ender Eye at the top
        recipe.setIngredient('S', Material.NETHER_STAR); // Nether Star in the middle
        recipe.setIngredient('H', Material.END_ROD);   // End Rod at the bottom (this keeps it in the recipe)

        getServer().addRecipe(recipe);
    }

    // Listener for the Ender Staff use event
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if player is using the Ender Staff
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && Objects.equals(item.getItemMeta().displayName(), Component.text("§5Ender Staff"))) {
                // Launch the Ender Pearl from the player's position
                EnderPearl enderPearl = player.launchProjectile(EnderPearl.class);
                // Mark this Ender Pearl as launched by the Ender Staff
                enderPearl.getPersistentDataContainer().set(new NamespacedKey(this, "from_ender_staff"), PersistentDataType.BYTE, (byte) 1);
                // Store the player that launched the Ender Pearl (for teleportation)
                enderPearl.getPersistentDataContainer().set(new NamespacedKey(this, "owner"), PersistentDataType.STRING, player.getUniqueId().toString());
            }
        }
    }

    // Listener to prevent damage and teleport the player if the Ender Pearl hits
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        // Check if the entity being damaged is a player and the damager is an Ender Pearl
        if (entity instanceof Player damagedPlayer) {

            if (event.getDamager() instanceof EnderPearl enderPearl) {

                // Check if the Ender Pearl was launched by the Ender Staff
                if (enderPearl.getPersistentDataContainer().has(new NamespacedKey(this, "from_ender_staff"), PersistentDataType.BYTE)) {
                    // Get the player who launched the Ender Pearl
                    String ownerUUID = enderPearl.getPersistentDataContainer().get(new NamespacedKey(this, "owner"), PersistentDataType.STRING);
                    assert ownerUUID != null;
                    Player owner = getServer().getPlayer(UUID.fromString(ownerUUID));

                    // If the player who launched the pearl is the same as the damaged player, teleport them
                    if (owner != null && owner.equals(damagedPlayer)) {
                        // Teleport the player to the Ender Pearl's impact location
                        Location impactLocation = enderPearl.getLocation();
                        damagedPlayer.teleport(impactLocation);

                        // Add End Rod particle effect at the impact location
                        damagedPlayer.getWorld().spawnParticle(Particle.END_ROD, impactLocation, 30, 0.5, 0.5, 0.5, 0.1);

                        // Play the Enderman teleport sound at the impact location
                        damagedPlayer.getWorld().playSound(impactLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    }

                    // Cancel the damage (no damage from the Ender Staff's Ender Pearl)
                    event.setCancelled(true);
                }
            }
        }
    }
}
