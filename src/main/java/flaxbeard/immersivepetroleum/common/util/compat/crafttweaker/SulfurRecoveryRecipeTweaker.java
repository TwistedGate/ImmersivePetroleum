package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Document("mods/immersivepetroleum/SRU")
@Name("mods.immersivepetroleum.Hydrotreater")
public class SulfurRecoveryRecipeTweaker implements IRecipeManager<HighPressureRefineryRecipe>{
	@Override
	public RecipeType<HighPressureRefineryRecipe> getRecipeType(){
		return IPRecipeTypes.HYDROTREATER.get();
	}
	
	/**
	 * Removes all recipes
	 */
	@Method
	public void removeAll(){
		HighPressureRefineryRecipe.recipes.clear();
	}
	
	@Method
	public void removeByOutputItem(IIngredient output){
		HighPressureRefineryRecipe.recipes.values().removeIf(recipe -> output.matches(new MCItemStack(recipe.outputItem)));
	}
	
	@Method
	public void removeByOutputFluid(IFluidStack output){
		HighPressureRefineryRecipe.recipes.values().removeIf(recipe -> recipe.output.isFluidEqual(output.<FluidStack>getInternal()));
	}
	
	@Method
	public void addRecipe(String name, IFluidStack output, IItemStack outputItem, double chance, Many<KnownTag<Fluid>> inputFluid, int energy){
		name = fixRecipeName(name);
		
		ResourceLocation id = ResourceUtils.ct("hydrotreater/" + name);
		
		FluidTagInput primary = new FluidTagInput(inputFluid.getData().getTagKey(), inputFluid.getAmount());
		
		newRecipe(id, output, outputItem, chance, primary, null, energy);
	}
	
	@Method
	public void addRecipeWithSecondary(String name, IFluidStack output, IItemStack outputItem, double chance, Many<KnownTag<Fluid>> inputFluid, Many<KnownTag<Fluid>> inputFluidSecondary, int energy){
		name = fixRecipeName(name);
		
		ResourceLocation id = ResourceUtils.ct("hydrotreater/" + name);
		
		FluidTagInput primary = new FluidTagInput(inputFluid.getData().getTagKey(), inputFluid.getAmount());
		FluidTagInput secondary = new FluidTagInput(inputFluidSecondary.getData().getTagKey(), inputFluidSecondary.getAmount());
		
		newRecipe(id, output, outputItem, chance, primary, secondary, energy);
	}
	
	private void newRecipe(ResourceLocation id, IFluidStack output, IItemStack outputItem, double chance, FluidTagInput primary, FluidTagInput secondary, int energy){
		HighPressureRefineryRecipe recipe = new HighPressureRefineryRecipe(id, output.getInternal(), outputItem.getInternal(), primary, secondary, chance, energy, 1);
		// Does NOT work with this
		//CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe));
		
		// This however does, while it may not be the safest thing to do..
		HighPressureRefineryRecipe.recipes.put(id, recipe);
	}
}
