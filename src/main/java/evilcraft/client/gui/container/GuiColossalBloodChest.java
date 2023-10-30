package evilcraft.client.gui.container;

import evilcraft.core.client.gui.container.GuiWorking;
import evilcraft.inventory.container.ContainerColossalBloodChest;
import evilcraft.tileentity.TileColossalBloodChest;
import net.minecraft.entity.player.InventoryPlayer;

/**
 * GUI for the {@link evilcraft.block.ColossalBloodChest}.
 * @author rubensworks
 */
public class GuiColossalBloodChest extends GuiWorking<TileColossalBloodChest> {

    public static final int TEXTUREWIDTH = 236;
    public static final int TEXTUREHEIGHT = 189;

    public static final int TANKWIDTH = 16;
    public static final int TANKHEIGHT = 58;
    public static final int TANKX = TEXTUREWIDTH;
    public static final int TANKY = 0;
    public static final int TANKTARGETX = 28;
    public static final int TANKTARGETY = 82;

    public static final int EFFICIENCYBARWIDTH = 2;
    public static final int EFFICIENCYBARHEIGHT = 58;
    public static final int EFFICIENCYBARX = TEXTUREWIDTH;
    public static final int EFFICIENCYBARY = 58;
    public static final int EFFICIENCYBARTARGETX = 46;
    public static final int EFFICIENCYBARTARGETY = 82;

    /**
     * Make a new instance.
     * @param inventory The inventory of the player.
     * @param tile The tile entity that calls the GUI.
     */
    public GuiColossalBloodChest(InventoryPlayer inventory, TileColossalBloodChest tile) {
        super(new ContainerColossalBloodChest(inventory, tile), tile);
        this.setTank(TANKWIDTH, TANKHEIGHT, TANKX, TANKY, TANKTARGETX, TANKTARGETY);
    }

    @Override
    protected int getBaseYSize() {
        return TEXTUREHEIGHT;
    }

    @Override
    protected int getBaseXSize() {
        return TEXTUREWIDTH;
    }

    @Override
    protected void drawForgegroundString() {
        fontRendererObj.drawString(tile.getInventoryName(), 8 + offsetX, 4 + offsetY, 4210752);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        this.mc.renderEngine.bindTexture(texture);
        int minusFactor = (int)(((float)(TileColossalBloodChest.MAX_EFFICIENCY - tile.getEfficiency()) * EFFICIENCYBARHEIGHT) / TileColossalBloodChest.MAX_EFFICIENCY);
        this.drawTexturedModalRect(EFFICIENCYBARTARGETX + offsetX, EFFICIENCYBARTARGETY - EFFICIENCYBARHEIGHT + minusFactor,
                EFFICIENCYBARX, EFFICIENCYBARY + minusFactor, EFFICIENCYBARWIDTH, EFFICIENCYBARHEIGHT - minusFactor);
    }
}