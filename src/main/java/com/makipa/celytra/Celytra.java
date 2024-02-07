package com.makipa.celytra;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public final class Celytra extends JavaPlugin {

    private File dataFile;
    private YamlConfiguration dataConfig;

    @Override
    public void onEnable() {
        setupDataStorage();
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
    }

    private void setupDataStorage() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        dataFile = new File(getDataFolder(), "chestplateData.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public YamlConfiguration getDataConfig() {
        return dataConfig;
    }

    public File getDataFile() {
        return dataFile;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
