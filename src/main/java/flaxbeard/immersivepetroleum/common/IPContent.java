package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.register.IEPotions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricantEffect;
import flaxbeard.immersivepetroleum.client.particle.FlareFire;
import flaxbeard.immersivepetroleum.client.particle.FluidSpill;
import flaxbeard.immersivepetroleum.client.particle.IPParticleTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import flaxbeard.immersivepetroleum.common.blocks.metal.FlarestackBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.SeismicSurveyBlock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.*;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.CokerUnitLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.OilTankLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.PumpjackLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic;
import flaxbeard.immersivepetroleum.common.blocks.stone.*;
import flaxbeard.immersivepetroleum.common.blocks.wooden.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import flaxbeard.immersivepetroleum.common.entity.IPEntityTypes;
import flaxbeard.immersivepetroleum.common.fluids.CrudeOilFluid;
import flaxbeard.immersivepetroleum.common.fluids.DieselFluid;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid.IPFluidEntry;
import flaxbeard.immersivepetroleum.common.fluids.NapalmFluid.NapalmFluidBlock;
import flaxbeard.immersivepetroleum.common.items.*;
import flaxbeard.immersivepetroleum.common.lubehandlers.CrusherLubricationHandler;
import flaxbeard.immersivepetroleum.common.lubehandlers.ExcavatorLubricationHandler;
import flaxbeard.immersivepetroleum.common.lubehandlers.PumpjackLubricationHandler;
import flaxbeard.immersivepetroleum.common.util.IPCreativeTabs;
import flaxbeard.immersivepetroleum.common.util.IPEffects;
import flaxbeard.immersivepetroleum.common.util.damageSources.IPDamageSources;
import flaxbeard.immersivepetroleum.common.util.sounds.IPSounds;
import flaxbeard.immersivepetroleum.common.world.FeatureReservoir;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPContent{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/Content");

	public static class Multiblock{
		public static final MultiblockRegistration<DistillationTowerLogic.State> DISTILLATIONTOWER = IPRegisters.registerMetalMultiblock(
				"distillation_tower", new DistillationTowerLogic(), () -> DistillationTowerMultiblock.INSTANCE,
				builder -> builder.redstone(st -> st.rsState, DistillationTowerLogic.REDSTONE_IN).gui(IPMenuTypes.DISTILLATION_TOWER)
		);
		public static final MultiblockRegistration<PumpjackLogic.State> PUMPJACK = IPRegisters.registerMetalMultiblock(
				"pumpjack", new PumpjackLogic(), () -> PumpjackMultiblock.INSTANCE,
				builder -> builder.redstone(st -> st.rsState, PumpjackLogic.REDSTONE_IN)
		);
		public static final MultiblockRegistration<CokerUnitLogic.State> COKERUNIT = IPRegisters.registerMetalMultiblock(
				"coker_unit", new CokerUnitLogic(), () -> CokerUnitMultiblock.INSTANCE,
				builder -> builder.redstone(st -> st.rsState, CokerUnitLogic.Redstone_IN).gui(IPMenuTypes.COKER).withComparator()
		);
		public static final MultiblockRegistration<HydroTreaterLogic.State> HYDROTREATER = IPRegisters.registerMetalMultiblock(
				"hydrotreater", new HydroTreaterLogic(), () -> HydroTreaterMultiblock.INSTANCE,
				builder -> builder.redstone(st -> st.rsState, HydroTreaterLogic.Redstone_IN).gui(IPMenuTypes.HYDROTREATER)
		);
		public static final MultiblockRegistration<DerrickLogic.State> DERRICK = IPRegisters.registerMetalMultiblock(
				"derrick", new DerrickLogic(), () -> DerrickMultiblock.INSTANCE,
				builder -> builder.redstone(st -> st.rsState, DerrickLogic.Redstone_IN).gui(IPMenuTypes.DERRICK)
		);
		public static final MultiblockRegistration<OilTankLogic.State> OILTANK = IPRegisters.registerMetalMultiblock(
				"oiltank", new OilTankLogic(), () -> OilTankMultiblock.INSTANCE,
				builder -> builder.redstone(st -> st.rsState, OilTankLogic.Redstone_IN).withComparator()
		);

		private static void forceClassLoad(){

		}
	}
	
	public static class Fluids{
		public static final IPFluidEntry CRUDEOIL = IPFluid.makeFluid("crudeoil", 1000, 2250, 0.007, CrudeOilFluid::new, CrudeOilFluid.CrudeOilBlock::new);
		public static final IPFluidEntry DIESEL_SULFUR = IPFluid.makeFluid("diesel_sulfur", 789, 1750, DieselFluid::new);
		public static final IPFluidEntry DIESEL = IPFluid.makeFluid("diesel", 789, 1750, DieselFluid::new);
		public static final IPFluidEntry LUBRICANT = IPFluid.makeFluid("lubricant", 925, 1000);
		public static final IPFluidEntry GASOLINE = IPFluid.makeFluid("gasoline", 789, 1200);
		
		public static final IPFluidEntry NAPHTHA = IPFluid.makeFluid("naphtha", 750, 750);
		public static final IPFluidEntry NAPHTHA_CRACKED = IPFluid.makeFluid("naphtha_cracked", 750, 750);
		public static final IPFluidEntry BENZENE = IPFluid.makeFluid("benzene", 876, 700);
		public static final IPFluidEntry PROPYLENE = IPFluid.makeFluid("propylene", 2, 1);
		public static final IPFluidEntry ETHYLENE = IPFluid.makeFluid("ethylene", 1, 1);
		public static final IPFluidEntry LUBRICANT_CRACKED = IPFluid.makeFluid("lubricant_cracked", 925, 1000);
		public static final IPFluidEntry KEROSENE = IPFluid.makeFluid("kerosene", 810, 900);
		public static final IPFluidEntry GASOLINE_ADDITIVES = IPFluid.makeFluid("gasoline_additives", 800, 900);
		
		public static final IPFluidEntry NAPALM = IPFluid.makeFluid("napalm", 1000, 4000, 0.0105, NapalmFluidBlock::new);
		
		private static void forceClassLoad(){
		}
	}
	
	public static class Blocks{
		public static final RegistryObject<SeismicSurveyBlock> SEISMIC_SURVEY = IPRegisters.registerIPBlock("seismic_survey", SeismicSurveyBlock::new);
		
		public static final RegistryObject<GasGeneratorBlock> GAS_GENERATOR = IPRegisters.registerIPBlock("gas_generator", GasGeneratorBlock::new);
		public static final RegistryObject<AutoLubricatorBlock> AUTO_LUBRICATOR = IPRegisters.registerIPBlock("auto_lubricator", AutoLubricatorBlock::new);
		public static final RegistryObject<FlarestackBlock> FLARESTACK = IPRegisters.registerIPBlock("flarestack", FlarestackBlock::new);
		
		public static final RegistryObject<AsphaltBlock> ASPHALT = IPRegisters.registerIPBlock("asphalt", AsphaltBlock::new);
		public static final RegistryObject<SlabBlock> ASPHALT_SLAB = IPRegisters.registerBlock("asphalt_slab", () -> new AsphaltSlab(ASPHALT.get()));
		public static final RegistryObject<StairBlock> ASPHALT_STAIR = IPRegisters.registerBlock("asphalt_stair", () -> new AsphaltStairs(ASPHALT.get()));
		public static final RegistryObject<PetcokeBlock> PETCOKE = IPRegisters.registerIPBlock("petcoke_block", PetcokeBlock::new);
		public static final RegistryObject<WellBlock> WELL = IPRegisters.registerBlock("well", WellBlock::new);
		public static final RegistryObject<WellPipeBlock> WELL_PIPE = IPRegisters.registerBlock("well_pipe", WellPipeBlock::new);
		public static final RegistryObject<ParaffinWaxBlock> PARAFFIN_WAX = IPRegisters.registerIPBlock("paraffin_wax_block", ParaffinWaxBlock::new);
		
		private static void forceClassLoad(){
			registerItemBlock(Blocks.ASPHALT_SLAB);
			registerItemBlock(Blocks.ASPHALT_STAIR);
		}
		
		private static void registerItemBlock(RegistryObject<? extends Block> block){
			IPRegisters.registerItem(block.getId().getPath(), () -> new IPBlockItemBase(block.get(), new Item.Properties()));
		}
	}
	
	public static class Items{
		public static final RegistryObject<Item> PROJECTOR = IPRegisters.registerItem("projector", ProjectorItem::new);
		public static final RegistryObject<MotorboatItem> SPEEDBOAT = IPRegisters.registerItem("speedboat", MotorboatItem::new);
		public static final RegistryObject<OilCanItem> OIL_CAN = IPRegisters.registerItem("oil_can", OilCanItem::new);
		public static final RegistryObject<Item> BITUMEN = IPRegisters.registerItem("bitumen", IPItemBase::new);
		public static final RegistryObject<Item> PETCOKE = IPRegisters.registerItem("petcoke", () -> new IPItemBase(){
			@Override
			public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType){
				return 3200;
			}
		});
		public static final RegistryObject<Item> PETCOKEDUST = IPRegisters.registerItem("petcoke_dust", IPItemBase::new);
		public static final RegistryObject<Item> SURVEYRESULT = IPRegisters.registerItem("survey_result", SurveyResultItem::new);
		
		public static final RegistryObject<Item> PARAFFIN_WAX = IPRegisters.registerItem("paraffin_wax", () -> new IPItemBase(){
			@Override
			public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType){
				return 800;
			}
		});
		
		public static final RegistryObject<Item> GASOLINE_BOTTLE = IPRegisters.registerItem("gasoline_bottle", GasolineBottleItem::new);
		public static final RegistryObject<Item> MOLOTOV = IPRegisters.registerItem("molotov", () -> new MolotovItem(false));
		public static final RegistryObject<Item> MOLOTOV_LIT = IPRegisters.registerItem("molotov_lit", () -> new MolotovItem(true));
		
		private static void forceClassLoad(){
		}
	}
	
	public static class BoatUpgrades{
		public static final RegistryObject<IPUpgradeItem> REINFORCED_HULL = createBoatUpgrade("reinforced_hull");
		public static final RegistryObject<IPUpgradeItem> ICE_BREAKER = createBoatUpgrade("icebreaker");
		public static final RegistryObject<IPUpgradeItem> TANK = createBoatUpgrade("tank");
		public static final RegistryObject<IPUpgradeItem> RUDDERS = createBoatUpgrade("rudders");
		public static final RegistryObject<IPUpgradeItem> PADDLES = createBoatUpgrade("paddles");
		
		private static void forceClassLoad(){
		}
		
		private static <T extends Item> RegistryObject<IPUpgradeItem> createBoatUpgrade(String name){
			return IPRegisters.registerItem("upgrade_" + name, () -> new IPUpgradeItem(MotorboatItem.UPGRADE_TYPE));
		}
	}
	
	public static final RegistryObject<Item> DEBUGITEM = IPRegisters.registerItem("debug", DebugItem::new);

	public static class WorldGenFeatures
	{
		public static final RegistryObject<FeatureReservoir> RESERVOIR_FEATURE = IPRegisters.FEATURE_REGISTER.register("reservoir", FeatureReservoir::new);

		public static void forceClassLoad()
		{ }
	}

	/** block/item/fluid population */
	public static void modConstruction(){
		Fluids.forceClassLoad();
		Blocks.forceClassLoad();
		Items.forceClassLoad();
		BoatUpgrades.forceClassLoad();
		Multiblock.forceClassLoad();
		IPTileTypes.forceClassLoad();
		IPMenuTypes.forceClassLoad();
		Serializers.forceClassLoad();
		IPEffects.forceClassLoad();
		IPEntityTypes.forceClassLoad();
		IPParticleTypes.forceClassLoad();
		IPSounds.forceClassLoad();
		IPDamageSources.forceClassLoad();
		WorldGenFeatures.forceClassLoad();
		IPCreativeTabs.forceClassLoad();
	}
	
	public static void preInit(){
	}
	
	@SuppressWarnings("unchecked")
	public static void init(ParallelDispatchEvent event){
		//event.enqueueWork(IPWorldGen::registerReservoirGen);
		
		Fluids.CRUDEOIL.setEffect(IEPotions.FLAMMABLE.get(), 100, 1);
		Fluids.DIESEL.setEffect(IEPotions.FLAMMABLE.get(), 40, 1); // Realisticly diesel can not be ignited with an open flame..
		Fluids.DIESEL_SULFUR.setEffect(IEPotions.FLAMMABLE.get(), 40, 1);
		Fluids.GASOLINE.setEffect(IEPotions.FLAMMABLE.get(), 120, 2);
		Fluids.KEROSENE.setEffect(IEPotions.FLAMMABLE.get(), 120, 2);
		Fluids.NAPHTHA.setEffect(IEPotions.FLAMMABLE.get(), 120, 2);
		Fluids.NAPALM.setEffect(IEPotions.FLAMMABLE.get(), 140, 2);
		
		Fluids.LUBRICANT.setEffect(IEPotions.SLIPPERY.get(), 100, 1);
		
		ChemthrowerHandler.registerEffect(IPTags.Fluids.lubricant, new LubricantEffect());
		ChemthrowerHandler.registerEffect(IPTags.Fluids.lubricant, new ChemthrowerEffect_Potion(null, 0, IEPotions.SLIPPERY.get(), 60, 1));
		ChemthrowerHandler.registerEffect(IETags.fluidPlantoil, new LubricantEffect());
		
		ChemthrowerHandler.registerEffect(IPTags.Fluids.crudeOil, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE.get(), 60, 1));
		ChemthrowerHandler.registerEffect(IPTags.Fluids.gasoline, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE.get(), 60, 1));
		ChemthrowerHandler.registerEffect(IPTags.Fluids.naphtha, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE.get(), 60, 1));
		ChemthrowerHandler.registerEffect(IPTags.Fluids.benzene, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE.get(), 60, 1));
		ChemthrowerHandler.registerEffect(IPTags.Fluids.napalm, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE.get(), 60, 2));
		
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.crudeOil);
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.gasoline);
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.naphtha);
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.benzene);
		ChemthrowerHandler.registerFlammable(IPTags.Fluids.napalm);
		
		MultiblockHandler.registerMultiblock(DistillationTowerMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(PumpjackMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(CokerUnitMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(HydroTreaterMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(DerrickMultiblock.INSTANCE);
		MultiblockHandler.registerMultiblock(OilTankMultiblock.INSTANCE);
		
		LubricantHandler.register(IPTags.Fluids.lubricant, 3);
		LubricantHandler.register(IETags.fluidPlantoil, 12);
		
		FlarestackHandler.register(IPTags.Utility.burnableInFlarestack);
		
		LubricatedHandler.registerLubricatedTile(Multiblock.PUMPJACK.logic().getClass(), PumpjackLubricationHandler::new);
		LubricatedHandler.registerLubricatedTile(IEMultiblockLogic.EXCAVATOR.logic().getClass(), ExcavatorLubricationHandler::new);
		LubricatedHandler.registerLubricatedTile(IEMultiblockLogic.CRUSHER.logic().getClass(), CrusherLubricationHandler::new);
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event){
		event.registerSpriteSet(IPParticleTypes.FLARE_FIRE.get(), FlareFire.Factory::new);
		event.registerSpecial(IPParticleTypes.FLUID_SPILL.get(), new FluidSpill.Factory());
	}
}
