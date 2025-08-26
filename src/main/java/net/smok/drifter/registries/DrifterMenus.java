package net.smok.drifter.registries;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.adastra.common.utils.WorldUtils;
import earth.terrarium.botarium.common.registry.RegistryHelpers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.blocks.alert.AlertSystemMenu;
import net.smok.drifter.blocks.engine.EngineMenu;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.blocks.controller.ShipControllerMenu;
import org.apache.commons.lang3.function.TriFunction;

public final class DrifterMenus {


    public static final ResourcefulRegistry<MenuType<?>> MENUS = ResourcefulRegistries.create(BuiltInRegistries.MENU, Values.MOD_ID);


    public static final RegistryEntry<MenuType<ShipControllerMenu>> SHIP_CONTROLLER_MENU =
            registerMenu("ship_controller_menu", ShipControllerMenu::new, ShipControllerBlockEntity.class);

    public static final RegistryEntry<MenuType<AlertSystemMenu>> ALERT_SYSTEM_MENU =
            registerMenu("alert_system_menu", (id, inventory, alertSystemBlock) -> new AlertSystemMenu(id, alertSystemBlock), AlertPanelBlockEntity.class);

    public static final RegistryEntry<MenuType<EngineMenu>> ENGINE_MENU =
            registerMenu("engine_menu", EngineMenu::new, EnginePanelBlockEntity.class);



    private static <T extends AbstractContainerMenu, V extends BlockEntity> RegistryEntry<MenuType<T>> registerMenu(
            String id, TriFunction<Integer, Inventory, V, T> factory, Class<V> clazz) {

        return MENUS.register(id, () -> RegistryHelpers.createMenuType((syncId, inventory, byteBuf) -> {
            V tileEntity = WorldUtils.getTileEntity(clazz, inventory.player.level(), byteBuf.readBlockPos());
            return factory.apply(syncId, inventory, tileEntity);
        }));
    }
}
