package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.entity.ChemicalSprayerProjectileEntity;
import com.jesz.createdieselgenerators.entity.EntityRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItemRenderer;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class ChemicalSprayerItem extends Item implements CustomArmPoseItem, CapacityEnchantment.ICapacityEnchantable {
    boolean lighter;
    public ChemicalSprayerItem(Properties properties, boolean lighter) {
        super(properties.stacksTo(1));
        this.lighter = lighter;
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
            components.add(Lang.fluidName(fStack).component().withStyle(ChatFormatting.GRAY).append(" ").append(Lang.number(fStack.getAmount()).style(ChatFormatting.GOLD).component()).append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD)).append(Component.literal(" / ")).append(Lang.number(100 + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())*50).style(ChatFormatting.GRAY).component()).append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY)));
            return;
        }
        components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));

    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        if (!player.swinging) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return null;
    }
    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if(stackInHand.getTag()!= null) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackInHand.getTag().getCompound("Fluid"));
            if (fluidStack.getAmount() != 0)
                player.startUsingItem(hand);
        }

        return super.use(level, player, hand);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

        Level level = player.getLevel();
        if(stack.getTag()!= null) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stack.getTag().getCompound("Fluid"));
            if (!fluidStack.isEmpty()) {
//                float f = -Mth.sin(player.getYRot() * ((float)Math.PI / 180F)) * Mth.cos((player.getXRot()+90) * ((float)Math.PI / 180F));
//                float f1 = -Mth.sin((player.getXRot()+90) * ((float)Math.PI / 180F));
//                float f2 = Mth.cos(player.getYRot() * ((float)Math.PI / 180F)) * Mth.cos((player.getXRot()+90) * ((float)Math.PI / 180F));
//                player.setDeltaMovement(player.getDeltaMovement().add(f*-0.1, f1*-0.1, f2*-0.1));
                if(!level.isClientSide) {
                    if (count % 2 == 0) {
                        ChemicalSprayerProjectileEntity projectile = ChemicalSprayerProjectileEntity.spray(level, fluidStack, (FuelTypeManager.getGeneratedSpeed(fluidStack.getFluid()) != 0 && lighter) || fluidStack.getFluid().isSame(Fluids.LAVA), fluidStack.getFluid().isSame(Fluids.WATER));
                        projectile.setPos(player.position().add(0, 1.5f, 0));
                        projectile.shootFromRotation(player, player.getXRot() + new Random().nextFloat(-5, 5), player.getYRot() + new Random().nextFloat(-5, 5), 0.0f, 1.0f, 1.0f);
                        level.addFreshEntity(projectile);
                        fluidStack.setAmount(fluidStack.getAmount() - 1);

                    }
                    if (!(player instanceof Player p && p.isCreative()) && count % 25 == 0)
                        fluidStack.writeToNBT(stack.getTag().getCompound("Fluid"));
                }
            }
        }
        super.onUsingTick(stack, player, count);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1000;
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

        return Math.round(13 * Mth.clamp(FluidStack.loadFluidStackFromNBT(tankCompound).getAmount()/(
                (float)100 + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())*50
        ), 0, 1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        if(!ModList.get().isLoaded("dungeons_libraries"))
            return new FluidHandlerItemStack(stack, 100 + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * 50);
        return new FluidHandlerItemStack(stack, 100);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new ChemicalSprayerItemRenderer()));
    }
}
