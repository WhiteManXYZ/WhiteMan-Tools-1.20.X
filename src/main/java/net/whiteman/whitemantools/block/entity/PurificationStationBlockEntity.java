package net.whiteman.whitemantools.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.whiteman.whitemantools.block.custom.PurificationStationBlock;
import net.whiteman.whitemantools.item.ModItems;
import net.whiteman.whitemantools.recipe.PurificationStationRecipe;
import net.whiteman.whitemantools.screen.PurificationStationBlockMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PurificationStationBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> true;
                case 1 -> false;
                case 2 -> stack.getItem() == Items.COAL;
                case 3 -> stack.getItem() == ModItems.SAND_DUST.get();
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FUEL_SLOT = 2;
    private static final int SECONDARY_INPUT_SLOT = 3;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int purificationTime = 200;
    private int fuel;
    private int pressure;
    private int pressureLimit;
    private int modifier_material;

    public PurificationStationBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.PURIFICATION_STATION_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> PurificationStationBlockEntity.this.progress;
                    case 1 -> PurificationStationBlockEntity.this.purificationTime;
                    case 2 -> PurificationStationBlockEntity.this.fuel;
                    case 3 -> PurificationStationBlockEntity.this.pressure;
                    case 4 -> PurificationStationBlockEntity.this.modifier_material;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> PurificationStationBlockEntity.this.progress = pValue;
                    case 1 -> PurificationStationBlockEntity.this.purificationTime = pValue;
                    case 2 -> PurificationStationBlockEntity.this.fuel = pValue;
                    case 3 -> PurificationStationBlockEntity.this.pressure = pValue;
                    case 4 -> PurificationStationBlockEntity.this.modifier_material = pValue;
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.whiteman_tools.purification_station_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PurificationStationBlockMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("purification_station_block.progress", progress);
        pTag.putInt("purification_station_block.fuel", fuel);
        pTag.putInt("purification_station_block.pressure", pressure);
        pTag.putInt("purification_station_block.modifier_material", modifier_material);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("purification_station_block.progress");
        fuel = pTag.getInt("purification_station_block.fuel");
        pressure = pTag.getInt("purification_station_block.pressure");
        modifier_material = pTag.getInt("purification_station_block.modifier_material");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState, PurificationStationBlockEntity pBlockEntity) {
        if (this.fuel > 0) {
            pState = pState.setValue(PurificationStationBlock.LIT, Boolean.TRUE);
            pLevel.setBlock(pPos, pState, 3);
        } else {
            pState = pState.setValue(PurificationStationBlock.LIT, Boolean.FALSE);
            pLevel.setBlock(pPos, pState, 3);
        }

        if (noFuel(pBlockEntity)) {
            setFuel(pBlockEntity);
            setChanged(pLevel, pPos, pState);
        }
        if (hasRecipe() && pBlockEntity.fuel > 0) {
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                --pBlockEntity.fuel;
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private boolean noFuel(PurificationStationBlockEntity pBlockEntity) {
        return pBlockEntity.fuel <= 0 && this.itemHandler.getStackInSlot(FUEL_SLOT).is(Items.COAL);
    }

    private void setFuel(PurificationStationBlockEntity pBlockEntity) {
        pBlockEntity.fuel = 20;
        this.itemHandler.extractItem(FUEL_SLOT, 1, false);
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        Optional<PurificationStationRecipe> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().getResultItem(null);

        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasProgressFinished() {
        return progress >= purificationTime;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        Optional<PurificationStationRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty()) {
            return false;
        }
        ItemStack result = recipe.get().getResultItem(getLevel().registryAccess());

        return canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
    }

    private Optional<PurificationStationRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        return this.level.getRecipeManager().getRecipeFor(PurificationStationRecipe.Type.INSTANCE, inventory, level);
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }
}
