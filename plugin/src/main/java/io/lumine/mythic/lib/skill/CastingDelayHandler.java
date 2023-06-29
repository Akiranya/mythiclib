package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CastingDelayHandler extends TemporaryListener {
    private final SkillMetadata metadata;

    private final double slowness;
    private final int delayTicks;
    @NotNull
    private final BukkitRunnable runnable;

    @Nullable
    private final BossBar bossbar;

    private static final String MOVEMENT_SPEED_MODIFIER_KEY = "mythiclibSkillCasting";

    /**
     * Called when a skill has a non null casting delay.
     *
     * @param metadata Information about skill being cast
     * @param result   Result of skill being cast
     */
    public CastingDelayHandler(SkillMetadata metadata, SkillResult result) {
        super(PlayerMoveEvent.getHandlerList(), PlayerCastSkillEvent.getHandlerList());

        this.metadata = metadata;
        this.delayTicks = (int) (metadata.getParameter("delay") * 20);

        // Implement a runnable to run the task later
        (runnable = new BukkitRunnable() {
            private int counter = delayTicks;

            @Override
            public void run() {

                // Update bossbar
                if (hasBossbar())
                    bossbar.setProgress((double) (delayTicks - counter) / (double) delayTicks);

                counter--;

                // Terminate and cast
                if (counter <= 0) {
                    if (hasBossbar())
                        bossbar.removeAll();
                    cancel();
                    close();

                    metadata.getCast().castInstantly(metadata, result);
                }
            }
        }).runTaskTimer(MythicLib.plugin, 0, 1);

        // Play sound
        castIfNotNull(MythicLib.plugin.getMMOConfig().skillCastScript);

        // Slowness
        slowness = MythicLib.plugin.getMMOConfig().castingDelaySlowness;
        if (slowness > 0)
            new StatModifier(MOVEMENT_SPEED_MODIFIER_KEY, "MOVEMENT_SPEED", -slowness, ModifierType.RELATIVE).register(getCaster());

        // Bossbar
        bossbar = MythicLib.plugin.getMMOConfig().enableCastingDelayBossbar ? handleBossbar() : null;
    }

    public MMOPlayerData getCaster() {
        return metadata.getCaster().getData();
    }

    public boolean hasBossbar() {
        return bossbar != null;
    }

    private void castIfNotNull(@Nullable Skill skill) {
        if (skill != null)
            skill.cast(new SkillMetadata(skill, getCaster()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
            return;

        if (!event.getPlayer().equals(getCaster().getPlayer()))
            return;

        // Should moving cancel skill casting
        if (MythicLib.plugin.getMMOConfig().castingDelayCancelOnMove) {
            event.setCancelled(true);
            castIfNotNull(MythicLib.plugin.getMMOConfig().skillCancelScript);
            close();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCast(PlayerCastSkillEvent event) {
        if (event.getPlayer().equals(getCaster().getPlayer()))
            event.setCancelled(true);
    }

    @NotNull
    private BossBar handleBossbar() {
        final NamespacedKey namespacedKey = new NamespacedKey(MythicLib.plugin, "mythiclib_casting_" + getCaster().getUniqueId());
        final BossBar bossbar = Bukkit.createBossBar(namespacedKey, MythicLib.plugin.getMMOConfig().castingDelayBossbarFormat, BarColor.WHITE, BarStyle.SEGMENTED_20);
        bossbar.addPlayer(getCaster().getPlayer());
        return bossbar;
    }

    @Override
    public void whenClosed() {
        if (!runnable.isCancelled())
            runnable.cancel();

        // Clear slowness
        if (slowness > 0)
            getCaster().getStatMap().getInstance("MOVEMENT_SPEED").remove(MOVEMENT_SPEED_MODIFIER_KEY);
    }
}
