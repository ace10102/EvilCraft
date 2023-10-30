package evilcraft.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import evilcraft.block.SpiritFurnace;
import evilcraft.core.algorithm.Size;
import evilcraft.core.client.gui.container.GuiWorking;
import evilcraft.core.helper.L10NHelpers;
import evilcraft.inventory.container.ContainerSpiritFurnace;
import evilcraft.tileentity.TileSpiritFurnace;

/**
 * GUI for the {@link SpiritFurnace}.
 * @author rubensworks
 */
public class GuiSpiritFurnace extends GuiWorking<TileSpiritFurnace> {

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

    public static final int PROGRESS_INVALIDX = 192;
    public static final int PROGRESS_INVALIDY = 18;

    /**
     * Make a new instance.
     * @param inventory The inventory of the player.
     * @param tile The tile entity that calls the GUI.
     */
    public GuiSpiritFurnace(InventoryPlayer inventory, TileSpiritFurnace tile) {
        super(new ContainerSpiritFurnace(inventory, tile), tile);
        this.setTank(TANKWIDTH, TANKHEIGHT, TANKX, TANKY, TANKTARGETX, TANKTARGETY);
        this.setProgress(PROGRESSWIDTH, PROGRESSHEIGHT, PROGRESSX, PROGRESSY, PROGRESSTARGETX, PROGRESSTARGETY);
    }

    private String prettyPrintSize(Size size) {
        int[] c = size.getCoordinates();
        return c[0] + "x" + c[1] + "x" + c[2];
    }

    @Override
    protected void drawAdditionalForeground(int mouseX, int mouseY) {
        String prefix = SpiritFurnace.getInstance().getUnlocalizedName() + ".help.invalid";
        List<String> lines = new ArrayList<String>();
        lines.add(L10NHelpers.localize(prefix));
        if(tile.getEntity() == null) {
            lines.add(L10NHelpers.localize(prefix + ".noEntity"));
        } else if(!tile.isSizeValidForEntity()) {
            lines.add(L10NHelpers.localize(prefix + ".contentSize", prettyPrintSize(tile.getInnerSize())));
            lines.add(L10NHelpers.localize(prefix + ".requiredSize", prettyPrintSize(tile.getEntitySize())));
        } else if(tile.isForceHalt()) {
            lines.add(L10NHelpers.localize(prefix + ".forceHalt"));
        } else if(tile.isCaughtError()) {
            lines.add(L10NHelpers.localize(prefix + ".caughtError"));
        }
        if(lines.size() > 1) {
            this.drawTexturedModalRect(PROGRESSTARGETX + offsetX, PROGRESSTARGETY + offsetY, PROGRESS_INVALIDX, PROGRESS_INVALIDY, PROGRESSWIDTH, PROGRESSHEIGHT);
            if(isPointInRegion(PROGRESSTARGETX + offsetX, PROGRESSTARGETY + offsetY, PROGRESSWIDTH, PROGRESSHEIGHT, mouseX, mouseY)) {
                mouseX -= guiLeft;
                mouseY -= guiTop;
                drawTooltip(lines, mouseX, mouseY);
            }
        }
    }
}