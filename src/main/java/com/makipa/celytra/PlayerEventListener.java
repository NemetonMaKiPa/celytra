package com.makipa.celytra;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
        UUID playerId = player.getUniqueId();

        if (plugin.getDataConfig().contains(playerId.toString())) {
            ItemStack savedChestplate = plugin.getDataConfig().getItemStack(playerId.toString());
            originalChestplates.put(playerId, savedChestplate);
        }

        ItemStack currentChestplate = player.getInventory().getChestplate();
        if (currentChestplate == null || currentChestplate.getType() == Material.AIR) {
            ItemStack elytra = new ItemStack(Material.ELYTRA);
            ItemMeta meta = elytra.getItemMeta();
            meta.displayName(Component.text("Spawn-Elytra"));
            elytra.setItemMeta(meta);
            player.getInventory().setChestplate(elytra);
        } else {
            originalChestplates.put(playerId, currentChestplate);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        Location worldSpawn = player.getWorld().getSpawnLocation();

        if (playerLocation.distance(worldSpawn) > 50 && (!player.isGliding() || player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid())) {
            getLogger().info(player.getName() + " hat einen Elytra-Flug beendet");
            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate != null && chestplate.hasItemMeta()) {
                ItemMeta meta = chestplate.getItemMeta();
                Optional<Component> displayName = Optional.ofNullable(meta.displayName());

                if (displayName.isPresent() && displayName.get().toString().contains("Spawn-Elytra")) {
                    player.getInventory().setChestplate(null);

                    if (originalChestplates.containsKey(player.getUniqueId())) {
                        player.getInventory().setChestplate(originalChestplates.get(player.getUniqueId()));
                        originalChestplates.remove(player.getUniqueId());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.displayName(Component.text("Spawn-Elytra"));
        elytra.setItemMeta(meta);
        player.getInventory().setChestplate(elytra);
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
}
