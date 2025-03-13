package flaxbeard.immersivepetroleum.common.data;

public class IPBiomeModifierProvider{
/*	public static void method(DataGenerator generator, ExistingFileHelper exhelper, Consumer<DataProvider> add){
		//IPWorldGen.registerReservoirGen();
		
		final RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		
		final RegistryOps<JsonElement> jsonOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		final Registry<Biome> biomeReg = registryAccess.registryOrThrow(Registries.BIOME);
		final Registry<PlacedFeature> featureReg = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
		
		final AnyHolderSet<Biome> anyBiome = new AnyHolderSet<>(biomeReg.asLookup());
		
		final ImmutableMap.Builder<ResourceLocation, AddFeaturesBiomeModifier> modifiers = ImmutableMap.builder();
		for(Entry<String, Holder<PlacedFeature>> entry:IPWorldGen.features.entrySet()){
			ResourceLocation name = ResourceUtils.ip(entry.getKey());
			
			ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, name);
			Holder<PlacedFeature> featureHolder = featureReg.getHolderOrThrow(key);
			
			AddFeaturesBiomeModifier modifier = new AddFeaturesBiomeModifier(anyBiome, HolderSet.direct(featureHolder), Decoration.UNDERGROUND_ORES);
			modifiers.put(name, modifier);
		}
		
		add.accept(new JsonCodecProvider<>(generator.getPackOutput(), exhelper, ImmersivePetroleum.MODID, jsonOps, PackType.SERVER_DATA, "/worldgen/placed_feature/", ForgeMod.ADD_FEATURES_BIOME_MODIFIER_TYPE.get(), modifiers.build()));
	}
*/}
