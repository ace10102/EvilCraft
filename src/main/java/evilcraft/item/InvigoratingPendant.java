package evilcraft.item;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Optional;
import evilcraft.Reference;
import evilcraft.core.config.configurable.ConfigurableDamageIndicatedItemFluidContainer;
import evilcraft.core.config.extendedconfig.ExtendedConfig;
import evilcraft.core.config.extendedconfig.ItemConfig;
import evilcraft.core.helper.MinecraftHelpers;
import evilcraft.core.helper.WorldHelpers;
import evilcraft.core.helper.obfuscation.ObfuscationHelpers;
import evilcraft.fluid.Blood;
import evilcraft.modcompat.baubles.BaublesModCompat;

/**
 * Ring that can enable sight into the vengeance spirit realm.
 * @author rubensworks
 *
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = Reference.MOD_BAUBLES, striprefs = true)
public class InvigoratingPendant extends ConfigurableDamageIndicatedItemFluidContainer implements IBauble {

	private static final int TICK_MODULUS = MinecraftHelpers.SECOND_IN_TICKS / 2;
	
    private static InvigoratingPendant _instance = null;
    
    /**
     * Initialise the configurable.
     * @param eConfig The config.
     */
    public static void initInstance(ExtendedConfig<ItemConfig> eConfig) {
        if(_instance == null)
            _instance = new InvigoratingPendant(eConfig);
        else
            eConfig.showDoubleInitError();
    }
    
    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static InvigoratingPendant getInstance() {
        return _instance;
    }

    private InvigoratingPendant(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig, InvigoratingPendantConfig.capacity, Blood.getInstance());
        this.setMaxStackSize(1);
    }
    
    @Optional.Method(modid = Reference.MOD_BAUBLES)
    private void equipBauble(ItemStack itemStack, EntityPlayer player) {
    	IInventory inventory = BaublesApi.getBaubles(player);
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			if(inventory.getStackInSlot(i) == null && inventory.isItemValidForSlot(i, itemStack)) {
				inventory.setInventorySlotContents(i, itemStack.copy());
				if(!player.capabilities.isCreativeMode){
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}
				onEquipped(itemStack, player);
				break;
			}
		}
	}
    
    /**
     * Clear the bad effects of given player.
     * Each 'tick', a certain amount of bad effect duration reduction is reserved.
     * Each found effect it's duration is reduced by as much as possible (not larger than the reserved amount)
     * and the inner tank is drained according to how much was reduced.
     * If the reserved duration is not zero at the end, the next bad effect will be taken.
     * @param itemStack The pendant to drain.
     * @param player The player to receive the powers.
     */
    public void clearBadEffects(ItemStack itemStack, EntityPlayer player) {
    	int amount = InvigoratingPendantConfig.usage;
    	if(canDrain(amount, itemStack)) {
    		
    		int originalReducableDuration = InvigoratingPendantConfig.reduceDuration * MinecraftHelpers.SECOND_IN_TICKS;
    		int reducableDuration = originalReducableDuration;
    		
	    	@SuppressWarnings("unchecked")
			Iterator<PotionEffect> it = Lists.newLinkedList(player.getActivePotionEffects()).iterator();
	    	while(reducableDuration > 0 && it.hasNext() && canDrain(amount, itemStack)) {
	    		PotionEffect effect = it.next();
	    		int potionID = effect.getPotionID();
	    		
	    		boolean shouldClear = true;
	    		if(potionID >= 0 && potionID < Potion.potionTypes.length) {
	    			shouldClear = Potion.potionTypes[potionID].isBadEffect();
	    		}
	    		
	    		if(shouldClear) {	    			
	    			int reductionMultiplier = effect.getAmplifier() + 1;
	    			int reducableDurationForThisEffect = reducableDuration / reductionMultiplier;
	    			int remaining = effect.getDuration();
	    			int toReduce = Math.min(reducableDurationForThisEffect, remaining);
	    			int toDrain = amount;
	    			
	    			reducableDuration -= toReduce;
	    			if(remaining == toReduce) {
	    				player.removePotionEffect(potionID);
	    			} else {
	    				ObfuscationHelpers.setPotionEffectDuration(effect, remaining - toReduce);
	    				toDrain = (int) Math.ceil((double) (reductionMultiplier * amount)
	    						* ((double) toReduce / (double) originalReducableDuration));
	    			}
	    			if(!player.worldObj.isRemote) {
	    				drain(itemStack, toDrain, true);
	    			}
	    			ObfuscationHelpers.onChangedPotionEffect(player, effect, true);
	    		}
	    	}
    	}
	}
    
	@Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5) {
        if(entity instanceof EntityPlayer
        		&& WorldHelpers.efficientTick(world, TICK_MODULUS, entity.getEntityId())) {
        	clearBadEffects(itemStack, (EntityPlayer) entity);
        }
        super.onUpdate(itemStack, world, entity, par4, par5);
    }

	@Optional.Method(modid = Reference.MOD_BAUBLES)
	@Override
	public boolean canEquip(ItemStack itemStack, EntityLivingBase entity) {
		return BaublesModCompat.canUse();
	}

	@Optional.Method(modid = Reference.MOD_BAUBLES)
	@Override
	public boolean canUnequip(ItemStack itemStack, EntityLivingBase entity) {
		return true;
	}

	@Optional.Method(modid = Reference.MOD_BAUBLES)
	@Override
	public BaubleType getBaubleType(ItemStack itemStack) {
		return BaubleType.AMULET;
	}

	@Optional.Method(modid = Reference.MOD_BAUBLES)
	@Override
	public void onEquipped(ItemStack itemStack, EntityLivingBase entity) {
		
	}

	@Optional.Method(modid = Reference.MOD_BAUBLES)
	@Override
	public void onUnequipped(ItemStack itemStack, EntityLivingBase entity) {
		
	}

	@Optional.Method(modid = Reference.MOD_BAUBLES)
	@Override
	public void onWornTick(ItemStack itemStack, EntityLivingBase entity) {
		if(BaublesModCompat.canUse()) {
			this.onUpdate(itemStack, entity.worldObj, entity, 0, false);
		}
	}

}