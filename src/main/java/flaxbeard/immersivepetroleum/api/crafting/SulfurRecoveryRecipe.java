package flaxbeard.immersivepetroleum.api.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class SulfurRecoveryRecipe extends MultiblockRecipe{
	public static final IRecipeType<SulfurRecoveryRecipe> TYPE = IRecipeType.register(ImmersivePetroleum.MODID + ":hydrotreater");
	
	public static Map<ResourceLocation, SulfurRecoveryRecipe> recipes = new HashMap<>();
	
	public static SulfurRecoveryRecipe findRecipe(FluidStack fluid0, FluidStack fluid1){
		for(SulfurRecoveryRecipe recipe:recipes.values()){
			if((recipe.inputFluid0 != null && recipe.inputFluid0.test(fluid0)) && (recipe.inputFluid0 != null && recipe.inputFluid0.test(fluid1))){
				return recipe;
			}
		}
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(SulfurRecoveryRecipe recipe:recipes.values()){
				if(recipe.inputFluid0 != null){
					if((!ignoreAmount && recipe.inputFluid0.test(fluid)) || (ignoreAmount && recipe.inputFluid0.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public ItemStack outputItem;
	public FluidStack output;
	public FluidTagInput inputFluid0;
	public FluidTagInput inputFluid1;
	
	public float chance;
	
	protected int totalProcessTime;
	protected int totalProcessEnergy;
	
	public SulfurRecoveryRecipe(ResourceLocation id, FluidStack output, ItemStack outputItem, FluidTagInput inputFluid0, FluidTagInput inputFluid1, float chance, int energy, int time){
		super(ItemStack.EMPTY, TYPE, id);
		this.output = output;
		this.outputItem = outputItem;
		this.inputFluid0 = inputFluid0;
		this.inputFluid1 = inputFluid1;
		this.chance = chance;
		
		this.totalProcessEnergy = (int) Math.floor(energy * IPServerConfig.REFINING.hydrotreater_energyModifier.get());
		this.totalProcessTime = (int) Math.floor(time * IPServerConfig.REFINING.hydrotreater_timeModifier.get());
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public int getTotalProcessTime(){
		return this.totalProcessTime;
	}
	
	@Override
	public int getTotalProcessEnergy(){
		return this.totalProcessEnergy;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile){
		NonNullList<ItemStack> list = NonNullList.create();
		if(tile.getWorld().rand.nextFloat() <= chance){
			list.add(this.outputItem);
		}
		return list;
	}
	
	@Override
	protected IERecipeSerializer<SulfurRecoveryRecipe> getIESerializer(){
		return Serializers.HYDROTREATER_SERIALIZER.get();
	}
}
