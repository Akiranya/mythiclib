package io.lumine.mythic.lib.util.gson;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement element, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject object = element.getAsJsonObject();
        return new Location(
                Bukkit.getWorld(object.get("World").getAsString()),
                object.get("X").getAsDouble(),
                object.get("Y").getAsDouble(),
                object.get("Z").getAsDouble(),
                object.get("Yaw").getAsFloat(),
                object.get("Pitch").getAsFloat()
        );
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject object = new JsonObject();
        object.addProperty("World", location.getWorld().getName());
        object.addProperty("X", location.getX());
        object.addProperty("Y", location.getY());
        object.addProperty("Z", location.getZ());
        object.addProperty("Yaw", location.getYaw());
        object.addProperty("Pitch", location.getPitch());
        return object;
    }
}