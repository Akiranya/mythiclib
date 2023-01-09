package io.lumine.mythic.lib;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.RegionCondition;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UtilityMethods {
    /**
     * NOT FINAL CODE.
     * THIS WILL BE MASSIVELY REWORKED VERY SOON!
     */
    public static MMOCondition getCondition(String input) {
        MMOLineConfig config = new MMOLineConfig(input);
        String key = config.getKey().toLowerCase();
        switch (key) {
            case "region":
                if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                    return null;
                return new RegionCondition(config);
        }

        return null;
    }

    /**
     * @param loc Where we are looking for nearby entities
     * @return List of all entities surrounding a location. This method loops
     *         through the 9 surrounding chunks and collect all entities from
     *         them. This list can be cached and used multiple times in the same
     *         tick for projectile based spells which need to run entity
     *         checkups
     */
    public static List<Entity> getNearbyChunkEntities(Location loc) {
        List<Entity> entities = new ArrayList<>();

        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();
        // d

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                entities.addAll(Arrays.asList(loc.getWorld().getChunkAt(cx + x, cz + z).getEntities()));

        return entities;
    }

    /**
     * Interaction type is set to OFFENSE_SKILL by default. No bounding box checks
     *
     * @param source Player targeting the entity
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Entity target) {
        return canTarget(source, null, target, InteractionType.OFFENSE_SKILL);
    }

    /**
     * Interaction type is set to OFFENSE_SKILL by default.
     *
     * @param source Player targeting the entity
     * @param loc    If the given location is not null, this method checks if this
     *               location is inside the bounding box of the entity hit
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Location loc, Entity target) {
        return canTarget(source, loc, target, InteractionType.OFFENSE_SKILL);
    }

    /**
     * No bounding box checks
     *
     * @param source      Player targeting the entity
     * @param target      The entity being hit
     * @param interaction Type of interaction
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Entity target, InteractionType interaction) {
        return canTarget(source, null, target, interaction);
    }

    private static final double BOUNDING_BOX_EXPANSION = .2;

    /**
     * @param source      Player targeting the entity
     * @param loc         If the given location is not null, this method checks if this
     *                    location is inside the bounding box of the entity hit
     * @param target      The entity being hit
     * @param interaction Type of interaction
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(@Nullable Player source, @Nullable Location loc, @NotNull Entity target, @NotNull InteractionType interaction) {

        // Check for bounding box
        // Small computations first
        if (loc != null && !target.getBoundingBox().expand(BOUNDING_BOX_EXPANSION).contains(loc.toVector()))
            return false;

        // Interaction type check
        if (!MythicLib.plugin.getEntities().canTarget(source, target, interaction))
            return false;

        return true;
    }

    public static boolean isRealPlayer(Entity entity) {
        return entity instanceof Player && !entity.hasMetadata("NPC");
    }

    public static boolean isMetaItem(@Nullable ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
    }

    /**
     * @param player Player to heal
     * @param heal   Heal amount
     *               <br>
     *               Negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal) {
        heal(player, heal, false);
    }

    /**
     * @param player         Player to heal
     * @param heal           Heal amount
     * @param allowNegatives If passing a negative health value will damage the entity x)
     *                       <br>
     *                       If <code>false</code>, negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal, boolean allowNegatives) {
        if (heal > 0 || allowNegatives)
            player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + heal));
    }

    private static final Random RANDOM = new Random();

    /**
     * Super useful to display enum names like DIAMOND_SWORD in chat
     *
     * @param input String with lower cases and spaces only
     * @return Same string with capital letters at the beginning of each word.
     */
    public static String caseOnWords(String input) {
        StringBuilder builder = new StringBuilder(input);
        boolean isLastSpace = true;
        for (int i = 0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            if (isLastSpace && ch >= 'a' && ch <= 'z') {
                builder.setCharAt(i, (char) (ch + ('A' - 'a')));
                isLastSpace = false;
            } else isLastSpace = ch == ' ';
        }
        return builder.toString();
    }

    public static void dropItemNaturally(Location loc, ItemStack stack) {
        double dx = ((RANDOM.nextFloat() * 0.5F) + 0.25D) / 10;
        double dy = ((RANDOM.nextFloat() * 0.5F) + 0.25D) / 10;
        double dz = ((RANDOM.nextFloat() * 0.5F) + 0.25D) / 10;
        loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), stack).setVelocity(new Vector(dx, dy, dz));
    }

    /**
     * Used to find players in chunks around some location. This is
     * used when displaying individual holograms to a list of players.
     *
     * @param loc Target location
     * @return Players in chunks around the location
     */
    @Deprecated
    public static List<Player> getNearbyPlayers(Location loc) {
        final List<Player> players = new ArrayList<>();

        final int cx = loc.getChunk().getX(), cz = loc.getChunk().getZ();

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                for (Entity target : loc.getWorld().getChunkAt(cx + x, cz + z).getEntities())
                    if (target instanceof Player)
                        players.add((Player) target);

        return players;
    }

    public static void loadDefaultFile(String path, String name) {
        final String newPath = path.isEmpty() ? "" : "/" + path;
        final File folder = new File(MythicLib.plugin.getDataFolder() + newPath);
        if (!folder.exists()) folder.mkdir();

        final File file = new File(MythicLib.plugin.getDataFolder() + newPath, name);
        if (!file.exists()) try {
            Files.copy(MythicLib.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * The 'vanished' meta data should be supported by vanish plugins
     * to let all the plugins knows when a player is vanished.
     *
     * @return If a given player is vanished or not
     */
    public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished"))
            if (meta.asBoolean())
                return true;
        return false;
    }

    /**
     * @return Upper case string, with spaces and - replaced by _
     */
    public static String enumName(String str) {
        return str.toUpperCase().replace("-", "_").replace(" ", "_");
    }

    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    public static double[] getYawPitch(Vector axis) {
        double _2PI = 6.283185307179586D;
        double x = axis.getX();
        double z = axis.getZ();

        if (x == 0 && z == 0)
            return new double[]{0, axis.getY() > 0 ? -90 : 90};
        else {
            double theta = Math.atan2(-x, z);
            double yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);
            double xz = Math.sqrt(x * x + z * z);
            double pitch = (float) Math.toDegrees(Math.atan(-axis.getY() / xz));
            return new double[]{yaw, pitch};
        }
    }

    public static Vector rotate(Vector rotated, Vector axis) {
        double[] pitchYaw = getYawPitch(axis);
        return rotate(rotated, Math.toRadians(pitchYaw[0]), Math.toRadians(pitchYaw[1]));
    }

    public static Vector rotate(Vector rotated, double yaw, double pitch) {
        return rotAxisY(rotAxisX(rotated, pitch), -yaw);
    }

    private static Vector rotAxisX(Vector rotated, double angle) {
        double y = rotated.getY() * Math.cos(angle) - rotated.getZ() * Math.sin(angle);
        double z = rotated.getY() * Math.sin(angle) + rotated.getZ() * Math.cos(angle);
        return rotated.setY(y).setZ(z);
    }

    private static Vector rotAxisY(Vector rotated, double angle) {
        double x = rotated.getX() * Math.cos(angle) + rotated.getZ() * Math.sin(angle);
        double z = rotated.getX() * -Math.sin(angle) + rotated.getZ() * Math.cos(angle);
        return rotated.setX(x).setZ(z);
    }

    public static double getAltitude(Entity entity) {
        return getAltitude(entity.getLocation());
    }

    public static double getAltitude(Location loc) {
        Location moving = loc.clone();
        while (!moving.getBlock().getType().isSolid())
            moving.add(0, -1, 0);

        return loc.getY() - moving.getBlockY() - 1;
    }
}
