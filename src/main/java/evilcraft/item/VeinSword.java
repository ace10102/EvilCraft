package evilcraft.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import evilcraft.core.config.configurable.ConfigurableItemSword;
import evilcraft.core.config.extendedconfig.ExtendedConfig;
import evilcraft.core.config.extendedconfig.ItemConfig;
import evilcraft.core.helper.EnchantmentHelpers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * A strong sword that extracts blood.
 * @author rubensworks
 */
public class VeinSword extends ConfigurableItemSword {

    private static VeinSword _instance = null;

    /**
     * The looting level of this sword.
     */
    public static final int LOOTING_LEVEL = 2;

    /**
     * Initialize the configurable.
     * @param eConfig The config.
     */
    public static void initInstance(ExtendedConfig<ItemConfig> eConfig) {
        if(_instance == null)
            _instance = new VeinSword(eConfig);
        else
            eConfig.showDoubleInitError();
    }

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static VeinSword getInstance() {
        return _instance;
    }

    private VeinSword(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig, Item.ToolMaterial.GOLD);
        this.setMaxDamage(VeinSwordConfig.durability);
    }

    /**
     * Get the crafting result of this sword. It has looting 2 by default.
     * @return The sword with enchantment.
     */
    public static ItemStack createCraftingResult() {
        ItemStack sword = new ItemStack(VeinSword.getInstance());
        EnchantmentHelpers.setEnchantmentLevel(sword, Enchantment.looting, LOOTING_LEVEL);
        return sword;
    }

    @Override @SideOnly(Side.CLIENT) @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List itemList) {
        itemList.add(createCraftingResult());
    }
}