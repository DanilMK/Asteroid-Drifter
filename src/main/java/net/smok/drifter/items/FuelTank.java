package net.smok.drifter.items;

import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.adastra.common.utils.TooltipUtils;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidItem;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import earth.terrarium.botarium.common.fluid.utils.ClientFluidHooks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FuelTank extends Item implements BotariumFluidItem<WrappedItemFluidContainer> {
    private final long tankSize;

    public FuelTank(@NotNull Properties properties, long tankSize) {
        super(properties.stacksTo(1));
        this.tankSize = tankSize;
    }

    @Override
    public WrappedItemFluidContainer getFluidContainer(ItemStack itemStack) {
        return new WrappedItemFluidContainer(itemStack, new SimpleFluidContainer(FluidConstants.fromMillibuckets(tankSize), 1,
                (t, f) -> true    //Values.SHIP_FUEL.getStillFluid().get().isSame(f.getFluid())
        ));
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(TooltipUtils.getFluidComponent(FluidUtils.getTank(stack), FluidUtils.getTankCapacity(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return FluidUtils.hasFluid(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        WrappedItemFluidContainer fluidContainer = this.getFluidContainer(stack);
        return (int)((double)fluidContainer.getFirstFluid().getFluidAmount() / (double)fluidContainer.getTankCapacity(0) * 13.0);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return ClientFluidHooks.getFluidColor(FluidUtils.getTank(stack));
    }


}
