package net.whiteman.biosanity.world.neoplasm.core.hivemind;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static net.whiteman.biosanity.world.neoplasm.core.CoreConfig.*;

public class Hivemind {
    // TODO maybe make split in shards logic?

    // Hivemind section
    private final UUID id;
    private final Set<BlockPos> members = new LinkedHashSet<>();
    private final Set<BlockPos> activeMembers = new HashSet<>(); // Members that current loaded in chunk

    // Core section
    private int experiencePoints = STARTING_CORE_LEVEL.getNeededXp();
    private int stamina = 0;

    private int biomass = STARTING_BIOMASS_VALUE;
    private int minerals = STARTING_MINERALS_VALUE;
    private int energy = STARTING_ENERGY_VALUE;

    // AI section (wip)
    private int actionCooldown = 0;
    private int alertPoints = 0;

    public Hivemind(UUID id) {
        this.id = id;
    }

    public UUID getId() { return this.id; }

    public void tick(Level level) {
        long time = level.getGameTime();

        if (time % 20 == 0) this.updateActiveMembers(level);

        if (this.activeMembers.isEmpty()) return;

        // Some code
    }

    //region AI
    private void setActionCooldown(Level level) {
        this.actionCooldown = MIN_TICKS_REACTION + level.random.nextInt(MAX_ADDITIONAL_TICKS_REACTION);
    }

    public void increaseAlertPoints(int amount) {
        if (this.alertPoints >= MAX_ALERT_POINTS) return;

        this.alertPoints = Math.min(alertPoints + amount, MAX_ALERT_POINTS);
    }

    public void decreaseAlertPoints(int amount) {
        if (this.alertPoints <= 0) return;

        this.alertPoints = Math.max(alertPoints - amount, 0);
    }

    private void onResourceChanged() {
        // Alerts AI about new portion of resources
        // Some code
        if (this.biomass >= getMaxStorage() * 0.8 && this.actionCooldown > 5) {
            this.actionCooldown = 5;
        }
    }
    //endregion

    //region Hivemind members
    public void addMember(BlockPos pos) { members.add(pos); }
    public void removeMember(BlockPos pos) { members.remove(pos); }

    public Set<BlockPos> getAllMembers() { return members; }
    public Set<BlockPos> getActiveMembers() { return activeMembers; }

    public void updateActiveMembers(Level level) {
        this.activeMembers.clear();

        for (BlockPos pos : this.members) {
            if (level.shouldTickBlocksAt(pos)) {
                this.activeMembers.add(pos);
            }
        }
    }
    //endregion

    //region Hivemind resources getters/setters
    public int getBiomass() { return this.biomass; }

    public void modifyBiomass(int amount) {
        int oldVal = this.biomass;

        this.biomass = Math.max(0, Math.min(this.biomass + amount, getMaxStorage()));

        if (oldVal != this.biomass) {
            this.onResourceChanged();
        }
    }

    public int getMinerals() { return this.minerals; }

    public void modifyMinerals(int amount) {
        int oldVal = this.minerals;

        this.minerals = Math.max(0, Math.min(this.minerals + amount, getMaxStorage()));

        if (oldVal != this.minerals) {
            this.onResourceChanged();
        }
    }

    public int getEnergy() { return this.energy; }

    public void modifyEnergy(int amount) {
        int oldVal = this.energy;

        this.energy = Math.max(0, Math.min(this.energy + amount, getMaxStorage()));

        if (oldVal != this.energy) {
            this.onResourceChanged();
        }
    }

    public void modifyExperiencePoints(int amount) {
        int oldVal = this.experiencePoints;

        this.experiencePoints = Math.max(0, Math.min(this.experiencePoints + amount, MAX_XP));

        if (oldVal != this.experiencePoints) {
            this.onResourceChanged();
        }
    }

    public int getStamina() { return this.stamina; }

    public int getMaxStamina() { return HivemindLevel.getMaxStamina(getLevel()); }

    public void modifyStamina(int amount) {
        int oldVal = this.stamina;

        this.stamina = Math.max(0, Math.min(this.stamina + amount, getMaxStamina()));

        if (oldVal != this.stamina) {
            this.onResourceChanged();
        }
    }
    //endregion

    public HivemindLevel getLevel() { return HivemindLevel.getFromXp(this.experiencePoints); }

    public int getMaxStorage() { return START_MAX_STORAGE + (members.size() * CORE_EXPAND_STORAGE_VALUE); }

    // We suppose to save/load hivemind data
    // bc each restart actually it's a new object,
    // and it lost all achievements/resources etc.
    public CompoundTag save(CompoundTag pTag) {
        pTag.putUUID("id", this.id);
        pTag.putInt("experiencePoints", experiencePoints);
        pTag.putInt("stamina", stamina);
        pTag.putInt("biomass", biomass);
        pTag.putInt("minerals", minerals);
        pTag.putInt("energy", energy);
        pTag.putInt("alertPoints", alertPoints);
        return pTag;
    }

    public static Hivemind load(CompoundTag pTag) {
        Hivemind hive = new Hivemind(pTag.getUUID("id"));
        ListTag posList = pTag.getList("members", Tag.TAG_COMPOUND);

        for (int i = 0; i < posList.size(); i++) {
            CompoundTag tag = posList.getCompound(i);
            hive.addMember(NbtUtils.readBlockPos(tag));
        }
        hive.experiencePoints = pTag.getInt("experiencePoints");
        hive.stamina = pTag.getInt("stamina");
        hive.biomass = pTag.getInt("biomass");
        hive.minerals = pTag.getInt("minerals");
        hive.energy = pTag.getInt("energy");
        hive.alertPoints = pTag.getInt("alertPoints");
        return hive;
    }
}