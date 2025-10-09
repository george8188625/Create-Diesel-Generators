package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Consumer;

public class LighterItem extends Item implements CapacityEnchantment.ICapacityEnchantable, FueledToolItem {
    public LighterItem(Properties properties) {
        super(properties);
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltip, tooltipFlag);
        createTooltip(tooltip, stack);
    }
    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(enchantment == AllEnchantments.CAPACITY.get())
            return true;
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        if (stack.getTag() == null)
            return;
        CompoundTag tankCompound = stack.getTag().getCompound("Fluid");
        FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);

        boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fStack.getFluid()).normal().speed() != 0;
        if (flammable && stack.getTag().getInt("Type") == 2){
            stack.getTag().putInt("Type", 1);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        CompoundTag tag = stackInHand.getTag();

        level.playSound(player, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        if(tag == null){
            stackInHand.addTagElement("Type", IntTag.valueOf(1));
            return InteractionResultHolder.success(stackInHand);
        }
        if(tag.getInt("Type") == 0){
            if(player.isShiftKeyDown()){
                tag.putInt("Type", 1);
                return InteractionResultHolder.success(stackInHand);
            }
            CompoundTag tankCompound = stackInHand.getTag().getCompound("Fluid");
            FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);

            boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fStack.getFluid()).normal().speed() != 0;
            tag.putInt("Type", flammable ? 2 : 1);
            if (flammable && stackInHand.getTag().getInt("Type") == 2) {
                fStack.setAmount(fStack.getAmount()-1);
                fStack.writeToNBT(stackInHand.getTag().getCompound("Fluid"));
            }
            return InteractionResultHolder.success(stackInHand);
        }
        tag.putInt("Type", 0);

        return InteractionResultHolder.success(stackInHand);

    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        ItemStack itemstack = context.getItemInHand();
        if(itemstack.getTag() == null || itemstack.getTag().getInt("Type") != 2)
            return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) &&
                !CandleCakeBlock.canLight(blockstate) && !blockstate.is(AllTags.optionalTag(ForgeRegistries.BLOCKS, CreateDieselGenerators.rl("lighter_")))) {
            BlockPos blockpos1 = blockpos.relative(context.getClickedFace());
            if (BaseFireBlock.canBePlacedAt(level, blockpos1, context.getHorizontalDirection())) {
                level.playSound(player, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState blockstate1 = BaseFireBlock.getState(level, blockpos1);
                level.setBlock(blockpos1, blockstate1, 11);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos1, itemstack);
                }
                CompoundTag tankCompound = itemstack.getTag().getCompound("Fluid");
                FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
                if (fStack.getAmount() == 0) {
                    itemstack.getTag().putInt("Type", 1);
                    return InteractionResult.FAIL;
                }
                boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fStack.getFluid()).normal().speed() != 0;
                if (flammable && itemstack.getTag().getInt("Type") == 2){
                    fStack.setAmount(fStack.getAmount()-1);
                    fStack.writeToNBT(itemstack.getTag().getCompound("Fluid"));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
            }
        } else {
            level.playSound(player, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            if(blockstate.hasProperty(BlockStateProperties.LIT))
                level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, true), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
            CompoundTag tankCompound = itemstack.getTag().getCompound("Fluid");
            FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
            if (fStack.getAmount() == 0){
                itemstack.getTag().putInt("Type", 1);
                return InteractionResult.FAIL;
            }

            boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fStack.getFluid()).normal().speed() != 0;

            if (flammable && itemstack.getTag().getInt("Type") == 2){
                fStack.setAmount(fStack.getAmount()-1);
                fStack.writeToNBT(itemstack.getTag().getCompound("Fluid"));
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCurrentFillLevel(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (float) getCurrentFillLevel(stack) / getCapacity(stack));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return getFluidHandler(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new LighterItemRenderer()));
    }
}
