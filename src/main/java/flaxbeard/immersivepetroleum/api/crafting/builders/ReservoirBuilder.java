package flaxbeard.immersivepetroleum.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ReservoirBuilder extends IEFinishedRecipe<ReservoirBuilder>{
	private String fluid;
	private int fluidMinimum;
	private int fluidMaximum;
	private int fluidTrace;
	private int equilibrium;
	private int weight;
	
	private boolean isDimBlacklist = false;
	private final JsonArray dimensions = new JsonArray();
	
	private boolean isBioBlacklist = false;
	private final JsonArray biomes = new JsonArray();
	
	private ReservoirBuilder(){
		super(Serializers.RESERVOIR_SERIALIZER.get());
		addWriter(writer -> {
			writer.addProperty("fluid", this.fluid);
			writer.addProperty("fluidminimum", this.fluidMinimum);
			writer.addProperty("fluidcapacity", this.fluidMaximum);
			writer.addProperty("fluidtrace", this.fluidTrace);
			writer.addProperty("weight", this.weight);
			writer.addProperty("equilibrium", this.equilibrium);
		});
		
		addWriter(writer -> {
			JsonObject dimensions = new JsonObject();
			dimensions.add("isBlacklist", new JsonPrimitive(this.isDimBlacklist));
			dimensions.add("list", this.dimensions);
			writer.add("dimensions", dimensions);
		});
		
		addWriter(writer -> {
			JsonObject biomes = new JsonObject();
			biomes.add("isBlacklist", new JsonPrimitive(this.isBioBlacklist));
			biomes.add("list", this.biomes);
			writer.add("biomes", biomes);
		});
	}
	
	/**
	 * Creates a new ReservoirType builder instance.
	 * 
	 * @param name The name of the reservoir
	 * @return new {@link ReservoirBuilder} instance
	 */
	public static ReservoirBuilder builder(String name){
		return new ReservoirBuilder().addWriter(writer -> writer.addProperty("name", name));
	}
	
	/**
	 * Creates a new ReservoirType builder instance. This is a shorthand.
	 * 
	 * @param name   The name of the reservoir
	 * @param fluid  The type of fluid it holds
	 * @param min    The minimum amount of fluid the reservoir can hold
	 * @param max    The capacity of the reservoir
	 * @param trace  Trace amount of the fluid after being depleted
	 * @param weight chance for this reservoir to spawn
	 * @return The completed {@link ReservoirBuilder} once all parameters are added
	 */
	public static ReservoirBuilder builder(String name, Fluid fluid, double min, double max, double trace, int weight){
		return builder(name).setFluid(fluid).min(min).max(max).trace(trace).weight(weight);
	}
	
	/**
	 * Sets the fluid for this Reservoir.
	 * 
	 * @param fluid The fluid to set.
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder setFluid(@Nonnull Fluid fluid){
		this.fluid = Objects.requireNonNull(RegistryUtils.getRegistryNameOf(fluid)).toString();
		return this;
	}
	
	/**
	 * Sets minimum <code>amount</code> of fluid for this Reservoir. <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 * 
	 * @param amount The amount to set.
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder min(double amount){
		this.fluidMinimum = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * Sets maximum/capacity <code>amount</code> of fluid for this Reservoir. <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 * 
	 * @param amount The amount to set.
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder max(double amount){
		this.fluidMaximum = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * Replenish <code>amount</code> per tick. <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 * 
	 * @param amount The amount to set.
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder trace(double amount){
		this.fluidTrace = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * Reservoir Weight
	 * 
	 * @param weight the weight to provide the reservoir
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder weight(int weight){
		this.weight = weight;
		return this;
	}
	
	/**
	 * Sets maximum fluid <code>amount</code> for trace fluid to regenerate. <code><pre>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </pre></code>
	 *
	 * @param amount The amount to set.
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder equilibrium(double amount){
		this.equilibrium = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * <i>This may only be called once.</i><br>
	 * <br>
	 * Dimension check for this Reservoir.
	 * 
	 * @param isBlacklist Marks this as a blacklist when true. Whitelist otherwise.
	 * @param dimensions  Dimensions to blacklist/whitelist
	 * @return {@link ReservoirBuilder}
	 * @throws IllegalArgumentException when it has already been set
	 */
	public ReservoirBuilder setDimensions(boolean isBlacklist, @Nonnull ResourceLocation[] dimensions){
		if(!this.dimensions.isEmpty()){
			throw new IllegalArgumentException("Dimensions list already set.");
		}
		Objects.requireNonNull(dimensions);
		
		this.isDimBlacklist = isBlacklist;
		for(ResourceLocation rl:dimensions){
			if(rl != null){
				String rlString = rl.toString();
				if(!this.dimensions.contains(new JsonPrimitive(rlString))){
					this.dimensions.add(rlString);
				}
			}
		}
		
		return this;
	}
	
	/**
	 * <i>This may only be called once.</i><br>
	 * <br>
	 * Biome check for this Reservoir.
	 * 
	 * @param isBlacklist Marks this as a blacklist when true. Whitelist otherwise.
	 * @param biomes      Biomes to blacklist/whitelist
	 * @return {@link ReservoirBuilder}
	 * @throws IllegalArgumentException when it has already been set
	 */
	public ReservoirBuilder setBiomes(boolean isBlacklist, @Nonnull ResourceLocation[] biomes){
		if(!this.biomes.isEmpty()){
			throw new IllegalArgumentException("Biomes list already set.");
		}
		Objects.requireNonNull(biomes);
		
		this.isBioBlacklist = isBlacklist;
		for(ResourceLocation rl:biomes){
			if(rl != null){
				String rlString = rl.toString();
				if(!this.biomes.contains(new JsonPrimitive(rlString))){
					this.biomes.add(rlString);
				}
			}
		}
		
		return this;
	}
}
