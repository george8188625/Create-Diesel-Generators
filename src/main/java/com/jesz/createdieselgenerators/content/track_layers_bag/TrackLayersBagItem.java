package com.jesz.createdieselgenerators.content.track_layers_bag;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.mixins.UseOnContextInvoker;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
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
        ItemStack stack = context.getItemInHand();
        ItemStack tracks = getTracks(stack);
        if (tracks.isEmpty())
            return super.useOn(context);
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());

        if (!(clickedState.getBlock() instanceof TrackBlock)) {
            stack.set(CDGDataComponents.TRACKS, new TrackLayersBagItemDataComponent(tracks.copyWithCount(Math.max(0, tracks.getCount() - 1))));


            return ((TrackBlockItem) tracks.getItem()).place(new BlockPlaceContext(
                    context.getLevel(), context.getPlayer(), context.getHand(), tracks, ((UseOnContextInvoker)context).cdg_getHitResult()));
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult place(BlockPlaceContext context, BlockItem item) {
        if (!context.canPlace())
            return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();

        BlockState blockstate = item.getBlock().getStateForPlacement(context);
        CollisionContext collision = player == null ? CollisionContext.empty() : CollisionContext.of(player);

        if (!blockstate.canSurvive(level, pos) || !level.isUnobstructed(blockstate, pos, collision)) {
            return InteractionResult.FAIL;
        } else if (!level.setBlock(pos, blockstate, 11)) {
            return InteractionResult.FAIL;
        } else {
            BlockState clickedState = level.getBlockState(pos);

            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, clickedState));
            SoundType sound = clickedState.getSoundType(level, pos, context.getPlayer());
            level.playSound(player, pos, clickedState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

    }

    public void registerModelOverrides() {
       CatnipServices.PLATFORM.executeOnClientOnly(() -> () ->
               ItemProperties.register(CDGItems.TRACK_LAYERS_BAG.get(), CreateDieselGenerators.rl("tracks"),
               (stack, level, entity, seed) -> getTracks(stack).getCount()));
    }

    public static ItemModelBuilder addOverrideModels(DataGenContext<Item, TrackLayersBagItem> c,
                                                     RegistrateItemModelProvider p) {
        ItemModelBuilder builder = p.generated(() -> c.get());

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
