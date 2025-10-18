package net.smok.drifter.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CollidedAsteroid extends Fireball {

    public static final int LIFE_TIME = 20 * 30;

    private long lifeTime;
    private long startTime;
    private int explosionPower;


    public CollidedAsteroid(EntityType<? extends CollidedAsteroid> entityType, Level level) {
        super(entityType, level);

    }


    @Override
    public void tick() {
        super.tick();
        if (startTime != 0L && startTime + lifeTime < level().getGameTime()) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), explosionPower, false, Level.ExplosionInteraction.MOB);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity entity = result.getEntity();
            Entity owner = getOwner();
            entity.hurt(this.damageSources().fireball(this, owner), 6.0F);
            if (owner instanceof LivingEntity livingEntity) {
                this.doEnchantDamageEffects(livingEntity, entity);
            }
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt) start();
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putLong("LifeTime", lifeTime);
        compound.putLong("StartTime", startTime);
        compound.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("LifeTime", CompoundTag.TAG_LONG))
            lifeTime = compound.getLong("LifeTime");
        if (compound.contains("LifeTime", CompoundTag.TAG_LONG))
            startTime = compound.getLong("StartTime");
        if (compound.contains("ExplosionPower", CompoundTag.TAG_INT))
            this.explosionPower = compound.getByte("ExplosionPower");
    }

    public void startTo(Vec3 angle) {
        setDeltaMovement(angle);
        xPower = angle.x * 0.1;
        yPower = angle.y * 0.1;
        zPower = angle.z * 0.1;
        start();
    }

    public int getExplosionPower() {
        return explosionPower;
    }

    public void setExplosionPower(int explosionPower) {
        this.explosionPower = explosionPower;
    }

    public void start() {
        startTime = level().getGameTime();
        lifeTime = LIFE_TIME;
    }
}
