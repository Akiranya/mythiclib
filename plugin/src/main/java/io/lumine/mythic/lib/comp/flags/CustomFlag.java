package io.lumine.mythic.lib.comp.flags;

public enum CustomFlag {

    // MMOItems flags
    MI_WEAPONS(true),
    MI_COMMANDS(true),
    MI_CONSUMABLES(true),
    MI_TOOLS(true),

    // MythicLib
    MMO_ABILITIES(true),

    // MMOCore
    PVP_MODE(false),

    /**
     * @deprecated Not used anymore
     */
    @Deprecated
    ABILITY_PVP(true);

    private final boolean def;

    CustomFlag(boolean def) {
        this.def = def;
    }

    public boolean getDefault() {
        return def;
    }

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }
}
