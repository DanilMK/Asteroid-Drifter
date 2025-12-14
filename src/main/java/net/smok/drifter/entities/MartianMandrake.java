package net.smok.drifter.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MartianMandrake extends PathfinderMob {
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;


    public MartianMandrake(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.jumpControl = new OwnJumpControl(this);
        this.moveControl = new OwnMoveControl(this);
        this.setSpeedModifier(0.0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ChestGoal(level(), this, getLoot(), this::discard));
        goalSelector.addGoal(2, new PanicGoal(this, 1.25));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 5, 2.2, 2.2));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    private Consumer<Consumer<ItemStack>> getLoot() {
        return itemStackConsumer -> putItems(this.damageSources().genericKill(), false, itemStackConsumer);
    }

    private void putItems(DamageSource damageSource, boolean hitByPlayer, Consumer<ItemStack> itemStackConsumer) {
        ResourceLocation resourceLocation = this.getLootTable();
        LootTable lootTable = this.level().getServer().getLootData().getLootTable(resourceLocation);
        LootParams.Builder builder = new LootParams.Builder((ServerLevel)this.level())
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());
        if (hitByPlayer && this.lastHurtByPlayer != null) {
            builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
        }

        LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootParams, this.getLootTableSeed(), itemStackConsumer);
    }

    @Override
    protected float getJumpPower() {
        float f = 0.3F;
        if (this.horizontalCollision || this.moveControl.hasWanted() && this.moveControl.getWantedY() > this.getY() + 0.5) {
            f = 0.5F;
        }

        Path path = this.navigation.getPath();
        if (path != null && !path.isDone()) {
            Vec3 vec3 = path.getNextEntityPos(this);
            if (vec3.y > this.getY() + 0.5) {
                f = 0.5F;
            }
        }

        if (this.moveControl.getSpeedModifier() <= 0.6) {
            f = 0.2F;
        }

        return f + this.getJumpBoostPower();
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        double d = this.moveControl.getSpeedModifier();
        if (d > 0.0) {
            double e = this.getDeltaMovement().horizontalDistanceSqr();
            if (e < 0.01) {
                this.moveRelative(0.1F, new Vec3(0.0, 0.0, 1.0));
            }
        }

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)1);
        }
    }

    public float getJumpCompletion(float partialTick) {
        return this.jumpDuration == 0 ? 0.0F : (this.jumpTicks + partialTick) / this.jumpDuration;
    }

    public void setSpeedModifier(double speedModifier) {
        this.getNavigation().setSpeedModifier(speedModifier);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), speedModifier);
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);
        if (jumping) {
            this.playSound(SoundEvents.RABBIT_JUMP, this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }
    }


    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    public void customServerAiStep() {
        if (this.jumpDelayTicks > 0) {
            this.jumpDelayTicks--;
        }

        if (this.onGround()) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            OwnJumpControl ownJumpControl = getJumpControl();
            if (!ownJumpControl.wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path path = this.navigation.getPath();
                    Vec3 vec3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if (path != null && !path.isDone()) {
                        vec3 = path.getNextEntityPos(this);
                    }

                    this.facePoint(vec3.x, vec3.z);
                    this.startJumping();
                }
            } else if (!ownJumpControl.canJump()) {
                this.enableJumpControl();
            }
        }

        this.wasOnGround = this.onGround();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void facePoint(double x, double z) {
        this.setYRot((float)(Mth.atan2(z - this.getZ(), x - this.getX()) * 180.0F / (float)Math.PI) - 90.0F);
    }

    private void enableJumpControl() {
        getJumpControl().setCanJump(true);
    }

    private void disableJumpControl() {
        getJumpControl().setCanJump(false);
    }

    @Override
    public @NotNull OwnJumpControl getJumpControl() {
        return (OwnJumpControl) super.getJumpControl();
    }

    private void setLandingDelay() {
        if (this.moveControl.getSpeedModifier() < 2.2) {
            this.jumpDelayTicks = 10;
        } else {
            this.jumpDelayTicks = 1;
        }
    }

    private void checkLandingDelay() {
        this.setLandingDelay();
        this.disableJumpControl();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.jumpTicks != this.jumpDuration) {
            this.jumpTicks++;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }


    @Override
    public void handleEntityEvent(byte id) {
        if (id == 1) {
            this.spawnSprintParticle();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CROP_BREAK;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GRASS_BREAK;
    }

    public static class OwnJumpControl extends JumpControl {
        private final MartianMandrake mandrake;
        private boolean canJump;

        public OwnJumpControl(MartianMandrake mandrake) {
            super(mandrake);
            this.mandrake = mandrake;
        }

        public boolean wantJump() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean canJump) {
            this.canJump = canJump;
        }

        @Override
        public void tick() {
            if (this.jump) {
                this.mandrake.startJumping();
                this.jump = false;
            }
        }
    }

    public static class OwnMoveControl extends MoveControl {
        private final MartianMandrake mandrake;
        private double nextJumpSpeed;

        public OwnMoveControl(MartianMandrake mandrake) {
            super(mandrake);
            this.mandrake = mandrake;
        }

        @Override
        public void tick() {
            if (this.mandrake.onGround() && !this.mandrake.jumping && mandrake.getJumpControl().wantJump()) {
                this.mandrake.setSpeedModifier(0.0);
            } else if (this.hasWanted()) {
                this.mandrake.setSpeedModifier(this.nextJumpSpeed);
            }

            super.tick();
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speed) {
            if (this.mandrake.isInWater()) {
                speed = 1.5;
            }

            super.setWantedPosition(x, y, z, speed);
            if (speed > 0.0) {
                this.nextJumpSpeed = speed;
            }
        }
    }
}
