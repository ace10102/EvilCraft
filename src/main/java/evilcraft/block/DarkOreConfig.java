package evilcraft.block;

import evilcraft.Reference;
import evilcraft.core.config.ConfigurableProperty;
import evilcraft.core.config.ConfigurableTypeCategory;
import evilcraft.core.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link DarkOre}.
 * @author rubensworks
 */
public class DarkOreConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static DarkOreConfig _instance;

    /**
     * The amount of blocks per vein.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "How much ores per vein.")
    public static int blocksPerVein = 4;
    /**
     * The amount of veins per chunk.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "How many veins per chunk.")
    public static int veinsPerChunk = 7;
    /**
     * The start Y for ore spawning.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "Generation starts from this level.")
    public static int startY = 6;
    /**
     * The end Y for ore spawning.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.WORLDGENERATION, comment = "Generation ends of this level.")
    public static int endY = 66;

    /**
     * Make a new instance.
     */
    public DarkOreConfig() {
        super(true, "darkOre", null, DarkOre.class);
    }

    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_OREDARK;
    }
}