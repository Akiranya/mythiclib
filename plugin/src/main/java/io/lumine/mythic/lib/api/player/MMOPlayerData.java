package io.lumine.mythic.lib.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.player.skillmod.SkillModifierMap;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.listener.PlayerListener;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import io.lumine.mythic.lib.player.particle.ParticleEffectMap;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffectMap;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MMOPlayerData {
    private final UUID uuid;

    @Nullable
    private Player player;

    /**
     * Last time the player either logged in or logged out.
     */
    private long lastLogActivity;

    // Temporary player data
    private final CooldownMap cooldownMap = new CooldownMap();
    private final StatMap statMap = new StatMap(this);
    private final SkillModifierMap skillModifierMap = new SkillModifierMap(this);
    private final PermanentPotionEffectMap permEffectMap = new PermanentPotionEffectMap(this);
    private final ParticleEffectMap particleEffectMap = new ParticleEffectMap(this);
    private final PassiveSkillMap passiveSkillMap = new PassiveSkillMap(this);
    private final VariableList variableList = new VariableList(VariableScope.PLAYER);

    /**
     * Map used by other plugins to save any type of data. This
     * is typically used by MMOCore and MMOItems to store the player
     * resources when the player logs off.
     */
    private final Map<String, Object> externalData = new HashMap<>();

    private MMOPlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
    }

    /**
     * MMOPlayerData for offline players
     *
     * @param uuid Player UUID
     */
    public MMOPlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = null;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * @return The player's stat map which can be used by any other plugins to
     *         apply stat modifiers to ANY MMOItems/MMOCore/external stats,
     *         calculate stat values, etc.
     */
    public StatMap getStatMap() {
        return statMap;
    }

    /**
     * @return The player's skill modifier map. This map applies modifications
     *         to numerical skill parameters (damage, cooldown...)
     */
    public SkillModifierMap getSkillModifierMap() {
        return skillModifierMap;
    }

    /**
     * @deprecated Not implemented yet
     */
    @Deprecated
    public PermanentPotionEffectMap getPermanentEffectMap() {
        return permEffectMap;
    }

    /**
     * @deprecated Not implemented yet
     */
    @Deprecated
    public ParticleEffectMap getParticleEffectMap() {
        return particleEffectMap;
    }

    /**
     * @return All active skill triggers
     */
    public PassiveSkillMap getPassiveSkillMap() {
        return passiveSkillMap;
    }

    /**
     * Used to trigger skills with no attack metadata. This caches
     * the player statistics and create an attack metadata.
     *
     * @param triggerType Action performed to trigger the skills
     * @param target      The potential target to cast the skill onto
     */
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable Entity target) {
        Validate.isTrue(!triggerType.isActionHandSpecific(), "You must provide an action hand");
        triggerSkills(triggerType, EquipmentSlot.MAIN_HAND, target);
    }

    /**
     * Used to trigger skills with no attack metadata. This caches
     * the player statistics and create an attack metadata.
     *
     * @param triggerType Action performed to trigger the skills
     * @param actionHand  Hand used to perform action
     * @param target      The potential target to cast the skill onto
     */
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull EquipmentSlot actionHand, @Nullable Entity target) {
        Validate.notNull(actionHand, "Action hand cannot be null");
        triggerSkills(triggerType, statMap.cache(actionHand), target);
    }

    /**
     * @deprecated It is now useless to store AttackMetadatas in SkillMetadatas
     */
    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable AttackMetadata attackMetadata, @Nullable Entity target) {
        final Iterable<PassiveSkill> cast = triggerType.isActionHandSpecific() ? passiveSkillMap.isolateModifiers(caster.getActionHand()) : passiveSkillMap.getModifiers();
        triggerSkills(triggerType, caster, target, cast);
    }

    /**
     * Trigger skills with an attack metadata and target entity.
     *
     * @param triggerType Action performed to trigger the skills
     * @param target      The potential target to cast the skill onto
     */
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable Entity target) {
        final Iterable<PassiveSkill> cast = triggerType.isActionHandSpecific() ? passiveSkillMap.isolateModifiers(caster.getActionHand()) : passiveSkillMap.getModifiers();
        triggerSkills(triggerType, caster, target, cast);
    }

    /**
     * @deprecated It is now useless to store AttackMetadatas in SkillMetadatas
     */
    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable AttackMetadata attackMetadata, @Nullable Entity target, @NotNull Iterable<PassiveSkill> skills) {
        triggerSkills(triggerType, caster, target, skills);
    }

    /**
     * Trigger a specific set of skills, with an attack metadata.
     * You can also provide the player statistics used to cast the skills
     * which is for instance used for projectile trigger types.
     *
     * @param triggerType Action performed to trigger the skills
     * @param caster      The player cached statistics
     * @param target      The potential target to cast the skill onto
     * @param skills      The list of skills currently active for the player
     */
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable Entity target, @NotNull Iterable<PassiveSkill> skills) {
        if (getPlayer().getGameMode() == GameMode.SPECTATOR || !MythicLib.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MMO_ABILITIES))
            return;

        final TriggerMetadata triggerMeta = new TriggerMetadata(caster, target);

        for (PassiveSkill skill : skills) {
            final SkillHandler handler = skill.getTriggeredSkill().getHandler();
            if (handler.isTriggerable() && skill.getType().equals(triggerType))
                skill.getTriggeredSkill().cast(triggerMeta);
        }
    }

    public VariableList getVariableList() {
        return variableList;
    }

    /**
     * @return The last time, in millis, the player logged in or out
     * @deprecated Use {@link #getLastLogActivity()} instead
     */
    @Deprecated
    public long getLastLogin() {
        return getLastLogActivity();
    }

    /**
     * @return The last time, in millis, the player logged in or out
     */
    public long getLastLogActivity() {
        return lastLogActivity;
    }

    /**
     * When a player logs off, the MythicLib player data is cached for
     * an extra delay which is set to 24 hours before it is finally removed
     * from the memory.
     * <p>
     * Cache time out is set to one full day. If the player does NOT reconnect
     * after one day, the temporary player data will be completely lost.
     */
    private static final long CACHE_TIME_OUT = 1000 * 60 * 60 * 24;

    public boolean isTimedOut() {
        return !isOnline() && lastLogActivity + CACHE_TIME_OUT < System.currentTimeMillis();
    }

    /**
     * This method simply checks if the cached Player instance is null
     * because MythicLib uncaches it when the player leaves for memory purposes.
     *
     * @return If the player is currently online.
     */
    public boolean isOnline() {
        return player != null;
    }

    /**
     * Throws an IAE if the player is currently not online
     * OR if the Player instance was not cached in yet.
     * <p>
     * MythicLib updates the Player instance on event priority LOW
     * using {@link PlayerJoinEvent} here: {@link PlayerListener}
     *
     * @return Returns the corresponding Player instance.
     */
    @NotNull
    public Player getPlayer() {
        return Objects.requireNonNull(player, "Player is offline");
    }

    /**
     * Caches a new Player instance and refreshes the last log activity.
     * Provided player can be null if the player is disconnecting
     *
     * @param player Player instance to cache (null if logging off)
     */
    public void updatePlayer(@Nullable Player player) {
        this.player = player;
        this.lastLogActivity = System.currentTimeMillis();
    }

    /**
     * Used when damage mitigation or a crit occurs to apply cooldown
     *
     * @param cd    The type of mitigation
     * @param value Mitigation cooldown in seconds
     */
    public void applyCooldown(CooldownType cd, double value) {
        cooldownMap.applyCooldown(cd.name(), value);
    }

    /**
     * @param cd Cooldown type
     * @return If the mecanic is currently on cooldown for the player
     */
    public boolean isOnCooldown(CooldownType cd) {
        return cooldownMap.isOnCooldown(cd.name());
    }

    /**
     * Cooldown maps centralize cooldowns in MythicLib for easier use.
     * Can be used for item cooldows, item abilities, MMOCore player
     * skills or any other external plugin
     *
     * @return The main player's cooldown map
     */
    public CooldownMap getCooldownMap() {
        return cooldownMap;
    }

    @Nullable
    public <T> T getExternalData(String key, Class<T> objectType) {
        final @Nullable Object found = externalData.get(key);
        return found == null ? null : (T) found;
    }

    public void setExternalData(String key, Object obj) {
        externalData.put(key, obj);
    }

    public boolean hasExternalData(String key) {
        return externalData.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMOPlayerData)) return false;

        MMOPlayerData that = (MMOPlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    private static final Map<UUID, MMOPlayerData> data = new HashMap<>();

    /**
     * Called everytime a player enters the server. If the
     * resource data is not initialized yet, initializes it.
     * <p>
     * This is called async using {@link AsyncPlayerPreLoginEvent} which does
     * not provide a Player instance, meaning the cached Player instance is NOT
     * loaded yet. It is only loaded when the player logs in using {@link PlayerJoinEvent}
     *
     * @param player Player whose data should be initialized
     */
    public static MMOPlayerData setup(Player player) {
        final @Nullable MMOPlayerData found = data.get(player.getUniqueId());

        // Not loaded yet, checks for temporary data
        if (found == null) {
            final MMOPlayerData playerData = new MMOPlayerData(player);
            data.put(player.getUniqueId(), playerData);
            return playerData;
        }

        found.updatePlayer(player);
        return found;
    }

    /**
     * This essentially checks if a player logged in since the last time the
     * server started/was reloaded.
     *
     * @param uuid The player UUID to check
     * @return If the MMOPlayerData instance is loaded for a specific player
     * @deprecated Use {@link #has(UUID)} instead
     */
    @Deprecated
    public static boolean isLoaded(UUID uuid) {
        return has(uuid);
    }

    @NotNull
    public static MMOPlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    @NotNull
    public static MMOPlayerData get(UUID uuid) {
        return Objects.requireNonNull(data.get(uuid), "Player data not loaded");
    }

    /**
     * Use it at your own risk! Player data might not be loaded
     */
    @Nullable
    public static MMOPlayerData getOrNull(UUID uuid) {
        return data.get(uuid);
    }

    /**
     * This is being used to easily check if an online player corresponds to
     * a real player or a Citizens NPC. Citizens NPCs do not have any player
     * data associated to them
     *
     * @return Checks if plater data is loaded for a specific player UID
     */
    public static boolean has(Player player) {
        return has(player.getUniqueId());
    }

    /**
     * This is being used to easily check if an online player corresponds to
     * a real player or a Citizens NPC. Citizens NPCs do not have any player
     * data associated to them
     *
     * @return Checks if plater data is loaded for a specific player UID
     */
    public static boolean has(UUID uuid) {
        return data.containsKey(uuid);
    }

    /**
     * @return Currently loaded MMOPlayerData instances. This can be used to
     *         apply things like resource regeneration or other runnable based
     *         tasks instead of looping through online players and having to
     *         resort to a map-lookup-based get(Player) call
     */
    public static Collection<MMOPlayerData> getLoaded() {
        return data.values();
    }

    /**
     * Calls some method for every player online. Performance method
     * to avoid useless list calculations since it is being used by
     * MythicLib 20 times a second for every online player.
     * <p>
     * This method is more performant than the following code:
     * <code>Bukkit.getOnlinePlayers().forEach(player -> MMOPlayerData.get(player)......);</code>
     */
    public static void forEachOnline(Consumer<MMOPlayerData> action) {
        for (MMOPlayerData registered : data.values())
            if (registered.isOnline())
                action.accept(registered);
    }

    /**
     * Unloads all timed-out temporary player data. This should be
     * checked once an hour to make sure not to cause memory leaks.
     */
    public static void flushOfflinePlayerData() {
        Iterator<MMOPlayerData> iterator = data.values().iterator();
        while (iterator.hasNext()) {
            MMOPlayerData tempData = iterator.next();
            if (tempData.isTimedOut())
                iterator.remove();
        }
    }
}

