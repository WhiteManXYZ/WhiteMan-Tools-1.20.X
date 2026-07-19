package net.whiteman.biosanity.world.neoplasm.core.hivemind;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.world.neoplasm.ai.GoalRegistry;
import net.whiteman.biosanity.world.neoplasm.ai.IHivemindGoal;
import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConfig.*;

public class Hivemind {
    // TODO maybe make split in shards logic?

    // Hivemind section
    private final UUID id;
    private final Set<BlockPos> members = new LinkedHashSet<>();
    private final Set<BlockPos> activeMembers = new HashSet<>(); // Members that current loaded in chunk

    // Core section
    private int experiencePoints = STARTING_HIVEMIND_LEVEL.getNeededXp();
    private int stamina = 0;

    private int biomass = STARTING_BIOMASS_VALUE;
    private int minerals = STARTING_MINERALS_VALUE;
    private int energy = STARTING_ENERGY_VALUE;

    // AI section (wip)
    private final Map<BlockPos, IHivemindGoal> assignments = new HashMap<>();

    private int actionCooldown = 0;
    private int alertPoints = 0;

    public Hivemind(UUID id) {
        this.id = id;
    }

    public UUID getId() { return this.id; }

    public void tick(Level level) {
        long time = level.getGameTime();

        if (time % 20 == 0) {
            this.updateActiveMembers(level);

            decreaseAlertPoints(CALM_DOWN_RATE);
        }

        if (this.activeMembers.isEmpty()) return;

        tickAI(level);
    }

    //region AI
    public void tickAI(Level level) {
        actionCooldown--;
        // Some code
        if (actionCooldown <= 0) {
            distributeGoals(level);
            setActionCooldown(level);
        }
    }

    public void distributeGoals(Level level) {
        Set<Class<? extends IHivemindGoal>> takenNonStackable = new HashSet<>();

        // Checking each member goals, to prevent taking non-stackable goal
        for (BlockPos pos : activeMembers) {
            if (level.getBlockEntity(pos) instanceof NeoplasmCoreBE core) {
                IHivemindGoal current = core.getCurrentGoal();
                if (current != null && !current.isStackable()) {
                    takenNonStackable.add(current.getClass());
                }
            }
        }

        // We distribute new tasks to those who are free or who need to change their goals
        for (BlockPos pos : activeMembers) {
            if (level.getBlockEntity(pos) instanceof NeoplasmCoreBE core) {

                // We get a list of goals sorted by weight (Utility) from highest to lowest
                List<IHivemindGoal> candidates = GoalRegistry.createGoalsFor(core);
                candidates.sort((a, b) -> Double.compare(b.evaluateUtility(), a.evaluateUtility()));

                for (IHivemindGoal candidate : candidates) {
                    double weight = candidate.evaluateUtility();
                    if (weight <= 0) break;

                    // Looking for next available goal
                    if (!candidate.canUse()) continue;

                    // Non-stackable check
                    if (!candidate.isStackable() && takenNonStackable.contains(candidate.getClass())) {
                        continue;
                    }

                    // Mark goal as "used" if non-stackable
                    if (!candidate.isStackable()) {
                        takenNonStackable.add(candidate.getClass());
                    }

                    // Setting goal with checking
                    if (shouldSwitchGoal(core.getCurrentGoal(), candidate)) {
                        core.setCurrentGoal(candidate);
                    }

                    break; // Found the best goal for this core, moving to next
                }
            }
        }
    }

    private boolean shouldSwitchGoal(IHivemindGoal current, IHivemindGoal next) {
        if (current == null) return true;
        if (current.getClass() == next.getClass()) return false;

        // Switching only if new goal is more important than current
        return next.evaluateUtility() > (current.evaluateUtility() + 5d);
    }

    private void setActionCooldown(Level level) {
        RandomSource random = level.random;
        this.actionCooldown = Math.max(random.nextInt(getMaxReactionInTicks() + 1), MIN_TICKS_REACTION);
    }

    public void increaseAlertPoints(int amount) {
        if (this.alertPoints >= MAX_ALERT_POINTS) return;

        this.alertPoints = Math.min(alertPoints + amount, MAX_ALERT_POINTS);
    }

    public void decreaseAlertPoints(int amount) {
        if (this.alertPoints <= 0) return;

        this.alertPoints = Math.max(alertPoints - amount, 0);
    }

    public AlertLevel getAlertLevel() {
        return AlertLevel.getFromPoints(alertPoints);
    }

    private void onResourceChanged() {
        // Alerts AI about new portion of resources
        // Some code
        if (this.biomass >= getStorage() * 0.8 && this.actionCooldown > 5) {
            this.actionCooldown = 5;
        }
    }

    public double getAverageResourcesAmount() {
        double rawValue = (biomass + minerals + energy) / 3.0;
        return new BigDecimal(rawValue)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
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

        this.biomass = Math.max(0, Math.min(this.biomass + amount, getStorage()));

        if (oldVal != this.biomass) {
            this.onResourceChanged();
        }
    }

    public int getMinerals() { return this.minerals; }

    public void modifyMinerals(int amount) {
        int oldVal = this.minerals;

        this.minerals = Math.max(0, Math.min(this.minerals + amount, getStorage()));

        if (oldVal != this.minerals) {
            this.onResourceChanged();
        }
    }

    public int getEnergy() { return this.energy; }

    public void modifyEnergy(int amount) {
        int oldVal = this.energy;

        this.energy = Math.max(0, Math.min(this.energy + amount, getStorage()));

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

    public int getStorage() { return START_MAX_STORAGE + (members.size() * CORE_EXPAND_STORAGE_VALUE); }

    public int getMaxReactionInTicks() { return TICKS_REACTION_THRESHOLD + (members.size() * CORE_REACTION_LOAD_VALUE); }

    // We suppose to save/load hivemind data
    // bc each restart actually it's a new object,
    // and it lost all achievements/resources etc.
    public CompoundTag save(CompoundTag pTag) {
        pTag.putUUID("id", this.id);

        long[] memberPositions = members.stream()
                .mapToLong(BlockPos::asLong)
                .toArray();

        pTag.putLongArray("members", memberPositions);

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

        if (pTag.contains("members", Tag.TAG_LONG_ARRAY)) {
            long[] memberPositions = pTag.getLongArray("members");
            for (long posLong : memberPositions) {
                hive.addMember(BlockPos.of(posLong));
            }
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