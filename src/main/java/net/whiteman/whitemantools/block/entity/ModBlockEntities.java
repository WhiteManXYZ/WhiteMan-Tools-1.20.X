package net.whiteman.whitemantools.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WhiteManToolsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CrystalizingStationBlockEntity>> CRYSTALIZING_STATION_BE =
            BLOCK_ENTITIES.register("crystalizing_station_be", () ->
                    BlockEntityType.Builder.of(CrystalizingStationBlockEntity::new,
                            ModBlocks.CRYSTALIZING_STATION.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
