package com.jesz.createdieselgenerators.entity;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class EntityRegistry {

    public static final EntityEntry<ChemicalSprayerProjectileEntity> CHEMICAL_SPRAYER_PROJECTILE = REGISTRATE.entity("chemical_sprayer_projectile", ChemicalSprayerProjectileEntity::new, MobCategory.MISC)
                    .properties(b -> b.setTrackingRange(4)
                            .setUpdateInterval(20)
                            .setShouldReceiveVelocityUpdates(true))
                    .properties(ChemicalSprayerProjectileEntity::build)
                    .renderer(() -> ChemicalSprayerProjectileRenderer::new).register();
    public static void register(){}
}
