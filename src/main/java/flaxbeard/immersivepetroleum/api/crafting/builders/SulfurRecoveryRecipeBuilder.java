package flaxbeard.immersivepetroleum.api.crafting.builders;

import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class SulfurRecoveryRecipeBuilder extends IEFinishedRecipe<SulfurRecoveryRecipeBuilder>{
	
	public static SulfurRecoveryRecipeBuilder builder(FluidStack fluidOutput, ItemStack itemOutput, double chance, FluidStack fluidInput, int energy, int time){
		SulfurRecoveryRecipeBuilder builder = new SulfurRecoveryRecipeBuilder();
		builder.addFluid("resultfluid", fluidOutput);
		builder.addFluid("fluidinput", fluidInput);
		builder.addWriter(jsonObject -> {
			builder.serializerItemStackWithChance(itemOutput, chance);
		});
		builder.setTimeAndEnergy(time, energy);
		return builder;
	}
	
	protected SulfurRecoveryRecipeBuilder(){
		super(Serializers.HYDROTREATER_SERIALIZER.get());
	}
	
	protected SulfurRecoveryRecipeBuilder setTimeAndEnergy(int time, int energy){
		return setTime(time).setEnergy(energy);
	}
	
	protected JsonObject serializerItemStackWithChance(ItemStack stack, double chance){
		JsonObject itemJson = this.serializeItemStack(stack);
		itemJson.addProperty("chance", Double.toString(chance));
		return itemJson;
	}
}
