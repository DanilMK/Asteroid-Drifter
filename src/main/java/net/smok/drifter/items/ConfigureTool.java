package net.smok.drifter.items;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.smok.drifter.blocks.ShipBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigureTool extends Item {

    public static final String CONFIG_BLOCK_KEY = "configBlock";

    public ConfigureTool(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(CONFIG_BLOCK_KEY, CompoundTag.TAG_COMPOUND)) {
            BlockPos blockPos = NbtUtils.readBlockPos(tag.getCompound(CONFIG_BLOCK_KEY));
            tooltipComponents.add(Component.translatable("tooltip.asteroid_drifter.configure_linked_pos", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        boolean crouching = player.isCrouching();
        if (crouching && itemStack.getOrCreateTag().contains(CONFIG_BLOCK_KEY, CompoundTag.TAG_COMPOUND)) {
            forgetBlock(itemStack, player);
            return InteractionResultHolder.consume(itemStack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack itemStack = context.getItemInHand();
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        boolean alt = player.isCrouching();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        Block block = level.getBlockState(blockPos).getBlock();

        if (blockEntity instanceof ShipBlock otherBlock) {

            addOrRemember(itemStack, level, blockPos, otherBlock, player);
            return InteractionResult.CONSUME;
        }
        if (block instanceof ShipBlock otherBlock) {

            addOrRemember(itemStack, level, blockPos, otherBlock, player);
            return InteractionResult.CONSUME;
        }
        cancelLink(player);
        return InteractionResult.PASS;
    }


    private static void addOrRemember(ItemStack itemStack, Level level, BlockPos otherPos, ShipBlock otherBlock, Player player) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains(CONFIG_BLOCK_KEY, CompoundTag.TAG_COMPOUND)) {
            BlockPos linkedPos = NbtUtils.readBlockPos(tag.getCompound(CONFIG_BLOCK_KEY));
            if (level.getChunkAt(linkedPos).getBlockEntity(linkedPos) instanceof ShipBlock linkedBlock) {
                addConfigure(linkedPos, otherPos, linkedBlock, otherBlock, player);
                return;
            }
            if (level.getBlockState(linkedPos).getBlock() instanceof ShipBlock linkedBlock) {
                addConfigure(linkedPos, otherPos, linkedBlock, otherBlock, player);
                return;
            }
        }
        rememberBlock(itemStack, otherPos, player);
    }

    private static void cancelLink(Player player) {
        player.displayClientMessage(Component.translatable("action.asteroid_drifter.configure_cancel"), true);
    }


    private static void clearConfigure(ShipBlock block, Player player) {
        block.clear();
        player.displayClientMessage(Component.translatable("action.asteroid_drifter.configure_clear"), true);
    }

    private static void addConfigure(BlockPos linkedPos, BlockPos otherPos, ShipBlock linkedBlock, ShipBlock otherBlock, Player player) {
        boolean a = linkedBlock.bind(otherPos, otherBlock);
        boolean b = otherBlock.bind(linkedPos, linkedBlock);

        if (a || b) player.displayClientMessage(Component.translatable("action.asteroid_drifter.configure_linked"), true);
        else player.displayClientMessage(Component.translatable("action.asteroid_drifter.configure_cancel"), true);
    }

    private static void rememberBlock(ItemStack itemStack, BlockPos pos, Player player) {
        CompoundTag tag = itemStack.getOrCreateTag();
        CompoundTag posTag = NbtUtils.writeBlockPos(pos);
        tag.put(CONFIG_BLOCK_KEY, posTag);
        itemStack.setTag(tag);
        player.displayClientMessage(Component.translatable("action.asteroid_drifter.configure_remember"), true);
    }

    private static void forgetBlock(ItemStack itemStack, Player player) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.remove(CONFIG_BLOCK_KEY);
        player.displayClientMessage(Component.translatable("action.asteroid_drifter.configure_forget"), true);
    }
}
