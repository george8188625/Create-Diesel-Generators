package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.simibubi.create.foundation.utility.Couple;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CDGFuelType {
    float normalSpeed;
    float modularSpeed;
    float hugeSpeed;

    float normalStrength;
    float modularStrength;
    float hugeStrength;

    int normalBurn;
    int modularBurn;
    int hugeBurn;
    int soundSpeed;
    public CDGFuelType(float normalSpeed, float normalStrength, int normalBurn,
                       float modularSpeed, float modularStrength, int modularBurn,
                       float hugeSpeed, float hugeStrength, int hugeBurn, int soundSpeed){
        this.normalSpeed = normalSpeed;
        this.modularSpeed = modularSpeed;
        this.hugeSpeed = hugeSpeed;
        this.normalStrength = normalStrength;
        this.modularStrength = modularStrength;
        this.hugeStrength = hugeStrength;
        this.normalBurn = normalBurn;
        this.modularBurn = modularBurn;
        this.hugeBurn = hugeBurn;
        this.soundSpeed = soundSpeed;
    }

    public Couple<Float> getGenerated(BlockEntity be) {
        if(be instanceof HugeDieselEngineBlockEntity)
            return getGeneratedHuge();
        if(be instanceof LargeDieselGeneratorBlockEntity)
            return getGeneratedModular();
        return getGeneratedNormal();
    }

    public Couple<Float> getGeneratedNormal() {return Couple.create(normalSpeed, normalStrength);}
    public Couple<Float> getGeneratedModular() {return Couple.create(modularSpeed, modularStrength);}
    public Couple<Float> getGeneratedHuge() {return Couple.create(hugeSpeed, hugeStrength);}

    public int getBurn(BlockEntity be) {
        if(be instanceof HugeDieselEngineBlockEntity)
            return getBurnHuge();
        if(be instanceof LargeDieselGeneratorBlockEntity)
            return getBurnModular();
        return getBurnNormal();
    }

    public int getBurnNormal(){ return normalBurn; }
    public int getBurnModular(){ return modularBurn; }
    public int getBurnHuge(){ return hugeBurn; }
    public int getSoundSpeed() { return soundSpeed; }
}
