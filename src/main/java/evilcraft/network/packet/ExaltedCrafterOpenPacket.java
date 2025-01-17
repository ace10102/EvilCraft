package evilcraft.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import evilcraft.item.ExaltedCrafter;
import evilcraft.network.CodecField;
import evilcraft.network.PacketCodec;

/**
 * Packet for clearing the exalted crafting grid.
 * @author rubensworks
 */
public class ExaltedCrafterOpenPacket extends PacketCodec {

    @CodecField
    private int itemIndex = -1;

    /**
     * Make a new instance.
     */
    public ExaltedCrafterOpenPacket() {
    }

    /**
     * Make a new instance.
     * @param itemIndex The index of the crafter in the player inventory.
     */
    public ExaltedCrafterOpenPacket(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    @Override @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if(itemIndex >= 0) {
            ItemStack found = player.inventory.mainInventory[itemIndex];
            if(found != null && found.getItem() == ExaltedCrafter.getInstance()) {
                ExaltedCrafter.getInstance().openGuiForItemIndex(world, player, itemIndex);
            }
        }
    }
}