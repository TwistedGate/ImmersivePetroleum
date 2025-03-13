package flaxbeard.immersivepetroleum.common.data;


import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.data.models.NongeneratedModels;
import blusunrize.immersiveengineering.data.models.SplitModelBuilder;
import com.google.common.base.Preconditions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.metal.FlarestackBlock;
import flaxbeard.immersivepetroleum.common.blocks.metal.GasGeneratorBlock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.*;
import flaxbeard.immersivepetroleum.common.blocks.stone.WellPipeBlock;
import flaxbeard.immersivepetroleum.common.blocks.wooden.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IPBlockStates extends BlockStateProvider{
	final ExistingFileHelper exFileHelper;
	private final NongeneratedModels nongeneratedModels;
	public IPBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper){
		super(gen.getPackOutput(), ImmersivePetroleum.MODID, exFileHelper);
		this.exFileHelper = exFileHelper;
		this.nongeneratedModels = new NongeneratedModels(gen.getPackOutput(), exFileHelper);
	}
	
	@Override
	protected void registerStatesAndModels(){
		// Multiblocks
		distillationtower();
		pumpjack();
		cokerunit();
		hydrotreater();
		derrick();
		oiltank();
		
		// Oddballs
		autolubricator();
		flarestack();
		seismicsurvey();
		
		// "Normal" Blocks
		simpleBlockWithItem(IPContent.Blocks.PETCOKE.get());
		simpleBlockWithItem(IPContent.Blocks.PARAFFIN_WAX.get());
		gasGenerator();
		asphaltBlocks();
		
		{
			Block well = IPContent.Blocks.WELL.get();
			
			ModelFile wellModel = models()
					.cubeTop(RegistryUtils.getRegistryNameOf(well).toString(), mcLoc("block/bedrock"), modLoc("block/well_top_oil"))
					.renderType("cutout");
			getVariantBuilder(well).partialState()
				.setModels(new ConfiguredModel(wellModel));
		}
		
		{
			Block wellPipe = IPContent.Blocks.WELL_PIPE.get();
			final ResourceLocation wellPipeRL = RegistryUtils.getRegistryNameOf(wellPipe);
			
			ResourceLocation ieConreteTexture = ResourceUtils.ie("block/stone_decoration/concrete/concrete0");
			ResourceLocation concrete_cracked = modLoc("block/concrete_cracked");
			ResourceLocation wellPipeTexture = modLoc("block/well_pipe_top");
			
			ModelFile wellPipeModel = models().cubeBottomTop(wellPipeRL.toString(), ieConreteTexture, wellPipeTexture, wellPipeTexture);
			ModelFile wellPipeModel_cracked = models().cubeBottomTop(wellPipeRL.toString() + "_cracked", concrete_cracked, wellPipeTexture, wellPipeTexture);
			
			ModelFile wellPipeModel_cracked_mirrored = models()
				.withExistingParent(wellPipeRL.toString() + "_cracked_mirrored", "block/cube_mirrored")
				.texture("down", wellPipeTexture)
				.texture("up", wellPipeTexture)
				.texture("north", concrete_cracked)
				.texture("south", concrete_cracked)
				.texture("east", concrete_cracked)
				.texture("west", concrete_cracked);
			
			VariantBlockStateBuilder builder = getVariantBuilder(wellPipe);
			builder.partialState()
				.with(WellPipeBlock.BROKEN, false)
				.setModels(new ConfiguredModel(wellPipeModel));
			builder.partialState()
				.with(WellPipeBlock.BROKEN, true)
				.setModels(new ConfiguredModel(wellPipeModel_cracked), new ConfiguredModel(wellPipeModel_cracked_mirrored));
		}
		
		// Fluids
		for(IPFluid.IPFluidEntry f:IPFluid.FLUIDS){
			IPFluid source = f.source().get();
			Mutable<IClientFluidTypeExtensions> box = new MutableObject<>();
			source.getFluidType().initializeClient(box::setValue);
			ResourceLocation texture = box.getValue().getStillTexture();
			ModelFile model = this.models().getBuilder("block/fluid/" + ForgeRegistries.FLUIDS.getKey(source).getPath())
					.texture("particle", texture);
			
			getVariantBuilder(f.block().get()).partialState().setModels(new ConfiguredModel(model));
		}
	}
	
	private void asphaltBlocks(){
		ResourceLocation texture = modLoc("block/asphalt");
		simpleBlockWithItem(IPContent.Blocks.ASPHALT.get());
		slabWithItem(IPContent.Blocks.ASPHALT_SLAB.get(), texture);
		stairsWithItem(IPContent.Blocks.ASPHALT_STAIR.get(), texture);
	}
	
	private void stairsWithItem(StairBlock block, ResourceLocation texture){
		String name = RegistryUtils.getRegistryNameOf(block).toString();
		
		ModelFile stairs = models().stairs(name, texture, texture, texture);
		ModelFile stairsInner = models().stairsInner(name + "_inner", texture, texture, texture);
		ModelFile stairsOuter = models().stairsOuter(name + "_outer", texture, texture, texture);
		
		stairsBlock(block, stairs, stairsInner, stairsOuter);
		
		getItemBuilder(block).parent(stairs)
			.texture("particle", texture);
	}
	
	private void slabWithItem(SlabBlock block, ResourceLocation texture){
		ModelFile bottom = models().slab(getPath(block), texture, texture, texture);
		ModelFile top = models().slabTop(getPath(block) + "_top", texture, texture, texture);
		ModelFile doubleslab = models().getExistingFile(texture);
		
		getVariantBuilder(block)
			.partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottom))
			.partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(top))
			.partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleslab));
		
		getItemBuilder(block).parent(bottom)
			.texture("particle", texture);
	}
	
	private void distillationtower(){
		ResourceLocation idleTexture = modLoc("multiblock/distillation_tower");
		ResourceLocation modelNormal = modLoc("models/multiblock/obj/distillationtower.obj");
		ResourceLocation modelMirrored = modLoc("models/multiblock/obj/distillationtower_mirrored.obj");
		
		BlockModelBuilder normal = multiblockModel(IPContent.Multiblock.DISTILLATIONTOWER.block().get(), modelNormal, idleTexture, "_idle", DistillationTowerMultiblock.INSTANCE, false);
		BlockModelBuilder mirrored = multiblockModel(IPContent.Multiblock.DISTILLATIONTOWER.block().get(), modelMirrored, idleTexture, "_mirrored_idle", DistillationTowerMultiblock.INSTANCE, true);
		
		createMultiblock(IPContent.Multiblock.DISTILLATIONTOWER.block().get(), normal, mirrored, idleTexture);
	}
	
	private void pumpjack(){
		ResourceLocation texture = modLoc("multiblock/pumpjack_base");
		ResourceLocation modelNormal = modLoc("models/multiblock/obj/pumpjack.obj");
		ResourceLocation modelMirrored = modLoc("models/multiblock/obj/pumpjack_mirrored.obj");
		
		BlockModelBuilder normal = multiblockModel(IPContent.Multiblock.PUMPJACK.block().get(), modelNormal, texture, "", PumpjackMultiblock.INSTANCE, false);
		BlockModelBuilder mirrored = multiblockModel(IPContent.Multiblock.PUMPJACK.block().get(), modelMirrored, texture, "_mirrored", PumpjackMultiblock.INSTANCE, true);
		
		createMultiblock(IPContent.Multiblock.PUMPJACK.block().get(), normal, mirrored, texture);
	}
	
	private void cokerunit(){
		ResourceLocation texture = modLoc("multiblock/cokerunit");
		ResourceLocation modelNormal = modLoc("models/multiblock/obj/cokerunit.obj");
		ResourceLocation modelMirrored = modLoc("models/multiblock/obj/cokerunit_mirrored.obj");
		
		BlockModelBuilder normal = multiblockModel(IPContent.Multiblock.COKERUNIT.block().get(), modelNormal, texture, "", CokerUnitMultiblock.INSTANCE, false);
		BlockModelBuilder mirrored = multiblockModel(IPContent.Multiblock.COKERUNIT.block().get(), modelMirrored, texture, "_mirrored", CokerUnitMultiblock.INSTANCE, true);
		
		createMultiblock(IPContent.Multiblock.COKERUNIT.block().get(), normal, mirrored, texture);
	}
	
	private void hydrotreater(){
		ResourceLocation texture = modLoc("multiblock/hydrotreater");
		ResourceLocation modelNormal = modLoc("models/multiblock/obj/hydrotreater.obj");
		ResourceLocation modelMirrored = modLoc("models/multiblock/obj/hydrotreater_mirrored.obj");
		
		BlockModelBuilder normal = multiblockModel(IPContent.Multiblock.HYDROTREATER.block().get(), modelNormal, texture, "", HydroTreaterMultiblock.INSTANCE, false);
		BlockModelBuilder mirrored = multiblockModel(IPContent.Multiblock.HYDROTREATER.block().get(), modelMirrored, texture, "_mirrored", HydroTreaterMultiblock.INSTANCE, true);
		
		createMultiblock(IPContent.Multiblock.HYDROTREATER.block().get(), normal, mirrored, texture);
	}
	
	private void derrick(){
		ResourceLocation texture = modLoc("multiblock/derrick");
		ResourceLocation modelNormal = modLoc("models/multiblock/obj/derrick.obj");
		ResourceLocation modelMirrored = modLoc("models/multiblock/obj/derrick_mirrored.obj");
		
		BlockModelBuilder normal = multiblockModel(IPContent.Multiblock.DERRICK.block().get(), modelNormal, texture, "", DerrickMultiblock.INSTANCE, false);
		BlockModelBuilder mirrored = multiblockModel(IPContent.Multiblock.DERRICK.block().get(), modelMirrored, texture, "_mirrored", DerrickMultiblock.INSTANCE, true);
		
		createMultiblock(IPContent.Multiblock.DERRICK.block().get(), normal, mirrored, texture);
	}
	
	private void oiltank(){
		ResourceLocation texture = modLoc("multiblock/oiltank");
		ResourceLocation modelNormal = modLoc("models/multiblock/obj/oiltank.obj");
		ResourceLocation modelMirrored = modLoc("models/multiblock/obj/oiltank_mirrored.obj");
		
		BlockModelBuilder normal = multiblockModel(IPContent.Multiblock.OILTANK.block().get(), modelNormal, texture, "", OilTankMultiblock.INSTANCE, false);
		BlockModelBuilder mirrored = multiblockModel(IPContent.Multiblock.OILTANK.block().get(), modelMirrored, texture, "_mirrored", OilTankMultiblock.INSTANCE, true);
		
		createMultiblock(IPContent.Multiblock.OILTANK.block().get(), normal, mirrored, texture);
	}
	
	private BlockModelBuilder multiblockModel(Block block, ResourceLocation model, ResourceLocation texture, String add, TemplateMultiblock mb, boolean mirror){
		UnaryOperator<BlockPos> transform = UnaryOperator.identity();
		loadTemplateFor(mb);
		if(mirror){
			Vec3i size = mb.getSize(null);
			transform = p -> new BlockPos(size.getX() - p.getX() - 1, p.getY(), p.getZ());
		}
		final Vec3i offset = mb.getMasterFromOriginOffset();
		
		Stream<Vec3i> partsStream = mb.getStructure(null).stream()
			.filter(info -> !info.state().isAir())
			.map(info -> info.pos())
			.map(transform)
			.map(p -> p.subtract(offset));
		
		String name = getMultiblockPath(block) + add;
		NongeneratedModels.NongeneratedModel base = nongeneratedModels.withExistingParent(name, mcLoc("block"))
			.customLoader(ObjModelBuilder::begin).modelLocation(model).automaticCulling(false).flipV(true).end()
			.texture("texture", texture)
			.texture("particle", texture)
			.renderType("cutout");
		
		BlockModelBuilder split = this.models().withExistingParent(name + "_split", mcLoc("block"))
			.customLoader(SplitModelBuilder::begin)
			.innerModel(base)
			.parts(partsStream.collect(Collectors.toList()))
			.dynamic(false).end();
		
		return split;
	}

	private void loadTemplateFor(TemplateMultiblock mb)
	{
		final ResourceLocation name = mb.getUniqueName();
		if(TemplateMultiblock.SYNCED_CLIENT_TEMPLATES.containsKey(name))
			return;
		final String filePath = "structures/"+name.getPath()+".nbt";
		int slash = filePath.indexOf('/');
		String prefix = filePath.substring(0, slash);
		ResourceLocation shortLoc = new ResourceLocation(
				name.getNamespace(),
				filePath.substring(slash+1)
		);
		try
		{
			final Resource resource = exFileHelper.getResource(shortLoc, PackType.SERVER_DATA, "", prefix);
			try(final InputStream input = resource.open())
			{
				final CompoundTag nbt = NbtIo.readCompressed(input);
				final StructureTemplate template = new StructureTemplate();
				template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
				TemplateMultiblock.SYNCED_CLIENT_TEMPLATES.put(name, template);
			}
		} catch(IOException e)
		{
			throw new RuntimeException("Failed on "+name, e);
		}
	}

	private void autolubricator(){
		ResourceLocation texture = modLoc("models/lubricator");
		
		BlockModelBuilder lube_empty = this.models().withExistingParent("lube_empty", ResourceUtils.ie("block/ie_empty")).texture("particle", texture);
		
		BlockModelBuilder lubeModel = this.models().withExistingParent(getPath(IPContent.Blocks.AUTO_LUBRICATOR.get()), mcLoc("block"))
			.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/block/obj/autolubricator.obj")).flipV(true).end()
			.texture("texture", texture)
			.texture("particle", texture)
			.renderType("translucent");
		
		VariantBlockStateBuilder lubeBuilder = getVariantBuilder(IPContent.Blocks.AUTO_LUBRICATOR.get());
		for(Direction dir:AutoLubricatorBlock.FACING.getPossibleValues()){
			int rot = (int) ((dir.toYRot()) + 90 % 360);
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, false)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(new ConfiguredModel(lubeModel, 0, rot, false));
			
			lubeBuilder.partialState()
				.with(AutoLubricatorBlock.SLAVE, true)
				.with(AutoLubricatorBlock.FACING, dir)
				.setModels(new ConfiguredModel(lube_empty));
		}
	}
	
	private void flarestack(){
		ResourceLocation texture = modLoc("block/obj/flarestack");
		ConfiguredModel emptyModel = new ConfiguredModel(this.models().withExistingParent("flare_empty", ResourceUtils.ie("block/ie_empty")).texture("particle", texture));
		
		BlockModelBuilder flarestackModel = this.models().withExistingParent(getPath(IPContent.Blocks.FLARESTACK.get()), mcLoc("block"))
			.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/block/obj/flarestack.obj")).flipV(true).end()
			.texture("texture", texture)
			.texture("particle", texture)
			.renderType("cutout");
		
		VariantBlockStateBuilder flarestackBuilder = getVariantBuilder(IPContent.Blocks.FLARESTACK.get());
		
		flarestackBuilder.partialState()
			.with(FlarestackBlock.SLAVE, false)
			.setModels(new ConfiguredModel(flarestackModel, 0, 0, false));
		
		flarestackBuilder.partialState()
			.with(FlarestackBlock.SLAVE, true)
			.setModels(emptyModel);
	}


	private void seismicsurvey(){
		ResourceLocation texture = modLoc("block/obj/seismic_survey_tool");
		ConfiguredModel emptyModel = new ConfiguredModel(this.models().withExistingParent("seismic_empty", ResourceUtils.ie("block/ie_empty")).texture("particle", texture));
		
		BlockModelBuilder flarestackModel = this.models().withExistingParent(getPath(IPContent.Blocks.SEISMIC_SURVEY.get()), mcLoc("block"))
			.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/block/obj/seismic_survey_tool.obj")).flipV(true).end()
			.texture("texture", texture)
			.texture("particle", texture)
			.renderType("cutout");
		
		VariantBlockStateBuilder flarestackBuilder = getVariantBuilder(IPContent.Blocks.SEISMIC_SURVEY.get());
		
		flarestackBuilder.partialState()
			.with(FlarestackBlock.SLAVE, false)
			.setModels(new ConfiguredModel(flarestackModel, 0, 0, false));
		
		flarestackBuilder.partialState()
			.with(FlarestackBlock.SLAVE, true)
			.setModels(emptyModel);
	}
	
	private void gasGenerator(){
		ResourceLocation texture = modLoc("block/obj/generator");
		
		BlockModelBuilder model = this.models().getBuilder(getPath(IPContent.Blocks.GAS_GENERATOR.get()))
			.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/block/obj/generator.obj")).flipV(true).end()
			.texture("texture", texture)
			.texture("particle", texture)
			.renderType("cutout");
		
		VariantBlockStateBuilder builder = getVariantBuilder(IPContent.Blocks.GAS_GENERATOR.get());
		for(Direction dir:GasGeneratorBlock.FACING.getPossibleValues()){
			int rot = (int) (dir.toYRot() % 360);
			
			builder.partialState()
				.with(GasGeneratorBlock.FACING, dir)
				.setModels(new ConfiguredModel(model, 0, rot, false));
		}
	}
	
	/**
	 * From {@link blusunrize.immersiveengineering.common.data.BlockStates}
	 */
	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel, ResourceLocation particleTexture){
		createMultiblock(b, masterModel, mirroredModel, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, IEProperties.MIRRORED, 180, particleTexture);
	}
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} */
	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, Property<Boolean> isSlave, EnumProperty<Direction> facing, @Nullable Property<Boolean> mirroredState, int rotationOffset, ResourceLocation particleTex){
		Preconditions.checkArgument((mirroredModel == null) == (mirroredState == null));
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		
		boolean[] possibleMirrorStates;
//		boolean[] possibleSlaveStates = new boolean[]{false, true};
		if(mirroredState != null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored:possibleMirrorStates)
			for(Direction dir:facing.getPossibleValues()){
				final int angleY;
				final int angleX;
				if(facing.getPossibleValues().contains(Direction.UP)){
					angleX = -90 * dir.getStepY();
					if(dir.getAxis() != Direction.Axis.Y)
						angleY = getAngle(dir, rotationOffset);
					else
						angleY = 0;
				}else{
					angleY = getAngle(dir, rotationOffset);
					angleX = 0;
				}
				
			ModelFile model = mirrored ? mirroredModel : masterModel;
				PartialBlockstate partialState = builder.partialState()
						//.with(isSlave, slave)
						.with(facing, dir);

				if(mirroredState != null)
					partialState = partialState.with(mirroredState, mirrored);

				partialState.setModels(new ConfiguredModel(model, angleX, angleY, true));
			}
	}
	
	/** From {@link blusunrize.immersiveengineering.common.data.BlockStates} */
	private int getAngle(Direction dir, int offset){
		return (int) ((dir.toYRot() + offset) % 360);
	}
	
	private String getMultiblockPath(Block b){
		return "multiblock/" + getPath(b);
	}
	
	private String getPath(Block b){
		return RegistryUtils.getRegistryNameOf(b).getPath();
	}
	
	private void itemModelWithParent(Block block, ModelFile parent){
		getItemBuilder(block).parent(parent)
			.texture("particle", modLoc("block/" + getPath(block)));
	}
	
	private void simpleBlockWithItem(Block block){
		ModelFile file = cubeAll(block);
		
		getVariantBuilder(block).partialState()
			.setModels(new ConfiguredModel(file));
		itemModelWithParent(block, file);
	}
	
	private ItemModelBuilder getItemBuilder(Block block){
		return itemModels().getBuilder(modLoc("item/" + getPath(block)).toString());
	}
}
