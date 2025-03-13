package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.api.client.ieobj.DefaultCallback;
import blusunrize.immersiveengineering.data.models.IEOBJBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class IPItemModels extends ModelProvider<TRSRModelBuilder>{
	public IPItemModels(DataGenerator gen, ExistingFileHelper exHelper){
		super(gen.getPackOutput(), ImmersivePetroleum.MODID, ITEM_FOLDER, TRSRModelBuilder::new, exHelper);
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
		
		genericItem(IPContent.Items.BITUMEN);
		genericItem(IPContent.Items.PETCOKE);
		genericItem(IPContent.Items.PETCOKEDUST);
		genericItem(IPContent.Items.OIL_CAN);
		genericItem(IPContent.Items.SPEEDBOAT);
		genericItem(IPContent.Items.PARAFFIN_WAX);
		
		genericItem(IPContent.BoatUpgrades.ICE_BREAKER);
		genericItem(IPContent.BoatUpgrades.REINFORCED_HULL);
		genericItem(IPContent.BoatUpgrades.PADDLES);
		genericItem(IPContent.BoatUpgrades.RUDDERS);
		genericItem(IPContent.BoatUpgrades.TANK);
		
		genericItem(IPContent.Items.SURVEYRESULT);
		
		genericItem(IPContent.Items.GASOLINE_BOTTLE);
		genericItem(IPContent.Items.MOLOTOV);
		genericItem(IPContent.Items.MOLOTOV_LIT);
		
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
			createBucket(f.source().get());
	}
	
	private void flarestackItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.FLARESTACK.get(), "block/obj/flarestack.obj")
				.texture("texture", modLoc("block/obj/flarestack"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0.25F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0.25F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, -3, 0), new Vector3f(30, 225, 0), 0.4F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -4, 0), null, 0.5F);
	}
	
	private void surveyToolItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.SEISMIC_SURVEY.get(), "block/obj/seismic_survey_tool.obj")
				.texture("texture", modLoc("block/obj/seismic_survey_tool"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, null, null, 0.2F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, null, 0.2F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(0, -2, 0), null, 0.2F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, -2, 0), null, 0.2F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, -4, 0), new Vector3f(30, 225, 0), 0.3F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, 3, 0), null, 0.2F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -4, 0), null, 0.3F);
	}
	
	private void generatorItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.GAS_GENERATOR.get(), "block/obj/generator.obj")
				.texture("texture", modLoc("block/obj/generator"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 2.0F, 0), new Vector3f(0, 225, 0), 0.4F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(0, 2.0F, 0), new Vector3f(0, 45, 0), 0.4F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.375F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.375F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 13, 0), null, 0.8F);
		doTransform(trans, ItemDisplayContext.GUI, null, new Vector3f(30, 225, 0), 0.625F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, ItemDisplayContext.FIXED, null, null, 0.5F);
	}
	
	private void autolubeItem(){
		TRSRModelBuilder model = obj(IPContent.Blocks.AUTO_LUBRICATOR.get(), "block/obj/autolubricator.obj")
			.texture("texture", modLoc("models/lubricator"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(0, 2, 0), new Vector3f(0, 45, 0), 0.25F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0.25F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0.25F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 12, 0), null, 0.75F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, -3, 0), new Vector3f(30, 225, 0), 0.4F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, 3, 0), null, 0.25F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -4, 0), null, 0.5F);
	}
	
	private void projectorItem(){
		TRSRModelBuilder model = objIELoader(IPContent.Items.PROJECTOR.get(), "item/obj/projector.obj")
				.callback(DefaultCallback.INSTANCE)
				.end()
				.texture("texture", modLoc("projectors/projector"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(0, 4, -2), null, 0.75F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(12, 4, -2), null, 0.75F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(-6, -4, 4.225F), new Vector3f(90, 0, 0), 0.75F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(6, -4, 4.225F), new Vector3f(90, 0, 0), 0.75F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(8, 18.25F, 8), null, 1.0F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, 12, 0), new Vector3f(30, 135, 0), 1.0F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(4, 8, 4), null, 0.5F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(-6, 6, 5), new Vector3f(0, -90, 0), 0.75F);
	}
	
	private void distillationtowerItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.DISTILLATIONTOWER.block().get(), "multiblock/obj/distillationtower.obj")
			.texture("texture", modLoc("multiblock/distillation_tower"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, null, null, 0.03125F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, null, 0.03125F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, -5, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, -5, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(1.5F, 8, 1.5F), null, 0.2F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(-1, -6, 0), new Vector3f(30, 225, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(1, 0, 1), null, 0.0625F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void pumpjackItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.PUMPJACK.block().get(), "item/obj/pumpjack_itemmockup.obj")
			.texture("texture_base", modLoc("multiblock/pumpjack_base"))
			.texture("texture_armature", modLoc("models/pumpjack_armature"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(-1.75F, 2.5F, 1.25F), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(-1.75F, 2.5F, 1.75F), new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, 0, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, 0, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 8, -8), null, 0.2F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(6, -6, 0), new Vector3f(30, 225, 0), 0.1875F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(-1.5F, 3, -1.5F), null, 0.0625F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(-1, -8, -2), null, 0.0625F);
	}
	
	private void cokerunitItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.COKERUNIT.block().get(), "multiblock/obj/cokerunit.obj")
				.texture("texture", modLoc("multiblock/cokerunit"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, null, new Vector3f(0, 225, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, new Vector3f(0, 45, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.03125F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 12, 0), null, 0.125F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, -4, 0), new Vector3f(30, 225, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, -8, 0), null, 0.03125F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void hydrotreaterItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.HYDROTREATER.block().get(), "multiblock/obj/hydrotreater.obj")
				.texture("texture", modLoc("multiblock/hydrotreater"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, null, new Vector3f(0, 225, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, new Vector3f(0, 45, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 225, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(0, 2.5F, 0), new Vector3f(75, 45, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 8, 0), null, 0.25F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(-1, -1, 0), new Vector3f(30, 225, 0), 0.15625F);
		doTransform(trans, ItemDisplayContext.GROUND, null, null, 0.125F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -1, 0), null, 0.125F);
	}
	
	private void derrickItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.DERRICK.block().get(), "multiblock/obj/derrick.obj")
				.texture("texture", modLoc("multiblock/derrick"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 7.6F, 0), new Vector3f(0, 180, 0), 0.15625F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, -2, 0), new Vector3f(30, 45, 0), 0.0625F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, 1, 0), null, 0.0625F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -8, 0), null, 0.0625F);
	}
	
	private void oiltankItem(){
		TRSRModelBuilder model = obj(IPContent.Multiblock.OILTANK.block().get(), "multiblock/obj/oiltank.obj")
				.texture("texture", modLoc("multiblock/oiltank"));
		
		ModelBuilder<?>.TransformsBuilder trans = model.transforms();
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0.0625F);
		doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 7.6F, 0), new Vector3f(0, 180, 0), 0.15625F);
		doTransform(trans, ItemDisplayContext.GUI, new Vector3f(0, -2, 0), new Vector3f(30, 45, 0), 0.125F);
		doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(0, 1, 0), null, 0.0625F);
		doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(0, -1, 0), new Vector3f(0, 180, 0), 0.0625F);
	}
	
	private void doTransform(ModelBuilder<?>.TransformsBuilder transform, ItemDisplayContext type, @Nullable Vector3f translation, @Nullable Vector3f rotationAngle, float scale){
		ModelBuilder<?>.TransformsBuilder.TransformVecBuilder trans = transform.transform(type);
		if(translation != null)
			trans.translation(translation.x(), translation.y(), translation.z());
		if(rotationAngle != null)
			trans.rotation(rotationAngle.x(), rotationAngle.y(), rotationAngle.z());
		trans.scale(scale);
		trans.end();
	}
	
	private TRSRModelBuilder obj(ItemLike item, String model){
		return getBuilder(RegistryUtils.getRegistryNameOf(item.asItem()).toString())
				.customLoader(ObjModelBuilder::begin)
				.modelLocation(modLoc("models/" + model)).flipV(true).end();
	}
	
	private IEOBJBuilder<TRSRModelBuilder> objIELoader(ItemLike item, String model){
		return getBuilder(RegistryUtils.getRegistryNameOf(item.asItem()).toString())
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(modLoc("models/" + model));
	}
	
	private <I extends Item> void genericItem(RegistryObject<I> regObject){
		genericItem(regObject.get());
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
		withExistingParent(RegistryUtils.getRegistryNameOf(f.getBucket().asItem()).getPath(), ResourceUtils.forge("item/bucket"))
			.customLoader(DynamicFluidContainerModelBuilder::begin)
			.fluid(f);
	}
	
	private String name(ItemLike item){
		return RegistryUtils.getRegistryNameOf(item.asItem()).getPath();
	}
}
