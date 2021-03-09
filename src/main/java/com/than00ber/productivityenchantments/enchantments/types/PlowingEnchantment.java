package com.than00ber.productivityenchantments.enchantments.types;

import com.than00ber.productivityenchantments.Configs;
import com.than00ber.productivityenchantments.enchantments.CarvedVolume;
import com.than00ber.productivityenchantments.enchantments.CarverEnchantmentBase;
import com.than00ber.productivityenchantments.enchantments.IRightClickEffect;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

import java.util.Collections;

import static com.than00ber.productivityenchantments.Configs.PLOWING_CARVE_TYPE;
import static com.than00ber.productivityenchantments.ProductivityEnchantments.RegistryEvents.PLOWING;

public class PlowingEnchantment extends CarverEnchantmentBase implements IRightClickEffect {

    public PlowingEnchantment() {
        super(Rarity.COMMON, ToolType.HOE);
    }

    @Override
    public boolean canApplyTogether(Enchantment enchantment) {
        if (enchantment instanceof CarverEnchantmentBase)
            return ((CarverEnchantmentBase) enchantment).getToolType().equals(ToolType.HOE);
        return super.canApplyTogether(enchantment);
    }

    @Override
    public boolean isBlockValid(BlockState state, World world, BlockPos pos, ItemStack stack, ToolType type) {
        return state.isIn(Tags.Blocks.DIRT) && world.getBlockState(pos.up()).getBlock() == Blocks.AIR;
    }

    @Override
    public ActionResultType onRightClick(ItemStack stack, int level, Direction facing, CarverEnchantmentBase enchantment, World world, BlockPos origin, PlayerEntity player) {

        if (player instanceof ServerPlayerEntity) {

            if (!player.isSneaking() || !player.isCrouching()) {
                int radius = enchantment.getMaxEffectiveRadius(level);

                CarvedVolume area = new CarvedVolume(CarvedVolume.Shape.DISC, radius, origin, world)
                        .setToolRestrictions(stack, PLOWING.getToolType())
                        .filterViaCallback(PLOWING);

                if (PLOWING_CARVE_TYPE.get().equals(Configs.CarveType.CONNECTED))
                    area.filterConnectedRecursively();

                area.sortNearestToOrigin();

                BlockState state = enchantment.getMaxLevel() == level
                        ? Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, Collections.max(FarmlandBlock.MOISTURE.getAllowedValues()))
                        : Blocks.FARMLAND.getDefaultState();

                return this.performPlacements(world, player, stack, area.getVolume(), state);
            }
        }

        return ActionResultType.PASS;
    }
}
