package flaxbeard.immersivepetroleum.common.util;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.FORGE)
public class RegistryUtils{
	private static Registry<Biome> BIOME_REGISTRY;
	
	@SubscribeEvent
	public static void serverStart(ServerStartingEvent event){
		BIOME_REGISTRY = event.getServer().registryAccess().registryOrThrow(Registries.BIOME);
	}
	
	public static Optional<List<Holder<Biome>>> listBiomesInTag(TagKey<Biome> tag){
		return BIOME_REGISTRY.getTag(tag).map(holders -> holders.stream().toList());
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(Item item){
		return ForgeRegistries.ITEMS.getKey(item);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(Block block){
		return ForgeRegistries.BLOCKS.getKey(block);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(Fluid fluid){
		return ForgeRegistries.FLUIDS.getKey(fluid);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(Biome biome){
		return ForgeRegistries.BIOMES.getKey(biome);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(SoundEvent soundEvent){
		return ForgeRegistries.SOUND_EVENTS.getKey(soundEvent);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(MobEffect mobEffect){
		return ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(EntityType<?> entityType){
		return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(BlockEntityType<?> blockEntityType){
		return ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntityType);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(ParticleType<?> particleType){
		return ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(RecipeType<?> recipeType){
		return ForgeRegistries.RECIPE_TYPES.getKey(recipeType);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(RecipeSerializer<?> recipeSerializer){
		return ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipeSerializer);
	}
	
	@Nullable
	public static ResourceLocation getRegistryNameOf(MenuType<?> menuType){
		return ForgeRegistries.MENU_TYPES.getKey(menuType);
	}
	
	private RegistryUtils(){
	}
}
