package com.jesz.createdieselgenerators.content.track_layers_bag;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.mixins.UseOnContextInvoker;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.simibubi.create.content.trains.track.TrackPlacement;
import com.simibubi.create.foundation.utility.CreateLang;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.Optional;

public class TrackLayersBagItem extends Item {
    public TrackLayersBagItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemStack track = getTracks(stack);
        if(track.isEmpty())
            return Optional.empty();
        return Optional.of(new TrackLayersBagComponent(track));
    }

    int add(ItemStack bag, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof TrackBlockItem) {
            ItemStack stackInBag = getTracks(bag);
            if(stack.getItem() != stackInBag.getItem() && !stackInBag.isEmpty())
                return 0;

            int oldCount = stackInBag.getCount();
            if (stackInBag.isEmpty())
                stackInBag = stack.copy();
            else
                stackInBag.setCount(Math.min(oldCount + stack.getCount(), 1024));

            bag.set(CDGDataComponents.TRACKS, new TrackLayersBagItemDataComponent(stackInBag));
            return Math.min(stack.getCount(), 1024 - oldCount);
        }
        return 0;
    }

    static ItemStack removeOne(ItemStack bag) {
        ItemStack stackInBag = getTracks(bag);

        if (stackInBag.isEmpty())
            return ItemStack.EMPTY;

        ItemStack savedStack = stackInBag.copy();
        int amount = savedStack.getCount();
        savedStack.shrink(64);
        bag.set(CDGDataComponents.TRACKS, new TrackLayersBagItemDataComponent(savedStack));

        if (savedStack.getCount() == 0)
            bag.remove(CDGDataComponents.TRACKS);

        return stackInBag.copyWithCount(Math.min(amount, 64));
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 1 + (int) (((float)getTracks(stack).getCount() / 1024) * 12);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x66ff66;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getTracks(stack).getCount() != 0;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(AllDataComponents.TRACK_CONNECTING_FROM);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction click, Player player) {
        if (stack.getCount() != 1 || click != ClickAction.SECONDARY)
            return false;

        ItemStack stackInSlot = slot.getItem();

        if (stackInSlot.isEmpty() && slot.mayPlace(getTracks(stack))) {
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            slot.set(removeOne(stack));
        } else if(!stackInSlot.isEmpty() && slot.mayPickup(player)){
            int added = add(stack, stackInSlot);
            if(added > 0) {
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                stackInSlot.shrink(added);
            }
        }
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack otherStack, Slot slot, ClickAction click, Player player, SlotAccess slotAccess) {
        if(bag.getCount() != 1 || click != ClickAction.SECONDARY || !slot.allowModification(player))
            return false;
        if(otherStack.isEmpty()) {
            ItemStack extractedStack = removeOne(bag);
            if(!extractedStack.isEmpty()){
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                slotAccess.set(extractedStack);
            }
        }else{
            int added = add(bag, otherStack);
            if(added > 0){
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                otherStack.shrink(added);
            }
        }
        return true;
    }

    public static ItemStack getTracks(ItemStack stack) {
        TrackLayersBagItemDataComponent component = stack.get(CDGDataComponents.TRACKS);
        if (component == null)
            return ItemStack.EMPTY;
        return component.toStack();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack bag = context.getItemInHand();
        ItemStack tracks = getTracks(bag);

        if (tracks.isEmpty())
            return super.useOn(context);

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (player == null)
            return InteractionResult.PASS;

        Vec3 lookAngle = player.getLookAngle();

        if (!isFoil(bag)) {
            if (select(level, pos, lookAngle, bag)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM,
                        SoundSource.BLOCKS, 0.75f, 1);
                return InteractionResult.SUCCESS;
            }
            return ((TrackBlockItem) tracks.getItem()).place(new BlockPlaceContext(
                    context.getLevel(), context.getPlayer(), context.getHand(), tracks, ((UseOnContextInvoker)context).cdg_getHitResult()));
        }

        if (player.isShiftKeyDown()) {
            return clearSelection(bag, level, player).getResult();
        }

        boolean placing = !(state.getBlock() instanceof ITrackBlock);

        if (placing) {
            if (!state.canBeReplaced())
                pos = pos.relative(context.getClickedFace());

            state = ((TrackBlockItem) tracks.getItem()).getPlacementState(context);
            if (state == null)
                return InteractionResult.FAIL;
        }

        ItemStack offhandItem = player.getOffhandItem();
        boolean hasGirder = AllBlocks.METAL_GIRDER.isIn(offhandItem);

        TrackLayersBagPlacement.PlacementInfo info = TrackLayersBagPlacement.tryConnect(
                level, player, pos, state, bag, hasGirder, false
        );

        if (info.message != null && !level.isClientSide)
            player.displayClientMessage(CreateLang.translateDirect(info.message), true);

        if (!info.valid) {
            AllSoundEvents.DENY.playFrom(player, 1, 1);
            return InteractionResult.FAIL;
        }

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        bag.remove(AllDataComponents.TRACK_CONNECTING_FROM);

        SoundType soundtype = state.getSoundType();
        if (soundtype != null)
            level.playSound(null, pos, soundtype.getPlaceSound(),
                    SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F,
                    soundtype.getPitch() * 0.8F);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && isFoil(stack)) {
            return clearSelection(stack, level, player);
        } else {
            return super.use(level, player, usedHand);
        }
    }

    public static InteractionResultHolder<ItemStack> clearSelection(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.75f, 1.0f);
        } else {
            player.displayClientMessage(CreateLang.translateDirect("track.selection_cleared"), true);
            stack.remove(AllDataComponents.TRACK_CONNECTING_FROM);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static boolean select(LevelAccessor world, BlockPos pos, Vec3 lookVec, ItemStack heldItem) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof ITrackBlock track))
            return false;

        Pair<Vec3, Direction.AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(world, pos, blockState, lookVec);
        Vec3 axis = nearestTrackAxis.getFirst()
                .scale(nearestTrackAxis.getSecond() == Direction.AxisDirection.POSITIVE ? -1 : 1);
        Vec3 end = track.getCurveStart(world, pos, blockState, axis);
        Vec3 normal = track.getUpNormal(world, pos, blockState)
                .normalize();

        heldItem.set(AllDataComponents.TRACK_CONNECTING_FROM, new TrackPlacement.ConnectingFrom(pos, axis, normal, end));
        return true;
    }

    public void registerModelOverrides() {
       CatnipServices.PLATFORM.executeOnClientOnly(() -> () ->
               ItemProperties.register(CDGItems.TRACK_LAYERS_BAG.get(), CreateDieselGenerators.rl("tracks"),
               (stack, level, entity, seed) -> getTracks(stack).getCount()));
    }

    public static ItemModelBuilder addOverrideModels(DataGenContext<Item, TrackLayersBagItem> c,
                                                     RegistrateItemModelProvider p) {
        ItemModelBuilder builder = p.generated(c::get);

        builder.override()
                .predicate(CreateDieselGenerators.rl("tracks"), 0.01f)
                .model(p.getBuilder(c.getName() + "_filled")
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/track_layers_bag_filled")))
                .end();
        return builder;
    }

    public static ItemStack full() {
        ItemStack stack = CDGItems.TRACK_LAYERS_BAG.asStack();
        ((TrackLayersBagItem)stack.getItem()).add(stack, AllBlocks.TRACK.asStack(1024));
        return stack;
    }
}
