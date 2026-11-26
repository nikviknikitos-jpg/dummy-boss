package com.example.dummyboss.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CursedDummyEntity extends HostileEntity {
    public static class RecordedMove {
        public ItemStack weapon = ItemStack.EMPTY;
        public boolean wasJumping = false;
        public double strafeSide = 0.0;
        public float damageDealt = 4.0f;
        public boolean isRanged = false;

        public NbtCompound toNbt() {
            NbtCompound t = new NbtCompound();
            NbtCompound wc = new NbtCompound();
            weapon.writeNbt(wc);
            t.put("weapon", wc);
            t.putBoolean("jump", wasJumping);
            t.putDouble("strafe", strafeSide);
            t.putFloat("dmg", damageDealt);
            t.putBoolean("ranged", isRanged);
            return t;
        }
        public static RecordedMove fromNbt(NbtCompound t) {
            RecordedMove r = new RecordedMove();
            r.weapon = ItemStack.fromNbt(t.getCompound("weapon"));
            r.wasJumping = t.getBoolean("jump");
            r.strafeSide = t.getDouble("strafe");
            r.damageDealt = t.getFloat("dmg");
            r.isRanged = t.getBoolean("ranged");
            return r;
        }
    }

    public int phase = 0;
    public final List<RecordedMove> memory = new ArrayList<>();
    public int comboIndex = 0;
    public int actionCooldown = 60;
    public UUID ownerUuid;
    public int hurtPhase0 = 0;
    public ServerBossBar bossBar;

    public CursedDummyEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.36)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8);
    }

    public void setOwner(PlayerEntity p) { this.ownerUuid = p.getUuid(); }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 16));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            if (phase == 1 && age % 4 == 0) {
                getWorld().addParticle(ParticleTypes.PORTAL,
                        getX() + (random.nextDouble() - 0.5),
                        getY() + random.nextDouble() * 1.8,
                        getZ() + (random.nextDouble() - 0.5), 0, 0.02, 0);
            }
            return;
        }
        if (phase == 0) {
            if (hurtPhase0 > 0) {
                hurtPhase0--;
                hurtTime = hurtPhase0;
            }
            PlayerEntity near = getWorld().getClosestPlayer(this, 12.0);
            if (near != null) getLookControl().lookAt(near, 30, 30);
            setCustomName(Text.literal("§e[ОБУЧЕНИЕ: Бей меня!] §7(ударов: " + memory.size() + "/8)"));
            setCustomNameVisible(true);
        } else if (phase == 1) {
            if (bossBar != null) {
                bossBar.setPercent(getHealth() / getMaxHealth());
            }
            PlayerEntity target = getWorld().getClosestPlayer(this, 24.0);
            if (target != null && !memory.isEmpty()) {
                actionCooldown--;
                if (actionCooldown <= 0) {
                    RecordedMove m = memory.get(comboIndex % memory.size());
                    comboIndex++;
                    performRecordedMove(m, target);
                    actionCooldown = 25 + random.nextInt(11);
                }
            }
        }
    }

    public void performRecordedMove(RecordedMove move, PlayerEntity target) {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        // Equip copied weapon
        setStackInHand(Hand.MAIN_HAND, move.weapon.copy());
        // Face target
        getLookControl().lookAt(target, 30, 30);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double len = Math.sqrt(dx * dx + dz * dz) + 1e-6;
        dx /= len; dz /= len;
        if (move.isRanged) {
            // Backstep
            setVelocity(-dx * 0.4, 0.25, -dz * 0.4);
            velocityModified = true;
            ArrowEntity arrow = new ArrowEntity(sw, this);
            arrow.setVelocity(dx * 1.4, 0.2, dz * 1.4);
            arrow.setDamage(Math.max(4.0f, move.damageDealt * 0.75f));
            arrow.refreshPositionAndAngles(getX(), getEyeY() - 0.2, getZ(), 0, 0);
            sw.spawnEntity(arrow);
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 1f, 1f);
        } else {
            if (move.wasJumping) {
                setVelocity(dx * 0.7, 0.45, dz * 0.7);
                velocityModified = true;
                sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1f, 1f);
            }
            if (Math.abs(move.strafeSide) > 0.2) {
                // strafe left/right relative to target direction
                double perpX = -dz * move.strafeSide;
                double perpZ = dx * move.strafeSide;
                setVelocity(getVelocity().x + perpX * 0.5, getVelocity().y, getVelocity().z + perpZ * 0.5);
                velocityModified = true;
            }
            float dmg = Math.max(4.0f, move.damageDealt * 0.75f);
            if (squaredDistanceTo(target) < 9.0) {
                target.damage(sw.getDamageSources().mobAttack(this), dmg);
                Vec3d push = new Vec3d(dx, 0.3, dz);
                target.setVelocity(push.x, push.y, push.z);
                target.velocityModified = true;
                sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1f, 1f);
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (phase == 0) {
            // Records the move, always heals, never dies in phase 0
            PlayerEntity attacker = source.getAttacker() instanceof PlayerEntity p ? p : null;
            if (attacker != null && memory.size() < 8) {
                RecordedMove m = new RecordedMove();
                m.weapon = attacker.getMainHandStack().copy();
                m.wasJumping = !attacker.isOnGround();
                // strafe: project velocity onto right of attacker's look
                Vec3d look = attacker.getRotationVec(1.0f);
                double rightX = -look.z;
                double rightZ = look.x;
                double side = attacker.getVelocity().x * rightX + attacker.getVelocity().z * rightZ;
                m.strafeSide = Math.signum(side) * Math.min(1.0, Math.abs(side));
                m.damageDealt = amount;
                m.isRanged = false;
                memory.add(m);
                String descriptor = (m.wasJumping ? "Удар в прыжке" : Math.abs(m.strafeSide) > 0.2 ? "Рывок вбок" : "Прямой удар")
                        + " + [" + m.weapon.getName().getString() + "]";
                attacker.sendMessage(Text.literal("§dМанекен запомнил: " + descriptor + "!"), false);
                if (getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.ENCHANT, getX(), getY() + 1, getZ(), 10, 0.3, 0.5, 0.3, 0.1);
                    sw.spawnParticles(ParticleTypes.CRIT, getX(), getY() + 1, getZ(), 8, 0.3, 0.5, 0.3, 0.1);
                }
                getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.NEUTRAL, 1f, 1f);
                this.heal(amount);
                this.hurtPhase0 = 5;
                if (memory.size() >= 8 && phase == 0) awaken();
            }
            return false;
        }
        return super.damage(source, amount);
    }

    public void awaken() {
        if (phase != 0) return;
        phase = 1;
        setCustomName(Text.literal("§c§lПроклятый Манекен [Твой Ученик]"));
        setCustomNameVisible(true);
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 60, 0.8, 1.0, 0.8, 0.05);
            sw.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 60, 0.8, 1.0, 0.8, 0.05);
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2f, 0.8f);
        }
        bossBar = new ServerBossBar(getDisplayName(), ServerBossBar.Color.PURPLE, ServerBossBar.Style.PROGRESS);
        if (getWorld() instanceof ServerWorld sw) {
            for (ServerPlayerEntity p : sw.getPlayers()) {
                if (p.squaredDistanceTo(this) < 1024) bossBar.addPlayer(p);
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!getWorld().isClient && getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.FIREWORK, getX(), getY() + 1, getZ(), 60, 0.8, 1.0, 0.8, 0.15);
            for (int i = 0; i < 8; i++) {
                net.minecraft.entity.ExperienceOrbEntity orb = new net.minecraft.entity.ExperienceOrbEntity(sw, getX(), getY() + 1, getZ(), 25);
                sw.spawnEntity(orb);
            }
            sw.spawnEntity(new net.minecraft.entity.ItemEntity(sw, getX(), getY() + 1, getZ(),
                    new ItemStack(com.example.dummyboss.item.ModItems.DUMMY_SPAWNER)));
            sw.spawnEntity(new net.minecraft.entity.ItemEntity(sw, getX(), getY() + 1, getZ(),
                    new ItemStack(Items.DIAMOND, 2 + random.nextInt(3))));
            if (bossBar != null) bossBar.clearPlayers();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("phase", phase);
        NbtList list = new NbtList();
        for (RecordedMove m : memory) list.add(m.toNbt());
        nbt.put("memory", list);
        if (ownerUuid != null) nbt.putUuid("owner", ownerUuid);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        phase = nbt.getInt("phase");
        memory.clear();
        NbtList list = nbt.getList("memory", 10);
        for (int i = 0; i < list.size(); i++) memory.add(RecordedMove.fromNbt(list.getCompound(i)));
        if (nbt.containsUuid("owner")) ownerUuid = nbt.getUuid("owner");
    }

    @Override
    public net.minecraft.util.ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!getWorld().isClient && player instanceof ServerPlayerEntity sp) {
            if (phase == 0 && sp.isSneaking() && !memory.isEmpty()) {
                awaken();
                return net.minecraft.util.ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }
}
