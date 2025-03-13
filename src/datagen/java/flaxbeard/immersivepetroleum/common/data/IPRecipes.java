package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.data.recipebuilder.FluidAwareShapedRecipeBuilder;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.api.crafting.builders.CokerUnitRecipeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.builders.DistillationTowerRecipeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.builders.HighPressureRefineryRecipeBuilder;
import flaxbeard.immersivepetroleum.api.crafting.builders.ReservoirBuilder;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.items.GasolineBottleItem;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class IPRecipes extends RecipeProvider{
	private final Map<String, Integer> PATH_COUNT = new HashMap<>();
	
	protected Consumer<FinishedRecipe> out;
	public IPRecipes(DataGenerator generatorIn){
		super(generatorIn.getPackOutput());
	}
	
	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> out){
		this.out = out;
		
		itemRecipes();
		blockRecipes();
		speedboatUpgradeRecipes();
		distillationRecipes();
		cokerRecipes();
		hydrotreaterRecipes();
		reservoirs();
		refineryRecipes();
		paraffinWaxRecipes();
		
		MixerRecipeBuilder.builder(IPContent.Fluids.NAPALM.source().get(), 500)
			.addFluidTag(IPTags.Fluids.gasoline, 500)
			.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.ALUMINUM).dust, 3))
			.setEnergy(3200)
			.build(this.out, rl("mixer/napalm"));
		
		GeneratorFuelBuilder.builder(IPTags.Fluids.diesel, 322)
			.build(this.out, rl("fuels/diesel"));
		GeneratorFuelBuilder.builder(IPTags.Fluids.diesel_sulfur, 285)
			.build(this.out, rl("fuels/diesel_sulfur"));
		GeneratorFuelBuilder.builder(IPTags.Fluids.kerosene, 208)
			.build(this.out, rl("fuels/kerosene"));
	}
	
	private void reservoirs(){
		ReservoirBuilder.builder("aquifer", Fluids.WATER, 5000.000, 10000.000, 0.025, 30)
			.setDimensions(false, new ResourceLocation[]{
					Level.OVERWORLD.location()
			})
			.equilibrium(2000)
			.build(this.out, rl("reservoirs/aquifer"));
		
		ReservoirBuilder.builder("oil", IPContent.Fluids.CRUDEOIL.source().get(), 2500.000, 32500.000, 0.006, 40)
			.setDimensions(true, new ResourceLocation[]{
					Level.END.location()
			})
			.build(this.out, rl("reservoirs/oil"));
		
		ReservoirBuilder.builder("lava", Fluids.LAVA, 250.000, 1000.000, 0.0, 30)
			.setDimensions(true, new ResourceLocation[]{
					Level.END.location()
			})
			.build(this.out, rl("reservoirs/lava"));
	}
	
	private void distillationRecipes(){
		// setEnergy and setTime are 1024 and 1 by default. But still allows to be customized.
		
		DistillationTowerRecipeBuilder.builder(new FluidStack(IPContent.Fluids.NAPHTHA.get(), 17),
						new FluidStack(IPContent.Fluids.KEROSENE.get(), 18),
						new FluidStack(IPContent.Fluids.DIESEL_SULFUR.get(), 30),
						new FluidStack(IPContent.Fluids.LUBRICANT.get(), 12))
			.addByproduct(new ItemStack(IPContent.Items.BITUMEN.get()), 0.07)
			.addInput(IPTags.Fluids.crudeOil, 50)
			.setTimeAndEnergy(1, 1024)
			.build(this.out, rl("distillationtower/oil"));
		
		DistillationTowerRecipeBuilder.builder(new FluidStack(IPContent.Fluids.NAPHTHA.get(), 2),
						new FluidStack(IPContent.Fluids.GASOLINE_ADDITIVES.get(), 3),
						new FluidStack(IPContent.Fluids.DIESEL_SULFUR.get(), 5))
			.addInput(IPTags.Fluids.kerosene, 10)
			.setTimeAndEnergy(1, 1024)
			.build(this.out, rl("distillationtower/kerosene"));
		
		DistillationTowerRecipeBuilder.builder(new FluidStack(IPContent.Fluids.ETHYLENE.get(), 6),
						new FluidStack(IPContent.Fluids.PROPYLENE.get(), 2),
						new FluidStack(IPContent.Fluids.BENZENE.get(), 2))
			.addInput(IPTags.Fluids.naphtha_cracked, 10)
			.setTimeAndEnergy(1, 1024)
			.build(this.out, rl("distillationtower/naphtha_cracking"));
		
		DistillationTowerRecipeBuilder.builder(new FluidStack(IPContent.Fluids.KEROSENE.get(), 6),
						new FluidStack(IPContent.Fluids.DIESEL_SULFUR.get(), 10))
			.addInput(IPTags.Fluids.lubricant_cracked, 12)
			.setTimeAndEnergy(1, 1024)
			.build(this.out, rl("distillationtower/lubricant_cracking"));
	}
	
	/** Contains everything related to Petcoke */
	private void cokerRecipes(){
		CokerUnitRecipeBuilder.builder(new ItemStack(IPContent.Items.PETCOKE.get(), 2), IPContent.Fluids.DIESEL_SULFUR.get(), 27)
			.addInputItem(IPTags.Items.bitumen, 2)
			.addInputFluid(FluidTags.WATER, 125)
			.setTimeAndEnergy(30, 15360)
			.build(this.out, rl("coking/petcoke"));
		
		// Petcoke Compression and Decompression
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.PETCOKE.get())
			.define('c', IPTags.Items.petcoke)
			.pattern("ccc")
			.pattern("ccc")
			.pattern("ccc")
			.unlockedBy("has_petcoke_item", has(IPTags.Items.petcoke))
			.save(this.out, rl("petcoke_items_to_block"));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, IPContent.Items.PETCOKE.get(), 9)
			.requires(IPTags.getItemTag(IPTags.Blocks.petcoke))
			.unlockedBy("has_petcoke_block", has(IPTags.getItemTag(IPTags.Blocks.petcoke)))
			.save(this.out, rl("petcoke_block_to_items"));
		
		// Registering Petcoke as Fuel for the Blastfurnace
		BlastFurnaceFuelBuilder.builder(IPTags.Items.petcoke)
			.setTime(1200)
			.build(this.out, rl("blastfurnace/fuel_petcoke"));
		BlastFurnaceFuelBuilder.builder(IPTags.getItemTag(IPTags.Blocks.petcoke))
			.setTime(12000)
			.build(this.out, rl("blastfurnace/fuel_petcoke_block"));
		
		// Petcoke Dust recipes
		CrusherRecipeBuilder.builder(IPTags.Items.petcokeDust, 1)
			.addInput(IPTags.Items.petcoke)
			.setEnergy(2400)
			.build(this.out, rl("crusher/petcoke"));
		CrusherRecipeBuilder.builder(IPTags.Items.petcokeDust, 9)
			.addInput(IPTags.Items.petcokeStorage)
			.setEnergy(4800)
			.build(this.out, rl("crusher/petcoke_block"));
		
		// Petcoke dust and Iron Ingot to make Steel Ingot
		ArcFurnaceRecipeBuilder.builder(IETags.getTagsFor(EnumMetals.STEEL).ingot, 1)
			.addIngredient("input", Tags.Items.INGOTS_IRON)
			.addInput(IPTags.Items.petcokeDust)
			.addSlag(IETags.slag, 1)
			.setTime(400)
			.setEnergy(204800)
			.build(out, rl("arcfurnace/steel"));
		
		// 8 Petcoke Dust to 1 HOP Graphite Dust
		SqueezerRecipeBuilder.builder()
			.addResult(new IngredientWithSize(IETags.hopGraphiteDust))
			.addInput(new IngredientWithSize(IPTags.Items.petcokeDust, 8))
			.setEnergy(19200)
			.build(out, rl("squeezer/graphite_dust"));
	}
	
	private void hydrotreaterRecipes(){
		HighPressureRefineryRecipeBuilder.builder(new FluidStack(IPContent.Fluids.DIESEL.get(), 10), 80, 1)
			.addInputFluid(new FluidTagInput(IPTags.Fluids.diesel_sulfur, 10))
			.addSecondaryInputFluid(FluidTags.WATER, 5)
			.addItemWithChance(new ItemStack(IEItems.Ingredients.DUST_SULFUR), 0.05)
			.build(out, rl("hydrotreater/sulfur_recovery"));
		
		HighPressureRefineryRecipeBuilder.builder(new FluidStack(IPContent.Fluids.NAPHTHA_CRACKED.get(), 20), 2560, 5)
			.addInputFluid(new FluidTagInput(IPTags.Fluids.naphtha, 20))
			.addSecondaryInputFluid(FluidTags.WATER, 5)
			.build(out, rl("hydrotreater/naphtha_cracking"));
		
		HighPressureRefineryRecipeBuilder.builder(new FluidStack(IPContent.Fluids.LUBRICANT_CRACKED.get(), 24), 2560, 5)
			.addInputFluid(new FluidTagInput(IPTags.Fluids.lubricant, 24))
			.addSecondaryInputFluid(FluidTags.WATER, 5)
			.addItemWithChance(new ItemStack(IPContent.Items.PARAFFIN_WAX.get()), 0.024)
			.build(out, rl("hydrotreater/lubricant_cracking"));
		
		// Temporarly Disabled
		// PNC Compat
		// @formatter:off
		/*
		HighPressureRefineryRecipeBuilder.builder(new FluidStack(ModFluids.PLASTIC.get(), 1000), 61440, 60)
			.addCondition(new ModLoadedCondition("pneumaticcraft"))
			.addInputFluid(new FluidTagInput(IPTags.Fluids.ethylene, 100))
			.addItemWithChance(new ItemStack(IPContent.Items.BITUMEN.get()), 0.05)
			.build(out, rl("hydrotreater/ethylene_plastic"));
		
		HighPressureRefineryRecipeBuilder.builder(new FluidStack(ModFluids.PLASTIC.get(), 2000), 61440, 60)
			.addCondition(new ModLoadedCondition("pneumaticcraft"))
			.addInputFluid(new FluidTagInput(IPTags.Fluids.propylene, 100))
			.addItemWithChance(new ItemStack(IPContent.Items.BITUMEN.get()), 0.1)
			.build(out, rl("hydrotreater/propylene_plastic"));
		*/
		// @formatter:off
	}
	
	private void refineryRecipes(){
		RefineryRecipeBuilder.builder(new FluidStack(IPContent.Fluids.GASOLINE.get(), 10))
			.addInput(new FluidTagInput(IPTags.Fluids.naphtha, 7))
			.addInput(new FluidTagInput(IPTags.Fluids.gasoline_additives, 3))
			.setEnergy(80)
			.build(out, rl("refinery/gasoline"));
		
		RefineryRecipeBuilder.builder(new FluidStack(IEFluids.CREOSOTE.getStill(), 16))
			.addInput(new FluidTagInput(IPTags.Fluids.benzene, 8))
			.addInput(new FluidTagInput(IPTags.Fluids.propylene, 8))
			.setEnergy(240)
			.build(out, rl("refinery/phenol"));
		
		RefineryRecipeBuilder.builder(new FluidStack(IEFluids.ACETALDEHYDE.getStill(), 8))
			.addCatalyst(IETags.getTagsFor(EnumMetals.COPPER).plate)
			.addInput(new FluidTagInput(IPTags.Fluids.ethylene, 8))
			.setEnergy(120)
			.build(out, rl("refinery/acetaldehyde"));
	}
	
	private void speedboatUpgradeRecipes(){
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.BoatUpgrades.REINFORCED_HULL.get())
			.define('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.define('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.pattern("P P")
			.pattern("PBP")
			.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.unlockedBy("has_steel_block", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.save(this.out);
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.BoatUpgrades.ICE_BREAKER.get())
			.define('P', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.define('I', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.define('B', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.pattern("I P")
			.pattern(" IP")
			.pattern("PPB")
			.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.unlockedBy("has_steel_block", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage)))
			.save(this.out);
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.BoatUpgrades.TANK.get())
			.define('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.define('T', IEBlocks.MetalDevices.BARREL)
			.pattern(" P ")
			.pattern("PTP")
			.pattern(" P ")
			.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
			.save(this.out);
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.BoatUpgrades.RUDDERS.get())
			.define('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.define('R', IETags.ironRod)
			.pattern(" RR")
			.pattern("PPP")
			.pattern("PPP")
			.unlockedBy("has_iron_rod", has(IETags.ironRod))
			.save(this.out);
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.BoatUpgrades.PADDLES.get())
			.define('P', IETags.getItemTag(IETags.treatedWood))
			.define('S', IETags.treatedStick)
			.pattern("S S")
			.pattern("S S")
			.pattern("P P")
			.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
			.save(this.out);
	}
	
	private void blockRecipes(){
		FluidAwareShapedRecipeBuilder.builder(IPContent.Blocks.ASPHALT.get(), 8)
			.define('C', IPContent.Items.BITUMEN.get())
			.define('S', Tags.Items.SAND)
			.define('G', Tags.Items.GRAVEL)
			.define('B', new IngredientFluidStack(FluidTags.WATER, FluidType.BUCKET_VOLUME))
			.pattern("SCS")
			.pattern("GBG")
			.pattern("SCS")
			.unlockedBy("has_bitumen", has(IPContent.Items.BITUMEN.get()))
			.save(this.out, rl("asphalt"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.ASPHALT_STAIR.get(), 6)
			.define('A', IPTags.getItemTag(IPTags.Blocks.asphalt))
			.pattern("A  ")
			.pattern("AA ")
			.pattern("AAA")
			.unlockedBy("has_bitumen", has(IPContent.Items.BITUMEN.get()))
			.unlockedBy("has_slag", has(IEItems.Ingredients.SLAG))
			.save(this.out, rl("asphalt_stair"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.ASPHALT_SLAB.get(), 6)
			.define('A', IPTags.getItemTag(IPTags.Blocks.asphalt))
			.pattern("AAA")
			.unlockedBy("has_bitumen", has(IPContent.Items.BITUMEN.get()))
			.unlockedBy("has_slag", has(IEItems.Ingredients.SLAG))
			.save(this.out, rl("asphalt_slab"));
		
		FluidAwareShapedRecipeBuilder.builder(IPContent.Blocks.ASPHALT.get(), 1)
			.define('S', IPContent.Blocks.ASPHALT_SLAB.get())
			.pattern("S")
			.pattern("S")
			.unlockedBy("has_bitumen", has(IPContent.Items.BITUMEN.get()))
			.unlockedBy("has_slag", has(IEItems.Ingredients.SLAG))
			.save(this.out, rl("asphalt"));
		
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(IPContent.Blocks.ASPHALT.get()), RecipeCategory.MISC, IPContent.Blocks.ASPHALT_SLAB.get(), 2)
			.unlockedBy("has_asphalt", has(IPContent.Blocks.ASPHALT.get()))
			.save(this.out, "asphalt_slab_from_asphalt_stonecutting");
		
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(IPContent.Blocks.ASPHALT.get()), RecipeCategory.MISC, IPContent.Blocks.ASPHALT_STAIR.get())
			.unlockedBy("has_asphalt", has(IPContent.Blocks.ASPHALT.get()))
			.save(this.out, "asphalt_stairs_from_asphalt_stonecutting");
		
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.GAS_GENERATOR.get())
			.define('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.define('G', MetalDecoration.GENERATOR)
			.define('C', IEBlocks.MetalDevices.CAPACITOR_LV)
			.pattern("PPP")
			.pattern("PGC")
			.pattern("PPP")
			.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
			.unlockedBy("has_"+toPath(IEBlocks.MetalDevices.CAPACITOR_LV), has(IEBlocks.MetalDevices.CAPACITOR_LV))
			.unlockedBy("has_"+toPath(MetalDecoration.GENERATOR), has(MetalDecoration.GENERATOR))
			.save(this.out, rl("gas_generator"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.AUTO_LUBRICATOR.get())
			.define('G', Tags.Items.GLASS)
			.define('T', IETags.getItemTag(IETags.treatedWood))
			.define('P', IEBlocks.MetalDevices.FLUID_PIPE)
			.pattern(" G ")
			.pattern("G G")
			.pattern("TPT")
			.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
			.unlockedBy("has_"+toPath(IEBlocks.MetalDevices.FLUID_PIPE), has(IEBlocks.MetalDevices.FLUID_PIPE))
			.save(this.out, rl("auto_lubricator"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.FLARESTACK.get())
			.define('I', IETags.getTagsFor(EnumMetals.IRON).plate)
			.define('C', IEItems.Ingredients.COMPONENT_STEEL)
			.define('P', IEBlocks.MetalDevices.FLUID_PIPE)
			.define('A', IEBlocks.MetalDevices.FLUID_PLACER)
			.define('F', Items.FLINT_AND_STEEL)
			.pattern("IFI")
			.pattern("CAC")
			.pattern("IPI")
			.unlockedBy("has_bitumen", has(IPContent.Items.BITUMEN.get()))
			.unlockedBy("has_"+toPath(IEBlocks.MetalDevices.FLUID_PIPE), has(IEBlocks.MetalDevices.FLUID_PIPE))
			.save(this.out, rl("flarestack"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.SEISMIC_SURVEY.get())
			.pattern("SBH")
			.pattern("SBS")
			.pattern("MLM")
			.define('S', IEBlocks.MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD))
			.define('L', IEBlocks.MetalDecoration.ENGINEERING_LIGHT)
			.define('M', IEItems.Ingredients.COMPONENT_IRON)
			.define('H', IEItems.Ingredients.GUNPART_HAMMER)
			.define('B', IEItems.Ingredients.GUNPART_BARREL)
			.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.save(this.out, rl("seismic_survey_tool"));
	}
	
	private void itemRecipes(){
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Items.OIL_CAN.get())
			.define('R', Tags.Items.DYES_RED)
			.define('P', IETags.getTagsFor(EnumMetals.IRON).plate)
			.define('B', Items.BUCKET)
			.pattern(" R ")
			.pattern("PBP")
			.unlockedBy("has_rose_red", has(Items.RED_DYE))
			.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
			.save(this.out);
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Items.PROJECTOR.get())
			.define('I', Tags.Items.INGOTS_IRON)
			.define('W', IETags.getItemTag(IETags.treatedWood))
			.define('L', MetalDecoration.LANTERN)
			.define('S', Tags.Items.GLASS)
			.pattern("S  ")
			.pattern("IL ")
			.pattern(" IW")
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
			.save(this.out);
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Items.SPEEDBOAT.get())
			.define('P', IETags.getItemTag(IETags.treatedWood))
			.define('E', MetalDecoration.ENGINEERING_LIGHT)
			.define('M', IEItems.Ingredients.COMPONENT_IRON)
			.pattern("PME")
			.pattern("PPP")
			.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
			.unlockedBy("has_"+toPath(MetalDecoration.ENGINEERING_LIGHT), has(MetalDecoration.ENGINEERING_LIGHT))
			.save(this.out);
		
		FluidAwareShapedRecipeBuilder.builder(IEItems.Misc.TOOL_UPGRADES.get(ToolUpgradeItem.ToolUpgrade.DRILL_LUBE))
			.pattern(" i ")
			.pattern("ioi")
			.pattern(" ip")
			.define('o', new IngredientFluidStack(IPTags.Fluids.lubricant, FluidType.BUCKET_VOLUME))
			.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
			.define('p', IEBlocks.MetalDevices.FLUID_PIPE)
			.unlockedBy("has_drill", has(IEItems.Tools.DRILL))
			.save(out, rl(toPath(IEItems.Misc.TOOL_UPGRADES.get(ToolUpgradeItem.ToolUpgrade.DRILL_LUBE))));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Items.MOLOTOV.get())
			.pattern("W")
			.pattern("M")
			.define('M', IPContent.Items.GASOLINE_BOTTLE.get())
			.define('W', IPTags.Items.wool)
			.unlockedBy("has_gasoline_bottle", has(IPContent.Items.GASOLINE_BOTTLE.get()))
			.save(this.out, rl("molotov_wool"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Items.MOLOTOV.get())
			.pattern("F")
			.pattern("M")
			.define('M', IPContent.Items.GASOLINE_BOTTLE.get())
			.define('F', IETags.fabricHemp)
			.unlockedBy("has_gasoline_bottle", has(IPContent.Items.GASOLINE_BOTTLE.get()))
			.save(this.out, rl("molotov_fabric"));
		
		BottlingMachineRecipeBuilder.builder(IPContent.Items.GASOLINE_BOTTLE.get())
			.addFluidTag(IPTags.Fluids.gasoline, GasolineBottleItem.FILLED_AMOUNT)
			.addInput(Items.GLASS_BOTTLE)
			.build(this.out, rl("bottling/gasoline_bottle"));
	}
	
	private void paraffinWaxRecipes(){
		// Paraffin Wax Compression and Decompression
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IPContent.Blocks.PARAFFIN_WAX.get())
			.define('c', IPTags.Items.paraffinWax)
			.pattern("ccc")
			.pattern("ccc")
			.pattern("ccc")
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(this.out, rl("paraffin_wax_to_block"));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, IPContent.Items.PARAFFIN_WAX.get(), 9)
			.requires(IPTags.getItemTag(IPTags.Blocks.paraffinWaxBlock))
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(this.out, rl("paraffin_wax_block_to_items"));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IEItems.Ingredients.ERSATZ_LEATHER, 8)
			.pattern("fff")
			.pattern("fwf")
			.pattern("fff")
			.define('f', IETags.fabricHemp)
			.define('w', IPTags.Items.paraffinWax)
			.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(IEItems.Ingredients.ERSATZ_LEATHER)));
		
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.CANDLE, 1)
			.pattern("s")
			.pattern("w")
			.define('s', Tags.Items.STRING)
			.define('w', IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.CANDLE)));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_COPPER_BLOCK, 1)
			.requires(Tags.Items.STORAGE_BLOCKS_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.COPPER_BLOCK) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_CUT_COPPER, 1)
			.requires(Items.CUT_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.CUT_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_CUT_COPPER_STAIRS, 1)
			.requires(Items.CUT_COPPER_STAIRS)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.CUT_COPPER_STAIRS) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_CUT_COPPER_SLAB, 1)
			.requires(Items.CUT_COPPER_SLAB)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.CUT_COPPER_SLAB) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_EXPOSED_COPPER, 1)
			.requires(Items.EXPOSED_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.EXPOSED_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_EXPOSED_CUT_COPPER, 1)
			.requires(Items.EXPOSED_CUT_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.EXPOSED_CUT_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_EXPOSED_CUT_COPPER_STAIRS, 1)
			.requires(Items.EXPOSED_CUT_COPPER_STAIRS)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.EXPOSED_CUT_COPPER_STAIRS) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_EXPOSED_CUT_COPPER_SLAB, 1)
			.requires(Items.EXPOSED_CUT_COPPER_SLAB)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.EXPOSED_CUT_COPPER_SLAB) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_WEATHERED_COPPER, 1)
			.requires(Items.WEATHERED_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.WEATHERED_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_WEATHERED_CUT_COPPER, 1)
			.requires(Items.WEATHERED_CUT_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.WEATHERED_CUT_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_WEATHERED_CUT_COPPER_STAIRS, 1)
			.requires(Items.WEATHERED_CUT_COPPER_STAIRS)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.WEATHERED_CUT_COPPER_STAIRS) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_WEATHERED_CUT_COPPER_SLAB, 1)
			.requires(Items.WEATHERED_CUT_COPPER_SLAB)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.WEATHERED_CUT_COPPER_SLAB) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_OXIDIZED_COPPER, 1)
			.requires(Items.OXIDIZED_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.OXIDIZED_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_OXIDIZED_CUT_COPPER, 1)
			.requires(Items.OXIDIZED_CUT_COPPER)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.OXIDIZED_CUT_COPPER) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS, 1)
			.requires(Items.OXIDIZED_CUT_COPPER_STAIRS)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.OXIDIZED_CUT_COPPER_STAIRS) + "_paraffin_waxed"));
		
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WAXED_OXIDIZED_CUT_COPPER_SLAB, 1)
			.requires(Items.OXIDIZED_CUT_COPPER_SLAB)
			.requires(IPTags.Items.paraffinWax)
			.unlockedBy("has_paraffin_wax", has(IPTags.Items.paraffinWax))
			.save(out, rl(toPath(Items.OXIDIZED_CUT_COPPER_SLAB) + "_paraffin_waxed"));
		
	}
	
	private ResourceLocation rl(String str){
		if(PATH_COUNT.containsKey(str)){
			int count = PATH_COUNT.get(str) + 1;
			PATH_COUNT.put(str, count);
			return ResourceUtils.ip(str + count);
		}
		PATH_COUNT.put(str, 1);
		return ResourceUtils.ip(str);
	}
	
	private String toPath(ItemLike src){
		return RegistryUtils.getRegistryNameOf(src.asItem()).getPath();
	}
	
}
