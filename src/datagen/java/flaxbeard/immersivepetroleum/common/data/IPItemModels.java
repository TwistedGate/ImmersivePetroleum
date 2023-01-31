package flaxbeard.immersivepetroleum.common.data;

import javax.annotation.Nullable;

import com.mojang.math.Vector3f;

import blusunrize.immersiveengineering.api.client.ieobj.DefaultCallback;
import blusunrize.immersiveengineering.data.models.IEOBJBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicBucketModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IPItemModels extends ModelProvider<TRSRModelBuilder>{
	public IPItemModels(DataGenerator gen, ExistingFileHelper exHelper){
		super(gen, ImmersivePetroleum.MODID, ITEM_FOLDER, TRSRModelBuilder::new, exHelper);
//		super(gen, ImmersivePetroleum.MODID, exHelper);
	}
	
	@Override
	public String getName(){
		return "Item Models";
	}
	
	@Override
	protected void registerModels(){
		String debugItem = name(IPContent.DEBUGITEM.get());
		
		getBuilder(debugItem)
			.parent(getExistingFile(mcLoc("item/generated")))
			.texture("layer0", modLoc("item/schematic"));
		
		genericItem(IPContent.Items.BITUMEN.get());
		genericItem(IPContent.Items.PETCOKE.get());
		genericItem(IPContent.Items.PETCOKEDUST.get());
		genericItem(IPContent.Items.OIL_CAN.get());
		genericItem(IPContent.Items.SPEEDBOAT.get());
		genericItem(IPContent.Items.PARAFFIN_WAX.get());
		
		genericItem(IPContent.BoatUpgrades.ICE_BREAKER.get());
		genericItem(IPContent.BoatUpgrades.REINFORCED_HULL.get());
		genericItem(IPContent.BoatUpgrades.PADDLES.get());
		genericItem(IPContent.BoatUpgrades.RUDDERS.get());
		genericItem(IPContent.BoatUpgrades.TANK.get());
		
		genericItem(IPContent.Items.SURVEYRESULT.get());
		
		projectorItem();
		generatorItem();
		autolubeItem();
		flarestackItem();
		surveyToolItem();
		
		// Multiblock items
		pumpjackItem();
		distillationtowerItem();
		cokerunitItem();
		hydrotreaterItem();
		derrickItem();
		oiltankItem();
		
		for(IPFluid.IPFluidEntry f:IPFluid.FLUIDS)
			createBucket(f.still().get());
	}
	
	private void flarestackItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.FLARESTACK.get(), "block/obj/flarestack.obj")
				.texture("texture", modLoc("block/obj/flarestack"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, null, null, 0.25F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, null, null, 0.25F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, -3, 0), new Vector3f(30, 225, 0), 0.4F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -4, 0), null, 0.5F);
	}
	
	private void surveyToolItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.SEISMIC_SURVEY.get(), "block/obj/seismic_survey_tool.obj")
				.texture("texture", modLoc("block/obj/seismic_survey_tool"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, null, null, 0.2F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, null, null, 0.2F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(0, -2, 0), null, 0.2F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, -2, 0), null, 0.2F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, -4, 0), new Vector3f(30, 225, 0), 0.3F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, 3, 0), null, 0.2F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -4, 0), null, 0.3F);
	}
	
	private void generatorItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.GAS_GENERATOR.get(), "block/obj/generator.obj")
				.texture("texture", modLoc("block/obj/generator"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 2.0F, 0), new Vector3f(0, 225, 0), 0.4F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, new Vector3f(0, 2.0F, 0), new Vector3f(0, 45, 0), 0.4F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.375F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.375F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 13, 0), null, 0.8F);
		doTransform(trans, TransformType.GUI, null, new Vector3f(30, 225, 0), 0.625F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, TransformType.FIXED, null, null, 0.5F);
	}
	
	private void autolubeItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.AUTO_LUBRICATOR.get(), "block/obj/autolubricator.obj")
			.texture("texture", modLoc("models/lubricator"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, null, null, 0.25F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, null, null, 0.25F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, -3, 0), new Vector3f(30, 225, 0), 0.4F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -4, 0), null, 0.5F);
	}
	
	private void projectorItem(){
		TRSRModelBuilder model = objIELoader(IPContent.Items.PROJECTOR.get(), "item/obj/projector.obj")
				.callback(DefaultCallback.INSTANCE)
				.end()
				.texture("texture", modLoc("projectors/projector"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 4, -2), null, 0.75F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, new Vector3f(12, 4, -2), null, 0.75F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(-6, -4, 4.225F), new Vector3f(90, 0, 0), 0.75F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(6, -4, 4.225F), new Vector3f(90, 0, 0), 0.75F);
		doTransform(trans, TransformType.HEAD, new Vector3f(8, 18.25F, 8), null, 1.0F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, 12, 0), new Vector3f(30, 135, 0), 1.0F);
		doTransform(trans, TransformType.GROUND, new Vector3f(4, 8, 4), null, 0.5F);
		doTransform(trans, TransformType.FIXED, new Vector3f(-6, 6, 5), new Vector3f(0, -90, 0), 0.75F);
	}
	
	private void distillationtowerItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.DISTILLATIONTOWER.get(), "multiblock/obj/distillationtower.obj")
			.texture("texture", modLoc("multiblock/distillation_tower"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, null, null, 0.03125F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, null, null, 0.03125F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, -5, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, -5, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
		doTransform(trans, TransformType.HEAD, new Vector3f(1.5F, 8, 1.5F), null, 0.2F);
		doTransform(trans, TransformType.GUI, new Vector3f(-1, -6, 0), new Vector3f(30, 225, 0), 0.0625F);
		doTransform(trans, TransformType.GROUND, new Vector3f(1, 0, 1), null, 0.0625F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void pumpjackItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.PUMPJACK.get(), "item/obj/pumpjack_itemmockup.obj")
			.texture("texture_base", modLoc("multiblock/pumpjack_base"))
			.texture("texture_armature", modLoc("models/pumpjack_armature"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, new Vector3f(-1.75F, 2.5F, 1.25F), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, new Vector3f(-1.75F, 2.5F, 1.75F), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, 0, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, 0, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 8, -8), null, 0.2F);
		doTransform(trans, TransformType.GUI, new Vector3f(6, -6, 0), new Vector3f(30, 225, 0), 0.1875F);
		doTransform(trans, TransformType.GROUND, new Vector3f(-1.5F, 3, -1.5F), null, 0.0625F);
		doTransform(trans, TransformType.FIXED, new Vector3f(-1, -8, -2), null, 0.0625F);
	}
	
	private void cokerunitItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.COKERUNIT.get(), "multiblock/obj/cokerunit.obj")
				.texture("texture", modLoc("multiblock/cokerunit"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, null, new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, null, new Vector3f(0, 45, 0), 0.03125F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.03125F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.03125F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 12, 0), null, 0.125F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, -4, 0), new Vector3f(30, 225, 0), 0.0625F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, -8, 0), null, 0.03125F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void hydrotreaterItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.HYDROTREATER.get(), "multiblock/obj/hydrotreater.obj")
				.texture("texture", modLoc("multiblock/hydrotreater"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, null, new Vector3f(0, 225, 0), 0.0625F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, null, new Vector3f(0, 45, 0), 0.0625F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.0625F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.0625F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 8, 0), null, 0.25F);
		doTransform(trans, TransformType.GUI, new Vector3f(-1, -1, 0), new Vector3f(30, 225, 0), 0.15625F);
		doTransform(trans, TransformType.GROUND, null, null, 0.125F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -1, 0), null, 0.125F);
	}
	
	private void derrickItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.DERRICK.get(), "multiblock/obj/derrick.obj")
				.texture("texture", modLoc("multiblock/derrick"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 7.6F, 0), new Vector3f(0, 180, 0), 0.15625F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, -2, 0), new Vector3f(30, 45, 0), 0.0625F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, 1, 0), null, 0.0625F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void oiltankItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.OILTANK.get(), "multiblock/obj/oiltank.obj")
				.texture("texture", modLoc("multiblock/oiltank"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, TransformType.FIRST_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.FIRST_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.THIRD_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.THIRD_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, TransformType.HEAD, new Vector3f(0, 7.6F, 0), new Vector3f(0, 180, 0), 0.15625F);
		doTransform(trans, TransformType.GUI, new Vector3f(0, -2, 0), new Vector3f(30, 45, 0), 0.125F);
		doTransform(trans, TransformType.GROUND, new Vector3f(0, 1, 0), null, 0.0625F);
		doTransform(trans, TransformType.FIXED, new Vector3f(0, -1, 0), new Vector3f(0, 180, 0), 0.0625F);
	}
	
	private void doTransform(ModelBuilder<?>.TransformsBuilder transform, TransformType type, @Nullable Vector3f translation, @Nullable Vector3f rotationAngle, float scale){
		ModelBuilder<?>.TransformsBuilder.TransformVecBuilder trans = transform.transform(type);
		if(translation != null)
			trans.translation(translation.x(), translation.y(), translation.z());
		if(rotationAngle != null)
			trans.rotation(rotationAngle.x(), rotationAngle.y(), rotationAngle.z());
		trans.scale(scale);
		trans.end();
	}
	
	private TRSRModelBuilder obj(ItemLike item, String model){
		return getBuilder(item.asItem().getRegistryName().toString())
				.customLoader(OBJLoaderBuilder::begin)
				.modelLocation(modLoc("models/" + model)).flipV(true).end();
	}
	
	private IEOBJBuilder<TRSRModelBuilder> objIELoader(ItemLike item, String model){
		return getBuilder(item.asItem().getRegistryName().toString())
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(modLoc("models/" + model));
	}
	
	private void genericItem(Item item){
		if(item == null){
			StackTraceElement where = new NullPointerException().getStackTrace()[1];
			IPDataGenerator.log.warn("Skipping null item. ( {} -> {} )", where.getFileName(), where.getLineNumber());
			return;
		}
		String name = name(item);
		
		getBuilder(name)
			.parent(getExistingFile(mcLoc("item/generated")))
			.texture("layer0", modLoc("item/"+name));
	}
	
	private void createBucket(Fluid f){
		withExistingParent(f.getBucket().asItem().getRegistryName().getPath(), ResourceUtils.forge("item/bucket"))
			.customLoader(DynamicBucketModelBuilder::begin)
			.fluid(f);
	}
	
	private String name(ItemLike item){
		return item.asItem().getRegistryName().getPath();
	}
}
