package flaxbeard.immersivepetroleum.common.data.loot;

import blusunrize.immersiveengineering.common.util.loot.DropInventoryLootEntry;
import blusunrize.immersiveengineering.common.util.loot.MultiblockDropsLootContainer;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.util.loot.IPTileDropLootEntry;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IPBlockLoot extends BlockLootSubProvider
{
	private static final Set<Item> EXPLOSION_RESISTANT = Stream.of(
					Blocks.DRAGON_EGG,
					Blocks.BEACON,
					Blocks.CONDUIT,
					Blocks.SKELETON_SKULL,
					Blocks.WITHER_SKELETON_SKULL,
					Blocks.PLAYER_HEAD,
					Blocks.ZOMBIE_HEAD,
					Blocks.CREEPER_HEAD,
					Blocks.DRAGON_HEAD,
					Blocks.PIGLIN_HEAD,
					Blocks.SHULKER_BOX,
					Blocks.BLACK_SHULKER_BOX,
					Blocks.BLUE_SHULKER_BOX,
					Blocks.BROWN_SHULKER_BOX,
					Blocks.CYAN_SHULKER_BOX,
					Blocks.GRAY_SHULKER_BOX,
					Blocks.GREEN_SHULKER_BOX,
					Blocks.LIGHT_BLUE_SHULKER_BOX,
					Blocks.LIGHT_GRAY_SHULKER_BOX,
					Blocks.LIME_SHULKER_BOX,
					Blocks.MAGENTA_SHULKER_BOX,
					Blocks.ORANGE_SHULKER_BOX,
					Blocks.PINK_SHULKER_BOX,
					Blocks.PURPLE_SHULKER_BOX,
					Blocks.RED_SHULKER_BOX,
					Blocks.WHITE_SHULKER_BOX,
					Blocks.YELLOW_SHULKER_BOX).
			map(ItemLike::asItem).
			collect(Collectors.toSet());

	public IPBlockLoot() {
		super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags());
	}

	@Override
	protected void generate()
	{
		registerSelfDropping(IPContent.Blocks.ASPHALT.get());
		createSlabItemTable(IPContent.Blocks.ASPHALT_SLAB.get());
		registerSelfDropping(IPContent.Blocks.ASPHALT_STAIR.get());
		registerSelfDropping(IPContent.Blocks.FLARESTACK.get());
		registerSelfDropping(IPContent.Blocks.PETCOKE.get());
		registerSelfDropping(IPContent.Blocks.SEISMIC_SURVEY.get());
		registerSelfDropping(IPContent.Blocks.PARAFFIN_WAX.get());

		register(IPContent.Blocks.GAS_GENERATOR.get(), tileDrop());
		register(IPContent.Blocks.AUTO_LUBRICATOR.get(), tileDrop());

		registerMultiblock(IPContent.Multiblock.DISTILLATIONTOWER.block().get());
		registerMultiblock(IPContent.Multiblock.PUMPJACK.block().get());
		registerMultiblock(IPContent.Multiblock.HYDROTREATER.block().get());
		registerMultiblock(IPContent.Multiblock.COKERUNIT.block().get());
		registerMultiblock(IPContent.Multiblock.DERRICK.block().get());
		registerMultiblock(IPContent.Multiblock.OILTANK.block().get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks()
	{
		return IPRegisters.BLOCK_REGISTER.getEntries().
				stream().
				map(RegistryObject::get).
				filter(block -> !block.getLootTable().equals(BuiltInLootTables.EMPTY)).
				collect(Collectors.toList());
	}

	private <S extends SlabBlock> void createSlabItemTable(S block){
		LootPoolSingletonContainer.Builder<?> b = LootItem.lootTableItem(block)
			.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
					.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))));
		
		register(block, LootTable.lootTable().withPool(createPoolBuilder().setRolls(ConstantValue.exactly(1.0F)).add(b)));
	}
	
	private void registerMultiblock(Block b){
		register(b, dropInv(), dropOriginalBlock());
	}
	
	private LootPool.Builder dropOriginalBlock(){
		return createPoolBuilder().add(MultiblockDropsLootContainer.builder());
	}
	
	private LootPool.Builder dropInv(){
		return createPoolBuilder().add(DropInventoryLootEntry.builder());
	}
	
	private LootPool.Builder tileDrop(){
		return createPoolBuilder().add(IPTileDropLootEntry.builder());
	}
	
	private void registerSelfDropping(Block b, LootPool.Builder... pool){
		LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length + 1);
		withSelf[withSelf.length - 1] = singleItem(b);
		register(b, withSelf);
	}
	
	private LootPool.Builder singleItem(ItemLike in){
		return createPoolBuilder().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(in));
	}
	
	private void register(Block b, LootPool.Builder... pools){
		LootTable.Builder builder = LootTable.lootTable();
		for(LootPool.Builder pool:pools)
			builder.withPool(pool);
		register(b, builder);
	}
	
	private void register(Block b, LootTable.Builder table){
		this.add(b, table);
	}
	
	//private void register(ResourceLocation name, LootTable.Builder table){
	//	out.accept(toTableLoc(name), table);
	//}
	
	private LootPool.Builder createPoolBuilder(){
		return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
	}
	
	private ResourceLocation toTableLoc(ResourceLocation in){
		return new ResourceLocation(in.getNamespace(), "blocks/" + in.getPath());
	}
}
