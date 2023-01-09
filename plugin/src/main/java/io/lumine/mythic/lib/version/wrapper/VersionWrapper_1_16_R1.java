package io.lumine.mythic.lib.version.wrapper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTCompound;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.NBTTypeHelper;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftMagicNumbers.NBT;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;

public class VersionWrapper_1_16_R1 implements VersionWrapper {
    private final Map<Material, Material> oreDrops = new HashMap<>();
    private final Set<Material> generatorOutputs = new HashSet<>();

    public VersionWrapper_1_16_R1() {
        oreDrops.put(Material.IRON_ORE, Material.IRON_INGOT);
        oreDrops.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        oreDrops.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);

        generatorOutputs.add(Material.COBBLESTONE);
        generatorOutputs.add(Material.OBSIDIAN);
        generatorOutputs.add(Material.BASALT);
    }

    @Override
    public boolean isGeneratorOutput(Material material) {
        return generatorOutputs.contains(material);
    }

    @Override
    public Map<Material, Material> getOreDrops() {
        return oreDrops;
    }

    @Override
    public float getAttackCooldown(Player player) {
        return player.getAttackCooldown();
    }

    @Override
    public int getFoodRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem().getFoodInfo().getNutrition();
    }

    @Override
    public float getSaturationRestored(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getItem().getFoodInfo().getSaturationModifier();
    }

    @Override
    public void sendJson(Player player, String message) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message), ChatMessageType.CHAT, UUID.randomUUID()));
    }

    @Override
    public void sendActionBarRaw(Player player, String message) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message), ChatMessageType.GAME_INFO, UUID.randomUUID()));
    }

    @Override
    public int getNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage("Repair & Name")));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {
        Field field = null;

        try {
            field = Container.class.getField("windowId");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        FieldUtils.removeFinalModifier(field);

        try {
            FieldUtils.writeField(field, container, containerId);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player) {
        return new AnvilContainer(player);
    }

    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private class AnvilContainer extends ContainerAnvil {
        public AnvilContainer(Player player) {
            super(getNextContainerId(player), ((CraftPlayer) player).getHandle().inventory,
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage("Repair & Name"));
        }
    }

    @Override
    public NBTItem getNBTItem(org.bukkit.inventory.ItemStack item) {
        return new NBTItem_v1_16_R1(item);
    }

    public static class NBTItem_v1_16_R1 extends NBTItem {
        private final net.minecraft.server.v1_16_R1.ItemStack nms;
        private final NBTTagCompound compound;

        public NBTItem_v1_16_R1(org.bukkit.inventory.ItemStack item) {
            super(item);

            nms = CraftItemStack.asNMSCopy(item);
            compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
        }

        @Override
        public Object get(String path) {
            return compound.get(path);
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean hasTag(String path) {
            return compound.hasKey(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public NBTCompound getNBTCompound(String path) {
            return new NBTCompound_v1_16_R1(this, path);
        }

        @Override
        public NBTItem addTag(List<ItemTag> tags) {
            tags.forEach(tag -> {
                if (tag.getValue() instanceof Boolean)
                    compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
                else if (tag.getValue() instanceof Double)
                    compound.setDouble(tag.getPath(), (double) tag.getValue());
                else if (tag.getValue() instanceof String)
                    compound.setString(tag.getPath(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer)
                    compound.setInt(tag.getPath(), (int) tag.getValue());
                else if (tag.getValue() instanceof List<?>) {
                    NBTTagList tagList = new NBTTagList();
                    for (Object s : (List<?>) tag.getValue())
                        if (s instanceof String)
                            tagList.add(NBTTagString.a((String) s));
                    compound.set(tag.getPath(), tagList);
                }
            });
            return this;
        }

        @Override
        public NBTItem removeTag(String... paths) {
            for (String path : paths)
                compound.remove(path);
            return this;
        }

        @Override
        public Set<String> getTags() {
            return compound.getKeys();
        }

        @Override
        public org.bukkit.inventory.ItemStack toItem() {
            nms.setTag(compound);
            return CraftItemStack.asBukkitCopy(nms);
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getTypeId();
        }
    }

    private static class NBTCompound_v1_16_R1 extends NBTCompound {
        private final NBTTagCompound compound;

        public NBTCompound_v1_16_R1(NBTItem_v1_16_R1 item, String path) {
            super();
            compound = (item.hasTag(path) && NBTTypeHelper.COMPOUND.is(item.getTypeId(path))) ? item.compound.getCompound(path) : new NBTTagCompound();
        }

        public NBTCompound_v1_16_R1(NBTCompound_v1_16_R1 comp, String path) {
            super();
            compound = (comp.hasTag(path) && NBTTypeHelper.COMPOUND.is(comp.getTypeId(path))) ? comp.compound.getCompound(path) : new NBTTagCompound();
        }

        @Override
        public boolean hasTag(String path) {
            return compound.hasKey(path);
        }

        @Override
        public Object get(String path) {
            return compound.get(path);
        }

        @Override
        public NBTCompound getNBTCompound(String path) {
            return new NBTCompound_v1_16_R1(this, path);
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public Set<String> getTags() {
            return compound.getKeys();
        }

        @Override
        public int getTypeId(String path) {
            return compound.get(path).getTypeId();
        }
    }

    @Override
    public void playArmAnimation(Player player) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PlayerConnection connection = p.playerConnection;
        PacketPlayOutAnimation armSwing = new PacketPlayOutAnimation(p, 0);
        connection.sendPacket(armSwing);
        connection.a(new PacketPlayInArmAnimation(EnumHand.MAIN_HAND));
    }

    @Override
    public Sound getBlockPlaceSound(org.bukkit.block.Block block) {
        try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();

            net.minecraft.server.v1_16_R1.Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundEffectType soundEffectType = nmsBlock.getStepSound(nmsBlock.getBlockData());

            Field breakSound = SoundEffectType.class.getDeclaredField("Z");
            breakSound.setAccessible(true);
            SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);

            Field keyField = SoundEffect.class.getDeclaredField("b");
            keyField.setAccessible(true);
            MinecraftKey nmsString = (MinecraftKey) keyField.get(nmsSound);

            return Sound.valueOf(nmsString.getKey().replace(".", "_").toUpperCase());
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getSkullValue(Block block) {
        TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle()
                .getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (skullTile.gameProfile == null)
            return "";
        return skullTile.gameProfile.getProperties().get("textures").iterator().next().getValue();
    }

    @Override
    public void setSkullValue(Block block, String value) {
        TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle()
                .getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", value));
        skullTile.setGameProfile(profile);
        skullTile.update();
    }

    @Override
    public FurnaceRecipe getFurnaceRecipe(String path, ItemStack item, Material material, float exp, int cook) {
        return new FurnaceRecipe(new NamespacedKey(MythicLib.inst(), "mmoitems_furnace_" + path), item, material, exp, cook);
    }

    @Override
    public NBTItem copyTexture(NBTItem item) {
        return getNBTItem(new ItemStack(item.getItem().getType())).addTag(new ItemTag("CustomModelData", item.getInteger("CustomModelData")));
    }

    @Override
    public ItemStack textureItem(Material material, int model) {
        return getNBTItem(new ItemStack(material)).addTag(new ItemTag("CustomModelData", model)).toItem();
    }

    @Override
    public Enchantment getEnchantmentFromString(String s) {
        return Enchantment.getByKey(NamespacedKey.minecraft(s));
    }

    @Override
    public FurnaceRecipe getFurnaceRecipe(NamespacedKey key, ItemStack item, Material material, float exp, int cook) {
        return new FurnaceRecipe(key, item, material, exp, cook);
    }

    @Override
    public boolean isCropFullyGrown(Block block) {
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }

    @Override
    public boolean isUndead(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON || type == EntityType.ZOMBIE
                || type == EntityType.DROWNED || type == EntityType.HUSK || type.name().equals("PIG_ZOMBIE") || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.PHANTOM || type == EntityType.WITHER || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE;
    }
}
