package flaxbeard.immersivepetroleum.common.data.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class IPLoot implements LootTableSubProvider
{
	@Override
	public void generate(BiConsumer<ResourceLocation, LootTable.Builder> pOutput)
	{
		LootPool.Builder pool = LootPool.lootPool()
				.name("cookie_for_your_trouble")
				.add(LootItem.lootTableItem(Items.COOKIE))
				.setRolls(ConstantValue.exactly(1))
				.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

		pOutput.accept(ResourceUtils.ip("advancements/forming_coker_reward"), LootTable.lootTable().withPool(pool));
	}
}
