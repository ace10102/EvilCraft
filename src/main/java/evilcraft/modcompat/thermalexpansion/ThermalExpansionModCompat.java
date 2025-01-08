package evilcraft.modcompat.thermalexpansion;

import cpw.mods.fml.common.event.FMLInterModComms;
import evilcraft.Configs;
import evilcraft.EvilCraft;
import evilcraft.IInitListener;
import evilcraft.Reference;
import evilcraft.api.recipes.custom.IRecipe;
import evilcraft.block.*;
import evilcraft.core.recipe.custom.DurationXpRecipeProperties;
import evilcraft.core.recipe.custom.ItemFluidStackAndTierRecipeComponent;
import evilcraft.core.recipe.custom.ItemStackRecipeComponent;
import evilcraft.fluid.Blood;
import evilcraft.fluid.Poison;
import evilcraft.item.*;
import evilcraft.modcompat.IModCompat;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility plugin for Thermal Expansion.
 * @author rubensworks
 * @Maintainer Spoilers
 */
public class ThermalExpansionModCompat implements IModCompat {

    @Override
    public String getModID() {
        return Reference.MOD_THERMALEXPANSION;
    }

    @Override
    public void onInit(IInitListener.Step step) {
        if(step == IInitListener.Step.INIT) {
            registerThermalExpansionRecipes();
        }
    }

    private void registerThermalExpansionRecipes() {
        String TE = getModID();
        EvilCraft.log("Registering " + TE + " recipes");

        // Sawmill: Undead Wood
        if(Configs.isEnabled(UndeadLogConfig.class) && Configs.isEnabled(UndeadPlankConfig.class)) {
            NBTTagCompound sawmillUndeadWood = new NBTTagCompound();
            sawmillUndeadWood.setInteger("energy", 2000);
            sawmillUndeadWood.setTag("input", new NBTTagCompound());
            sawmillUndeadWood.setTag("primaryOutput", new NBTTagCompound());

            new ItemStack(UndeadLogConfig._instance.getBlockInstance()).writeToNBT(sawmillUndeadWood.getCompoundTag("input"));
            new ItemStack(UndeadPlankConfig._instance.getBlockInstance(), 6).writeToNBT(sawmillUndeadWood.getCompoundTag("primaryOutput"));
            FMLInterModComms.sendMessage(TE, "SawmillRecipe", sawmillUndeadWood);
        }

        // Pulverizer: Dark Ore
        if(Configs.isEnabled(DarkOreConfig.class) && Configs.isEnabled(DarkGemConfig.class)) {
            boolean crushedEnabled = Configs.isEnabled(DarkGemCrushedConfig.class);
            NBTTagCompound pulverizerDarkOre = new NBTTagCompound();
            pulverizerDarkOre.setInteger("energy", 2000);
            pulverizerDarkOre.setTag("input", new NBTTagCompound());
            pulverizerDarkOre.setTag("primaryOutput", new NBTTagCompound());
            if(crushedEnabled) {
                pulverizerDarkOre.setTag("secondaryOutput", new NBTTagCompound());
                pulverizerDarkOre.setInteger("secondaryChance", 30);
            }

            new ItemStack(DarkOre.getInstance()).writeToNBT(pulverizerDarkOre.getCompoundTag("input"));
            new ItemStack(DarkGem.getInstance(), 2).writeToNBT(pulverizerDarkOre.getCompoundTag("primaryOutput"));
            if(crushedEnabled) {
                new ItemStack(DarkGemCrushedConfig._instance.getItemInstance(), 1).writeToNBT(pulverizerDarkOre.getCompoundTag("secondaryOutput"));
            }
            FMLInterModComms.sendMessage(TE, "PulverizerRecipe", pulverizerDarkOre);
        }

        // Pulverizer: Dark Gem -> Crushed
        if(Configs.isEnabled(DarkGemConfig.class) && Configs.isEnabled(DarkGemCrushedConfig.class)) {
            NBTTagCompound pulverizerDarkOre = new NBTTagCompound();
            pulverizerDarkOre.setInteger("energy", 4000);
            pulverizerDarkOre.setTag("input", new NBTTagCompound());
            pulverizerDarkOre.setTag("primaryOutput", new NBTTagCompound());

            new ItemStack(DarkGem.getInstance()).writeToNBT(pulverizerDarkOre.getCompoundTag("input"));
            new ItemStack(DarkGemCrushedConfig._instance.getItemInstance(), 1).writeToNBT(pulverizerDarkOre.getCompoundTag("primaryOutput"));
            FMLInterModComms.sendMessage(TE, "PulverizerRecipe", pulverizerDarkOre);
        }

        // Crucible: Poison
        ArrayList<ItemStack> materialPoisonousList = OreDictionary.getOres(Reference.DICT_MATERIALPOISONOUS);
        for(ItemStack materialPoisonous : materialPoisonousList) {
            NBTTagCompound cruciblePoison = new NBTTagCompound();
            cruciblePoison.setInteger("energy", 2000);
            cruciblePoison.setTag("input", new NBTTagCompound());
            cruciblePoison.setTag("output", new NBTTagCompound());
            if(materialPoisonous.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                materialPoisonous = materialPoisonous.copy();
                materialPoisonous.setItemDamage(0);
            }
            materialPoisonous.writeToNBT(cruciblePoison.getCompoundTag("input"));
            new FluidStack(Poison.getInstance(), 250).writeToNBT(cruciblePoison.getCompoundTag("output"));
            FMLInterModComms.sendMessage(TE, "CrucibleRecipe", cruciblePoison);
        }

        // Crucible: Ender
        if(Configs.isEnabled(EnderTearConfig.class)) {
            Fluid ender = FluidRegistry.getFluid("ender");
            if(ender != null) {
                NBTTagCompound crucibleEnder = new NBTTagCompound();
                crucibleEnder.setInteger("energy", 40000);
                crucibleEnder.setTag("input", new NBTTagCompound());
                crucibleEnder.setTag("output", new NBTTagCompound());

                new ItemStack(EnderTearConfig._instance.getItemInstance()).writeToNBT(crucibleEnder.getCompoundTag("input"));
                new FluidStack(ender, EnderTearConfig.mbLiquidEnder).writeToNBT(crucibleEnder.getCompoundTag("output"));
                FMLInterModComms.sendMessage(TE, "CrucibleRecipe", crucibleEnder);
            }
        }

        // Crucible: Hardened Blood Shard
        if(Configs.isEnabled(HardenedBloodShardConfig.class)) {
            NBTTagCompound crucibleBloodShard = new NBTTagCompound();
            crucibleBloodShard.setInteger("energy", 200);
            crucibleBloodShard.setTag("input", new NBTTagCompound());
            crucibleBloodShard.setTag("output", new NBTTagCompound());

            new ItemStack(HardenedBloodShardConfig._instance.getItemInstance()).writeToNBT(crucibleBloodShard.getCompoundTag("input"));
            new FluidStack(Blood.getInstance(), 100).writeToNBT(crucibleBloodShard.getCompoundTag("output"));
            FMLInterModComms.sendMessage(TE, "CrucibleRecipe", crucibleBloodShard);
        }

        // Fluid Transposer: Blood Infuse
        if(Configs.isEnabled(BloodInfuserConfig.class)) {
            for(IRecipe<ItemFluidStackAndTierRecipeComponent, ItemStackRecipeComponent, DurationXpRecipeProperties> recipe : BloodInfuser.getInstance().getRecipeRegistry().allRecipes()) {
                if(recipe.getInput().getTier() == 0) {
                    NBTTagCompound bloodInfuse = new NBTTagCompound();
                    bloodInfuse.setInteger("energy", recipe.getProperties().getDuration() * 10);
                    bloodInfuse.setTag("input", new NBTTagCompound());
                    bloodInfuse.setTag("output", new NBTTagCompound());
                    bloodInfuse.setTag("fluid", new NBTTagCompound());

                    recipe.getInput().getItemStack().writeToNBT(bloodInfuse.getCompoundTag("input"));
                    recipe.getOutput().getItemStack().writeToNBT(bloodInfuse.getCompoundTag("output"));
                    bloodInfuse.setBoolean("reversible", false);
                    FluidStack fluid = recipe.getInput().getFluidStack().copy();
                    fluid.amount *= 1.5;
                    fluid.writeToNBT(bloodInfuse.getCompoundTag("fluid"));
                    FMLInterModComms.sendMessage(TE, "TransposerFillRecipe", bloodInfuse);
                }
            }
        }

        // Fluid Transposer: Condensed Blood
        if(Configs.isEnabled(CondensedBloodConfig.class)) {
            NBTTagCompound condensedBlood = new NBTTagCompound();
            condensedBlood.setInteger("energy", 400);
            condensedBlood.setTag("input", new NBTTagCompound());
            condensedBlood.setTag("fluid", new NBTTagCompound());
            condensedBlood.setBoolean("overwrite", true);

            new ItemStack(CondensedBloodConfig._instance.getItemInstance()).writeToNBT(condensedBlood.getCompoundTag("input"));
            new FluidStack(Blood.getInstance(), 500).writeToNBT(condensedBlood.getCompoundTag("fluid"));
            FMLInterModComms.sendMessage(TE, "TransposerExtractRecipe", condensedBlood);
        }

        // Pulverizer: Blood-Waxed Coal
        if(Configs.isEnabled(BloodWaxedCoalConfig.class)) {
            NBTTagCompound pulverizerDustCoal = new NBTTagCompound();
            pulverizerDustCoal.setInteger("energy", 2400);
            pulverizerDustCoal.setTag("input", new NBTTagCompound());
            pulverizerDustCoal.setTag("primaryOutput", new NBTTagCompound());

            new ItemStack(BloodWaxedCoalConfig._instance.getItemInstance()).writeToNBT(pulverizerDustCoal.getCompoundTag("input"));
            List<ItemStack> dustCoalList = OreDictionary.getOres("dustCoal");
            if(!dustCoalList.isEmpty()) {
                ItemStack dustCoal = dustCoalList.get(0).copy();
                dustCoal.stackSize = 2;
                dustCoal.writeToNBT(pulverizerDustCoal.getCompoundTag("primaryOutput"));

                List<ItemStack> sulfurList = OreDictionary.getOres("dustSulfur");
                if(!sulfurList.isEmpty()) {
                    pulverizerDustCoal.setTag("secondaryOutput", new NBTTagCompound());
                    pulverizerDustCoal.setInteger("secondaryChance", 20);

                    ItemStack dustSulfur = sulfurList.get(0).copy();
                    dustSulfur.stackSize = 1;
                    dustSulfur.writeToNBT(pulverizerDustCoal.getCompoundTag("secondaryOutput"));
                }
                FMLInterModComms.sendMessage(TE, "PulverizerRecipe", pulverizerDustCoal);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Sawmill, Pulverizer, Magma Crucible and Fluid Transposer recipes.";
    }
}