package net.whiteman.biosanity.client.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BiosanityMod.MOD_ID);

    public static final RegistryObject<SoundEvent> COMPRESSOR_WORK = SOUND_EVENTS.register("compressor_work",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BiosanityMod.MOD_ID, "compressor_work")));
}