package net.whiteman.biosanity.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.whiteman.biosanity.block.custom.PurificationStationBlock;
import net.whiteman.biosanity.block.entity.ModBlockEntities;
import net.whiteman.biosanity.item.ModItems;
import net.whiteman.biosanity.recipe.ModRecipes;
import net.whiteman.biosanity.recipe.purification_station.AbstractStationRecipe;
import net.whiteman.biosanity.recipe.purification_station.PaintingRecipe;
import net.whiteman.biosanity.recipe.purification_station.PurificationRecipe;
import net.whiteman.biosanity.screen.PurificationStationBlockMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.whiteman.biosanity.util.block.purification_station.ModifiersUtils.*;

public class PurificationStationBlockEntity extends BlockEntity implements MenuProvider {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FUEL_SLOT = 2;
    private static final int SECOND_INPUT_SLOT = 3;

    private static final int FUEL_CONVERSION_TIME = 50;
    public static final int PURIFICATION_TIME = 400;
    public static final int MAX_FUEL_COUNT = 20;
    public static final int MAX_MODIFIER_COUNT = 4;
    public static final int MAX_PRESSURE = 140;

    public static final Ingredient ALLOWED_FUEL = Ingredient.merge(Arrays.asList(
            Ingredient.of(Items.COAL),
            Ingredient.of(Items.CHARCOAL)
    ));
    public static final Ingredient ALLOWED_MODIFICATORS = Ingredient.merge(Arrays.asList(
            Ingredient.of(ModItems.SAND_DUST.get()),
            Ingredient.of(Tags.Items.DYES)
    ));

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
                case 2 -> ALLOWED_FUEL.test(stack);
                case 3 -> ALLOWED_MODIFICATORS.test(stack);
                default -> super.isItemValid(slot, stack);
            };
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;
    
    private int progress = 0;
    private int maxProgress = 200;
    private int fuel_conversion_progress = 0;
    private int fuel;
    private int pressure;
    private DyeColor currentModifierColor = DyeColor.WHITE;
    private int currentModifierColorToInt;

    private int modifier_amount;
    private ModifierType modifier_type = ModifierType.NONE;
    public record ModifierState(ModifierType type, int amount) {}

    public PurificationStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PURIFICATION_STATION_BE.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> PurificationStationBlockEntity.this.progress;
                    case 1 -> PurificationStationBlockEntity.this.fuel;
                    case 2 -> PurificationStationBlockEntity.this.pressure;
                    case 3 -> PurificationStationBlockEntity.this.modifier_amount;
                    case 4 -> PurificationStationBlockEntity.this.fuel_conversion_progress;
                    case 5 -> PurificationStationBlockEntity.this.getPurificationTime();
                    case 6 -> PurificationStationBlockEntity.this.modifier_type.ordinal();
                    case 7 -> packColor(convertToColors(currentModifierColor));
                    case 8 -> PurificationStationBlockEntity.this.currentModifierColor.ordinal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> PurificationStationBlockEntity.this.progress = value;
                    case 1 -> PurificationStationBlockEntity.this.fuel = value;
                    case 2 -> PurificationStationBlockEntity.this.pressure = value;
                    case 3 -> PurificationStationBlockEntity.this.modifier_amount = value;
                    case 4 -> PurificationStationBlockEntity.this.fuel_conversion_progress = value;
                    case 5 -> PurificationStationBlockEntity.this.maxProgress = value;
                    case 6 -> PurificationStationBlockEntity.this.modifier_type = ModifierType.values()[value];
                    // MAKE ID!!!!!!!!!11111
                    case 8 -> PurificationStationBlockEntity.this.currentModifierColor = DyeColor.values()[value];
                }
            }

            @Override
            public int getCount() {
                return 9;
            }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state, PurificationStationBlockEntity blockEntity) {
        if (level.isClientSide) return;
        boolean changed = false;

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
            changed = true;
        }
        // Modifier material intake
        if (blockEntity.modifier_amount <= 0 && blockEntity.checkInputModifierMaterial()) {
            blockEntity.modifier_type = determiteInputModifierType();
            blockEntity.modifier_amount = ModifierManager.getCapacity(blockEntity.modifier_type);
            blockEntity.currentModifierColor = ModifierManager.getColor(blockEntity.itemHandler.getStackInSlot(SECOND_INPUT_SLOT).getItem());
            blockEntity.itemHandler.extractItem(SECOND_INPUT_SLOT, 1, false);
            changed = true;
        }
        // Fuel to pressure conversion
        // If pressure limit is reached, we are disallowing fuel consumption
        if (blockEntity.fuel > 0 && blockEntity.pressure < MAX_PRESSURE) {
            ++blockEntity.fuel_conversion_progress;

            if (blockEntity.fuel_conversion_progress >= FUEL_CONVERSION_TIME) {
                --blockEntity.fuel;
                ++blockEntity.pressure;
                blockEntity.fuel_conversion_progress = 0;
            }
            changed = true;
        }
        // Crafting (purification or painting)
        if (blockEntity.hasRecipe() && blockEntity.pressure >= ModifierManager.getNecessaryPressure(blockEntity.modifier_type) && blockEntity.modifier_amount > 0) {
            if (blockEntity.maxProgress == 0) blockEntity.maxProgress = blockEntity.getPurificationTime();
            blockEntity.progress += 1;
            changed = true;

            if (blockEntity.progress >= blockEntity.maxProgress) {
                --blockEntity.modifier_amount;
                blockEntity.pressure -= ModifierManager.getNecessaryPressure(blockEntity.modifier_type);
                blockEntity.craftItem();
                blockEntity.maxProgress = 0;
                blockEntity.progress = 0;
            }
            setChanged(level, pos, state);
        } else {
            if (blockEntity.progress != 0 || blockEntity.maxProgress != 0) {
                blockEntity.progress = 0;
                blockEntity.maxProgress = 0;
                changed = true;
            }
        }
        if (changed) {
            setChanged(level, pos, state);
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
    
    public boolean checkInputFuel() {
        return ALLOWED_FUEL.test(this.itemHandler.getStackInSlot(FUEL_SLOT));
    }

    public boolean checkInputModifierMaterial() {
        return ALLOWED_MODIFICATORS.test(this.itemHandler.getStackInSlot(SECOND_INPUT_SLOT));
    }

    public int getPurificationTime() {
        Optional<? extends AbstractStationRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty() || getLevel() == null) {
            return 200; // Fallback
        }
        return recipeOptional.get().getTime();
    }

    private void craftItem() {
        Optional<? extends AbstractStationRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;

        ItemStack result = recipe.get().getResultItem(RegistryAccess.EMPTY);

        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe() {
        Optional<? extends AbstractStationRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty() || getLevel() == null) {
            return false;
        }

        AbstractStationRecipe recipe = recipeOptional.get();
        ItemStack result = recipeOptional.get().getResultItem(getLevel().registryAccess());
        ItemStack inputStack = this.itemHandler.getStackInSlot(0);

        // Input ingredient item check
        if (inputStack.getItem() == result.getItem()) {
            return false;
        }
        // Modifier type check
        if (recipe.getModifier() != this.modifier_type) {
            return false;
        }
        // Recipe & purifier dye color check
        if (recipe.getModifier() == ModifierType.DYE) {
            if (recipe.getColor() != this.currentModifierColor) {
                return false;
            }
        }
        // Amount and type item checks
        return canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
    }

    private Optional<? extends AbstractStationRecipe> getCurrentRecipe() {
        if (level == null) return Optional.empty();

        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        // Attempt to find purification recipe
        Optional<PurificationRecipe> purification = level.getRecipeManager()
                .getRecipeFor(ModRecipes.PURIFICATION_TYPE.get(), inventory, level);
        if (purification.isPresent()) return purification;

        // Otherwise attempting to painting recipe
        List<PaintingRecipe> allPaintingRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.PAINTING_TYPE.get());

        return allPaintingRecipes.stream()
                .filter(recipe -> recipe.matches(inventory, level))
                .filter(recipe -> {
                    if (this.modifier_type == ModifierType.DYE) {
                        return recipe.getColor() == this.currentModifierColor;
                    }
                    return true;
                })
                .findFirst();
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private ModifierType determiteInputModifierType() {
        ItemStack stack = this.itemHandler.getStackInSlot(SECOND_INPUT_SLOT);
        if (stack.isEmpty()) return ModifierType.NONE;

        if (stack.is(ModItems.SAND_DUST.get())) return ModifierType.SAND_DUST;
        if (stack.is(Tags.Items.DYES)) return ModifierType.DYE;

        return ModifierType.NONE;
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
        tag.putInt("purification_station_block.modifier_amount", modifier_amount);
        tag.putString("ModifierType", this.modifier_type.name());
        tag.putString("ModifierColor", this.currentModifierColor.name());
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
        modifier_amount = tag.getInt("purification_station_block.modifier_amount");
        if (tag.contains("ModifierType", Tag.TAG_STRING)) {
            try {
                this.modifier_type = ModifierType.valueOf(tag.getString("ModifierType"));
            } catch (IllegalArgumentException e) {
                this.modifier_type = ModifierType.NONE; // Fallback
            }
        }
        if (tag.contains("ModifierColor", Tag.TAG_STRING)) {
            try {
                this.currentModifierColor = DyeColor.valueOf(tag.getString("ModifierColor"));
            } catch (IllegalArgumentException e) {
                this.currentModifierColor = DyeColor.WHITE; // Fallback
            }
        }
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
