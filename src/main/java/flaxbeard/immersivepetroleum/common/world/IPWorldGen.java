package flaxbeard.immersivepetroleum.common.world;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public class IPWorldGen{
//	public static Map<String, Holder<PlacedFeature>> features = new HashMap<>();

	/*public static void registerReservoirGen(){
		Holder<PlacedFeature> reservoirFeature = register(RESERVOIR_FEATURE.getId(), RESERVOIR_FEATURE, new NoneFeatureConfiguration());
		features.put(RESERVOIR_FEATURE.getId().getPath(), reservoirFeature);
	}*/
	
	/*
	@SubscribeEvent
	public void onBiomeLoad(BiomeLoadingEvent event){
		BiomeGenerationSettingsBuilder generation = event.getGeneration();
		for(Entry<String, Holder<PlacedFeature>> entry:features.entrySet()){
			generation.addFeature(Decoration.UNDERGROUND_ORES, entry.getValue());
		}
	}
	*/
	
	/*private static <Cfg extends FeatureConfiguration, F extends Feature<Cfg>> Holder<PlacedFeature> register(ResourceLocation rl, RegistryObject<F> feature, Cfg cfg){
		Holder<ConfiguredFeature<?, ?>> configured = BuiltInRegistries.register(Registries.CONFIGURED_FEATURE, rl, new ConfiguredFeature<>(feature.get(), cfg));
		return BuiltInRegistries.register(Registries.PLACED_FEATURE, rl, new PlacedFeature(configured, List.of()));
	}*/
}
