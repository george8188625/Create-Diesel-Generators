package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.function.Consumer;

public class LighterItem extends Item implements CapacityEnchantment.ICapacityEnchantable {
    public LighterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        if(stack.getTag() != null) {
            CompoundTag tankCompound = stack.getTag().getCompound("Fluid");

            FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
            if(fStack.isEmpty()){
                components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
                return;
            }
            components.add(Lang.fluidName(fStack).component().withStyle(ChatFormatting.GRAY).append(" ").append(Lang.number(fStack.getAmount()).style(ChatFormatting.GOLD).component()).append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD)).append(Component.literal(" / ")).append(Lang.number(ConfigRegistry.TOOL_CAPACITY.get() + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())*ConfigRegistry.TOOL_CAPACITY_ENCHANTMENT.get()).style(ChatFormatting.GRAY).component()).append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY)));
            return;
        }
        components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));

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
        if(stack.getTag() != null) {
            CompoundTag tankCompound = stack.getTag().getCompound("Fluid");
            FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
            if(FuelTypeManager.getGeneratedSpeed(fStack.getFluid()) == 0 && stack.getTag().getInt("Type") == 2){
                stack.getTag().putInt("Type", 1);
            }

        }
        super.inventoryTick(stack, level, entity, p_41407_, p_41408_);
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
            tag.putInt("Type", FuelTypeManager.getGeneratedSpeed(fStack.getFluid()) == 0 ? 1 : 2);
            if(FuelTypeManager.getGeneratedSpeed(fStack.getFluid()) != 0 && stackInHand.getTag().getInt("Type") == 2){
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
        if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
            if(ConfigRegistry.COMBUSTIBLES_BLOW_UP.get()){
                BlockEntity cb = level.getBlockEntity(blockpos);
                if(cb != null) {
                    IFluidHandler tank = cb.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);

                    if (tank == null)
                        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
                    if (FuelTypeManager.getGeneratedSpeed(tank.getFluidInTank(0).getFluid()) != 0) {
                        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
                        level.explode(null, null, null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 3 + ((float) tank.getFluidInTank(0).getAmount() / 500), true, Level.ExplosionInteraction.BLOCK);

                        CompoundTag tankCompound = itemstack.getTag().getCompound("Fluid");
                        FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
                        if (FuelTypeManager.getGeneratedSpeed(fStack.getFluid()) != 0 && itemstack.getTag().getInt("Type") == 2) {
                            fStack.setAmount(fStack.getAmount() - 1);
                            fStack.writeToNBT(itemstack.getTag().getCompound("Fluid"));
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }
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
                if(fStack.getAmount() == 0){
                    itemstack.getTag().putInt("Type", 1);
                    return InteractionResult.FAIL;
                }
                if(FuelTypeManager.getGeneratedSpeed(fStack.getFluid()) != 0 && itemstack.getTag().getInt("Type") == 2){
                    fStack.setAmount(fStack.getAmount()-1);
                    fStack.writeToNBT(itemstack.getTag().getCompound("Fluid"));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
            }
        } else {
            level.playSound(player, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, (p_41303_) -> {
                    p_41303_.broadcastBreakEvent(context.getHand());
                });
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if(stack.getTag() != null) {
            CompoundTag tankCompound = stack.getTag().getCompound("Fluid");
            FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
            return !fStack.isEmpty();
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if(stack.getTag() == null)
            return 0;
        CompoundTag tankCompound = stack.getTag().getCompound("Fluid");

        return Math.round(13 * Mth.clamp(FluidStack.loadFluidStackFromNBT(tankCompound).getAmount()/((float)ConfigRegistry.TOOL_CAPACITY.get() + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())*ConfigRegistry.TOOL_CAPACITY_ENCHANTMENT.get()), 0, 1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        if(!ModList.get().isLoaded("dungeons_libraries"))
            return new FluidHandlerItemStack(stack, ConfigRegistry.TOOL_CAPACITY.get() + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * ConfigRegistry.TOOL_CAPACITY_ENCHANTMENT.get());
        return new FluidHandlerItemStack(stack, ConfigRegistry.TOOL_CAPACITY.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new LighterItemRenderer()));
    }
}
