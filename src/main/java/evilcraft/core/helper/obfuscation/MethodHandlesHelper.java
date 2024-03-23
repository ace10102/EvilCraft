package evilcraft.core.helper.obfuscation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import evilcraft.EvilCraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class MethodHandlesHelper {
    // are we in a dev environment
    private static final boolean DEV_ENVIRONMENT = (boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");

    // net.minecraft.client.gui.GuiMainMenu.
    public static final String GUIMAINMENU_TITLEPANORAMAPATHS = DEV_ENVIRONMENT ? "titlePanoramaPaths" : "field_73978_o";
    // net.minecraft.potion.Potion.
    private static final String POTION_POTIONTYPES = DEV_ENVIRONMENT ? "potionTypes" : "field_76425_a";
    // net.minecraftforge.oredict.ShapedOreRecipe.
    private static final String SHAPEDORERECIPE_WIDTH = "width";
    private static final String SHAPEDORERECIPE_HEIGHT = "height";

    // net.minecraft.entity.EntityLivingBase.
    private static final String ENTITYLIVINGBASE_GETDEATHSOUND = DEV_ENVIRONMENT ? "getDeathSound" : "func_70673_aS";
    // net.minecraft.entity.EntityLiving.
    private static final String ENTITYLIVING_GETLIVINGSOUND = DEV_ENVIRONMENT ? "getLivingSound" : "func_70639_aQ";

    public static Field panoramaPaths = FMLCommonHandler.instance().getSide() == Side.CLIENT ? findFieldFaster(GuiMainMenu.class, GUIMAINMENU_TITLEPANORAMAPATHS) : null;
    public static Field potionTypes = findFieldFaster(Potion.class, POTION_POTIONTYPES);
    public static Field recipeWidth = findFieldFaster(ShapedOreRecipe.class, SHAPEDORERECIPE_WIDTH);
    public static Field recipeHeight = findFieldFaster(ShapedOreRecipe.class, SHAPEDORERECIPE_HEIGHT);

    public static Method getDeathSound = findMethodFaster(EntityLivingBase.class, ENTITYLIVINGBASE_GETDEATHSOUND);
    public static Method getLivingSound = findMethodFaster(EntityLiving.class, ENTITYLIVING_GETLIVINGSOUND);

    private static final MethodHandle MH_GuiMainMenu_titlePanoramaPaths;
    private static final MethodHandle MH_Potion_potionTypes;
    private static final MethodHandle MH_ShapedOreRecipe_width;
    private static final MethodHandle MH_ShapedOreRecipe_height;

    private static final MethodHandle MH_getDeathSound;
    private static final MethodHandle MH_getLivingSound;
    static {
        try {
            MH_GuiMainMenu_titlePanoramaPaths = panoramaPaths != null ? MethodHandles.lookup().unreflectSetter(panoramaPaths) : null;
            MH_Potion_potionTypes = MethodHandles.lookup().unreflectSetter(potionTypes);
            MH_ShapedOreRecipe_width = MethodHandles.lookup().unreflectGetter(recipeWidth);
            MH_ShapedOreRecipe_height = MethodHandles.lookup().unreflectGetter(recipeHeight);
            MH_getDeathSound = MethodHandles.lookup().unreflect(getDeathSound);
            MH_getLivingSound = MethodHandles.lookup().unreflect(getLivingSound);
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setTitlePanoramaPaths(ResourceLocation[] titlePanoramaPaths) {
        try {
            MH_GuiMainMenu_titlePanoramaPaths.invoke(titlePanoramaPaths);
            EvilCraft.log("Successfully Evilified the Main Menu", Level.INFO);
        } catch(Throwable e) {
            EvilCraft.log("MethodHandle setTitlePanoramaPaths errored on setting the panorama paths: ", Level.ERROR);
            e.printStackTrace();
        }
    }

    public static void setPotionTypesArray(Potion[] potionTypes) {
        try {
            MH_Potion_potionTypes.invoke(potionTypes);
            EvilCraft.log("Successfully Extended the Potion Array", Level.INFO);
        } catch(Throwable e) {
            EvilCraft.log("MethodHandle setPotionTypesArray errored on setting the potion array: ", Level.ERROR);
            e.printStackTrace();
        }
    }

    public static int getShapedOreRecipeWidth(ShapedOreRecipe recipe) {
        try {
            return (int)MH_ShapedOreRecipe_width.invoke(recipe);
        } catch(Throwable e) {
            EvilCraft.log("MethodHandle getShapedOreRecipeWidth errored on recipe: " + recipe.getClass().getName(), Level.ERROR);
        }
        return 0;
    }

    public static int getShapedOreRecipeHeight(ShapedOreRecipe recipe) {
        try {
            return (int)MH_ShapedOreRecipe_height.invoke(recipe);
        } catch(Throwable e) {
            EvilCraft.log("MethodHandle getShapedOreRecipeHeight errored on recipe: " + recipe.getClass().getName(), Level.ERROR);
        }
        return 0;
    }

    public static String getDeathSound(EntityLivingBase entity) {
        try {
            return (String)MH_getDeathSound.invoke(entity);
        } catch(Throwable e) {
            EvilCraft.log("MethodHandle getDeathSound errored on entity: " + entity.getClass().getName(), Level.ERROR);
        }
        return null;
    }

    public static String getLivingSound(EntityLiving entity) {
        try {
            return (String)MH_getLivingSound.invoke(entity);
        } catch(Throwable e) {
            EvilCraft.log("MethodHandle getLivingSound errored on entity: " + entity.getClass().getName(), Level.ERROR);
        }
        return null;
    }

    // find fields without looping through string[] @cpw.mods.fml.relauncher.ReflectionHelper.findField
    public static Field findFieldFaster(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch(Exception e) {
            throw new UnableToFindFieldException(new String[] {fieldName}, e);
        }
    }

    // find methods without looping through string[] @cpw.mods.fml.relauncher.ReflectionHelper.findMethod
    public static Method findMethodFaster(Class<?> clazz, String methodName, Class<?>... methodTypes) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, methodTypes);
            m.setAccessible(true);
            return m;
        } catch(Exception e) {
            throw new UnableToFindMethodException(new String[] {methodName}, e);
        }
    }
}