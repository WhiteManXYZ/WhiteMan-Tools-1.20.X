package net.whiteman.biosanity.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.whiteman.biosanity.world.item.ModItems;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.level.block.entity.PurificationStationBE;
import net.whiteman.biosanity.world.util.ColoredItemsRegistry;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.world.util.ModifierUtils.ModifierType;

public class PurificationStationMenu extends AbstractContainerMenu {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int MODIFIER_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int SLOT_COUNT = 4;
    public final PurificationStationBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public PurificationStationMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, (extraData != null) ? inv.player.level().getBlockEntity(extraData.readBlockPos()) : null, new SimpleContainerData(9));
    }

    public PurificationStationMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.PURIFICATION_STATION_BLOCK_MENU.get(), pContainerId);
        checkContainerSize(inv, SLOT_COUNT);
        if (entity instanceof PurificationStationBE be) {
            this.blockEntity = be;
        } else {
            this.blockEntity = null;
        }
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        if (this.blockEntity != null) {
            this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                this.addSlot(new SlotItemHandler(iItemHandler, 0, 71, 40));
                this.addSlot(new SlotItemHandler(iItemHandler, 1, 17, 43));
                this.addSlot(new SlotItemHandler(iItemHandler, 2, 17, 17));
                this.addSlot(new SlotItemHandler(iItemHandler, 3, 127, 40));
            });
        } else {
            // CLIENT STUB: If BE is null, create empty slots
            ItemStackHandler dummy = new ItemStackHandler(4);
            this.addSlot(new SlotItemHandler(dummy, 0, 71, 40));
            this.addSlot(new SlotItemHandler(dummy, 1, 17, 43));
            this.addSlot(new SlotItemHandler(dummy, 2, 17, 17));
            this.addSlot(new SlotItemHandler(dummy, 3, 127, 40));
        }

        addDataSlots(data);
    }

    // I rechecked this and in my case I want use vanilla system, for parity (im obsessed with this lol).
    // So I took the original code and rewrote it to suit my needs.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 26 = player inventory slots (which map to the InventoryPlayer slot numbers 0 - 26)
    //  27 - 35 = hotbar slots (which will map to the InventoryPlayer slot numbers 27 - 35)
    //  36 - 39 = TileInventory slots, which map to our TileEntity slot numbers 36 - 39)
    public static final int INVENTORY_SLOT_COUNT = 36;
    public static final int INGREDIENT_SLOT_IN_ORDER = INVENTORY_SLOT_COUNT + INGREDIENT_SLOT;
    public static final int FUEL_SLOT_IN_ORDER = INVENTORY_SLOT_COUNT + FUEL_SLOT;
    public static final int MODIFIER_SLOT_IN_ORDER = INVENTORY_SLOT_COUNT + MODIFIER_SLOT;
    public static final int RESULT_SLOT_IN_ORDER = INVENTORY_SLOT_COUNT + RESULT_SLOT;
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int clickedSlotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(clickedSlotIndex);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (clickedSlotIndex == RESULT_SLOT_IN_ORDER) {
                if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (clickedSlotIndex != INGREDIENT_SLOT_IN_ORDER && clickedSlotIndex != FUEL_SLOT_IN_ORDER & clickedSlotIndex != MODIFIER_SLOT_IN_ORDER) {
                if (this.canProcess(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 36, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isFuel(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 37, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isModifier(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 38, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                    // Clicked in inventory case
                } else if (clickedSlotIndex < 27) {
                    if (!this.moveItemStackTo(itemstack1, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                    // Clicked in hotbar case
                } else if (clickedSlotIndex < 36 && !this.moveItemStackTo(itemstack1, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
                // Move to inventory/hotbar (bc we clicked in our TileEntity menu)
            } else if (!this.moveItemStackTo(itemstack1, 0, 35, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        if (pPlayer.isSpectator()) return true;

        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.PURIFICATION_STATION_BLOCK.get());
    }

    private boolean canProcess(ItemStack pStack) {
        return pStack.is(ModItems.ALGANIT.get()) || ColoredItemsRegistry.isPaintable(pStack);
    }

    private boolean isFuel(ItemStack pStack) {
        return PurificationStationBE.ALLOWED_FUEL.test(pStack);
    }

    private boolean isModifier(ItemStack pStack) {
        return PurificationStationBE.ALLOWED_MODIFICATORS.test(pStack);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getFuel() {
        return this.data.get(1);
    }

    public int getPressure() {return this.data.get(2); }

    public int getModifierMaterialAmount() {
        return this.data.get(3);
    }

    public int getFuelConversionProgress() {
        return this.data.get(4);
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(5);
        int progressArrowSize = 24;

        return progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public ModifierType getModifierType() {
        return ModifierType.values()[this.data.get(6)];
    }

    public DyeColor getDye() {
        return DyeColor.values()[this.data.get(8)];
    }

    public int getModifierColor() {
        return this.data.get(7);
    }
}
