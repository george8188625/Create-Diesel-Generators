package com.jesz.createdieselgenerators.content.track_layers_bag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record TrackLayersBagItemDataComponent(ResourceLocation itemId, int count) {

    public static final Codec<TrackLayersBagItemDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(TrackLayersBagItemDataComponent::itemId),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").forGetter(TrackLayersBagItemDataComponent::count)
            )
            .apply(instance, TrackLayersBagItemDataComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrackLayersBagItemDataComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, TrackLayersBagItemDataComponent::itemId,
                    ByteBufCodecs.VAR_INT, TrackLayersBagItemDataComponent::count,
                    TrackLayersBagItemDataComponent::new);

    public TrackLayersBagItemDataComponent(ItemStack stack) {
        this(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount());
    }

    public ItemStack toStack() {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrackLayersBagItemDataComponent t))
            return false;
        return t.count() == count() && t.itemId().equals(itemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId(), count());
    }
}
