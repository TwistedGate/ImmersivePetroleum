package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CokerUnitRecipe extends IPMultiblockRecipe{
	public static Map<ResourceLocation, RecipeHolder<CokerUnitRecipe>> recipes = new HashMap<>();
	
	@Nullable
	public static RecipeHolder<CokerUnitRecipe> findRecipe(ItemStack stack, FluidStack fluid){
		for(RecipeHolder<CokerUnitRecipe> holder: recipes.values()){
			CokerUnitRecipe recipe = holder.value();
			
			if((recipe.inputItem != null && recipe.inputItem.test(stack)) && (recipe.inputFluid != null && recipe.inputFluid.test(fluid))){
				return holder;
			}
		}
		
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull ItemStack stack, @Nonnull FluidStack fluid){
		Objects.requireNonNull(stack);
		Objects.requireNonNull(fluid);
		
		if(!stack.isEmpty() && !fluid.isEmpty()){
			for(RecipeHolder<CokerUnitRecipe> holder: recipes.values()){
				CokerUnitRecipe recipe = holder.value();
				
				if(recipe.inputItem != null && recipe.inputFluid != null && recipe.inputItem.test(stack) && recipe.inputFluid.test(fluid)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull ItemStack stack, boolean ignoreAmount){
		Objects.requireNonNull(stack);
		
		if(!stack.isEmpty()){
			for(RecipeHolder<CokerUnitRecipe> holder: recipes.values()){
				CokerUnitRecipe recipe = holder.value();
				
				if(recipe.inputItem != null && test(recipe.inputItem, stack, ignoreAmount)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(RecipeHolder<CokerUnitRecipe> holder: recipes.values()){
				CokerUnitRecipe recipe = holder.value();
				
				if(recipe.inputFluid != null && test(recipe.inputFluid, fluid, ignoreAmount)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private final ItemStack outputItem;
	private final FluidStack outputFluid;
	
	private final IngredientWithSize inputItem;
	private final SizedFluidIngredient inputFluid;

	private final int outputItemMaxStack;
	
	public CokerUnitRecipe(ItemStack outputItem, FluidStack outputFluid, IngredientWithSize inputItem, SizedFluidIngredient inputFluid, int energy, int time){
		super(IPRecipeTypes.COKER, time, energy);
		this.outputFluid = outputFluid;
		this.outputItem = outputItem;
		this.inputFluid = inputFluid;
		this.inputItem = inputItem;
		this.outputItemMaxStack = outputItem.getCount() > 0 ? outputItem.getMaxStackSize() / outputItem.getCount():1;
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.cokerUnit_timeModifier::get, IPServerConfig.REFINING.cokerUnit_energyModifier::get);
	}
	
	public ItemStack getOutputItem(){
		return this.outputItem.copy();
	}
	
	public FluidStack getOutputFluid(){
		return this.outputFluid.copy();
	}
	
	public IngredientWithSize getInputItem(){
		return this.inputItem;
	}
	
	public SizedFluidIngredient getInputFluid(){
		return this.inputFluid;
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(getOutputItem());
		return list;
	}

	public int getOutputItemMaxStack() {
		return this.outputItemMaxStack;
	}
	
	@Override
	protected IERecipeSerializer<CokerUnitRecipe> getIESerializer(){
		return Serializers.COKER_SERIALIZER.get();
	}
}
