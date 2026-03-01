package net.whiteman.biosanity.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BiosanityMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<PurificationStationBlockEntity>> PURIFICATION_STATION_BE =
            BLOCK_ENTITIES.register("purification_station_be", () ->
                    BlockEntityType.Builder.of(PurificationStationBlockEntity::new,
                            ModBlocks.PURIFICATION_STATION_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<NeoplasmDevourBlockEntity>> NEOPLASM_DEVOUR_BE =
            BLOCK_ENTITIES.register("neoplasm_devour_be", () ->
                    BlockEntityType.Builder.of(NeoplasmDevourBlockEntity::new,
                            ModBlocks.NEOPLASM_DEVOUR_BLOCK.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
