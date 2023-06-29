package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.logging.Level;

@MechanicMetadata
public class DispatchCommandMechanic extends TargetMechanic {
    private final String format;
    private final boolean fromConsole, asOperator;

    public DispatchCommandMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("format");

        format = config.getString("format");
        asOperator = config.getBoolean("op", false);
        fromConsole = config.getBoolean("from_console", true);
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        final String command = meta.parseString(format);
        if (asOperator && !target.isOp()) try {
            target.setOp(true);
            Bukkit.dispatchCommand(target, meta.parseString(format));
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not run command '" + command + "' as entity '" + target.getUniqueId() + "': " + exception.getMessage());
        } finally {
            target.setOp(false);
        }

        Bukkit.dispatchCommand(fromConsole ? Bukkit.getConsoleSender() : target, meta.parseString(format));
    }
}
