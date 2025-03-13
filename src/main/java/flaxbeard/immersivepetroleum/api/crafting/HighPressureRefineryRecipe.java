package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HighPressureRefineryRecipe extends IPMultiblockRecipe {
	
	public static Map<ResourceLocation, HighPressureRefineryRecipe> recipes = new HashMap<>();

	private static final RandomSource RANDOM = RandomSource.create();

	public static HighPressureRefineryRecipe findRecipe(@Nonnull FluidStack input, @Nonnull FluidStack secondary){
		Objects.requireNonNull(input);
		Objects.requireNonNull(secondary);

		for(HighPressureRefineryRecipe recipe : recipes.values()){
			//final HighPressureRefineryRecipe recipe = (HighPressureRefineryRecipe) holder.getRecipeUsed();

			if(secondary.isEmpty()){
				if(recipe.inputFluidSecondary == null && (recipe.inputFluid != null && recipe.inputFluid.test(input))){
					return recipe;
				}
			}else{
				if((recipe.inputFluid != null && recipe.inputFluid.test(input)) && (recipe.inputFluidSecondary != null && recipe.inputFluidSecondary.test(secondary))){
					return recipe;
				}
			}
		}
		return null;
	}

	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);

		if(!fluid.isEmpty()){
			for(HighPressureRefineryRecipe recipe : recipes.values()){
				//final HighPressureRefineryRecipe recipe = (HighPressureRefineryRecipe) holder.getRecipeUsed();

				if(recipe.inputFluid != null){
					if((!ignoreAmount && recipe.inputFluid.test(fluid)) || (ignoreAmount && recipe.inputFluid.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean hasRecipeWithSecondaryInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(HighPressureRefineryRecipe recipe :recipes.values()){
				//final HighPressureRefineryRecipe recipe = (HighPressureRefineryRecipe) holder.getRecipeUsed();
				if(recipe.inputFluidSecondary != null){
					if((!ignoreAmount && recipe.inputFluidSecondary.test(fluid)) || (ignoreAmount && recipe.inputFluidSecondary.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	public final ItemStack outputItem;
	public final double chance;

	public final FluidStack output;

	public final FluidTagInput inputFluid;
	@Nullable
	public final FluidTagInput inputFluidSecondary;

	/**
	 * @param id                  {@link ResourceLocation} ID to create the recipe with
	 * @param output              {@link FluidStack} to output
	 * @param outputItem          {@link ItemStack} to output
	 * @param inputFluid          {@link FluidStack} to input
	 * @param inputFluidSecondary {@link FluidStack} for secondary input
	 * @param chance              double chance of the {@link ItemStack} output
	 * @param energy              amount of FE to consume
	 * @param time                duration of the recipe
	 */
	public HighPressureRefineryRecipe(ResourceLocation id, FluidStack output, ItemStack outputItem, FluidTagInput inputFluid, @Nullable FluidTagInput inputFluidSecondary, double chance, int energy, int time){
		super(ItemStack.EMPTY, IPRecipeTypes.HYDROTREATER, id);
		this.output = output;
		this.outputItem = outputItem;
		this.inputFluid = inputFluid;
		this.inputFluidSecondary = inputFluidSecondary;
		this.chance = chance;

		this.fluidOutputList = Collections.singletonList(output);
		this.fluidInputList = Arrays.asList(inputFluidSecondary != null ? new FluidTagInput[]{inputFluid, inputFluidSecondary} : new FluidTagInput[]{inputFluid});

		timeAndEnergy(time, energy);
		modifyTimeAndEnergy(IPServerConfig.REFINING.hydrotreater_timeModifier::get, IPServerConfig.REFINING.hydrotreater_energyModifier::get);
	}

	public boolean hasSecondaryItem(){
		return this.outputItem != null && !this.outputItem.isEmpty();
	}

	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}

	public FluidTagInput getInputFluid(){
		return this.inputFluid;
	}

	@Nullable
	public FluidTagInput getSecondaryInputFluid(){
		return this.inputFluidSecondary;
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs() {
		NonNullList<ItemStack> list = NonNullList.create();
		if (RANDOM.nextFloat() <= chance) {
			list.add(this.outputItem);
		}
		return list;
	}

	@Override
	protected IERecipeSerializer<HighPressureRefineryRecipe> getIESerializer(){
		return Serializers.HYDROTREATER_SERIALIZER.get();
	}
}
