package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;
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

    public boolean isEngineForConnectorDisplay( BlockPos pos ) {

        Direction.Axis axis = getBlockState().getValue(AXIS);
        for (Direction d : List.of(axis == Direction.Axis.Z ? Direction.UP : Direction.NORTH, axis == Direction.Axis.Z ? Direction.DOWN : Direction.SOUTH, axis == Direction.Axis.X ? Direction.UP : Direction.EAST, axis == Direction.Axis.X ? Direction.DOWN : Direction.WEST)) {
            BlockState st = getLevel().getBlockState(getBlockPos().relative(d, 2));
            if(st.getBlock() instanceof HugeDieselEngineBlock && st.getValue(FACING) == d.getOpposite())
                return(getBlockPos().relative(d, 2).equals(pos));
        }
        return false;
    }

    public List<Pair<BlockPos, Couple<Float>>> engines = new ArrayList<>(4);

    public void update(BlockPos sourcePos, int direction, float stress, float speed) {
        Pair<BlockPos, Couple<Float>> found = null;
        for (Pair<BlockPos, Couple<Float>> engine : engines)
            if(engine.getFirst().equals(sourcePos)){
                found = engine;
                break;
            }

        List<Pair<BlockPos, Couple<Float>>> newEngines = new ArrayList<>(engines);
        if (found != null) {
            Couple<Float> status = found.getSecond();
            if (status.getFirst() == stress && status.getSecond() == speed)
                return;
            newEngines.remove(found);
        }
        newEngines.add(Pair.of(sourcePos, Couple.create(stress, speed)));
        engines = newEngines;


        AtomicReference<Float> maxSpeed = new AtomicReference<>(0f);

        for (Pair<BlockPos, Couple<Float>> engine : engines) {
            if (engine.getSecond().getSecond() > maxSpeed.get())
                maxSpeed.set(engine.getSecond().getSecond());
        }

        this.speed = maxSpeed.get();
        this.movementDirection = direction;

        reActivateSource = true;
    }

    public boolean canBePoweredBy() {
        return initialTicks == 0;
    }

    public void removeGenerator(BlockPos sourcePos) {
        List<Pair<BlockPos, Couple<Float>>> newEngines = new ArrayList<>(engines);
        boolean removed = newEngines.removeIf(p -> p.getFirst().equals(sourcePos));
        engines = newEngines;

        if (engines.isEmpty()) {
            movementDirection = 0;
            speed = 0;
            stressCapacity = 0;
        }
        if (removed)
            reActivateSource = true;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        compound.putInt("Direction", movementDirection);
        if (initialTicks > 0)
            compound.putInt("Warmup", initialTicks);
        ListTag engineList = new ListTag();

        for (Pair<BlockPos, Couple<Float>> engine : List.copyOf(engines)){
            CompoundTag tag = new CompoundTag();
            tag.putFloat("Capacity", engine.getSecond().getFirst());
            tag.putFloat("Speed", engine.getSecond().getSecond());
            tag.put("Pos", NbtUtils.writeBlockPos(engine.getFirst()));
            engineList.add(tag);
        };
        compound.putFloat("GeneratedSpeed", speed);
        compound.put("Engines", engineList);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        movementDirection = compound.getInt("Direction");
        initialTicks = compound.getInt("Warmup");

        ListTag engineList = compound.getList("Engines", CompoundTag.TAG_COMPOUND);
        List<Pair<BlockPos, Couple<Float>>> newEngines = new ArrayList<>();
        for (int i = 0; i < engineList.size(); i++) {
            newEngines.add(Pair.of(NBTHelper.readBlockPos(engineList.getCompound(i), "Pos"),
                    Couple.create(engineList.getCompound(i).getFloat("Capacity"),
                            engineList.getCompound(i).getFloat("Speed"))));
        }
        engines = newEngines;

        speed = compound.getFloat("GeneratedSpeed");
    }

    private float getActiveEngineSpeed() {
        float maxSpeed = 0f;
        for (Pair<BlockPos, Couple<Float>> engine : engines) {
            if (isEngineFueled(engine.getFirst()))
                maxSpeed = Math.max(maxSpeed, engine.getSecond().getSecond());
        }
        return maxSpeed;
    }

    private boolean isEngineFueled(BlockPos enginePos) {
        if (level == null)
            return false;
        var be = level.getBlockEntity(enginePos);
        if (be instanceof HugeDieselEngineBlockEntity engine)
            return engine.enabled();
        return false;
    }

    @Override
    public float getGeneratedSpeed() {
        if (movementDirection == 0)
            return 0;
        return movementDirection * getActiveEngineSpeed();
    }

    @Override
    public float calculateAddedStressCapacity() {
        if(movementDirection == 0)
            return 0;
        float capacity = 0;
        for (Pair<BlockPos, Couple<Float>> engine : engines) {
            if (isEngineFueled(engine.getFirst()))
                capacity += engine.getSecond().getFirst();
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public int getRotationAngleOffset(Direction.Axis axis) {
        int combinedCoords = axis.choose(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
    }
}
