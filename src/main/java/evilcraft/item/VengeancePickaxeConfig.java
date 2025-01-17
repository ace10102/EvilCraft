package evilcraft.item;

import evilcraft.core.config.ConfigurableProperty;
import evilcraft.core.config.ConfigurableTypeCategory;
import evilcraft.core.config.extendedconfig.ItemConfig;

/**
 * Config for the {@link VengeancePickaxe}.
 * @author rubensworks
 */
public class VengeancePickaxeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static VengeancePickaxeConfig _instance;

    /**
     * The area of effect in blocks in which this tool could enable vengeance spirits.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "The area of effect in blocks in which this tool could enable vengeance spirits.", isCommandable = true)
    public static int areaOfEffect = 5;

    /**
     * The ^-1 chance for which vengeance spirits could be toggled.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "The ^-1 chance for which vengeance spirits could be toggled.", isCommandable = true)
    public static int vengeanceChance = 1;

    /**
     * Make a new instance.
     */
    public VengeancePickaxeConfig() {
        super(true, "vengeancePickaxe", null, VengeancePickaxe.class);
    }
}