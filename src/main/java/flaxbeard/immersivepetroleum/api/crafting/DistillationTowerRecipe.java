package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DistillationTowerRecipe extends IPMultiblockRecipe {
	public static Map<ResourceLocation, DistillationTowerRecipe> recipes = new HashMap<>();

	private static final RandomSource RANDOM = RandomSource.create();
	
	/** May return null! */
	public static DistillationTowerRecipe findRecipe(FluidStack input){
		if(!recipes.isEmpty()){
			for(DistillationTowerRecipe recipe:recipes.values()){
				//final DistillationTowerRecipe r = (DistillationTowerRecipe)holder.getRecipeUsed();
				if(recipe.input != null && recipe.input.testIgnoringAmount(input)){
					return recipe;
				}
			}
		}
		return null;
	}

	@Nullable
	public static DistillationTowerRecipe getRecipe(ResourceLocation id){
		return recipes.get(id);
	}

	@Nullable
	public static DistillationTowerRecipe loadFromNBT(CompoundTag nbt){
		FluidStack input = FluidStack.loadFluidStackFromNBT(nbt.getCompound("input"));
		return findRecipe(input);
	}

	protected final FluidTagInput input;
	protected final FluidStack[] fluidOutput;
	protected final ItemStack[] itemOutput;
	protected final double[] chances;

	public DistillationTowerRecipe(ResourceLocation id, FluidStack[] fluidOutput, ItemStack[] itemOutput, FluidTagInput input, int energy, int time, double[] chances){
		super(ItemStack.EMPTY, IPRecipeTypes.DISTILLATION, id);
		this.fluidOutput = fluidOutput;
		this.itemOutput = itemOutput;
		this.chances = chances;

		this.input = input;
		this.fluidInputList = Collections.singletonList(input);
		this.fluidOutputList = Arrays.asList(this.fluidOutput);
		this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, itemOutput));

		timeAndEnergy(time, energy);
		modifyTimeAndEnergy(IPServerConfig.REFINING.distillationTower_timeModifier::get, IPServerConfig.REFINING.distillationTower_energyModifier::get);
	}

	@Override
	protected IERecipeSerializer<DistillationTowerRecipe> getIESerializer(){
		return Serializers.DISTILLATION_SERIALIZER.get();
	}

	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		if(this.itemOutput.length == 0 && this.chances.length == 0)
			return NonNullList.create();

		NonNullList<ItemStack> output = NonNullList.create();
		for(int i = 0;i < this.itemOutput.length;i++){
			if(RANDOM.nextFloat() <= this.chances[i]){
				output.add(this.itemOutput[i]);
			}
		}

		return output;
	}

	public FluidTagInput getInputFluid(){
		return this.input;
	}

	public double[] chances(){
		return this.chances;
	}
}