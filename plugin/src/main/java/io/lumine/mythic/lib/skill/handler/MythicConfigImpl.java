package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.utils.config.MemorySection;
import io.lumine.mythic.bukkit.utils.config.file.FileConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.lumine.mythic.core.config.GenericConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class MythicConfigImpl implements GenericConfig, Cloneable, MythicConfig {
    private String configName;
    private File file;
    private ConfigurationSection fc;

    public MythicConfigImpl(String name, ConfigurationSection fc) {
        this.configName = name;
        this.file = null;
        this.fc = fc;
    }

    @Override
    public FileConfiguration getFileConfiguration() {
        throw new RuntimeException("Not supported");
    }

    public void setKey(String key) {
        this.configName = key;
    }

    public String getKey() {
        return this.configName;
    }

    public String getNode() {
        return this.configName != null && this.configName.length() != 0 ? this.configName + "." : "";
    }

    public void deleteNodeAndSave() {
        this.fc.set(this.getNode(), (Object)null);
        this.save();
    }

    public boolean isSet(String field) {
        ConfigurationSection var10000 = this.fc;
        String var10001 = this.getNode();
        return var10000.isSet(var10001 + field);
    }

    public void set(String key, Object value) {
        this.fc.set(this.getNode() + key, value);
    }

    public void setSave(String key, Object value) {
        this.fc.set(this.getNode() + key, value);
        this.save();
    }

    public void unset(String key) {
        this.fc.set(this.getNode() + key, (Object)null);
    }

    public void unsetSave(String key) {
        this.unset(key);
        this.save();
    }

    public void load() {
        this.fc = YamlConfiguration.loadConfiguration(this.file);
    }

    public void save() {
        throw new RuntimeException("Not supported");
    }

    public MythicConfig getNestedConfig(String field) {
        return new MythicConfigImpl(this.getNode() + field, this.fc);
    }

    public Map<String, MythicConfig> getNestedConfigs(String key) {
        Map<String, MythicConfig> map = new HashMap();
        if (!this.isSet(key)) {
            return map;
        } else {
            Iterator var3 = this.getKeys(key).iterator();

            while(var3.hasNext()) {
                String k = (String)var3.next();
                map.put(k, new MythicConfigImpl(this.getNode() + key + "." + k, this.fc));
            }

            return map;
        }
    }

    public String getString(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        return this.fc.getString(key, this.fc.getString(key.toLowerCase()));
    }

    public String getString(String[] key) {
        return this.getString(key, (String)null);
    }

    public String getString(String field, String def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        return this.fc.getString(key, this.fc.getString(key.toLowerCase(), def));
    }

    public String getString(String[] key, String def, String... args) {
        String s = null;
        String[] var5 = key;
        int var6 = key.length;

        int var7;
        String a;
        for(var7 = 0; var7 < var6; ++var7) {
            a = var5[var7];
            s = this.getString(a, (String)null);
            if (s != null) {
                return s;
            }
        }

        var5 = args;
        var6 = args.length;

        for(var7 = 0; var7 < var6; ++var7) {
            a = var5[var7];
            if (a != null) {
                return a;
            }
        }

        return def;
    }

    public PlaceholderString getPlaceholderString(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        String s = this.fc.getString(key);
        return s == null ? null : PlaceholderString.of(s);
    }

    public PlaceholderString getPlaceholderString(String field, String def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        String s = this.fc.getString(key, def);
        return s == null ? null : PlaceholderString.of(s);
    }

    public String getColorString(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        String s = this.fc.getString(key);
        if (s != null) {
            s = ChatColor.translateAlternateColorCodes('&', s);
        }

        return s;
    }

    public String getColorString(String field, String def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        String s = this.fc.getString(key, def);
        if (s != null) {
            s = ChatColor.translateAlternateColorCodes('&', s);
        }

        return s;
    }

    public boolean getBoolean(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.getBoolean(var10001 + field);
    }

    public boolean getBoolean(String field, boolean def) {
        return this.fc.getBoolean(this.getNode() + field, def);
    }

    public int getInteger(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        return this.fc.getInt(key, this.fc.getInt(key.toLowerCase()));
    }

    public int getInteger(String field, int def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        return this.fc.getInt(key, this.fc.getInt(key.toLowerCase(), def));
    }

    public int getInteger(String[] keys, int def) {
        String[] var3 = keys;
        int var4 = keys.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String key = var3[var5];
            String var10000 = this.getNode();
            key = var10000 + key;
            if (this.fc.isInt(key)) {
                return this.fc.getInt(key);
            }
        }

        return def;
    }

    /** @deprecated */
    @Deprecated
    public int getInt(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.getInt(var10001 + field);
    }

    /** @deprecated */
    @Deprecated
    public int getInt(String field, int def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        return this.fc.getInt(this.getNode() + field, def);
    }

    public double getDouble(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.getDouble(var10001 + field);
    }

    public double getDouble(String field, double def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        return this.fc.getDouble(this.getNode() + field, def);
    }

    public List<String> getStringList(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.getStringList(var10001 + field);
    }

    public List<String> getColorStringList(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var7 = this.fc;
        String var10001 = this.getNode();
        List<String> list = var7.getStringList(var10001 + field);
        List<String> parsed = new ArrayList();
        if (list != null) {
            Iterator var5 = list.iterator();

            while(var5.hasNext()) {
                String str = (String)var5.next();
                parsed.add(ChatColor.translateAlternateColorCodes('&', str));
            }
        }

        return parsed;
    }

    public List<PlaceholderString> getPlaceholderStringList(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var7 = this.fc;
        String var10001 = this.getNode();
        List<String> list = var7.getStringList(var10001 + field);
        List<PlaceholderString> parsed = new ArrayList();
        if (list != null) {
            Iterator var5 = list.iterator();

            while(var5.hasNext()) {
                String str = (String)var5.next();
                parsed.add(PlaceholderString.of(str));
            }
        }

        return parsed;
    }

    public List<Map<?, ?>> getMapList(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.getMapList(var10001 + field);
    }

    public List<?> getList(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        if (this.fc.isSet(key)) {
            return this.fc.getList(key);
        } else {
            return this.fc.isSet(key.toLowerCase()) ? this.fc.getList(key.toLowerCase()) : null;
        }
    }

    public List<Byte> getByteList(String field) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        if (this.fc.isSet(key)) {
            return this.fc.getByteList(key);
        } else {
            return this.fc.isSet(key.toLowerCase()) ? this.fc.getByteList(key.toLowerCase()) : null;
        }
    }

    public ItemStack getItemStack(String field, String def) {
        String var10000 = this.getNode();
        String key = var10000 + field;
        if (this.fc.isSet(key)) {
            return this.fc.getItemStack(key);
        } else if (this.fc.isSet(key.toLowerCase())) {
            return this.fc.getItemStack(key.toLowerCase());
        } else {
            try {
                return new ItemStack(Material.valueOf(def));
            } catch (Exception var5) {
                return null;
            }
        }
    }

    public boolean isConfigurationSection(String section) {
        String var10000 = this.getNode();
        String key = var10000 + section;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.isConfigurationSection(var10001 + section);
    }

    public Set<String> getKeys(String section) {
        String var10000 = this.getNode();
        String key = var10000 + section;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.getConfigurationSection(var10001 + section).getKeys(false);
    }

    public boolean isList(String section) {
        String var10000 = this.getNode();
        String key = var10000 + section;
        ConfigurationSection var3 = this.fc;
        String var10001 = this.getNode();
        return var3.isList(var10001 + section);
    }

    public PlaceholderInt getPlaceholderInt(String key, String def) {
        String s = this.getString(key, def);
        return s == null ? null : PlaceholderInt.of(s);
    }

    public PlaceholderInt getPlaceholderInt(String[] key, String def, String... args) {
        String s = this.getString(key, def, args);
        return s == null ? null : PlaceholderInt.of(s);
    }

    public PlaceholderDouble getPlaceholderDouble(String key, String def) {
        String s = this.getString(key, def);
        return s == null ? null : PlaceholderDouble.of(s);
    }

    public PlaceholderDouble getPlaceholderDouble(String[] key, String def, String... args) {
        String s = this.getString(key, def, args);
        return s == null ? null : PlaceholderDouble.of(s);
    }

    public <T extends Enum> T getEnum(String field, Class<T> clazz, T def) {
        try {
            String in = this.getString(field);
            if (in == null) {
                return def;
            } else {
                T value = (T) Enum.valueOf(clazz, in.toUpperCase());
                return value == null ? def : value;
            }
        } catch (Error | Exception var6) {
            return def;
        }
    }

    public void merge(MythicConfig tmplConfig, List<String> keysToIgnore) {
        ConfigurationSection thisFile = this.fc;
        FileConfiguration tmplFile = tmplConfig.getFileConfiguration();
        String thisMob = this.configName;
        String tmplMob = tmplConfig.getKey();
        Iterator var7 = tmplConfig.getKeys("").iterator();

        while(var7.hasNext()) {
            String k = (String)var7.next();
            if (!keysToIgnore.contains(k)) {
                if (this.getStringList(k).size() > 0) {
                    List<String> currentStringList = this.getStringList(k);
                    currentStringList.addAll(tmplConfig.getStringList(k));
                    this.set(k, currentStringList);
                } else if (!this.isSet(k)) {
                    this.set(k, tmplFile.get(tmplMob + "." + k));
                } else if (thisFile.get(thisMob + "." + k) instanceof MemorySection) {
                    MemorySection memSec = (MemorySection)thisFile.get(thisMob + "." + k);
                    Set<String> templateMemSec = ((MemorySection)tmplFile.get(tmplMob + "." + k)).getKeys(false);
                    templateMemSec.forEach((m) -> {
                        if (!memSec.isSet(m)) {
                            memSec.set(k, tmplFile.get(tmplMob + "." + k + "." + m));
                        }

                    });
                }
            }
        }

    }

    public File getFile() {
        return this.file;
    }
}
