package flaxbeard.immersivepetroleum.common;

import flaxbeard.immersivepetroleum.common.blocks.tileentities.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class IPTileTypes{
	// Multiblocks
	//public static final MultiblockBEType<PumpjackTileEntity> PUMP = IPRegisters.registerMultiblockTE("pumpjack", PumpjackTileEntity::new, IPContent.Multiblock.PUMPJACK);
	//public static final MultiblockBEType<OilTankTileEntity> OILTANK = IPRegisters.registerMultiblockTE("oiltank", OilTankTileEntity::new, IPContent.Multiblock.OILTANK);
	/*public static final MultiblockBEType<DistillationTowerTileEntity> TOWER = IPRegisters.registerMultiblockTE("distillationtower", DistillationTowerTileEntity::new, IPContent.Multiblock.DISTILLATIONTOWER);
	public static final MultiblockBEType<CokerUnitTileEntity> COKER = IPRegisters.registerMultiblockTE("cokerunit", CokerUnitTileEntity::new, IPContent.Multiblock.COKERUNIT);
	public static final MultiblockBEType<HydrotreaterTileEntity> TREATER = IPRegisters.registerMultiblockTE("hydrotreater", HydrotreaterTileEntity::new, IPContent.Multiblock.HYDROTREATER);
	public static final MultiblockBEType<DerrickTileEntity> DERRICK = IPRegisters.registerMultiblockTE("derrick", DerrickTileEntity::new, IPContent.Multiblock.DERRICK);
	*/

	// Normal Blocks
	public static final RegistryObject<BlockEntityType<GasGeneratorTileEntity>> GENERATOR = IPRegisters.registerTE("gasgenerator", GasGeneratorTileEntity::new, IPContent.Blocks.GAS_GENERATOR);
	public static final RegistryObject<BlockEntityType<AutoLubricatorTileEntity>> AUTOLUBE = IPRegisters.registerTE("autolubricator", AutoLubricatorTileEntity::new, IPContent.Blocks.AUTO_LUBRICATOR);
	public static final RegistryObject<BlockEntityType<FlarestackTileEntity>> FLARE = IPRegisters.registerTE("flarestack", FlarestackTileEntity::new, IPContent.Blocks.FLARESTACK);
	public static final RegistryObject<BlockEntityType<WellTileEntity>> WELL = IPRegisters.registerTE("well", WellTileEntity::new, IPContent.Blocks.WELL);
	public static final RegistryObject<BlockEntityType<WellPipeTileEntity>> WELL_PIPE = IPRegisters.registerTE("well_pipe", WellPipeTileEntity::new, IPContent.Blocks.WELL_PIPE);
	
	public static final RegistryObject<BlockEntityType<SeismicSurveyTileEntity>> SEISMIC_SURVEY = IPRegisters.registerTE("seismic_survey", SeismicSurveyTileEntity::new, IPContent.Blocks.SEISMIC_SURVEY);

	public static void forceClassLoad()
	{ }
}
