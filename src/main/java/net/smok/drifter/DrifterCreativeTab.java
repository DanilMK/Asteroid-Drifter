package net.smok.drifter;

import com.teamresourceful.resourcefullib.common.item.tabs.ResourcefulCreativeTab;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.smok.drifter.registries.DrifterItems;
import net.smok.drifter.registries.Values;

import java.util.function.Supplier;

public final class DrifterCreativeTab {

    public static final ResourcefulRegistry<CreativeModeTab> TABS =
            ResourcefulRegistries.create(BuiltInRegistries.CREATIVE_MODE_TAB, Values.MOD_ID);
    public static final Supplier<CreativeModeTab> TAB =
            new ResourcefulCreativeTab(new ResourceLocation(Values.MOD_ID, "main"))
                    .setItemIcon(DrifterItems.MEDIUM_ASTEROID).addRegistry(DrifterItems.ITEMS).build();
}
