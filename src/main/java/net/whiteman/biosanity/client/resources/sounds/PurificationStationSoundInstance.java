package net.whiteman.biosanity.client.resources.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.whiteman.biosanity.world.level.block.entity.PurificationStationBE;

@OnlyIn(Dist.CLIENT)
public class PurificationStationSoundInstance extends AbstractTickableSoundInstance {
    private final PurificationStationBE blockEntity;

    public PurificationStationSoundInstance(PurificationStationBE tile, SoundEvent sound) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.blockEntity = tile;
        this.x = tile.getBlockPos().getX() + 0.5f;
        this.y = tile.getBlockPos().getY() + 0.5f;
        this.z = tile.getBlockPos().getZ() + 0.5f;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.15f;
    }

    @Override
    public void tick() {
        if (this.blockEntity.isRemoved()) {
            this.stop();
            return;
        }

        boolean isWorking = blockEntity.isConverting();

        float fadeVelocity = 0.15f;
        if (isWorking) {
            this.volume = Math.min(1.0f, this.volume + fadeVelocity);
        } else {
            this.volume = Math.max(0.0f, this.volume - fadeVelocity);

            if (this.volume <= 0.0f) {
                this.stop();
            }
        }
    }
}