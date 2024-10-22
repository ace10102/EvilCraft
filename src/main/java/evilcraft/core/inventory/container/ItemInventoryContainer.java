package evilcraft.core.inventory.container;

import evilcraft.core.helper.InventoryHelpers;
import evilcraft.core.inventory.IGuiContainerProvider;
import evilcraft.core.inventory.slot.SlotLocked;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * A container for an item.
 * @author rubensworks
 * @Maintainer Spoilers
 * @param <I> The item instance.
 */
public abstract class ItemInventoryContainer<I extends Item & IGuiContainerProvider> extends ExtendedInventoryContainer {

    protected I item;
    protected int itemIndex;
    protected ItemStack host;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param item The item.
     * @param itemIndex The index of the item in use inside the player inventory.
     */
    public ItemInventoryContainer(InventoryPlayer inventory, I item, int itemIndex) {
        super(inventory, item);
        this.item = item;
        this.itemIndex = itemIndex;
        this.host = inventory.mainInventory[itemIndex];
    }

    /**
     * Get the item instance.
     * @return The item.
     */
    public I getItem() {
        return item;
    }

    /**
     * Can the player use this item
     * @param player The player
     * @return true if player designated item is equal to container item
     */
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        ItemStack item = getItemStack(player);
        return item != null && item.getItem() == getItem();
    }

    /**
     * Get the internal reference stack
     * @param player The player
     * @return the item in the slot of the stored index
     */
    public ItemStack getItemStack(EntityPlayer player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex);
    }

    /**
     * Override createNewSlot behavior to correctly handle slot behavior dependant on contents
     */
    @Override
    protected Slot createNewSlot(IInventory inventory, int index, int x, int y) {
        ItemStack test = inventory.getStackInSlot(index) != null ? inventory.getStackInSlot(index) : null;

        if(!(inventory instanceof InventoryPlayer) && !(inventory instanceof InventoryEnderChest)) {
            return new Slot(inventory, index, x, y) {

                @Override
                public boolean isItemValid(ItemStack attemptedStack) {
                    return attemptedStack.getItem() != item;
                }
            };
        }
        return test != null && test == host ? new SlotLocked(inventory, index, x, y) : new Slot(inventory, index, x, y);
    }

    /**
     * Override slotClick behavior to correctly handle quick-swap access security
     */
    @Override
    public ItemStack slotClick(int slotId, int keyOrdinal, int clickType, EntityPlayer player) {
        if(clickType == 2 && keyOrdinal >= 0 && keyOrdinal < 9) {
            int hotbarSlotIndex = this.inventorySlots.size() - (9 - keyOrdinal);
            Slot hotbarTargetSlot = getSlot(hotbarSlotIndex);
            Slot hoverSlot = getSlot(slotId);
            if(hotbarTargetSlot instanceof SlotLocked || hoverSlot instanceof SlotLocked) {
                return null;
            }
        }
        return super.slotClick(slotId, keyOrdinal, clickType, player);
    }
}