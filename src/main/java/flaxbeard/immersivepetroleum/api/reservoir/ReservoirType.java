package flaxbeard.immersivepetroleum.api.reservoir;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListBiome;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListDimension;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ReservoirType extends IESerializableRecipe{
	static final Lazy<ItemStack> EMPTY_LAZY = Lazy.of(() -> ItemStack.EMPTY);
	
	public static Map<ResourceLocation, ReservoirType> map = new HashMap<>();
	
	public final String name;
	public final ResourceLocation fluidLocation;
	public final int weight;
	
	public final int minSize;
	public final int maxSize;
	public final int residual;
	public final int equilibrium;
	
	private final Fluid fluid;
	
	private BWListBiome biomes = new BWListBiome(false);
	private BWListDimension dimensions = new BWListDimension(false);
	
	/**
	 * Creates a new reservoir.
	 *
	 * @param name          The name of this reservoir type
	 * @param id            The "recipeId" of this reservoir
	 * @param fluidLocation The registry name of the fluid this reservoir is containing
	 * @param minSize       Minimum amount of fluid in this reservoir
	 * @param maxSize       Maximum amount of fluid in this reservoir
	 * @param residual      Leftover fluid amount after depletion
	 * @param equilibrium   Maximum amount of fluid that residuals regenerate at
	 * @param weight        The weight for this reservoir
	 */
	public ReservoirType(String name, ResourceLocation id, ResourceLocation fluidLocation, int minSize, int maxSize, int residual, int equilibrium, int weight){
		this(name, id, ForgeRegistries.FLUIDS.getValue(fluidLocation), minSize, maxSize, residual, equilibrium, weight);
	}
	
	/**
	 * Creates a new reservoir.
	 * 
	 * @param name     The name of this reservoir type
	 * @param id       The "recipeId" of this reservoir
	 * @param fluid    The fluid this reservoir is containing
	 * @param minSize  Minimum amount of fluid in this reservoir
	 * @param maxSize  Maximum amount of fluid in this reservoir
	 * @param residual      Leftover fluid amount after depletion
	 * @param equilibrium   Maximum amount of fluid that residuals regenerate at
	 * @param weight   The weight for this reservoir
	 */
	public ReservoirType(String name, ResourceLocation id, Fluid fluid, int minSize, int maxSize, int residual, int equilibrium, int weight){
		super(EMPTY_LAZY, IPRecipeTypes.RESERVOIR, id);
		this.name = name;
		this.fluidLocation = RegistryUtils.getRegistryNameOf(fluid);
		this.fluid = fluid;
		this.residual = residual;
		this.equilibrium = equilibrium;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.weight = weight;
	}
	
	public ReservoirType(CompoundTag nbt){
		super(EMPTY_LAZY, IPRecipeTypes.RESERVOIR, ResourceLocation.parse(nbt.getString("id")));
		
		this.name = nbt.getString("name");
		
		this.fluidLocation = ResourceLocation.parse(nbt.getString("fluid"));
		this.fluid = ForgeRegistries.FLUIDS.getValue(this.fluidLocation);
		
		this.minSize = nbt.getInt("minSize");
		this.maxSize = nbt.getInt("maxSize");
		this.residual = nbt.getInt("residual");
		this.equilibrium = nbt.getInt("equilibrium");
		
		this.biomes.readFromNbt(nbt.getCompound("biomes"));
		this.dimensions.readFromNbt(nbt.getCompound("dimensions"));
		
		this.weight = nbt.getInt("weight");
	}
	
	@Override
	protected IERecipeSerializer<ReservoirType> getIESerializer(){
		return Serializers.RESERVOIR_SERIALIZER.get();
	}
	
	public CompoundTag writeToNBT(){
		return writeToNBT(new CompoundTag());
	}
	
	public CompoundTag writeToNBT(CompoundTag nbt){
		nbt.putString("name", this.name);
		nbt.putString("id", this.id.toString());
		nbt.putString("fluid", this.fluidLocation.toString());
		
		nbt.putInt("minSize", this.minSize);
		nbt.putInt("maxSize", this.maxSize);
		nbt.putInt("residual", this.residual);
		nbt.putInt("equilibrium", this.equilibrium);
		
		nbt.put("biomes", this.biomes.writeToNbt());
		nbt.put("dimensions", this.dimensions.writeToNbt());
		
		nbt.putInt("weight", this.weight);
		
		return nbt;
	}
	
	@Deprecated(forRemoval = true)
	public void setBiomes(boolean blacklist, List<ResourceLocation> names){
		throw new UnsupportedOperationException();
	}
	
	@Deprecated(forRemoval = true)
	public void setDimensions(boolean blacklist, List<ResourceLocation> names){
		throw new UnsupportedOperationException();
	}
	
	public void setBiomes(@Nonnull BWListBiome list){
		this.biomes = Objects.requireNonNull(list);
	}
	
	public void setDimensions(@Nonnull BWListDimension list){
		this.dimensions = Objects.requireNonNull(list);
	}
	
	public BWListDimension getDimensions(){
		return this.dimensions;
	}
	
	public BWListBiome getBiomes(){
		return this.biomes;
	}
	
	@Nonnull
	@Override
	public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess){
		return ItemStack.EMPTY;
	}
	
	public Fluid getFluid(){
		return this.fluid;
	}
	
	@Override
	public String toString(){
		return this.writeToNBT().toString();
	}
}
