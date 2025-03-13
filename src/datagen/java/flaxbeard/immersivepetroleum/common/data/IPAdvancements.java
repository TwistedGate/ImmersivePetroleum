package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockAdvancementTrigger;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.commands.CommandFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class IPAdvancements extends ForgeAdvancementProvider {
	public IPAdvancements(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> pRegistries, ExistingFileHelper helper){
		super(generatorIn.getPackOutput(), pRegistries, helper, List.of(IPAdvancements::registerAdvancements) );
	}

	private static void registerAdvancements(HolderLookup.Provider lookup, Consumer<Advancement> consumer, ExistingFileHelper fileHelper){
		Advancement start = Advancement.Builder.advancement()
			.display(IPContent.Blocks.SEISMIC_SURVEY.get(),
				Component.translatable("advancement.immersivepetroleum.root"),
				Component.translatable("advancement.immersivepetroleum.root.desc"),
				ResourceUtils.ip("textures/block/asphalt.png"),
				FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/root"), fileHelper);
		
		Advancement derrick = advancement(start, IPContent.Multiblock.DERRICK.block().get(), "mb_derrick", FrameType.GOAL, true, true, false)
			.addCriterion("derrick", createMultiblockTrigger("derrick"))
			.save(consumer, ResourceUtils.ip("main/mb_derrick"), fileHelper);
		
		Advancement pumpjack = advancement(derrick, IPContent.Multiblock.PUMPJACK.block().get(), "mb_pumpjack", FrameType.GOAL, true, true, false)
			.addCriterion("pumpjack", createMultiblockTrigger("pumpjack"))
			.save(consumer, ResourceUtils.ip("main/mb_pumpjack"), fileHelper);
		
		advancement(derrick, IPContent.Multiblock.OILTANK.block().get(), "mb_oiltank", FrameType.GOAL, true, true, false)
			.addCriterion("oiltank", createMultiblockTrigger("oiltank"))
			.save(consumer, ResourceUtils.ip("main/mb_oiltank"), fileHelper);
		
		Advancement tower = advancement(pumpjack, IPContent.Multiblock.DISTILLATIONTOWER.block().get(), "mb_distillationtower", FrameType.GOAL, true, true, false)
			.addCriterion("distillationtower", createMultiblockTrigger("distillationtower"))
			.save(consumer, ResourceUtils.ip("main/mb_distillationtower"), fileHelper);
		
		Advancement bitumen = advancement(tower, IPContent.Items.BITUMEN.get(), "bitumen", FrameType.TASK, true, true, false)
			.addCriterion("bitumen", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Items.BITUMEN.get()))
			.save(consumer, ResourceUtils.ip("main/bitumen"), fileHelper);
		
		Advancement cokerunit = advancement(bitumen, IPContent.Multiblock.COKERUNIT.block().get(), "mb_cokerunit", FrameType.GOAL, true, true, false)
			.addCriterion("cokerunit", createMultiblockTrigger("cokerunit"))
			.rewards(reward(10, ResourceUtils.ip("advancements/forming_coker_reward")))
			.save(consumer, ResourceUtils.ip("main/mb_cokerunit"), fileHelper);
		
		advancement(cokerunit, IPContent.Items.PETCOKE.get(), "petcoke", FrameType.TASK, true, true, false)
			.addCriterion("petcoke", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Items.PETCOKE.get()))
			.save(consumer, ResourceUtils.ip("main/petcoke"), fileHelper);
		
		Advancement hydrotreater = advancement(tower, IPContent.Multiblock.HYDROTREATER.block().get(), "mb_hydrotreater", FrameType.GOAL, true, true, false)
			.addCriterion("hydrotreater", createMultiblockTrigger("hydrotreater"))
			.save(consumer, ResourceUtils.ip("main/mb_hydrotreater"), fileHelper);
		
		motorboat(consumer, tower, fileHelper);
		
		advancement(tower, IPContent.Blocks.GAS_GENERATOR.get(), "gas_generator", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/gas_generator"), fileHelper);
		
		advancement(tower, IPContent.Blocks.AUTO_LUBRICATOR.get(), "auto_lubricator", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/auto_lubricator"), fileHelper);
		
		advancement(tower, IPContent.Items.OIL_CAN.get(), "oil_can", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/oil_can"), fileHelper);
		
		advancement(start, IPContent.Items.PROJECTOR.get(), "projector", FrameType.TASK, true, true, false)
			.addCriterion("projector", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Items.PROJECTOR.get()))
			.save(consumer, ResourceUtils.ip("main/projector"), fileHelper);
		
		advancement(start, IPContent.Blocks.FLARESTACK.get(), "flarestack", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/flarestack"), fileHelper);
		
		Advancement cracking = advancement(hydrotreater, IPContent.Fluids.NAPHTHA_CRACKED.bucket().get(), "cracking", FrameType.TASK, true, true, false)
			.addCriterion("cracking", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.NAPHTHA_CRACKED.bucket().get()))
			.save(consumer, ResourceUtils.ip("main/cracking"), fileHelper);
		
		Advancement naphtha_distillates = advancement(cracking, IPContent.Fluids.BENZENE.bucket().get(), "naphtha_distillates", FrameType.GOAL, true, true, false)
			.addCriterion("benzene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.BENZENE.bucket().get()))
			.addCriterion("propylene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.PROPYLENE.bucket().get()))
			.addCriterion("ethylene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.ETHYLENE.bucket().get()))
			.save(consumer, ResourceUtils.ip("main/naphtha_distillates"), fileHelper);
		
		advancement(naphtha_distillates, IEBlocks.StoneDecoration.DUROPLAST.get().asItem(), "duroplast", FrameType.CHALLENGE, true, true, false)
			.addCriterion("benzene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.BENZENE.bucket().get()))
			.addCriterion("propylene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.PROPYLENE.bucket().get()))    //These are here to make sure the player made their duroplast from naphtha
			.addCriterion("ethylene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.ETHYLENE.bucket().get()))
			.addCriterion("duroplast_block", InventoryChangeTrigger.TriggerInstance.hasItems(IEBlocks.StoneDecoration.DUROPLAST.get().asItem()))
			.save(consumer, ResourceUtils.ip("main/duroplast"), fileHelper);
		
		Advancement kerosene = advancement(tower, IPContent.Fluids.GASOLINE_ADDITIVES.bucket().get(), "kerosene", FrameType.TASK, true, true, false)
			.addCriterion("kerosene", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.GASOLINE_ADDITIVES.bucket().get()))
			.save(consumer, ResourceUtils.ip("main/kerosene"), fileHelper);
		
		Advancement gasoline = advancement(kerosene, IPContent.Fluids.GASOLINE.bucket().get(), "gasoline", FrameType.GOAL, true, true, false)
			.addCriterion("gasoline", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.GASOLINE.bucket().get()))
			.save(consumer, ResourceUtils.ip("main/gasoline"), fileHelper);
		
		advancement(gasoline, IPContent.Fluids.NAPALM.bucket().get(), "napalm", FrameType.TASK, true, true, false)
			.addCriterion("napalm", InventoryChangeTrigger.TriggerInstance.hasItems(IPContent.Fluids.NAPALM.bucket().get()))
			.save(consumer, ResourceUtils.ip("main/napalm"), fileHelper);
	}
	
	private static void motorboat(Consumer<Advancement> consumer, Advancement start, ExistingFileHelper fileHelper){
		Advancement fill_motorboat = advancement(start, IPContent.Items.SPEEDBOAT.get(), "motorboat", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/motorboat"), fileHelper);
		
		advancement(fill_motorboat, IPContent.BoatUpgrades.ICE_BREAKER.get(), "ice_breaker", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/ice_breaker"), fileHelper);
		
		advancement(fill_motorboat, IPContent.BoatUpgrades.PADDLES.get(), "paddles", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/paddles"), fileHelper);
		
		advancement(fill_motorboat, IPContent.BoatUpgrades.REINFORCED_HULL.get(), "reinforced_hull", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/reinforced_hull"), fileHelper);
		
		advancement(fill_motorboat, IPContent.BoatUpgrades.RUDDERS.get(), "rudders", FrameType.CHALLENGE, true, true, false)
			.rewards(reward(50, ResourceUtils.ie("advancements/shader_rare")))
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/rudders"), fileHelper);
		
		advancement(fill_motorboat, IPContent.BoatUpgrades.TANK.get(), "tank", FrameType.TASK, true, true, false)
			.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
			.save(consumer, ResourceUtils.ip("main/tank"), fileHelper);
	}
	
	static final ItemPredicate HAMMER = ItemPredicate.Builder.item().of(Tools.HAMMER).build();
	protected static MultiblockAdvancementTrigger.Instance createMultiblockTrigger(String mbName){
		return MultiblockAdvancementTrigger.create(ResourceUtils.ip("multiblocks/" + mbName), HAMMER);
	}
	
	protected static Advancement.Builder advancement(Advancement parent, ItemLike display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden){
		Advancement.Builder builder = Advancement.Builder.advancement()
			.parent(parent)
			.display(display,
				Component.translatable("advancement.immersivepetroleum." + name),
				Component.translatable("advancement.immersivepetroleum." + name + ".desc"),
			null, frame, showToast, announceToChat, hidden);
		return builder;
	}
	
	protected static Advancement.Builder advancement(Advancement parent, ItemStack display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden){
		Advancement.Builder builder = Advancement.Builder.advancement()
			.parent(parent)
			.display(display,
				Component.translatable("advancement.immersivepetroleum." + name),
				Component.translatable("advancement.immersivepetroleum." + name + ".desc"),
			null, frame, showToast, announceToChat, hidden);
		return builder;
	}
	
	protected static AdvancementRewards reward(int exp, @Nullable ResourceLocation lootTable){
		ResourceLocation[] loot = new ResourceLocation[0];
		if(lootTable != null){
			loot = new ResourceLocation[]{lootTable};
		}
		return new AdvancementRewards(exp, loot, new ResourceLocation[0], CommandFunction.CacheableFunction.NONE);
	}
}
