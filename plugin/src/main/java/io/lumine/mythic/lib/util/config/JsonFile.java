package io.lumine.mythic.lib.util.config;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class JsonFile extends ConfigFile<JsonObject> {
    public JsonFile(@NotNull String name) {
        this(MythicLib.plugin, "", name);
    }

    public JsonFile(@NotNull String folder, @NotNull String name) {
        this(MythicLib.plugin, folder, name);
    }

    public JsonFile(@NotNull Plugin plugin, @NotNull String folder, @NotNull String name) {
        super(plugin, new File(plugin.getDataFolder() + folder, name + ".json"));

        // File does not exist
        if (!getFile().exists()) {
            setContent(new JsonObject());
            return;
        }

        // Read object
        try {
            final FileReader reader = new FileReader(getFile());
            setContent(MythicLib.plugin.getGson().fromJson(reader, JsonObject.class));
            reader.close();
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Could not load JSON file '" + getFile().getName() + "': " + exception.getMessage());
            if (!hasContent()) setContent(new JsonObject());
        }
    }

    public void save() {
        try {

            // Create file
            if (!getFile().exists()) getFile().getParentFile().mkdir();

            // Save object
            final FileWriter writer = new FileWriter(getFile());
            writer.write(getContent().toString());
            writer.close();
        } catch (IOException exception) {
            getPlugin().getLogger().log(Level.SEVERE, "Could not save JSON file '" + getFile().getName() + "': " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}