package flaxbeard.immersivepetroleum.api.reservoir;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
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
import java.util.*;
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
	
	private BWList biomes = new BWList(false);
	private BWList dimensions = new BWList(false);
	
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
		super(EMPTY_LAZY, IPRecipeTypes.RESERVOIR, new ResourceLocation(nbt.getString("id")));
		
		this.name = nbt.getString("name");
		
		this.fluidLocation = new ResourceLocation(nbt.getString("fluid"));
		this.fluid = ForgeRegistries.FLUIDS.getValue(this.fluidLocation);
		
		this.minSize = nbt.getInt("minSize");
		this.maxSize = nbt.getInt("maxSize");
		this.residual = nbt.getInt("residual");
		this.equilibrium = nbt.getInt("equilibrium");
		
		this.biomes = new BWList(nbt.getCompound("biomes"));
		this.dimensions = new BWList(nbt.getCompound("dimensions"));
		
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
		
		nbt.put("biomes", this.biomes.toNbt());
		nbt.put("dimensions", this.dimensions.toNbt());
		
		nbt.putInt("weight", this.weight);
		
		return nbt;
	}
	
	public void setBiomes(boolean blacklist, ResourceLocation... names){
		setBiomes(blacklist, Arrays.asList(names));
	}
	
	public void setBiomes(boolean blacklist, List<ResourceLocation> names){
		this.biomes = new BWList(new HashSet<>(names), blacklist);
	}
	
	public void setDimensions(boolean blacklist, ResourceLocation... names){
		setDimensions(blacklist, Arrays.asList(names));
	}
	
	public void setDimensions(boolean blacklist, List<ResourceLocation> names){
		this.dimensions = new BWList(new HashSet<>(names), blacklist);
	}
	
	public Set<ResourceLocation> getBiomeList(){
		return this.biomes.getSet();
	}
	
	public Set<ResourceLocation> getDimensionList(){
		return this.dimensions.getSet();
	}
	
	public BWList getDimensions(){
		return this.dimensions;
	}
	
	public BWList getBiomes(){
		return this.biomes;
	}
	
	@Override
	@Nonnull
	public ItemStack getResultItem(RegistryAccess registryAccess){
		return ItemStack.EMPTY;
	}
	
	public Fluid getFluid(){
		return this.fluid;
	}
	
	@Override
	public String toString(){
		return this.writeToNBT().toString();
	}
	
	static Set<ResourceLocation> toSet(ListTag nbtList){
		Set<ResourceLocation> set = new HashSet<>();
		if(nbtList.size() > 0){
			nbtList.forEach(tag -> {
				if(tag instanceof StringTag){
					set.add(new ResourceLocation(tag.getAsString()));
				}
			});
		}
		return set;
	}
	
	static ListTag toNbt(Set<ResourceLocation> set){
		ListTag nbtList = new ListTag();
		if(set.size() > 0){
			set.forEach(rl -> nbtList.add(StringTag.valueOf(rl.toString())));
		}
		return nbtList;
	}
	
	/**
	 * Simple Black/White-List.
	 * 
	 * @author TwistedGate
	 */
	public static class BWList{
		private Set<ResourceLocation> set;
		private boolean isBlacklist;
		public BWList(boolean isBlacklist){
			this(new HashSet<>(), isBlacklist);
		}
		
		public BWList(Set<ResourceLocation> set, boolean isBlacklist){
			this.set = set;
			this.isBlacklist = isBlacklist;
		}
		
		public BWList(CompoundTag tag){
			this.isBlacklist = tag.getBoolean("isBlacklist");
			
			if(tag.contains("list", Tag.TAG_LIST)){
				ListTag list = tag.getList("list", Tag.TAG_STRING);
				
				Set<ResourceLocation> set = new HashSet<>();
				if(list.size() > 0){
					list.forEach(t -> {
						if(t instanceof StringTag){
							set.add(new ResourceLocation(t.getAsString()));
						}
					});
				}
				this.set = set;
			}else{
				this.set = new HashSet<>();
			}
		}
		
		public boolean isBlacklist(){
			return this.isBlacklist;
		}
		
		public boolean add(ResourceLocation rl){
			return this.set.add(rl);
		}
		
		public boolean addAll(Collection<? extends ResourceLocation> c){
			return this.set.addAll(c);
		}
		
		public boolean hasEntries(){
			return this.set.size() > 0;
		}
		
		public boolean valid(ResourceLocation rl){
			if(this.set.isEmpty()){
				// An empty set is considered to be "allow anywhere". Regardless of "isBlacklist" value.
				return true;
			}
			
			boolean contains = this.set.contains(rl);
			return this.isBlacklist ? !contains : contains;
		}
		
		public Set<ResourceLocation> getSet(){
			return Collections.unmodifiableSet(this.set);
		}
		
		public void forEach(Consumer<ResourceLocation> action){
			this.set.forEach(action);
		}
		
		public CompoundTag toNbt(){
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("isBlacklist", this.isBlacklist);
			tag.put("list", toNbtList());
			return tag;
		}
		
		private ListTag toNbtList(){
			ListTag nbtList = new ListTag();
			if(this.set.size() > 0){
				this.set.forEach(rl -> nbtList.add(StringTag.valueOf(rl.toString())));
			}
			return nbtList;
		}
	}
}
