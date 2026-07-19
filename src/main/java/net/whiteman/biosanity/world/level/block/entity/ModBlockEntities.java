package net.whiteman.biosanity.world.level.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;
import net.whiteman.biosanity.world.neoplasm.rot.NeoplasmRotBE;
import net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBE;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BiosanityMod.MOD_ID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<PurificationStationBE>> PURIFICATION_STATION_BE =
            BLOCK_ENTITIES.register("purification_station_be", () ->
                    BlockEntityType.Builder.of(PurificationStationBE::new,
                            ModBlocks.PURIFICATION_STATION_BLOCK.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<NeoplasmRotBE>> NEOPLASM_ROT_BE =
            BLOCK_ENTITIES.register("neoplasm_rot_be", () ->
                    BlockEntityType.Builder.of(NeoplasmRotBE::new,
                            ModBlocks.NEOPLASM_ROT_BLOCK.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<NeoplasmCoreBE>> NEOPLASM_CORE_BE =
            BLOCK_ENTITIES.register("neoplasm_core_be", () ->
                    BlockEntityType.Builder.of(NeoplasmCoreBE::new,
                            ModBlocks.NEOPLASM_CORE_BLOCK.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<NeoplasmVeinBE>> NEOPLASM_VEIN_BE =
            BLOCK_ENTITIES.register("neoplasm_vein_be", () ->
                    BlockEntityType.Builder.of(NeoplasmVeinBE::new,
                            ModBlocks.NEOPLASM_VEIN_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
