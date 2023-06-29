package io.lumine.mythic.lib.util.network;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mythiclib
 * 18/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MythicPacketSniffer extends LightInjector {

    private static final Map<String, Map<String, Field>> fields = new ConcurrentHashMap<>();


    /**
     * Initializes the injector and starts to listen to packets.
     * <p>
     * Note that, while it is possible to create more than one instance per plugin,
     * it's more efficient and recommended to just have only one.
     *
     * @param plugin The {@link Plugin} which is instantiating this injector.
     * @throws NullPointerException     If the provided {@code plugin} is {@code null}.
     * @throws IllegalStateException    When <b>not</b> called from the main thread.
     * @throws IllegalArgumentException If the provided {@code plugin} is not enabled.
     */
    public MythicPacketSniffer(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    protected @Nullable Object onPacketReceiveAsync(@Nullable Player sender, @NotNull Channel channel, @NotNull Object packet) {
        if (sender == null)
            return packet;
        final String packetName = packet.getClass().getSimpleName();

        if (packetName.equals("PacketPlayInArmAnimation")) {
            Object hand = getField(packet, "a");
            if (hand == null || !hand.toString().equals("MAIN_HAND"))
                return packet;

            // We need to run this synchronously because bukkit sucks
            Bukkit.getScheduler().runTask(getPlugin(), () -> {
                Entity entity = getTarget(sender);
                if (entity != null)
                    triggerEvent(sender);
            });
        }

        return packet;
    }

    @Override
    protected @Nullable Object onPacketSendAsync(@Nullable Player receiver, @NotNull Channel channel, @NotNull Object packet) {
        return packet;
    }

    private @Nullable Entity getTarget(Player player) {
        double range = 3.5;
        return player.getNearbyEntities(range, range, range)
                .stream()
                .filter(entity -> !(entity instanceof Player && ((Player) entity).getGameMode().ordinal() == 3))
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> isLineOfSight(player, entity))
                .min((a, b) -> (int) (a.getLocation().distanceSquared(player.getLocation()) - b.getLocation().distanceSquared(player.getLocation())))
                .orElse(null);
    }

    private boolean isLineOfSight(Player player, Entity targetEntity) {
        Location eyeLocation = player.getEyeLocation();
        Vector playerDirection = eyeLocation.getDirection();
        RayTraceResult result = player.getWorld().rayTrace(eyeLocation, playerDirection, 100, FluidCollisionMode.NEVER, true, 0.1, entity -> entity.equals(targetEntity));
        return result != null && result.getHitEntity() == targetEntity;
    }

    private void triggerEvent(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerInteractEvent event = new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, BlockFace.EAST, EquipmentSlot.HAND);
                Bukkit.getPluginManager().callEvent(event);
            }
        }.runTask(getPlugin());
    }

    @Nullable
    private Object getField(Object object, String field) {
        return getField(object, object.getClass(), field);
    }

    @Nullable
    private Object getField(Object object, Class<?> c, String field) {
        if (fields.containsKey(c.getCanonicalName())) {
            Map<String, Field> fs = fields.get(c.getCanonicalName());
            if (fs.containsKey(field)) {
                try {
                    return fs.get(field).get(object);
                } catch (ReflectiveOperationException e) {
                    return null;
                }
            }
        }

        Class<?> current = c;
        Field f;
        while (true)
            try {
                f = current.getDeclaredField(field);
                break;
            } catch (ReflectiveOperationException e1) {
                current = current.getSuperclass();
                if (current != null) {
                    continue;
                }
                return null;
            }

        f.setAccessible(true);

        Map<String, Field> map;
        if (fields.containsKey(c.getCanonicalName())) {
            map = fields.get(c.getCanonicalName());
        } else {
            map = new ConcurrentHashMap<>();
            fields.put(c.getCanonicalName(), map);
        }

        map.put(f.getName(), f);

        try {
            return f.get(object);
        } catch (ReflectiveOperationException e) {
            return null;
        }

    }
}
