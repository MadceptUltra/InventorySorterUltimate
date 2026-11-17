package dev.inventorysorter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.inventorysorter.InventorySorterMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SorterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(InventorySorterMod.MOD_ID + ".json");

    private static SorterConfig instance = new SorterConfig();

    public boolean enabled = true;
    public int sortingDelay = 1;
    public boolean disableInCreative = true;

    public static SorterConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            SorterConfig loaded = GSON.fromJson(reader, SorterConfig.class);
            if (loaded != null) {
                loaded.sortingDelay = Math.max(0, loaded.sortingDelay);
                instance = loaded;
            }
        } catch (Exception e) {
            InventorySorterMod.LOG.error("Failed to load config from {}", CONFIG_PATH, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            InventorySorterMod.LOG.error("Failed to save config to {}", CONFIG_PATH, e);
        }
    }
}
