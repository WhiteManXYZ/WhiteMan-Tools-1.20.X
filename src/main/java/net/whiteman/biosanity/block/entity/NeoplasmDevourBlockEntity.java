package net.whiteman.biosanity.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NeoplasmDevourBlockEntity extends BlockEntity {
    private BlockState originalState = Blocks.AIR.defaultBlockState();

    public NeoplasmDevourBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEOPLASM_DEVOUR_BE.get(), pos, state);
    }

    public void setOriginalState(BlockState state) {
        this.originalState = state;
        setChanged();
    }

    public BlockState getOriginalState() {
        return this.originalState;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("OriginalBlock", NbtUtils.writeBlockState(originalState));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OriginalBlock", 10)) { // 10 - compound tag
            HolderGetter<Block> holdergetter = this.level != null ?
                    this.level.holderLookup(Registries.BLOCK) :
                    BuiltInRegistries.BLOCK.asLookup();

            this.originalState = NbtUtils.readBlockState(holdergetter, tag.getCompound("OriginalBlock"));
        }
    }
}