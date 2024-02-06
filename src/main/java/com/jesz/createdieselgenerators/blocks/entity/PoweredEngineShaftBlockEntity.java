package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock.FACING;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class PoweredEngineShaftBlockEntity extends GeneratingKineticBlockEntity {
    float stressCapacity;
    float speed;
    int movementDirection;
    int initialTicks;
    public PoweredEngineShaftBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        movementDirection = 0;
    }
    public boolean isEngineForConnectorDisplay( BlockPos pos ){

        Direction.Axis axis = getBlockState().getValue(AXIS);
        for( Direction d : List.of(axis == Direction.Axis.Z ? Direction.UP : Direction.NORTH,axis == Direction.Axis.Z ? Direction.DOWN : Direction.SOUTH,axis == Direction.Axis.X ? Direction.UP : Direction.EAST,axis == Direction.Axis.X ? Direction.DOWN : Direction.WEST)){
            BlockState st = getLevel().getBlockState(getBlockPos().relative(d, 2));
            if(st.getBlock() instanceof HugeDieselEngineBlock && st.getValue(FACING) == d.getOpposite())
                return(getBlockPos().relative(d, 2).equals(pos));
        }
        return false;
    }
    public Map<BlockPos, Couple<Float>> engines = new HashMap<>();
    public void update(BlockPos sourcePos, int direction, float stress, float speed){
        if(engines.containsKey(sourcePos))
            engines.replace(sourcePos, Couple.create(stress, speed));
        else
            engines.put(sourcePos, Couple.create(stress, speed));
        AtomicReference<Float> maxSpeed = new AtomicReference<>(0f);
        Map<BlockPos, Couple<Float>> map = Map.copyOf(engines);

        for (Couple<Float> s : map.values()) {
            if (s.getSecond() > maxSpeed.get())
                maxSpeed.set(s.getSecond());
        }
        this.speed = maxSpeed.get();
        this.movementDirection = direction;
        reActivateSource = true;
    }
    public boolean canBePoweredBy(BlockPos globalPos) {
        return initialTicks == 0;
    }
    public void removeGenerator(BlockPos sourcePos) {
        engines.remove(sourcePos);
        if(engines.isEmpty()){
            movementDirection = 0;
            speed = 0;
            stressCapacity = 0;
        }
        reActivateSource = true;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("Direction", movementDirection);
        if (initialTicks > 0)
            compound.putInt("Warmup", initialTicks);
        ListTag engineList = new ListTag();
        Map<BlockPos, Couple<Float>> map = Map.copyOf(engines);

        map.forEach((p, s) -> {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("Capacity", s.getFirst());
            tag.putFloat("Speed", s.getSecond());
            tag.put("Pos", NbtUtils.writeBlockPos(p));
            engineList.add(tag);
        });
        compound.putFloat("GeneratedSpeed", speed);
        compound.put("Engines", engineList);

    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        movementDirection = compound.getInt("Direction");
        initialTicks = compound.getInt("Warmup");
        ListTag engineList = compound.getList("Engines", CompoundTag.TAG_COMPOUND);
        HashMap<BlockPos, Couple<Float>> map = new HashMap<>();
        for (int i = 0; i < engineList.size(); i++) {
            map.put(NbtUtils.readBlockPos(engineList.getCompound(i).getCompound("Pos")), Couple.create(engineList.getCompound(i).getFloat("Capacity"), engineList.getCompound(i).getFloat("Speed")));
        }
        engines.clear();
        engines = map;
        speed = compound.getFloat("GeneratedSpeed");
    }

    @Override
    public float getGeneratedSpeed() {
        return movementDirection * speed;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if(movementDirection == 0)
            return 0;
        AtomicReference<Float> stress = new AtomicReference<>(0f);
        Map<BlockPos, Couple<Float>> map = Map.copyOf(engines);

        map.forEach((b, s) -> stress.updateAndGet(f -> f + s.getFirst()/s.getSecond()));
        this.lastCapacityProvided = capacity;
        return stress.get();
    }
    @Override
    public int getRotationAngleOffset(Direction.Axis axis) {
        int combinedCoords = axis.choose(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
    }
}
