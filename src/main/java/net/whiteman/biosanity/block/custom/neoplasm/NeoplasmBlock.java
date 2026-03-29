package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class NeoplasmBlock extends Block {
    public NeoplasmBlock(Properties pProperties) { super(pProperties
            .friction(0.8F)
            .ignitedByLava()
            .sound(SoundType.SLIME_BLOCK)
            .mapColor(MapColor.TERRACOTTA_RED));
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 2;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 1;
    }

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance) {
        // Due to the fleshy origin of the neoplasm
        // we reduce for a little fall damage
        super.fallOn(level, state, pos, entity, fallDistance * 0.85F);
    }
}
