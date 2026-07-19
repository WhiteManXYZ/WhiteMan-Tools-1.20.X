package net.whiteman.biosanity.world.neoplasm.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.neoplasm.common.INeoplasmNode;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class NeoplasmCoreBlock extends BaseEntityBlock implements INeoplasmNode {
    public NeoplasmCoreBlock(Properties pProperties) { super(pProperties); }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new NeoplasmCoreBE(pPos, pState);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override public boolean isCore() {
        return true;
    }

    @Override
    public void onPlace(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof NeoplasmCoreBE core) {
                HivemindManager manager = HivemindManager.get(level);

                List<BlockPos> neighbors = core.findNeighborCores();

                UUID id;
                if (neighbors != null) {
                    id = manager.joinOrCreateHivemind(pos, neighbors);
                } else {
                   id = manager.createHivemind(pos);
                }

                core.setHivemindId(id);
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof NeoplasmCoreBE core) {
                HivemindManager data = HivemindManager.get(level);
                Hivemind hive = data.getHivemindById(core.getHivemindId());

                if (hive != null) {
                    // Remove member
                    hive.removeMember(pos);
                    hive.updateActiveMembers(level);

                    // Remove hivemind if empty
                    if (hive.getAllMembers().isEmpty()) {
                        data.deleteHivemind(hive.getId());
                    }

                    data.setDirty();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.NEOPLASM_CORE_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1, pBlockEntity));
    }
}
