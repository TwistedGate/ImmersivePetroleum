package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListBiome;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListDimension;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReservoirSerializer extends IERecipeSerializer<ReservoirType>{
	@Override
	public ReservoirType readFromJson(ResourceLocation recipeId, JsonObject json, IContext context){
		String name = GsonHelper.getAsString(json, "name");
		ResourceLocation fluid = ResourceLocation.parse(GsonHelper.getAsString(json, "fluid"));
		int min = GsonHelper.getAsInt(json, "fluidminimum");
		int max = GsonHelper.getAsInt(json, "fluidcapacity");
		int trace = GsonHelper.getAsInt(json, "fluidtrace");
		int equilibrium = GsonHelper.getAsInt(json, "equilibrium", 0);
		int weight = GsonHelper.getAsInt(json, "weight");
		
		ReservoirType reservoir = new ReservoirType(name, recipeId, fluid, min, max, trace, equilibrium, weight);
		
		readBiomes(json, reservoir::setBiomes);
		readDimensions(json, reservoir::setDimensions);
		
		ImmersivePetroleum.log.debug("Loaded reservoir {} as {}, with {}mB to {}mB of {} and {}mB trace at {}mB equilibrium, with {} of weight.",
				recipeId, name, min, max, fluid, trace, equilibrium, weight);
		
		return reservoir;
	}
	
	private void readBiomes(JsonObject json, Consumer<BWListBiome> setBiomes){
		if(!GsonHelper.isValidNode(json, "biomes"))
			return;
		
		json = GsonHelper.getAsJsonObject(json, "biomes");
		
		if(!GsonHelper.isValidNode(json, "list"))
			return;
		
		JsonArray array = GsonHelper.getAsJsonArray(json, "list");
		
		Set<BWListBiome.Validator> set = new HashSet<>();
		array.forEach(rl -> set.add(new BWListBiome.Validator(rl.getAsString())));
		
		boolean isBlacklist = GsonHelper.getAsBoolean(json, "isBlacklist");
		
		setBiomes.accept(new BWListBiome(set, isBlacklist));
	}
	
	private void readDimensions(JsonObject json, Consumer<BWListDimension> setDimensions){
		if(!GsonHelper.isValidNode(json, "dimensions"))
			return;
		
		json = GsonHelper.getAsJsonObject(json, "dimensions");
		
		if(!GsonHelper.isValidNode(json, "list"))
			return;
		
		JsonArray array = GsonHelper.getAsJsonArray(json, "list");
		
		Set<BWListDimension.Validator> set = new HashSet<>();
		array.forEach(rl -> set.add(new BWListDimension.Validator(rl.getAsString())));
		
		boolean isBlacklist = GsonHelper.getAsBoolean(json, "isBlacklist");
		
		setDimensions.accept(new BWListDimension(set, isBlacklist));
	}
	
	private void readBWListFromJson(JsonObject json, String key, BiConsumer<Boolean, List<ResourceLocation>> consumer){
		if(!GsonHelper.isValidNode(json, key))
			return;
		
		json = GsonHelper.getAsJsonObject(json, key);
		
		if(!GsonHelper.isValidNode(json, "list"))
			return;
		
		JsonArray array = GsonHelper.getAsJsonArray(json, "list");
		
		List<ResourceLocation> list = new ArrayList<>();
		array.forEach(rl -> list.add(ResourceLocation.parse(rl.getAsString())));
		
		boolean isBlacklist = GsonHelper.getAsBoolean(json, "isBlacklist");
		consumer.accept(isBlacklist, list);
	}
	
	@Nullable
	@Override
	public ReservoirType fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer){
		String name = buffer.readUtf();
		ResourceLocation id = buffer.readResourceLocation();
		ResourceLocation fluidLocation = buffer.readResourceLocation();
		
		int minSize = buffer.readInt();
		int maxSize = buffer.readInt();
		int residual = buffer.readInt();
		int equilibrium = buffer.readInt();
		int weight = buffer.readInt();
		
		ReservoirType type = new ReservoirType(name, id, fluidLocation, minSize, maxSize, residual, equilibrium, weight);
		
		type.setBiomes(BWListBiome.decode(buffer));
		type.setDimensions(BWListDimension.decode(buffer));
		
		return type;
	}
	
	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull ReservoirType recipe){
		buffer.writeUtf(recipe.name);
		buffer.writeResourceLocation(recipe.getId());
		buffer.writeResourceLocation(recipe.fluidLocation);
		
		buffer.writeInt(recipe.minSize);
		buffer.writeInt(recipe.maxSize);
		buffer.writeInt(recipe.residual);
		buffer.writeInt(recipe.equilibrium);
		buffer.writeInt(recipe.weight);
		
		recipe.getBiomes().encode(buffer);
		recipe.getDimensions().encode(buffer);
	}
	
	@Override
	public ItemStack getIcon(){
		return ItemStack.EMPTY;
	}
}
