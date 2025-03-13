package flaxbeard.immersivepetroleum.common.crafting.serializers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.api.crafting.builders.DistillationTowerRecipeBuilder;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.fluids.FluidStack;

public class DistillationTowerRecipeSerializer extends IERecipeSerializer<DistillationTowerRecipe>{
	@Override
	public DistillationTowerRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context){
		FluidTagInput input = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input"));
		JsonArray fluidResults = GsonHelper.getAsJsonArray(json, "results");
		
		FluidStack[] fluidOutput = new FluidStack[fluidResults.size()];
		for(int i = 0;i < fluidOutput.length;i++)
			fluidOutput[i] = ApiUtils.jsonDeserializeFluidStack(fluidResults.get(i).getAsJsonObject());
		
		ItemStack[] array0 = new ItemStack[0];
		double[] array1 = new double[0];
		if(json.has("byproducts")){
			JsonArray itemResults = GsonHelper.getAsJsonArray(json, "byproducts");
			
			List<ItemStack> byproducts = new ArrayList<>(0);
			List<Double> chances = new ArrayList<>(0);
			for(int i = 0;i < itemResults.size();i++){
				Tuple<ItemStack, Double> chancedStack = DistillationTowerRecipeBuilder.deserializeItemStackWithChance(itemResults.get(i).getAsJsonObject());
				
				byproducts.add(chancedStack.getA());
				chances.add(chancedStack.getB());
			}
			
			if(byproducts.size() != chances.size()){
				int d = Math.abs(chances.size() - byproducts.size());
				throw new JsonSyntaxException(d + " byproduct" + (d > 1 ? "s have" : " has") + " a missing value or too many.");
			}
			
			array0 = byproducts.toArray(new ItemStack[0]);
			array1 = new double[chances.size()];
			for(int i = 0;i < chances.size();i++)
				array1[i] = chances.get(i).doubleValue();
		}
		
		int energy = GsonHelper.getAsInt(json, "energy");
		int time = GsonHelper.getAsInt(json, "time");
		
		return new DistillationTowerRecipe(recipeId, fluidOutput, array0, input, energy, time, array1);
	}
	
	@Override
	public DistillationTowerRecipe fromNetwork(@Nonnull ResourceLocation recipeId, FriendlyByteBuf buffer){
		FluidStack[] fluidOutput = new FluidStack[buffer.readInt()];
		for(int i = 0;i < fluidOutput.length;i++)
			fluidOutput[i] = buffer.readFluidStack();
		
		ItemStack[] byproducts = new ItemStack[buffer.readInt()];
		for(int i = 0;i < byproducts.length;i++)
			byproducts[i] = buffer.readItem();
		
		double[] chances = new double[buffer.readInt()];
		for(int i = 0;i < chances.length;i++)
			chances[i] = buffer.readDouble();
		
		FluidTagInput input = FluidTagInput.read(buffer);
		int energy = buffer.readInt();
		int time = buffer.readInt();
		
		return new DistillationTowerRecipe(recipeId, fluidOutput, byproducts, input, energy, time, chances);
	}
	
	@Override
	public void toNetwork(FriendlyByteBuf buffer, DistillationTowerRecipe recipe){
		buffer.writeInt(recipe.getFluidOutputs().size());
		for(FluidStack stack:recipe.getFluidOutputs())
			buffer.writeFluidStack(stack);
		
		buffer.writeInt(recipe.getItemOutputs().size());
		for(ItemStack stack:recipe.getItemOutputs())
			buffer.writeItem(stack);
		
		buffer.writeInt(recipe.chances().length);
		for(double d:recipe.chances())
			buffer.writeDouble(d);
		
		recipe.getInputFluid().write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.getTotalProcessTime());
	}
	
	@Override
	public ItemStack getIcon(){
		return new ItemStack(IPContent.Multiblock.DISTILLATIONTOWER.iconStack().getItem());
	}
}
