package net.smok.drifter.menus;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import net.minecraft.nbt.CompoundTag;
import net.smok.drifter.utils.CustomDataSlot;

import java.util.List;

public class FluidDataSlot extends CustomDataSlot {

    private FluidContainer cached;
    private final FluidContainer fluidContainer;

    public FluidDataSlot(FluidContainer fluidContainer) {
        this.fluidContainer = fluidContainer;
    }

    @Override
    public boolean changed() {
        if (cached == null || cached.getSize() != fluidContainer.getSize()) return true;
        List<FluidHolder> fluids = fluidContainer.getFluids();
        List<FluidHolder> cachedFluids = cached.getFluids();
        for (int i = 0; i < cachedFluids.size(); i++) {
            if (!fluids.get(i).is(cachedFluids.get(i).getFluid())) return true;
            if (fluids.get(i).getFluidAmount() != cachedFluids.get(i).getFluidAmount()) return true;
        }
        return false;
    }

    @Override
    public void sendData(CompoundTag data) {
        fluidContainer.serialize(data);
        cached = fluidContainer.copy();
    }

    @Override
    public void receiveData(CompoundTag data) {
        fluidContainer.deserialize(data);
    }

}
