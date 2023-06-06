package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.LocationMechanic;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.Arrays;

@MechanicMetadata
public class SoundMechanic extends LocationMechanic {
    private Sound sound;
    private final String soundString;
    private final float vol, pitch;

    public SoundMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("sound");
        soundString = config.getString("sound");
        //The string given as input might not correspond to an element of the enum (custom sound)
        if (Arrays.stream(Sound.values()).map(Enum::name).anyMatch(name -> UtilityMethods.enumName(soundString).equals(name)))
            sound = Sound.valueOf(UtilityMethods.enumName(soundString));
        vol = (float) config.getDouble("volume", 1);
        pitch = (float) config.getDouble("pitch", 1);
    }

    @Override
    public void cast(SkillMetadata meta, Location loc) {
        if (sound == null)
            loc.getWorld().playSound(loc, soundString, vol, pitch);
        else
            loc.getWorld().playSound(loc, sound, vol, pitch);
    }
}