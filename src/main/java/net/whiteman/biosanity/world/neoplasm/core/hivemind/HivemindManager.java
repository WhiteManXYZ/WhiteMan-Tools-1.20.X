package net.whiteman.biosanity.world.neoplasm.core.hivemind;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.whiteman.biosanity.BiosanityMod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static net.whiteman.biosanity.world.neoplasm.NeoplasmConfig.HIVEMIND_MAX_CORES;

@Mod.EventBusSubscriber(modid = BiosanityMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HivemindManager extends SavedData {
    private static final int CLEANUP_INTERVAL = 14400;

    private final Map<UUID, Hivemind> hiveminds = new HashMap<>();
    private final Map<BlockPos, UUID> posToId = new HashMap<>();

    /**
     * Saves data from all Hiveminds to disk.
     * Uses the WorldSavedData to write .dat file in each dimension
     * @param level Level (dimension), for which the save is being performed. Should be {@link net.minecraft.server.level.ServerLevel}
     */
    public static HivemindManager get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        return serverLevel.getDataStorage().computeIfAbsent(
                HivemindManager::load,
                HivemindManager::new,
                BiosanityMod.MOD_ID + "_core_hiveminds"
        );
    }

    /**
     * Get Hivemind by ID
     * @param id Hivemind ID
     */
    public @Nullable Hivemind getHivemindById(UUID id) {
        if (id == null) return null;
        return hiveminds.get(id);
    }

    /**
     * Get Hivemind from pos (eg. block pos)
     * @param pos Position
     */
    public @Nullable Hivemind getHivemindByPos(BlockPos pos) {
        UUID id = posToId.get(pos);
        return getHivemindById(id);
    }

    /**
     * Get all existing Hiveminds
     */
    public Collection<Hivemind> getAllHiveminds() {
        return hiveminds.values();
    }

    /**
     * Registers BlockPos of block in Hivemind by ID
     * @param pos BlockPos of block that will be added in Hivemind (note: registers only position, not the block itself!)
     * @param id Hivemind ID
     */
    public void registerBlock(BlockPos pos, UUID id) {
        posToId.put(pos, id);
        this.setDirty();
    }

    /**
     * Creates or joins existing Hivemind.
     * Logic: If there's a core nearby, join its Hivemind.
     * If there's none, create a new Hivemind.
     * @param pos BlockPos of block that will be added in Hivemind
     * @param neighbors List of all neighbors that theoretically may be registered of some Hivemind
     * @return UUID of joined/created Hivemind
     */
    public @NotNull UUID joinOrCreateHivemind(BlockPos pos, List<BlockPos> neighbors) {
        UUID targetId = null;

        // Looking for neighbor, that already joined hivemind
        // (we want to join too (except if hivemind is full))
        for (BlockPos n : neighbors) {
            if (posToId.containsKey(n)) {
                targetId = posToId.get(n);
                break;
            }
        }

        // If found, check size limit
        if (targetId != null) {
            Hivemind hive = hiveminds.get(targetId);
            if (hive != null && hive.getAllMembers().size() >= HIVEMIND_MAX_CORES) {
                // Set it to null so next condition can be true
                targetId = null;
            }
        }

        // If not - create own
        if (targetId == null) {
            targetId = UUID.randomUUID();
            hiveminds.put(targetId, new Hivemind(targetId));
        }

        // The joining itself
        // (doesn't matter, to created or already existing hivemind)
        Hivemind hive = hiveminds.get(targetId);
        if (hive != null) {
            hive.addMember(pos);
            registerBlock(pos, targetId);
            this.setDirty();
        }

        return targetId;
    }

    public @NotNull UUID createHivemind(BlockPos pos) {
        UUID targetId;

        targetId = UUID.randomUUID();
        hiveminds.put(targetId, new Hivemind(targetId));

        // Joining
        Hivemind hive = hiveminds.get(targetId);
        if (hive != null) {
            hive.addMember(pos);
            registerBlock(pos, targetId);
            this.setDirty();
        }

        return targetId;
    }

    /**
     * Deletes Hivemind by ID
     * @param id Hivemind ID
     */
    public void deleteHivemind(UUID id) {
        Hivemind hive = hiveminds.remove(id);
        if (hive != null) {
            for (BlockPos pos : hive.getAllMembers()) {
                posToId.remove(pos);
            }
        }
        this.setDirty();
    }

    /**
     * Ticks all existing Hiveminds in the level.
     * Performs a garbage collection of empty hiveminds every N minutes ({@value CLEANUP_INTERVAL} ticks)
     * @param event The level tick event
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
            HivemindManager manager = HivemindManager.get(event.level);
            long gameTime = event.level.getGameTime();

            for (Hivemind hive : manager.getAllHiveminds()) {
                hive.tick(event.level);
            }

            // Garbage collector
            if (gameTime % CLEANUP_INTERVAL == 0) {
                List<UUID> toDelete = new ArrayList<>();

                for (Hivemind hive : manager.getAllHiveminds()) {
                    if (hive.getAllMembers().isEmpty()) {
                        toDelete.add(hive.getId());
                    }
                }

                for (UUID id : toDelete) {
                    manager.deleteHivemind(id);
                }
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Hivemind hive : hiveminds.values()) {
            CompoundTag hiveTag = new CompoundTag();
            hive.save(hiveTag);
            list.add(hiveTag);
        }
        tag.put("hiveminds", list);
        return tag;
    }

    public static HivemindManager load(CompoundTag tag) {
        HivemindManager data = new HivemindManager();
        ListTag list = tag.getList("hiveminds", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag hiveTag = list.getCompound(i);
            Hivemind hive = Hivemind.load(hiveTag);
            data.hiveminds.put(hive.getId(), hive);

            for (BlockPos pos : hive.getAllMembers()) {
                data.posToId.put(pos, hive.getId());
            }
        }
        return data;
    }
}
