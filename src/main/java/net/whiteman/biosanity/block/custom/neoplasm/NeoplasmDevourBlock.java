package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.whiteman.biosanity.block.entity.NeoplasmDevourBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class NeoplasmDevourBlock extends NeoplasmBlock implements EntityBlock {
    public static final EnumProperty<NeoplasmResourceType> TYPE = EnumProperty.create("type", NeoplasmResourceType.class);

    public NeoplasmDevourBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, NeoplasmResourceType.NONE));
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeoplasmDevourBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        // Test feature
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof NeoplasmDevourBlockEntity devourBE) {
                BlockState original = devourBE.getOriginalState();

                if (!original.isAir()) {
                    if (level.random.nextFloat() < 0.5f) {
                        Block.popResource(level, pos, new ItemStack(original.getBlock().asItem()));
                    } else {
                        System.out.println("Drop biomass");
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
