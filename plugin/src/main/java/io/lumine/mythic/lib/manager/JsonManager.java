package io.lumine.mythic.lib.manager;

import com.google.gson.JsonElement;
import io.lumine.mythic.lib.MythicLib;

@Deprecated
public class JsonManager {
    public <T> T parse(String s, Class<T> c) {
        return MythicLib.plugin.getGson().fromJson(s, c);
    }

    public String toString(JsonElement json) {
        return MythicLib.plugin.getGson().toJson(json);
    }
}
