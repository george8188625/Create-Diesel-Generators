package com.jesz.createdieselgenerators.content.tools.hammer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Optional;
import java.util.function.Consumer;

public class HammerItem extends TieredItem {
    Multimap<Attribute, AttributeModifier> toolAttributes;

    public HammerItem(Properties properties, Tier tier) {
        super(tier, properties);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 6, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.1, AttributeModifier.Operation.ADDITION));
        toolAttributes = builder.build();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? toolAttributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionHand otherHand = InteractionHand.values()[(~hand.ordinal()) & 1];
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOtherHand = player.getItemInHand(otherHand);

        HammerRecipe.HammerInv hammerInv = new HammerRecipe.HammerInv(itemInOtherHand);
        Optional<HammerRecipe> recipe = level.getRecipeManager().getRecipeFor(CDGRecipes.HAMMERING.getType(), hammerInv, level);
        if (recipe.isPresent()) {
            ItemStack processingItem = itemInOtherHand.copy();
            itemInOtherHand.shrink(1);
            processingItem.setCount(1);

            CompoundTag tag = itemInHand.getOrCreateTag();
            tag.put("ProcessingItem", processingItem.save(new CompoundTag()));
            player.startUsingItem(hand);
            return InteractionResultHolder.success(itemInHand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;
        synchronized ("hammer_release") {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("ProcessingItem"))
                return stack;
            ItemStack processingItem = ItemStack.of(tag.getCompound("ProcessingItem"));

            HammerRecipe.HammerInv hammerInv = new HammerRecipe.HammerInv(processingItem);
            Optional<HammerRecipe> recipe = level.getRecipeManager().getRecipeFor(CDGRecipes.HAMMERING.getType(), hammerInv, level);
            tag.remove("ProcessingItem");
            if (recipe.isEmpty()) {
                player.getInventory().placeItemBackInInventory(processingItem);
                return stack;
            }
            recipe.get().rollResults();
            player.getInventory().placeItemBackInInventory(recipe.get().assemble(hammerInv, level.registryAccess()).copy());
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
            return stack;
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int tick) {
        if (AnimationTickHolder.getTicks() % 10 == 0) {
            level.playLocalSound(entity.xo, entity.yo, entity.zo, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.3f, 1f, true);
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("ProcessingItem")) {
                super.onUseTick(level, entity, stack, tick);
                return;
            }
            ItemStack processingItem = ItemStack.of(tag.getCompound("ProcessingItem"));
            for (int i = 0; i < 30; i++) {
                Vec3 offset = VecHelper.offsetRandomly(entity.position().add(Math.sin(-entity.getYRot() / 180 * Math.PI) / 2, 1.3, Math.cos(-entity.getYRot() / 180 * Math.PI) / 2), level.getRandom(), .3f);
                Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .1f);

                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, processingItem), offset.x(), offset.y(),
                        offset.z(), motion.x(), motion.y(), motion.z());
            }
        }
        super.onUseTick(level, entity, stack, tick);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tick) {
        synchronized ("hammer_release") {
            if (!(entity instanceof Player player))
                return;
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("ProcessingItem"))
                return;

            ItemStack processingItem = ItemStack.of(tag.getCompound("ProcessingItem"));
            player.getInventory().placeItemBackInInventory(processingItem);
            tag.remove("ProcessingItem");
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        synchronized ("hammer_release") {
            if (!(entity instanceof Player player))
                return;
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("ProcessingItem"))
                return;

            ItemStack processingItem = ItemStack.of(tag.getCompound("ProcessingItem"));
            player.getInventory().placeItemBackInInventory(processingItem);
            tag.remove("ProcessingItem");
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 90;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new HammerItemRenderer()));
    }


}
