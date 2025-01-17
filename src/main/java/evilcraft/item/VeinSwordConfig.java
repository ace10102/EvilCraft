package evilcraft.item;

import evilcraft.core.config.ConfigurableProperty;
import evilcraft.core.config.ConfigurableTypeCategory;
import evilcraft.core.config.extendedconfig.ItemConfig;

/**
 * Config for the {@link VeinSword}.
 * @author rubensworks
 */
public class VeinSwordConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static VeinSwordConfig _instance;

    /**
     * The multiply boost this sword has on the blood that is obtained.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "The multiply boost this sword has on the blood that is obtained.", isCommandable = true)
    public static double extractionBoost = 2.0;

    /**
     * Maximum uses for this item.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "Maximum uses for this item.")
    public static int durability = 32;

    /**
     * Make a new instance.
     */
    public VeinSwordConfig() {
        super(true, "veinSword", null, VeinSword.class);
    }
}