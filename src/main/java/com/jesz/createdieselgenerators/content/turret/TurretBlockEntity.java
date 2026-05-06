package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterItem;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringBehaviour;
import com.jesz.createdieselgenerators.mixin_interfaces.IEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretBlockEntity extends KineticBlockEntity {

    public float oldHorizontalRotation;
    public float oldVerticalRotation;
    public float oldTargetedVerticalRotation = 0;
    public float oldTargetedHorizontalRotation = 0;

    public float horizontalRotation;
    public float verticalRotation;
    public float targetedVerticalRotation = 0;
    public float targetedHorizontalRotation = 0;

    public Player controllingPlayer;
    public LivingEntity controllingEntity;
    public Entity targetedEntity;
    public Direction controllingEntityDirection;
    public boolean sync;
    boolean removePlayer = false;

    public TurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    int t;

    @Override
    public void tick() {
        super.tick();

        if (controllingEntity == null)
            targetedEntity = null;
        if (controllingEntity != null) {
            controllingEntity.setYHeadRot(-targetedHorizontalRotation+180);
            if (controllingEntity.getRootVehicle() instanceof SeatEntity seat) {
                if (Math.sqrt(seat.blockPosition().distSqr(worldPosition)) > 1) {
                    ((IEntity)controllingEntity).setTurretPos(null);
                    controllingEntity = null;
                }
            } else {
                ((IEntity) controllingEntity).setTurretPos(null);
                controllingEntity = null;
            }
        } else {
            if (t==0)
                for(Direction direction : Direction.Plane.HORIZONTAL){
                    List<SeatEntity> list = level.getEntitiesOfClass(SeatEntity.class, new AABB(getBlockPos().relative(direction)));
                    if(!list.isEmpty()){
                        List<Entity> passengers = list.get(0).getPassengers();
                        if(!passengers.isEmpty() && !(passengers.get(0) instanceof Player)) {
                            Entity possibleControllingEntity = passengers.get(0);
                            if(((IEntity)possibleControllingEntity).getTurretPos() == null) {
                                ((IEntity)possibleControllingEntity).setTurretPos(worldPosition);
                                if (possibleControllingEntity instanceof LivingEntity le) {
                                    controllingEntity = le;
                                    controllingEntityDirection = direction;
                                }
                            }
                        }
                    }
                }
        }

        if (controllingEntity != null && controllingPlayer == null && targetedEntity != null) {
            AABB aabb = getTargetBB();
            Vec3 targetPos = SableCompanion.INSTANCE.projectOutOfSubLevel(targetedEntity.level(), targetedEntity.position());
            Vec3 turretPos = getWorldPos();
            targetedHorizontalRotation = (float) (Math.atan2(targetPos.x - turretPos.x, targetPos.z - turretPos.z) * 180 / Math.PI) + 180;            targetedVerticalRotation = calculatePitch(targetPos.add(0, 0.5, 0));

            if (!aabb.contains(targetPos) || targetedEntity.isRemoved() || !isWithinRange(targetPos.add(0, 0.5, 0)) || targetPos.distanceTo(getWorldPos()) < 1)
                targetedEntity = null;
        }

        t++;
        if (t >= 40) {
            t = 0;
            updateTargetedEntity();
        }
        if (t % 8 == 0 && sync) {
            sendData();
            sync = false;
        }

        oldHorizontalRotation = horizontalRotation;
        oldVerticalRotation = verticalRotation;
        horizontalRotation = AngleHelper.angleLerp(controllingEntity == null || controllingPlayer != null ? 0.2f : 0.7f, horizontalRotation, targetedHorizontalRotation);
        verticalRotation = AngleHelper.angleLerp(controllingEntity == null || controllingPlayer != null ? 0.2f : 0.7f, verticalRotation, targetedVerticalRotation);
        if (oldTargetedHorizontalRotation != targetedHorizontalRotation || oldTargetedVerticalRotation != targetedVerticalRotation)
            sendData();
        oldTargetedHorizontalRotation = targetedHorizontalRotation;
        oldTargetedVerticalRotation = targetedVerticalRotation;

        if (controllingPlayer == null)
            return;

        if (Math.sqrt(controllingPlayer.distanceToSqr(getWorldPos())) > 3 || controllingPlayer.isCrouching())
            removePlayer();

        if (removePlayer || controllingPlayer.isRemoved()) {
            controllingPlayer = null;
            removePlayer = false;
            return;
        }
        targetedVerticalRotation = Mth.clamp(controllingPlayer.xRotO, -50, 1);
        targetedHorizontalRotation = -controllingPlayer.yHeadRotO+180;
    }


    float cachedPitch = 0;
    public float calculatePitch(Vec3 targetPos) {
        Vec3 start = getWorldPos().add(0, 0.625f, 0);

        float initialVelocity = getShootingForce();

        double dx = targetPos.x - start.x;
        double dz = targetPos.z - start.z;
        double dY = targetPos.y - start.y;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float closestPitch = 0;
        float closestDistance = 9999;

        if (simulate(cachedPitch, initialVelocity, horizontalDist, dY) < 1)
            return cachedPitch;

        for (float pitch = -50; pitch <= 10; pitch += 1f) {
            float distance = simulate(pitch, initialVelocity, horizontalDist, dY);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPitch = pitch;
            }
        }

        cachedPitch = closestPitch;
        return closestPitch;
    }

    public float getShootingForce() {
        float speed = Math.abs(getSpeed());
        return (float) Math.log(speed) * 0.3f + 0.2f;
    }

    public float simulate(float pitch, float initialVelocity, double horizontalDist, double dY) {
        double pX = 0;
        double pY = 0;

        double vX = Math.cos(Math.toRadians(-pitch)) * initialVelocity;
        double vY = Math.sin(Math.toRadians(-pitch)) * initialVelocity;

        float closestDistance = 9999;

        for (int t = 0; t < 200; t++) {
            vY -= 0.015;

            pX += vX;
            pY += vY;

            vX *= 0.95;
            vY *= 0.95;

            if (pY < dY && vY < 0)
                break;

            float distance = (float) Math.sqrt(((horizontalDist - pX) * (horizontalDist - pX)) + (dY - pY) * (dY - pY));

            if (distance < closestDistance) {
                closestDistance = distance;
            }
        }

        return closestDistance;
    }

    public boolean isWithinRange(Vec3 targetPos) {
        Vec3 turretPos = getWorldPos().add(0, 0.625f, 0);

        double dx = targetPos.x - turretPos.x;
        double dz = targetPos.z - turretPos.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        return horizontalDistance < 22;
    }

    @Override
    public void remove() {
        super.remove();
        if(controllingEntity != null)
            ((IEntity)controllingEntity).setTurretPos(null);
    }

    public void updateTargetedEntity(){
        AABB aabb = getTargetBB();
        List<Entity> entities = level.getEntities(null, aabb).stream().filter(e -> {
            if (!(e instanceof LivingEntity entity))
                return false;
            return isValidTarget(entity);
        }).sorted((entity, t1) -> {
            if (entity == targetedEntity)
                return 1;
            if (t1 == targetedEntity)
                return 0;
            return (int) t1.position().distanceTo(getWorldPos());
        }).toList();
        if (entities.isEmpty())
            targetedEntity = null;
        else
            targetedEntity = entities.get(0);
    }

    public boolean isValidTarget(LivingEntity entity) {
        Vec3 entityPos = SableCompanion.INSTANCE.projectOutOfSubLevel(entity.level(), entity.position());

        if (!TargetingConditions.forCombat().test(controllingEntity, entity))
            return false;
        if (entity.isRemoved() || !isWithinRange(entityPos) || entityPos.distanceTo(getWorldPos()) < 2)
            return false;
        if (filtering.getFilter().getItem() instanceof SpawnEggItem egg)
            if (egg.getType(null) != entity.getType())
                return false;
        if (filtering.getFilter().getItem() instanceof EntityFilterItem) {
            if (!EntityFilterItem.test(filtering.getFilter(), entity))
                return false;
        } else if (entity instanceof Player)
            return false;
        return true;
    }

    public AABB getTargetBB() {
        Vec3 pos = getWorldPos();
        return new AABB(pos.x - (controllingEntityDirection == Direction.WEST ? -1.5 : 33), pos.y - 5, pos.z - (controllingEntityDirection == Direction.NORTH ? -1.5 : 33), pos.x + (controllingEntityDirection == Direction.EAST ? -1.5 : 33), pos.y + 10, pos.z + (controllingEntityDirection == Direction.SOUTH ? -1.5 : 33));
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        targetedVerticalRotation = compound.getFloat("VerticalRotation");
        targetedHorizontalRotation = compound.getFloat("HorizontalRotation");
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putFloat("VerticalRotation", targetedVerticalRotation);
        compound.putFloat("HorizontalRotation", targetedHorizontalRotation);
    }

    private FilteringBehaviour filtering;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new EntityFilteringBehaviour(this, new ChemicalTurretBlockEntity.ChemicalTurretValueBox());
        behaviours.add(filtering);
    }

    public void setControllingPlayer(Player player){
        BlockPos worldPos = BlockPos.containing(getWorldPos());
        if(level.isClientSide)
            AllSoundEvents.CONTROLLER_CLICK.play(level, player, worldPos);
        BlockPos tPos = ((IEntity)player).getTurretPos();
        if(tPos != null && level.getBlockEntity(tPos) instanceof TurretBlockEntity be)
            be.removePlayer();
        if(player.distanceToSqr(getWorldPos()) > 9){
            if(player instanceof ServerPlayer sp)
                sp.connection.send(new ClientboundSetActionBarTextPacket(CreateDieselGenerators.lang("actionbar.turret.too_far_away")));
            return;
        }
        if(player instanceof ServerPlayer sp)
            sp.connection.send(new ClientboundSetActionBarTextPacket(CreateLang.translateDirect("contraption.controls.start_controlling", Component.translatable(getBlockState().getBlock().getDescriptionId()))));
        controllingPlayer = player;
        ((IEntity)controllingPlayer).setTurretPos(worldPosition);
    }

    public void removePlayer() {
        BlockPos worldPos = BlockPos.containing(getWorldPos());
        if(level.isClientSide)
            AllSoundEvents.CONTROLLER_CLICK.play(level, controllingPlayer, worldPos);
        if(controllingPlayer instanceof ServerPlayer sp)
            sp.connection.send(new ClientboundSetActionBarTextPacket(CreateDieselGenerators.lang("actionbar.turret.stopped_controlling", Component.translatable(getBlockState().getBlock().getDescriptionId()))));
        ((IEntity)controllingPlayer).setTurretPos(null);
        removePlayer = true;
    }

    public Vec3 getWorldPos() {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, Vec3.atCenterOf(worldPosition));
    }
}
