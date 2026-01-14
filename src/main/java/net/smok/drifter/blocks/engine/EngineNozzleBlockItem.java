package net.smok.drifter.blocks.engine;

import earth.terrarium.adastra.common.utils.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EngineNozzleBlockItem extends BlockItem {

    private final EngineNozzleBlock block;

    public EngineNozzleBlockItem(EngineNozzleBlock block, Properties properties) {
        super(block, properties);
        this.block = block;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        TooltipUtils.addDescriptionComponent(tooltipComponents, Component.translatable("tooltip.asteroid_drifter.add_max_speed", String.format("%,f", block.getMaxSpeed())));
    }
}
