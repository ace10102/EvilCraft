package evilcraft.item;
import evilcraft.core.config.configurable.ConfigurableItem;
import evilcraft.core.config.extendedconfig.ExtendedConfig;
import evilcraft.core.config.extendedconfig.ItemConfig;

/**
 * A bloody book.
 * @author rubensworks
 *
 */
public class Blook extends ConfigurableItem {
    
    private static Blook _instance = null;
    
    /**
     * Initialise the configurable.
     * @param eConfig The config.
     */
    public static void initInstance(ExtendedConfig<ItemConfig> eConfig) {
        if(_instance == null)
            _instance = new Blook(eConfig);
        else
            eConfig.showDoubleInitError();
    }
    
    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static Blook getInstance() {
        return _instance;
    }

    private Blook(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig);
    }

}
