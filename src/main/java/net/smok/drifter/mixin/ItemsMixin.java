package net.smok.drifter.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.smok.drifter.items.SeedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Items.class)
public class ItemsMixin {


    @Redirect(method = "<clinit>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/ItemNameBlockItem;"))
    private static ItemNameBlockItem carrotReplace(Block block, Item.Properties properties) {

        if(block.equals(Blocks.CARROTS)) return new SeedItem(block, properties, new ResourceLocation("asteroid_drifter:carrots"));
        if(block.equals(Blocks.POTATOES)) return new SeedItem(block, properties, new ResourceLocation("asteroid_drifter:potatoes"));
        return new ItemNameBlockItem(block, properties);
    }
}
