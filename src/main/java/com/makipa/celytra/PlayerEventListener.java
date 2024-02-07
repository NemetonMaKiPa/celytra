package com.makipa.celytra;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerEventListener implements Listener {
    private final Celytra plugin;
    private final Map<UUID, ItemStack> originalChestplates = new HashMap<>();

    public PlayerEventListener(Celytra plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveElytra(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        giveElytra(player);
    }

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();

        Location playerLocation = player.getLocation();
        Location worldSpawn = player.getWorld().getSpawnLocation();
        Material blockType = playerLocation.getBlock().getType();
        boolean isOverWaterOrLava = blockType == Material.WATER || blockType == Material.LAVA;
        getLogger().info("onEntityToggleGlide triggered");

            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate != null && chestplate.hasItemMeta()) {
                ItemMeta meta = chestplate.getItemMeta();
                Optional<Component> displayName = Optional.ofNullable(meta.displayName());

                if (displayName.isPresent() && Component.text("Spawn-Elytra").equals(displayName.get())) {
                    getLogger().info("hat Spawn-Elytra");
                    player.getInventory().setChestplate(null);
                    if (playerLocation.distance(worldSpawn) > 50 && (!event.isGliding() || isOverWaterOrLava)) {
                        getLogger().info("distance > 50, !isGliding oder Wasser/Lava");
                        removeElytra(player);
                    }
                }
            }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        ItemStack currentChestplate = player.getInventory().getChestplate();
        if (currentChestplate != null) {
            plugin.getDataConfig().set(playerId.toString(), currentChestplate);
            originalChestplates.remove(playerId);

            try {
                plugin.getDataConfig().save(plugin.getDataFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void giveElytra(Player player) {
        UUID playerId = player.getUniqueId();
        ItemStack currentChestplate = player.getInventory().getChestplate();

        if (currentChestplate != null && currentChestplate.getType() != Material.AIR) {
            originalChestplates.put(playerId, currentChestplate.clone());
            player.getInventory().setChestplate(null);
        }

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.displayName(Component.text("Spawn-Elytra"));
        elytra.setItemMeta(meta);
        player.getInventory().setChestplate(elytra);
    }

    private void removeElytra(Player player) {
        getLogger().info("removeElytra triggered");
        UUID playerId = player.getUniqueId();
        ItemStack chestplate = player.getInventory().getChestplate();

        if (chestplate != null && chestplate.hasItemMeta()) {
            ItemMeta meta = chestplate.getItemMeta();
            Optional<Component> displayName = Optional.ofNullable(meta.displayName());

            if (displayName.isPresent() && Component.text("Spawn-Elytra").equals(displayName.get())) {
                player.getInventory().setChestplate(null);

                if (originalChestplates.containsKey(playerId)) {
                    player.getInventory().setChestplate(originalChestplates.get(playerId));
                    originalChestplates.remove(playerId);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        removeElytraIfConditionsMet(player);
    }

    private void removeElytraIfConditionsMet(Player player) {
        UUID playerId = player.getUniqueId();
        Location playerLocation = player.getLocation();
        Location worldSpawn = player.getWorld().getSpawnLocation();

        if (playerLocation.distance(worldSpawn) > 50) {
            removeElytra(player);
        }
    }
}
