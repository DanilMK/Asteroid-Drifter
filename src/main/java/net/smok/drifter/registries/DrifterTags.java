package net.smok.drifter.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class DrifterTags {
    public static final TagKey<Block> MANDRAKE_CONTAINERS = TagKey.create(BuiltInRegistries.BLOCK.key(),
            new ResourceLocation(Values.MOD_ID, "mandrake_containers"));

    public static final TagKey<Item> MANDRAKE_FOODS = TagKey.create(BuiltInRegistries.ITEM.key(),
            new ResourceLocation(Values.MOD_ID, "mandrake_foods"));

    public static final TagKey<Item> LEAVES = TagKey.create(BuiltInRegistries.ITEM.key(),
            new ResourceLocation(Values.MOD_ID, "leaves"));
}
