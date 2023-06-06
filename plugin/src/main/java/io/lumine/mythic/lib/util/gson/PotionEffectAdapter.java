package io.lumine.mythic.lib.util.gson;

import com.google.gson.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;

public class PotionEffectAdapter implements JsonSerializer<PotionEffect>, JsonDeserializer<PotionEffect> {

    @Override
    public JsonElement serialize(PotionEffect effect, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject object = new JsonObject();
        object.addProperty("Type", effect.getType().getName());
        object.addProperty("Duration", effect.getDuration());
        object.addProperty("Amplifier", effect.getAmplifier());
        object.addProperty("Ambient", effect.isAmbient());
        object.addProperty("Particles", effect.hasParticles());
        object.addProperty("Icon", effect.hasIcon());
        return object;
    }

    @Override
    public PotionEffect deserialize(JsonElement element, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject object = element.getAsJsonObject();
        return new PotionEffect(
                PotionEffectType.getByName(object.get("Type").getAsString()),
                object.get("Duration").getAsInt(),
                object.get("Amplifier").getAsInt(),
                object.get("Ambient").getAsBoolean(),
                object.get("Particles").getAsBoolean(),
                object.get("Icon").getAsBoolean()
        );
    }
}