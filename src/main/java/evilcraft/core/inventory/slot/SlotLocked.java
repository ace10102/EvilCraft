package evilcraft.core.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * A secure container slot that cannot be interacted with.
 * @author Spoilers
 */
public class SlotLocked extends Slot {

    public SlotLocked(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return false;
    }

    @Override
    public boolean getHasStack() {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int i) {
        return null;
    }
}