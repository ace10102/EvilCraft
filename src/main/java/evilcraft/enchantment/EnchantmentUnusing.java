package evilcraft.enchantment;

import evilcraft.core.config.configurable.ConfigurableEnchantment;
import evilcraft.core.config.extendedconfig.EnchantmentConfig;
import evilcraft.core.config.extendedconfig.ExtendedConfig;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

/**
 * Enchantment that stop your tool from being usable when on low durability.
 * @author rubensworks
 * @Maintainer Spoilers
 */
public class EnchantmentUnusing extends ConfigurableEnchantment {

    private static EnchantmentUnusing _instance = null;

    /**
     * Initialize the configurable.
     * @param eConfig The config.
     */
    public static void initInstance(ExtendedConfig<EnchantmentConfig> eConfig) {
        if(_instance == null)
            _instance = new EnchantmentUnusing(eConfig);
        else
            eConfig.showDoubleInitError();
    }

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static EnchantmentUnusing getInstance() {
        return _instance;
    }

    private EnchantmentUnusing(ExtendedConfig<EnchantmentConfig> eConfig) {
        super(eConfig, 1, EnumEnchantmentType.all);
    }

    @Override
    public int getMinEnchantability(int par1) {
        return 10;
    }

    @Override
    public int getMaxEnchantability(int par1) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canApply(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem().isItemTool(itemStack) && !itemStack.getHasSubtypes();
    }

    /**
     * Check if the given item can be used.
     * @param itemStack The {@link ItemStack} that will be unused.
     * @return If the item can be used.
     */
    public static boolean unuseTool(ItemStack itemStack) {
        int damageBorder = itemStack.getMaxDamage() - 5;
        if(!itemStack.getHasSubtypes() && itemStack.getItemDamage() >= damageBorder) {
            itemStack.setItemDamage(damageBorder);
            return true;
        }
        return false;
    }
}