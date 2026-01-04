package net.smok.drifter.blocks.alert;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smok.drifter.network.ServerBoundPackets;
import net.smok.drifter.registries.Values;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

public class Icon {

    // Always size 16
    public static final ItemStack[] COLORS_WOOL = new ItemStack[] {                 new ItemStack(Items.BLACK_WOOL),
            new ItemStack(Items.GRAY_WOOL),   new ItemStack(Items.LIGHT_GRAY_WOOL), new ItemStack(Items.WHITE_WOOL),
            new ItemStack(Items.BROWN_WOOL),  new ItemStack(Items.RED_WOOL),        new ItemStack(Items.ORANGE_WOOL),
            new ItemStack(Items.YELLOW_WOOL), new ItemStack(Items.GREEN_WOOL),      new ItemStack(Items.LIME_WOOL),
            new ItemStack(Items.CYAN_WOOL),   new ItemStack(Items.BLUE_WOOL),       new ItemStack(Items.LIGHT_BLUE_WOOL),
            new ItemStack(Items.PURPLE_WOOL), new ItemStack(Items.MAGENTA_WOOL),    new ItemStack(Items.PINK_WOOL)
    };

    // Always size 16
    public static final Icon[] ICON_PRESETS = new Icon[] {
            new Icon(Values.ALERT_EFFECT.get(),5, true),
            new Icon(Values.ALERT_EFFECT.get(),7, true),
            new Icon(Values.ALERT_EFFECT.get(),11, true),
            new Icon(Values.ALERT_EFFECT.get(),2, true),
            new Icon(MobEffects.REGENERATION,5, false),
            new Icon(MobEffects.HUNGER,6, false),
            new Icon(MobEffects.POISON,8, false),
            new Icon(MobEffects.LUCK,9, false),
            new Icon(MobEffects.ABSORPTION,11, false),
            new Icon(MobEffects.WATER_BREATHING,12, false),
            new Icon(Items.CLOCK,7, false),
            new Icon(Items.COMPASS,3, false),
            new Icon(Items.BLAZE_POWDER,5, false),
            new Icon(Items.REDSTONE,5, false),
            new Icon(Items.COAL,1, false),
            new Icon(Items.EMERALD,9, false)
    };

    private ResourceLocation id;
    private int colorIndex;
    private boolean paintIcon;
    private @Nullable ItemStack asItem;
    private @Nullable MobEffect asMobEffect;

    public Icon(@NotNull MobEffect id, int colorIndex, boolean paintIcon) {
        this.id = BuiltInRegistries.MOB_EFFECT.getKey(id);
        this.colorIndex = colorIndex;
        this.paintIcon = paintIcon;
        this.asItem = null;
        this.asMobEffect = id;
    }

    public Icon(@NotNull Item id, int colorIndex, boolean paintIcon) {
        this.id = BuiltInRegistries.ITEM.getKey(id);
        this.colorIndex = colorIndex;
        this.paintIcon = paintIcon;
        this.asItem = new ItemStack(id);
        this.asMobEffect = null;
    }

    public Icon() {
        this(ICON_PRESETS[0]);
    }

    @Contract(pure = true)
    public Icon(@NotNull Icon preset) {
        id = preset.id;
        colorIndex = preset.colorIndex;
        paintIcon = preset.paintIcon;
        asItem = preset.asItem;
        asMobEffect = preset.asMobEffect;
    }

    public Icon(ResourceLocation id, int colorIndex, boolean paintIcon) {
        this.colorIndex = colorIndex;
        this.paintIcon = paintIcon;
        setId(id);
    }

    public Color getColor() {
        return COLORS[colorIndex];
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public String getIdAsString() {
        if (asItem != null) return id.getNamespace() + ":item/" + id.getPath();
        if (asMobEffect != null) return id.getNamespace() + ":mob_effect/" + id.getPath();
        return id.toString();
    }

    public void setId(String id) {
        setId(new ResourceLocation(id));
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setId(ResourceLocation id) {
        if (id.getPath().contains("mob_effect/")) {
            ResourceLocation name = new ResourceLocation(id.getNamespace(), id.getPath().replace("mob_effect/", ""));
            if (BuiltInRegistries.MOB_EFFECT.containsKey(name)) {
                this.id = name;
                asMobEffect = BuiltInRegistries.MOB_EFFECT.get(name);
                asItem = null;
            }

        } else if (id.getPath().contains("item/")) {
            ResourceLocation name = new ResourceLocation(id.getNamespace(), id.getPath().replace("item/", ""));
            if (BuiltInRegistries.ITEM.containsKey(name)) {
                this.id = name;
                asItem = BuiltInRegistries.ITEM.get(name).getDefaultInstance();
                asMobEffect = null;
            }
        } else {
            if (BuiltInRegistries.MOB_EFFECT.containsKey(id)) {
                this.id = id;
                asMobEffect = BuiltInRegistries.MOB_EFFECT.get(id);
                asItem = null;
            }
            else if(BuiltInRegistries.ITEM.containsKey(id)) {
                this.id = id;
                asItem = BuiltInRegistries.ITEM.get(id).getDefaultInstance();
                asMobEffect = null;
            }
        }
    }

    public void setColorIndex(int color) {
        this.colorIndex = Mth.clamp(color, 0, 15);
    }

    public boolean isPaintIcon() {
        return paintIcon;
    }

    public void setPaintIcon(boolean paintIcon) {
        this.paintIcon = paintIcon;
    }

    public @Nullable ItemStack getAsItem() {
        return asItem;
    }

    public @Nullable MobEffect getAsMobEffect() {
        return asMobEffect;
    }

    public static final ServerBoundPackets.ByteCodec<Icon> BYTE_CODEC = ServerBoundPackets.codec((byteBuf, icon) -> {
        byteBuf.writeResourceLocation(icon.id);
        byteBuf.writeInt(icon.colorIndex);
        byteBuf.writeBoolean(icon.paintIcon);
    }, byteBuf -> {
        Icon icon = new Icon();
        icon.setId(byteBuf.readResourceLocation());
        icon.colorIndex = byteBuf.readInt();
        icon.paintIcon = byteBuf.readBoolean();
        return icon;
    });


    private static final Color[] COLORS = new Color[]{Color.black, Color.darkGray, Color.gray, Color.white,
            new Color(0x5a381e), Color.red, Color.orange, Color.yellow, Color.green, new Color(0x455522),
            Color.cyan, new Color(0x217fb9), Color.blue, new Color(0x5d1d91), Color.magenta, Color.pink};

    @Override
    public String toString() {
        return "Icon{" +
                "id=" + id +
                ", color=" + colorIndex +
                ", paintIcon=" + paintIcon +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Icon icon = (Icon) object;
        return colorIndex == icon.colorIndex && paintIcon == icon.paintIcon && Objects.equals(id, icon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, colorIndex, paintIcon);
    }


    @Contract(" -> new")
    public static @NotNull SavedDataSlot<Icon> savedDataSlot() {
        return savedDataSlot(ICON_PRESETS[0], "icon");
    }

    @Contract("_ -> new")
    public static @NotNull SavedDataSlot<Icon> savedDataSlot(Icon defaultIcon) {
        return savedDataSlot(defaultIcon, "icon");
    }

    @Contract("_ -> new")
    public static @NotNull SavedDataSlot<Icon> savedDataSlot(String name) {
        return savedDataSlot(ICON_PRESETS[0], name);
    }

    public static @NotNull SavedDataSlot<Icon> savedDataSlot(Icon defaultIcon, String name) {
        return new SavedDataSlot<>(new Icon(defaultIcon)) {
            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(name, CompoundTag.TAG_COMPOUND)) {
                    CompoundTag tag = compoundTag.getCompound(name);

                    if (tag.contains("id", CompoundTag.TAG_STRING))
                        getValue().setId(tag.getString("id"));
                    if (tag.contains("color", CompoundTag.TAG_INT))
                        getValue().setColorIndex(tag.getInt("color"));
                    if (tag.contains("paint"))
                        getValue().setPaintIcon(tag.getBoolean("paint"));
                }
            }

            @Override
            public void save(CompoundTag compoundTag) {
                CompoundTag tag = new CompoundTag();
                tag.putString("id", getValue().getIdAsString());
                tag.putInt("color", getValue().getColorIndex());
                tag.putBoolean("paint", getValue().isPaintIcon());
                compoundTag.put(name, tag);
            }
        };
    }

}
