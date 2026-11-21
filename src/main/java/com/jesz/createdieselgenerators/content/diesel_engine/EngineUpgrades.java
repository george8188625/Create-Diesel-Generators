package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public interface EngineUpgrades {
    List<EngineUpgrades> allUpgrades = new ArrayList<>();
    EngineUpgrades EMPTY = register(new EmptyUpgrade());
    EngineUpgrades SILENCER = register(new SilencerUpgrade());
    EngineUpgrades TURBOCHARGER = register(new TurbochargerUpgrade());

    static EngineUpgrades register(EngineUpgrades upgrade) {
        allUpgrades.add(upgrade);
        return upgrade;
    }

    static EngineUpgrades get(ResourceLocation rl) {

        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades) {
            if (upgrade.getId().equals(rl)) {
                return upgrade;
            }
        }
        return EMPTY;
    }

    ResourceLocation getId();

    default boolean canAddOn(IEngine engine) {
        return true;
    }

    default float getSpeed(float speed, IEngine engine) {
        return speed;
    }

    default float getCapacity(float capacity, IEngine engine) {
        return capacity;
    }

    @OnlyIn(Dist.CLIENT)
    default <T extends SmartBlockEntity & IEngine> EngineSoundInstance createSoundInstance(T engine, Vec3 pos) {
        return new EngineSoundInstance(CDGSoundEvents.ENGINE_NORMAL.get(), SoundSource.NEUTRAL, pos, 0.2f);
    }

    default <T extends SmartBlockEntity & IEngine> float getPitchMultiplier(T engine) {
        return 1f;
    }

    default <T extends SmartBlockEntity & IEngine> float getBurnRateMultiplier(T engine) {
        return 1f;
    }

    default <T extends SmartBlockEntity & IEngine> float getVolume(T engine) {
        return 0.5f;
    }

    ItemStack getItem();

    default void render(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {

    }

    static void renderPartial(BlockEntity be, PoseStack ms, MultiBufferSource buffer,
                              PartialModel normalModel, PartialModel normalVerticalModel,
                              PartialModel modularModel, PartialModel hugeModel, int light) {
        if (be instanceof DieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(FACING);
            if (facing.getAxis() == Direction.Axis.Y) {
                CachedBuffers.partial(normalVerticalModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 180)
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            } else {
                CachedBuffers.partial(normalModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.toYRot())
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            }
        } else if (be instanceof ModularDieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(ModularDieselEngineBlock.FACING);

            CachedBuffers.partial(modularModel, be.getBlockState())
                    .center()
                    .rotateYDegrees(facing.toYRot())
                    .uncenter()
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        } else if (be instanceof HugeDieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(HugeDieselEngineBlock.FACING);

            if (facing.getAxis() == Direction.Axis.Y) {
                CachedBuffers.partial(hugeModel, be.getBlockState())
                        .center().rotateZDegrees(90)
                        .rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 90)
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            } else {
                CachedBuffers.partial(hugeModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.getAxis() == Direction.Axis.X ? (facing.toYRot()) : (facing.toYRot()) + 180)
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            }

        }
    }

    class EmptyUpgrade implements EngineUpgrades {
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("none");
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }
    }

    class SilencerUpgrade implements EngineUpgrades {
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("silencer");
        }

        @Override
        public void render(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
            renderPartial(be, ms, buffer, CDGPartialModels.ENGINE_SILENCER, CDGPartialModels.ENGINE_SILENCER_VERTICAL,
                    CDGPartialModels.MODULAR_ENGINE_SILENCER, CDGPartialModels.HUGE_ENGINE_SILENCER, light);
        }

        @Override
        public ItemStack getItem() {
            return CDGItems.ENGINE_SILENCER.get().getDefaultInstance();
        }

        @Override
        public <T extends SmartBlockEntity & IEngine> float getVolume(T engine) {
            return 0.02f;
        }
    }

    class TurbochargerUpgrade implements EngineUpgrades {
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("turbocharger");
        }

        @Override
        public float getSpeed(float speed, IEngine engine) {
            return (float) (speed * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get());
        }

        @Override
        public float getCapacity(float capacity, IEngine engine) {
            return (float) (capacity * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get());
        }

        @Override
        public void render(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
            renderPartial(be, ms, buffer, CDGPartialModels.ENGINE_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER_VERTICAL,
                    CDGPartialModels.MODULAR_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER, light);
        }

        @Override
        public ItemStack getItem() {
            return CDGItems.ENGINE_TURBO.get().getDefaultInstance();
        }

        @Override
        public boolean canAddOn(IEngine engine) {
            return engine instanceof DieselEngineBlockEntity || engine instanceof ModularDieselEngineBlockEntity;
        }

        @Override
        public <T extends SmartBlockEntity & IEngine> float getPitchMultiplier(T engine) {
            return 1.5f;
        }

        @Override
        public <T extends SmartBlockEntity & IEngine> float getBurnRateMultiplier(T engine) {
            return CDGConfig.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.get().floatValue();
        }
    }
}
