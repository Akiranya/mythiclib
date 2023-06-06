package io.lumine.mythic.lib.util.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * TODO
 *
 * @deprecated Not implemented
 */
@Deprecated
public class YamlFile extends ConfigFile<ConfigurationSection> {
    public YamlFile(Plugin plugin, File file) {
        super(plugin, file);

        throw new RuntimeException();
    }

    @Override
    public void save() {
        throw new RuntimeException();
    }
}
