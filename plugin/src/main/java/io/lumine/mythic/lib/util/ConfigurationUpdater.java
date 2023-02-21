package io.lumine.mythic.lib.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

/**
 * mythiclib
 * 01/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class ConfigurationUpdater {

    private final Path configurationFile;
    private final String defaultConfigurationFile;
    private final ClassLoader loader;

    public ConfigurationUpdater(Path configurationFile, String defaultConfigurationFile, ClassLoader loader) {
        this.configurationFile = configurationFile;
        this.defaultConfigurationFile = defaultConfigurationFile;
        this.loader = loader;
    }

    public void update() {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configurationFile.toFile());
        try (final InputStream inputStream = loader.getResourceAsStream(defaultConfigurationFile)) {
            if (inputStream == null)
                throw new RuntimeException("Could not find default configuration file: " + defaultConfigurationFile);
            boolean changed = false;
            try (Reader reader = new InputStreamReader(inputStream)) {
                final YamlConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(reader);

                for (String key : defaultConfiguration.getKeys(true)) {
                    if (configuration.contains(key))
                        continue;

                    configuration.set(key, defaultConfiguration.get(key));
                    changed = true;
                }
            }
            if (changed)
                configuration.save(configurationFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
