package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.data.loot.IPBlockLoot;
import flaxbeard.immersivepetroleum.common.data.loot.IPLoot;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPDataGenerator{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/DataGenerator");
	
	@SubscribeEvent
	public static void generate(GatherDataEvent event){
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper exhelper = event.getExistingFileHelper();
		//StaticTemplateManager.EXISTING_HELPER = exhelper;
		CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

		if(event.includeServer()){
			IPBlockTags blockTags = new IPBlockTags(generator, provider, exhelper);
			generator.addProvider(true, blockTags);
			generator.addProvider(true, new IPItemTags(generator, provider, blockTags, exhelper));
			generator.addProvider(true, new IPFluidTags(generator, provider, exhelper));
			generator.addProvider(true, new LootTableProvider(generator.getPackOutput(), Collections.emptySet(), List.of(
					new LootTableProvider.SubProviderEntry(IPBlockLoot::new, LootContextParamSets.BLOCK),
					new LootTableProvider.SubProviderEntry(IPLoot::new, LootContextParamSets.ADVANCEMENT_REWARD)
			)));
			generator.addProvider(true, new IPRecipes(generator));
			generator.addProvider(true, new IPAdvancements(generator, provider, exhelper));
			
			generator.addProvider(true, new IPBlockStates(generator, exhelper));
			generator.addProvider(true, new IPItemModels(generator, exhelper));

			List<DataProvider> providers = IPWorldGen.makeProviders(generator.getPackOutput(), provider);
			if (providers != null && !providers.isEmpty())
			{
				for(final DataProvider data : providers)
				{
					generator.addProvider(true, data);
				}
			}

			generator.addProvider(true, new IPMultiblockTextutesAttach(generator.getPackOutput(), exhelper));
			//IPBiomeModifierProvider.method(generator, exhelper, d -> generator.addProvider(true, d));
		}
	}
}
