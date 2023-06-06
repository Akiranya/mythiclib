package io.lumine.mythic.lib.util;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public interface Jsonable {

    @NotNull
    public JsonObject toJson();
}
