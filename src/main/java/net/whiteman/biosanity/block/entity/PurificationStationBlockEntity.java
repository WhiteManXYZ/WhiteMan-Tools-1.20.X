package net.whiteman.biosanity.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
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
import net.whiteman.biosanity.block.custom.PurificationStationBlock;
import net.whiteman.biosanity.item.ModItems;
import net.whiteman.biosanity.recipe.PurificationStationRecipe;
import net.whiteman.biosanity.screen.PurificationStationBlockMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PurificationStationBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PURIFICATION_TIME = 400;
    public static final int MAX_FUEL_COUNT = 20;
    public static final int MAX_MODIFIER_COUNT = 4;
    
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FUEL_SLOT = 2;
    private static final int SECOND_INPUT_SLOT = 3;
    private static final int NECESSARY_PRESSURE_FOR_ITEM = 30;
    private static final int FUEL_CONVERSION_TIME = 50;
    private static final int MAX_PRESSURE = 140;
    
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> true;
                case 1 -> false;
                case 2 -> stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
                case 3 -> stack.is(ModItems.SAND_DUST.get());
                default -> super.isItemValid(slot, stack);
            };
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;
    
    private int progress = 0;
    private int fuel_conversion_progress = 0;
    private int fuel;
    private int modifier_material;
    private int pressure;

    public PurificationStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PURIFICATION_STATION_BE.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> PurificationStationBlockEntity.this.progress;
                    case 1 -> PurificationStationBlockEntity.this.fuel;
                    case 2 -> PurificationStationBlockEntity.this.pressure;
                    case 3 -> PurificationStationBlockEntity.this.modifier_material;
                    case 4 -> PurificationStationBlockEntity.this.fuel_conversion_progress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> PurificationStationBlockEntity.this.progress = value;
                    case 1 -> PurificationStationBlockEntity.this.fuel = value;
                    case 2 -> PurificationStationBlockEntity.this.pressure = value;
                    case 3 -> PurificationStationBlockEntity.this.modifier_material = value;
                    case 4 -> PurificationStationBlockEntity.this.fuel_conversion_progress = value;
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
    }
    

    public void tick(Level level, BlockPos pos, BlockState state, PurificationStationBlockEntity blockEntity) {
        // Lit condition
        if (blockEntity.fuel > 0) {
            state = state.setValue(PurificationStationBlock.LIT, Boolean.TRUE);
            level.setBlock(pos, state, 3);
        } else {
            state = state.setValue(PurificationStationBlock.LIT, Boolean.FALSE);
            level.setBlock(pos, state, 3);
        }
        // Fuel intake
        if (blockEntity.fuel <= 0 && blockEntity.checkInputFuel()) {
            blockEntity.fuel = MAX_FUEL_COUNT;
            blockEntity.itemHandler.extractItem(FUEL_SLOT, 1, false);
            setChanged(level, pos, state);
        }
        // Modifier material intake
        if (blockEntity.modifier_material <= 0 && blockEntity.checkInputModifierMaterial()) {
            blockEntity.modifier_material = MAX_MODIFIER_COUNT;
            blockEntity.itemHandler.extractItem(SECOND_INPUT_SLOT, 1, false);
            setChanged(level, pos, state);
        }
        // Fuel to pressure conversion
        // If pressure limit is reached, we are disallowing fuel consumption
        if (blockEntity.fuel > 0 && blockEntity.pressure < MAX_PRESSURE) {
            ++blockEntity.fuel_conversion_progress;
            setChanged(level, pos, state);

            if (blockEntity.fuel_conversion_progress >= FUEL_CONVERSION_TIME) {
                --blockEntity.fuel;
                ++blockEntity.pressure;
                blockEntity.fuel_conversion_progress = 0;
            }
        }
        // Crafting (purification)
        if (blockEntity.hasRecipe() && blockEntity.pressure > NECESSARY_PRESSURE_FOR_ITEM && blockEntity.modifier_material > 0) {
            blockEntity.progress += 1;
            setChanged(level, pos, state);

            if (blockEntity.progress >= PURIFICATION_TIME) {
                --blockEntity.modifier_material;
                blockEntity.pressure -= NECESSARY_PRESSURE_FOR_ITEM;
                blockEntity.craftItem();
                blockEntity.progress = 0;
            }
        } else {
            blockEntity.progress = 0;
        }
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        if (level != null)
            Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    
    private boolean checkInputFuel() {
        return this.itemHandler.getStackInSlot(FUEL_SLOT).is(Items.COAL) || this.itemHandler.getStackInSlot(FUEL_SLOT).is(Items.CHARCOAL);
    }

    private boolean checkInputModifierMaterial() {
        return this.itemHandler.getStackInSlot(SECOND_INPUT_SLOT).is(ModItems.SAND_DUST.get());
    }

    private void craftItem() {
        Optional<PurificationStationRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;

        ItemStack result = recipe.get().getResultItem(RegistryAccess.EMPTY);

        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe() {
        Optional<PurificationStationRecipe> recipe = getCurrentRecipe();

        if (recipe.isEmpty() || getLevel() == null) {
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

        return level != null ? level.getRecipeManager().getRecipeFor(PurificationStationRecipe.Type.INSTANCE, inventory, level) : Optional.empty();
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.biosanity.purification_station_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new PurificationStationBlockMenu(containerId, playerInventory, this, this.data);
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("purification_station_block.progress", progress);
        tag.putInt("purification_station_block.fuel_conversion_progress", fuel_conversion_progress);
        tag.putInt("purification_station_block.fuel", fuel);
        tag.putInt("purification_station_block.pressure", pressure);
        tag.putInt("purification_station_block.modifier_material", modifier_material);
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("purification_station_block.progress");
        fuel_conversion_progress = tag.getInt("purification_station_block.fuel_conversion_progress");
        fuel = tag.getInt("purification_station_block.fuel");
        pressure = tag.getInt("purification_station_block.pressure");
        modifier_material = tag.getInt("purification_station_block.modifier_material");
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
}
