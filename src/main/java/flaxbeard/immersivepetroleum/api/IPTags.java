package flaxbeard.immersivepetroleum.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class IPTags{
	private static final Map<TagKey<Block>, TagKey<Item>> toItemTag = new HashMap<>();
	
	public static class Blocks{
		public static final TagKey<Block> asphalt = createBlockTag(ResourceUtils.forge("asphalt"));
		public static final TagKey<Block> petcoke = createBlockTag(ResourceUtils.forge("storage_blocks/petcoke"));
		public static final TagKey<Block> waxBlock = createBlockTag(ResourceUtils.forge("storage_blocks/wax"));
		public static final TagKey<Block> paraffinWaxBlock = createBlockTag(ResourceUtils.forge("storage_blocks/wax/paraffin"));
	}
	
	public static class Items{
		public static final TagKey<Item> bitumen = createItemWrapper(ResourceUtils.forge("bitumen"));
		public static final TagKey<Item> petcoke = createItemWrapper(ResourceUtils.forge("coal_petcoke"));
		public static final TagKey<Item> petcokeDust = createItemWrapper(ResourceUtils.forge("dusts/coal_petcoke"));
		public static final TagKey<Item> petcokeStorage = createItemWrapper(ResourceUtils.forge("storage_blocks/coal_petcoke"));
		public static final TagKey<Item> paraffinWax = createItemWrapper(ResourceUtils.forge("wax/paraffin"));
		public static final TagKey<Item> wax = createItemWrapper(ResourceUtils.forge("wax"));
		public static final TagKey<Item> waxBlock = createItemWrapper(ResourceUtils.forge("storage_blocks/wax"));
		public static final TagKey<Item> paraffinWaxBlock = createItemWrapper(ResourceUtils.forge("storage_blocks/wax/paraffin"));
	}
	
	public static class Fluids{
		public static final TagKey<Fluid> crudeOil = createFluidWrapper(ResourceUtils.forge("crude_oil"));
		public static final TagKey<Fluid> diesel = createFluidWrapper(ResourceUtils.forge("diesel"));
		public static final TagKey<Fluid> diesel_sulfur = createFluidWrapper(ResourceUtils.forge("diesel_sulfur"));
		public static final TagKey<Fluid> gasoline = createFluidWrapper(ResourceUtils.forge("gasoline"));
		public static final TagKey<Fluid> lubricant = createFluidWrapper(ResourceUtils.forge("lubricant"));
		public static final TagKey<Fluid> napalm = createFluidWrapper(ResourceUtils.forge("napalm"));
		public static final TagKey<Fluid> naphtha = createFluidWrapper(ResourceUtils.forge("naphtha"));
		public static final TagKey<Fluid> naphtha_cracked = createFluidWrapper(ResourceUtils.forge("naphtha_cracked"));
		public static final TagKey<Fluid> benzene = createFluidWrapper(ResourceUtils.forge("benzene"));
		public static final TagKey<Fluid> propylene = createFluidWrapper(ResourceUtils.forge("propylene"));
		public static final TagKey<Fluid> ethylene = createFluidWrapper(ResourceUtils.forge("ethylene"));
		public static final TagKey<Fluid> lubricant_cracked = createFluidWrapper(ResourceUtils.forge("lubricant_cracked"));
		public static final TagKey<Fluid> kerosene = createFluidWrapper(ResourceUtils.forge("kerosene"));
		public static final TagKey<Fluid> gasoline_additives = createFluidWrapper(ResourceUtils.forge("gasoline_additives"));
	}
	
	public static class Utility{
		public static final TagKey<Fluid> burnableInFlarestack = createFluidWrapper(ResourceUtils.ip("burnable_in_flarestack"));
		public static final TagKey<Item> toolboxTools = createItemWrapper(ResourceUtils.ie("toolbox/tools"));
	}
	
	public static TagKey<Item> getItemTag(TagKey<Block> blockTag){
		Preconditions.checkArgument(toItemTag.containsKey(blockTag));
		return toItemTag.get(blockTag);
	}
	
	private static TagKey<Block> createBlockTag(ResourceLocation name){
		TagKey<Block> blockTag = createBlockWrapper(name);
		toItemTag.put(blockTag, createItemWrapper(name));
		return blockTag;
	}
	
	public static void forAllBlocktags(BiConsumer<TagKey<Block>, TagKey<Item>> out){
		for(Entry<TagKey<Block>, TagKey<Item>> entry:toItemTag.entrySet())
			out.accept(entry.getKey(), entry.getValue());
	}
	
	private static TagKey<Block> createBlockWrapper(ResourceLocation name){
		return BlockTags.create(name);
	}
	
	private static TagKey<Item> createItemWrapper(ResourceLocation name){
		return ItemTags.create(name);
	}
	
	private static TagKey<Fluid> createFluidWrapper(ResourceLocation name){
		return FluidTags.create(name);
	}
}
