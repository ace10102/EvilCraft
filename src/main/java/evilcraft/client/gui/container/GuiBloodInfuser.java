package evilcraft.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import evilcraft.block.BloodInfuser;
import evilcraft.core.client.gui.container.GuiWorking;
import evilcraft.inventory.container.ContainerBloodInfuser;
import evilcraft.tileentity.TileBloodInfuser;

/**
 * GUI for the {@link BloodInfuser}.
 * @author rubensworks
 */
public class GuiBloodInfuser extends GuiWorking<TileBloodInfuser> {

    public static final int TEXTUREWIDTH = 176;
    public static final int TEXTUREHEIGHT = 166;

    public static final int TANKWIDTH = 16;
    public static final int TANKHEIGHT = 58;
    public static final int TANKX = TEXTUREWIDTH;
    public static final int TANKY = 0;
    public static final int TANKTARGETX = 43;
    public static final int TANKTARGETY = 72;

    public static final int PROGRESSWIDTH = 24;
    public static final int PROGRESSHEIGHT = 16;
    public static final int PROGRESSX = 192;
    public static final int PROGRESSY = 0;
    public static final int PROGRESSTARGETX = 102;
    public static final int PROGRESSTARGETY = 36;

    /**
     * Make a new instance.
     * @param inventory The inventory of the player.
     * @param tile The tile entity that calls the GUI.
     */
    public GuiBloodInfuser(InventoryPlayer inventory, TileBloodInfuser tile) {
        super(new ContainerBloodInfuser(inventory, tile), tile);
        this.setTank(TANKWIDTH, TANKHEIGHT, TANKX, TANKY, TANKTARGETX, TANKTARGETY);
        this.setProgress(PROGRESSWIDTH, PROGRESSHEIGHT, PROGRESSX, PROGRESSY, PROGRESSTARGETX, PROGRESSTARGETY);
    }
}