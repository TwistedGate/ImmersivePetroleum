package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class CokerUnitRecipeSerializer extends IERecipeSerializer<CokerUnitRecipe>{
	
	@Override
	public CokerUnitRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context){
		FluidStack outputFluid = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "resultfluid"));
		FluidTagInput inputFluid = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "inputfluid"));
		
		Lazy<ItemStack> outputItem = readOutput(json.get("result"));
		IngredientWithSize inputItem = IngredientWithSize.deserialize(GsonHelper.getAsJsonObject(json, "input"));
		
		int energy = GsonHelper.getAsInt(json, "energy");
		int time = GsonHelper.getAsInt(json, "time");
		
		return new CokerUnitRecipe(recipeId, outputItem, outputFluid, inputItem, inputFluid, energy, time);
	}
	
	@Override
	public CokerUnitRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer){
		IngredientWithSize inputItem = IngredientWithSize.read(buffer);
		ItemStack outputItem = buffer.readItem();
		
		FluidTagInput inputFluid = FluidTagInput.read(buffer);
		FluidStack outputFluid = FluidStack.readFromPacket(buffer);
		
		int energy = buffer.readInt();
		int time = buffer.readInt();
		
		return new CokerUnitRecipe(recipeId, Lazy.of(() -> outputItem), outputFluid, inputItem, inputFluid, energy, time);
	}
	
	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, CokerUnitRecipe recipe){
		recipe.inputItem.write(buffer);
		buffer.writeItem(recipe.outputItem.copy());
		
		recipe.inputFluid.write(buffer);
		recipe.outputFluid.writeToPacket(buffer);
		
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.getTotalProcessTime());
	}
	
	@Override
	public ItemStack getIcon(){
		return new ItemStack(IPContent.Multiblock.COKERUNIT.iconStack().getItem());
	}
}
