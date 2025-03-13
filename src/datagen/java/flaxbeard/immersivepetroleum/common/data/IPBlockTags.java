package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class IPBlockTags extends BlockTagsProvider {
	public IPBlockTags(DataGenerator dataGen, CompletableFuture<HolderLookup.Provider> provider,  ExistingFileHelper exFileHelper){
		super(dataGen.getPackOutput(), provider, ImmersivePetroleum.MODID, exFileHelper);
	}
	
	@Override
	protected void addTags(HolderLookup.Provider provider){
		// IP Tags
		
		tag(IPTags.Blocks.asphalt).add(IPContent.Blocks.ASPHALT.get());
		tag(IPTags.Blocks.petcoke).add(IPContent.Blocks.PETCOKE.get());
		
		tag(IPTags.Blocks.waxBlock).add(IPContent.Blocks.PARAFFIN_WAX.get());
		tag(IPTags.Blocks.paraffinWaxBlock).add(IPContent.Blocks.PARAFFIN_WAX.get());
		
		// Minecraft Tags
		
		tag(BlockTags.STAIRS).add(IPContent.Blocks.ASPHALT_STAIR.get());
		tag(BlockTags.SLABS).add(IPContent.Blocks.ASPHALT_SLAB.get());
		
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
			IPContent.Blocks.ASPHALT.get(),
			IPContent.Blocks.ASPHALT_STAIR.get(),
			IPContent.Blocks.ASPHALT_SLAB.get(),
			IPContent.Blocks.PETCOKE.get(),
			IPContent.Blocks.GAS_GENERATOR.get(),
			IPContent.Blocks.FLARESTACK.get(),
			IPContent.Blocks.WELL_PIPE.get(),
			IPContent.Blocks.SEISMIC_SURVEY.get(),
			//MBs
			IPContent.Multiblock.DERRICK.block().get(),
			IPContent.Multiblock.PUMPJACK.block().get(),
			IPContent.Multiblock.OILTANK.block().get(),
			IPContent.Multiblock.DISTILLATIONTOWER.block().get(),
			IPContent.Multiblock.COKERUNIT.block().get(),
			IPContent.Multiblock.HYDROTREATER.block().get()
		);
		
		tag(BlockTags.MINEABLE_WITH_SHOVEL).add(IPContent.Blocks.PARAFFIN_WAX.get());
		
		tag(BlockTags.MINEABLE_WITH_AXE).add(
			IPContent.Blocks.AUTO_LUBRICATOR.get()
		);
		
		tag(BlockTags.NEEDS_STONE_TOOL).add(
			IPContent.Blocks.ASPHALT.get(),
			IPContent.Blocks.ASPHALT_STAIR.get(),
			IPContent.Blocks.ASPHALT_SLAB.get(),
			IPContent.Blocks.PETCOKE.get(),
			IPContent.Blocks.WELL_PIPE.get(),
			IPContent.Blocks.AUTO_LUBRICATOR.get()
		);
		
		tag(BlockTags.NEEDS_IRON_TOOL).add(
			IPContent.Blocks.GAS_GENERATOR.get(),
			IPContent.Blocks.FLARESTACK.get(),
			IPContent.Blocks.SEISMIC_SURVEY.get()
		);
	}
	
	private void setMiningLevel(Supplier<? extends Block> block, Tiers tier){
		TagKey<Block> with = switch(tier){
			case WOOD -> BlockTags.MINEABLE_WITH_AXE;
			case STONE, GOLD, IRON, DIAMOND, NETHERITE -> BlockTags.MINEABLE_WITH_PICKAXE;
			default -> throw new IllegalArgumentException("Unexpected value: " + tier);
		};
		tag(with).add(block.get());
		
		if(tier == Tiers.WOOD)
			return;
		
		TagKey<Block> type = switch(tier){
			case STONE -> BlockTags.NEEDS_STONE_TOOL;
			case IRON, GOLD -> BlockTags.NEEDS_IRON_TOOL;
			case DIAMOND, NETHERITE -> BlockTags.NEEDS_DIAMOND_TOOL;
			default -> throw new IllegalArgumentException("Unexpected value: " + tier);
		};
		
		tag(type).add(block.get());
	}
}
