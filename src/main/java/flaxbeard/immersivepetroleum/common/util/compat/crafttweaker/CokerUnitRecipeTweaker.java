package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Document("mods/immersivepetroleum/Coker")
@Name("mods.immersivepetroleum.CokerUnit")
public class CokerUnitRecipeTweaker implements IRecipeManager<CokerUnitRecipe>{
	@Override
	public RecipeType<CokerUnitRecipe> getRecipeType(){
		return IPRecipeTypes.COKER.get();
	}
	
	/**
	 * Removes all recipes
	 */
	@Method
	public void removeAll(){
		CokerUnitRecipe.recipes.clear();
	}
	
	/**
	 * Removes all recipes that output the given IIngredient.
	 * 
	 * @param output {@link IIngredient} output to remove
	 * @docParam output <item:immersivepetroleum:petcoke>
	 * @docParam output <tag:items:forge:coal_petcoke>
	 */
	@Method
	public void remove(IIngredient output){
		CokerUnitRecipe.recipes.values().
				removeIf(recipe -> output.matches(new MCItemStack(recipe.outputItem.copy())));
	}
	
	/**
	 * Removes all recipes that output the given IFluidStack.
	 * 
	 * @param output <fluid:immersivepetroleum:diesel>
	 */
	@Method
	public void remove(IFluidStack output){
		CokerUnitRecipe.recipes.values().
				removeIf(recipe -> recipe.outputFluid.isFluidEqual(output.<FluidStack>getInternal()));
	}
	
	/**
	 * Adds a recipe to the Coker
	 * 
	 * @param name        The recipe name, without the resource location
	 * @param inputItem   The input ingredient
	 * @param outputItem  The output ingredient
	 * @param inputFluid  The input fluid
	 * @param outputFluid The output fluid
	 * @param energy      energy required per tick
	 * @docParam name "clay_from_sand"
	 * @docParam inputItem <item:minecraft:sand>
	 * @docParam outputItem <item:minecraft:clay_ball>
	 * @docParam inputFluid <tag:fluids:minecraft:water> * 125
	 * @docParam outputFluid <tag:fluids:minecraft:water> * 25
	 * @docParam energy 1024
	 */
	@Method
	public void addRecipe(String name, IItemStack inputItem, IItemStack outputItem, Many<KnownTag<Fluid>> inputFluid, IFluidStack outputFluid, int energy){
		name = fixRecipeName(name);
		
		ResourceLocation id = ResourceUtils.ct("cokerunit/" + name);
		FluidStack outFluid = outputFluid.getInternal();
		FluidTagInput inFluid = new FluidTagInput(inputFluid.getData().getTagKey(), inputFluid.getAmount());
		
		IngredientWithSize inStack = new IngredientWithSize(Ingredient.of(inputItem.getInternal()), inputItem.getAmount());
		Lazy<ItemStack> outStack = Lazy.of(outputItem::getInternal);
		
		CokerUnitRecipe recipe = new CokerUnitRecipe(id, outStack, outFluid, inStack, inFluid, energy, 30);
		
		// Does NOT work with this
		//CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe));
		
		// This however does, while it may not be the safest thing to do..
		CokerUnitRecipe.recipes.put(id, recipe);
	}
}
