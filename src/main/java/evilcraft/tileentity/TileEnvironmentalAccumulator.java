package evilcraft.tileentity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import evilcraft.api.ILocation;
import evilcraft.api.degradation.IDegradable;
import evilcraft.api.recipes.custom.IRecipe;
import evilcraft.block.EnvironmentalAccumulator;
import evilcraft.block.EnvironmentalAccumulatorConfig;
import evilcraft.client.particle.EntityTargettedBlurFX;
import evilcraft.client.particle.ExtendedEntityBubbleFX;
import evilcraft.core.algorithm.Location;
import evilcraft.core.degradation.DegradationExecutor;
import evilcraft.core.helper.EntityHelpers;
import evilcraft.core.helper.L10NHelpers;
import evilcraft.core.helper.LocationHelpers;
import evilcraft.core.helper.MinecraftHelpers;
import evilcraft.core.inventory.SimpleInventory;
import evilcraft.core.recipe.custom.EnvironmentalAccumulatorRecipeComponent;
import evilcraft.core.recipe.custom.EnvironmentalAccumulatorRecipeProperties;
import evilcraft.core.weather.WeatherType;
import evilcraft.tileentity.environmentalaccumulator.IEAProcessingFinishedEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector4f;

import java.util.List;
import java.util.Random;

/**
 * Machine that can accumulate the weather and put it in a bottle.
 * @author immortaleeb
 */
public class TileEnvironmentalAccumulator extends EvilCraftBeaconTileEntity implements IBossDisplayData, IDegradable, IInventory {

    public static final int MAX_AGE = 50;
    public static final int SPREAD = 25;

    private static final int ITEM_MOVE_COOLDOWN_DURATION = 1;

    private static final double WEATHER_CONTAINER_MIN_DROP_HEIGHT = 0.0;
    private static final double WEATHER_CONTAINER_MAX_DROP_HEIGHT = 2.0;
    private static final double WEATHER_CONTAINER_SPAWN_HEIGHT = 
            EnvironmentalAccumulatorConfig.defaultProcessItemTickCount * EnvironmentalAccumulatorConfig.defaultProcessItemSpeed + 1;

    private static final float ITEM_MIN_SPAWN_HEIGHT = 1.0f;

    private static final int DEGRADATION_RADIUS_BASE = 5;
    private static final int DEGRADATION_TICK_INTERVAL = 100;

    private DegradationExecutor degradationExecutor;
    // This number rises with the number of uses of the env. accum.
    private int degradation = 0;
    private ILocation location = null;

    private static final ILocation[] waterOffsets = new ILocation[] { new Location(-2, -1, -2), new Location(-2, -1, 2), new Location(2, -1, -2), new Location(2, -1, 2), };

    /**
     * Holds the state of the environmental accumulator.
     * The following states are possible: idle (the default case), cooling down, processing an item and dropping an item.
     * The different states can be found as public static variables of {@link EnvironmentalAccumulator}.
     */
    private int state = 0;
    private int tick = 0;

    private SimpleInventory inventory;

    // The recipe we're currently working on
    private IRecipe<EnvironmentalAccumulatorRecipeComponent, EnvironmentalAccumulatorRecipeComponent, EnvironmentalAccumulatorRecipeProperties> recipe;

    /**
     * Make a new instance.
     */
    public TileEnvironmentalAccumulator() {
        super();

        degradationExecutor = new DegradationExecutor(this);

        inventory = new SimpleInventory(1, EnvironmentalAccumulatorConfig._instance.getNamedId(), 64);

        if(MinecraftHelpers.isClientSide()) {
            setBeamInnerColor(getInnerColorByState(state));
            setBeamOuterColor(getOuterColorByState(state));
        }
    }

    @SideOnly(Side.CLIENT)
    private Vector4f getInnerColorByState(int state) {
        if(state == EnvironmentalAccumulator.STATE_PROCESSING_ITEM)
            return new Vector4f(0.48046875F, 0.29296875F, 0.1171875F, 0.05f);
        if(state == EnvironmentalAccumulator.STATE_IDLE)
            return new Vector4f(0.48046875F, 0.29296875F, 0.1171875F, 0.13f);
        else
            return new Vector4f(0, 0, 0, 0.13f);
    }

    @SideOnly(Side.CLIENT)
    private Vector4f getOuterColorByState(int state) {
        if(state == EnvironmentalAccumulator.STATE_COOLING_DOWN)
            return new Vector4f(0, 0, 0, 0.13f);
        else
            return new Vector4f(0.30078125F, 0.1875F, 0.08203125F, 0.13f);
    }

    /**
     * Get the maximum cooldown tick for accumulating weather.
     * @return The maximum cooldown tick.
     */
    public int getMaxCooldownTick() {
        EnvironmentalAccumulatorRecipeProperties result = (recipe == null) ? null : recipe.getProperties();
        if(result == null)
            return EnvironmentalAccumulatorConfig.defaultTickCooldown;
        else
            return result.getCooldownTime();
    }

    /**
     * Get the Y coordinate of the current moving item.
     * @return The Y coordinate of the inner item.
     */
    @SideOnly(Side.CLIENT)
    public float getMovingItemY() {
        if(state == EnvironmentalAccumulator.STATE_PROCESSING_ITEM)
            return ITEM_MIN_SPAWN_HEIGHT + (getItemMoveDuration() - tick) * getItemMoveSpeed();
        else
            return -1;
    }

    /**
     * Get the current recipe we're working on.
     * @return Returns the recipe being processed, or null in case we're not processing anything at the moment.
     */
    public IRecipe<EnvironmentalAccumulatorRecipeComponent, EnvironmentalAccumulatorRecipeComponent, EnvironmentalAccumulatorRecipeProperties> getRecipe() {
        return recipe;
    }

    private int getItemMoveDuration() {
        if(recipe == null)
            return EnvironmentalAccumulatorConfig.defaultProcessItemTickCount;
        else
            return recipe.getProperties().getDuration();
    }

    private float getItemMoveSpeed() {
        if(recipe == null)
            return (float)EnvironmentalAccumulatorConfig.defaultProcessItemSpeed;
        else
            return (float)recipe.getProperties().getProcessingSpeed();
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        // Keep ticking if necessary
        if(tick > 0)
            tick--;

        if(state == EnvironmentalAccumulator.STATE_IDLE) {
            updateEnvironmentalAccumulatorIdle();
        } // Are we processing an item?
        else if(state == EnvironmentalAccumulator.STATE_PROCESSING_ITEM) {
            if(worldObj.isRemote) {
                showWaterBeams();
                if(tick > MAX_AGE) {
                    showAccumulatingParticles();
                }
            }
            // Are we done moving the item?
            if(tick == 0) {
                dropItemStack();
                activateFinishedProcessingItemState();
            }
        } // Have we just finished processing an item?
        else if(state == EnvironmentalAccumulator.STATE_FINISHED_PROCESSING_ITEM) {
            // We stay in this state for a while so the client gets some time to
            // show the corresponding effect when an item is finished processing

            // Are we done waiting for the client to update?
            if(tick == 0) {
                activateCooldownState();

                // Remove the items in our inventory
                this.decrStackSize(0, this.getInventoryStackLimit());
            }
        } // Are we cooling down?
        else if(state == EnvironmentalAccumulator.STATE_COOLING_DOWN) {
            // TODO: in the rewrite of this tile entity, it should be ensured that the
            // random effect is equal on client and server side?
            degradationExecutor.runRandomEffect(worldObj.isRemote);

            // Are we done cooling down?
            if(tick == 0)
                activateIdleState();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void showWaterBeams() {
        Random random = worldObj.rand;
        ILocation target = new Location(xCoord, yCoord, zCoord);
        for(int j = 0; j < waterOffsets.length; j++) {
            ILocation offset = waterOffsets[j];
            ILocation location = target.add(offset);
            double x = location.getCoordinates()[0] + 0.5;
            double y = location.getCoordinates()[1] + 0.5;
            double z = location.getCoordinates()[2] + 0.5;

            float rotationYaw = (float)LocationHelpers.getYaw(location, target);
            float rotationPitch = (float)LocationHelpers.getPitch(location, target);

            for(int i = 0; i < random.nextInt(2); i++) {
                double particleX = x - 0.2 + random.nextDouble() * 0.4;
                double particleY = y - 0.2 + random.nextDouble() * 0.4;
                double particleZ = z - 0.2 + random.nextDouble() * 0.4;

                double speed = 2;

                double particleMotionX = MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI) * MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * speed;
                double particleMotionY = MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * speed * 5;
                double particleMotionZ = MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI) * MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * speed;

                FMLClientHandler.instance().getClient().effectRenderer.addEffect(new ExtendedEntityBubbleFX(worldObj, particleX, particleY, particleZ, particleMotionX, particleMotionY, particleMotionZ, 0.02D));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void showAccumulatingParticles() {
        showAccumulatingParticles(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, SPREAD);
    }

    @SideOnly(Side.CLIENT)
    public static void showAccumulatingParticles(World world, double centerX, double centerY, double centerZ, double spread) {
        Random rand = world.rand;
        for(int j = 0; j < rand.nextInt(20); j++) {
            float scale = 0.6F - rand.nextFloat() * 0.4F;
            float red = rand.nextFloat() * 0.1F + 0.2F;
            float green = rand.nextFloat() * 0.1F + 0.3F;
            float blue = rand.nextFloat() * 0.1F + 0.2F;
            float ageMultiplier = MAX_AGE + 10;

            double motionX = spread - rand.nextDouble() * 2 * spread;
            double motionY = spread - rand.nextDouble() * 2 * spread;
            double motionZ = spread - rand.nextDouble() * 2 * spread;

            FMLClientHandler.instance().getClient().effectRenderer.addEffect(new EntityTargettedBlurFX(world, scale, motionX, motionY, motionZ, red, green, blue, ageMultiplier, centerX, centerY, centerZ));
        }
    }

    @SuppressWarnings("rawtypes")
    private void updateEnvironmentalAccumulatorIdle() {
        // Look for items thrown into the beam
        List entityItems = worldObj.getEntitiesWithinAABB(EntityItem.class,
                AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord + WEATHER_CONTAINER_MIN_DROP_HEIGHT,
                        this.zCoord, this.xCoord + 1.0, this.yCoord + WEATHER_CONTAINER_MAX_DROP_HEIGHT, this.zCoord + 1.0));

        // Loop over all recipes until we find an item dropped in the accumulator that matches a recipe
        for(IRecipe<EnvironmentalAccumulatorRecipeComponent, EnvironmentalAccumulatorRecipeComponent, EnvironmentalAccumulatorRecipeProperties> recipe : EnvironmentalAccumulator.getInstance().getRecipeRegistry().allRecipes()) {
            EnvironmentalAccumulatorRecipeComponent input = recipe.getInput();

            ItemStack recipeStack = input.getItemStack();
            WeatherType weatherType = input.getWeatherType();

            // Loop over all dropped items
            for(Object obj : entityItems) {
                EntityItem entityItem = (EntityItem)obj;
                ItemStack stack = entityItem.getEntityItem();

                if(recipeStack.getItem() == stack.getItem() && recipeStack.getItemDamage() == stack.getItemDamage() && recipeStack.stackSize <= stack.stackSize && (weatherType == null || weatherType.isActive(worldObj))) {

                    // Save the required input items in the inventory
                    this.setInventorySlotContents(0, stack.copy());

                    // Save the recipe
                    this.recipe = recipe;

                    if(!worldObj.isRemote) {
                        decreaseStackSize(entityItem, recipeStack);
                    }
                    activateProcessingItemState();
                    return;
                }
            }
        }
    }

    private void decreaseStackSize(EntityItem entityItem, ItemStack stack) {
        entityItem.getEntityItem().stackSize -= stack.stackSize;

        if(entityItem.getEntityItem().stackSize == 0)
            entityItem.setDead();
    }

    private void dropItemStack() {
        if(!worldObj.isRemote) {
            // EntityItem that will contain the dropped itemstack
            EntityItem entity = new EntityItem(worldObj, this.xCoord, this.yCoord + WEATHER_CONTAINER_SPAWN_HEIGHT, this.zCoord);

            if(recipe == null) {
                // No recipe found, throw the item stack in the inventory back
                // (NOTE: this can be caused because of weather changes)
                entity.setEntityItemStack(this.getStackInSlot(0));
            } else {
                // Recipe found, throw back the result
                entity.setEntityItemStack(recipe.getProperties().getResultOverride().getResult(getWorldObj(), xCoord, yCoord, zCoord, recipe.getOutput().getConditionalItemStack(this.getStackInSlot(0))));

                // Change the weather to the resulting weather
                WeatherType weatherSource = recipe.getInput().getWeatherType();
                if(weatherSource != null)
                    weatherSource.deactivate(worldObj);

                WeatherType weatherResult = recipe.getOutput().getWeatherType();
                if(weatherResult != null)
                    weatherResult.activate(worldObj);
            }
            // Drop the items on the ground
            worldObj.spawnEntityInWorld(entity);
        }
    }

    private void activateIdleState() {
        tick = 0;
        state = EnvironmentalAccumulator.STATE_IDLE;
        if(!worldObj.isRemote)
            sendUpdate();
    }

    private void activateProcessingItemState() {
        // Set the duration for processing the item
        if(recipe == null)
            tick = EnvironmentalAccumulatorConfig.defaultProcessItemTickCount;
        else
            tick = recipe.getProperties().getDuration();
        state = EnvironmentalAccumulator.STATE_PROCESSING_ITEM;
        if(!worldObj.isRemote)
            sendUpdate();
    }

    private void activateFinishedProcessingItemState() {
        tick = ITEM_MOVE_COOLDOWN_DURATION;
        state = EnvironmentalAccumulator.STATE_FINISHED_PROCESSING_ITEM;
        if(!worldObj.isRemote)
            sendUpdate();
    }

    private void activateCooldownState() {
        degradation++;
        degradationExecutor.setTickInterval(DEGRADATION_TICK_INTERVAL / degradation);
        tick = getMaxCooldownTick();
        state = EnvironmentalAccumulator.STATE_COOLING_DOWN;
        if(!worldObj.isRemote)
            sendUpdate();
    }

    @Override
    public void onUpdateReceived() {
        // If we receive an update from the server and our new state is the
        // finished processing item state, show the corresponding effect
        if(worldObj.isRemote && state == EnvironmentalAccumulator.STATE_FINISHED_PROCESSING_ITEM) {
            // Show an effect indicating the item finished processing.
            IEAProcessingFinishedEffect effect = (recipe == null) ? null : recipe.getProperties().getFinishedProcessingEffect();

            if(effect == null) // fall back to default case
                this.worldObj.playAuxSFX(2002, (int)Math.round(xCoord), (int)Math.round(yCoord + WEATHER_CONTAINER_SPAWN_HEIGHT), (int)Math.round(zCoord), 16428);
            else
                effect.executeEffect(this, recipe);
        }
        // Change the beam colors if we receive an update
        setBeamColors(state);
    }

    /**
     * Set the beam colors.
     * @param state The state to base the colors on.
     */
    public void setBeamColors(int state) {
        if(worldObj.isRemote) {
            setBeamInnerColor(getInnerColorByState(state));
            setBeamOuterColor(getOuterColorByState(state));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        degradation = compound.getInteger("degradation");
        tick = compound.getInteger("tick");
        state = compound.getInteger("state");

        String recipeId = compound.getString("recipe");
        if(recipeId != null)
            recipe = EnvironmentalAccumulator.getInstance().getRecipeRegistry().findRecipeByNamedId(recipeId);

        degradationExecutor.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("degradation", degradation);
        compound.setInteger("tick", tick);
        compound.setInteger("state", state);
        String recipeId = (recipe == null) ? null : recipe.getNamedId();
        if(recipeId != null)
            compound.setString("recipe", recipeId);
        degradationExecutor.writeToNBT(compound);
    }

    @Override
    public float getMaxHealth() {
        if(state == EnvironmentalAccumulator.STATE_PROCESSING_ITEM)
            return getItemMoveDuration();
        if(state == EnvironmentalAccumulator.STATE_FINISHED_PROCESSING_ITEM)
            return 0;
        return getMaxCooldownTick();
    }

    @Override
    public float getHealth() {
        if(state == EnvironmentalAccumulator.STATE_PROCESSING_ITEM)
            return tick;
        if(state == EnvironmentalAccumulator.STATE_COOLING_DOWN)
            return getMaxCooldownTick() - tick;
        return getMaxCooldownTick();
    }

    @Override
    public IChatComponent func_145748_c_() {
        String message = L10NHelpers.localize("chat.bossDisplay.charge", L10NHelpers.localize(EnvironmentalAccumulator.getInstance().getUnlocalizedName() + ".name"));
        return new ChatComponentText(message);
    }

    @Override
    public ILocation getLocation() {
        if(location == null)
            location = new Location(xCoord, yCoord, zCoord);
        return location;
    }

    @Override
    public int getRadius() {
        return DEGRADATION_RADIUS_BASE + degradation / 10;
    }

    @Override
    public List<Entity> getAreaEntities() {
        return EntityHelpers.getEntitiesInArea(getWorld(), xCoord, yCoord, zCoord, getRadius());
    }

    @Override
    public double getDegradation() {
        return this.degradation;
    }

    @Override
    public World getWorld() {
        return this.worldObj;
    }

    @Override
    public int getSizeInventory() {
        return inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if(i >= getSizeInventory() || i < 0)
            return null;
        return inventory.getStackInSlot(i);
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        return inventory.decrStackSize(i, j);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        return inventory.getStackInSlotOnClosing(i);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        inventory.setInventorySlotContents(i, itemstack);
    }

    @Override
    public String getInventoryName() {
        return inventory.getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return inventory.hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        return inventory.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return false;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }
}