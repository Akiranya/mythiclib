package io.lumine.mythic.lib.util.gson;

import com.google.gson.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.UUID;

public class AttributeModifierAdapter implements JsonSerializer<AttributeModifier>, JsonDeserializer<AttributeModifier> {

    @Override
    public AttributeModifier deserialize(JsonElement element, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject object = element.getAsJsonObject();
        return new AttributeModifier(
                UUID.fromString(object.get("UUID").getAsString()),
                object.get("Name").getAsString(),
                object.get("Amount").getAsDouble(),
                AttributeModifier.Operation.valueOf(object.get("Operation").getAsString()),
                object.has("Slot") ? EquipmentSlot.valueOf(object.get("Slot").getAsString()) : null);
    }

    @Override
    public JsonElement serialize(AttributeModifier modifier, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject object = new JsonObject();
        object.addProperty("UUID", modifier.getUniqueId().toString());
        object.addProperty("Name", modifier.getName());
        object.addProperty("Amount", modifier.getAmount());
        object.addProperty("Operation", modifier.getOperation().name());
        if (modifier.getSlot() != null) object.addProperty("Slot", modifier.getSlot().name());
        return object;
    }
}