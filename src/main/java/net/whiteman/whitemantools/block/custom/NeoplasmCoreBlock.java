package net.whiteman.whitemantools.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class NeoplasmCoreBlock extends NeoplasmBlock {
    public NeoplasmCoreBlock(Properties pProperties) { super(pProperties); }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.tick(pState, pLevel, pPos, pRandom);
        spreadInfection(pState, pLevel, pPos, pRandom);
    }

    // For future features...
    protected void spreadInfection(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {}
}
