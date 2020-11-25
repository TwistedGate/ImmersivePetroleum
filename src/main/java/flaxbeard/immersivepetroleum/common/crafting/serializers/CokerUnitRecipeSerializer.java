package flaxbeard.immersivepetroleum.common.crafting.serializers;

import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CokerUnitRecipeSerializer extends IERecipeSerializer<CokerUnitRecipe>{
	
	@Override
	public CokerUnitRecipe readFromJson(ResourceLocation recipeId, JsonObject json){
		FluidTagInput outputFluid = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "resultfluid"));
		FluidTagInput inputFluid = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "inputfluid"));
		
		ItemStack outputItem = readOutput(json.get("result"));
		Ingredient inputItem = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
		
		int energy = 2048;
		if(json.has("energy")) energy = JSONUtils.getInt(json, "energy");
		
		return new CokerUnitRecipe(recipeId, outputItem, outputFluid, inputItem, inputFluid, energy);
	}
	
	@Override
	public CokerUnitRecipe read(ResourceLocation recipeId, PacketBuffer buffer){
		Ingredient inputItem = Ingredient.read(buffer);
		ItemStack outputItem = buffer.readItemStack();
		
		FluidTagInput inputFluid = FluidTagInput.read(buffer);
		FluidTagInput outputFluid = FluidTagInput.read(buffer);
		
		int energy = buffer.readInt();
		
		return new CokerUnitRecipe(recipeId, outputItem, outputFluid, inputItem, inputFluid, energy);
	}
	
	@Override
	public void write(PacketBuffer buffer, CokerUnitRecipe recipe){
		recipe.inputItem.write(buffer);
		buffer.writeItemStack(recipe.outputItem);
		
		recipe.inputFluid.write(buffer);
		recipe.outputFluid.write(buffer);
		
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
	
	@Override
	public ItemStack getIcon(){
		return new ItemStack(IPContent.Multiblock.cokerunit);
	}
}
