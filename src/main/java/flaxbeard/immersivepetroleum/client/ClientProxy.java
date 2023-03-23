package flaxbeard.immersivepetroleum.client;

import java.util.ArrayList;
import java.util.List;

import com.electronwill.nightconfig.core.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.lib.manual.ManualElementItem;
import blusunrize.lib.manual.ManualElementTable;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualEntry.EntryData;
import blusunrize.lib.manual.ManualEntry.SpecialElementData;
import blusunrize.lib.manual.ManualInstance;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.client.gui.CokerUnitScreen;
import flaxbeard.immersivepetroleum.client.gui.DerrickScreen;
import flaxbeard.immersivepetroleum.client.gui.DistillationTowerScreen;
import flaxbeard.immersivepetroleum.client.gui.HydrotreaterScreen;
import flaxbeard.immersivepetroleum.client.particle.FluidParticleData;
import flaxbeard.immersivepetroleum.client.render.SeismicResultRenderer;
import flaxbeard.immersivepetroleum.client.render.debugging.DebugRenderHandler;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.RecipeReloadListener;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem.ClientInputHandler;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ClientProxy extends CommonProxy{
	
	@Override
	public void setup(){
	}
	
	@Override
	public void registerContainersAndScreens(){
		MenuScreens.register(IPMenuTypes.DISTILLATION_TOWER.getType(), DistillationTowerScreen::new);
		MenuScreens.register(IPMenuTypes.COKER.getType(), CokerUnitScreen::new);
		MenuScreens.register(IPMenuTypes.DERRICK.getType(), DerrickScreen::new);
		MenuScreens.register(IPMenuTypes.HYDROTREATER.getType(), HydrotreaterScreen::new);
	}
	
	@Override
	public void completed(ParallelDispatchEvent event){
		event.enqueueWork(() -> ManualHelper.addConfigGetter(str -> switch(str){
			case "distillationtower_operationcost" -> (int) (1024 * IPServerConfig.REFINING.distillationTower_energyModifier.get());
			case "coker_operationcost" -> (int) (1024 * IPServerConfig.REFINING.cokerUnit_energyModifier.get());
			case "hydrotreater_operationcost_lower" -> (int) (80 * IPServerConfig.REFINING.hydrotreater_energyModifier.get());
			case "hydrotreater_operationcost_upper" -> (int) (512 * IPServerConfig.REFINING.hydrotreater_energyModifier.get());
			case "pumpjack_consumption" -> IPServerConfig.EXTRACTION.pumpjack_consumption.get();
			case "pumpjack_speed" -> IPServerConfig.EXTRACTION.pumpjack_speed.get();
			case "pumpjack_days" -> {
				int oil_min = 1000000;
				int oil_max = 5000000;
				for(ReservoirType reservoir:ReservoirType.map.values()){
					if(reservoir.name.equals("oil")){
						oil_min = reservoir.minSize;
						oil_max = reservoir.maxSize;
						break;
					}
				}
				
				float averageSize = (oil_min + oil_max) / 2F;
				float pumpspeed = IPServerConfig.EXTRACTION.pumpjack_speed.get();
				yield Mth.floor((averageSize / pumpspeed) / 24000F);
			}
			case "autolubricant_speedup" -> 1.25D;
			case "portablegenerator_flux" -> FuelHandler.getFluxGeneratedPerTick(IPContent.Fluids.GASOLINE.still().get());
			default -> {
				// Last resort
				Config cfg = IPServerConfig.getRawConfig();
				if(cfg.contains(str)){
					yield cfg.get(str);
				}
				yield null;
			}
		}));
		
		setupManualPages();
	}
	
	@Override
	public void preInit(){
	}
	
	@Override
	public void preInitEnd(){
	}
	
	@Override
	public void init(){
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		MinecraftForge.EVENT_BUS.register(new RecipeReloadListener(null));
		
		MinecraftForge.EVENT_BUS.register(new DebugRenderHandler());
		MinecraftForge.EVENT_BUS.register(new SeismicResultRenderer());
		
		ProjectorItem.ClientInputHandler.keybind_preview_flip.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(ClientInputHandler.keybind_preview_flip);
	}
	
	@Override
	public void renderTile(BlockEntity te, VertexConsumer iVertexBuilder, PoseStack transform, MultiBufferSource buffer){
		BlockEntityRenderer<BlockEntity> tesr = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te);
		
		// Crash prevention
		if(tesr == null)
			return;
		
		if(te instanceof PumpjackTileEntity pumpjack){
			transform.pushPose();
			transform.mulPose(new Quaternion(0, -90, 0, true));
			transform.translate(1, 1, -2);
			
			float pt = 0;
			if(MCUtil.getPlayer() != null){
				pumpjack.activeTicks = MCUtil.getPlayer().tickCount;
				pt = Minecraft.getInstance().getFrameTime();
			}
			
			tesr.render(pumpjack, pt, transform, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
			transform.popPose();
		}else{
			transform.pushPose();
			transform.mulPose(new Quaternion(0, -90, 0, true));
			transform.translate(0, 1, -4);
			
			tesr.render(te, 0, transform, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
			transform.popPose();
		}
	}
	
	@Override
	public void drawUpperHalfSlab(PoseStack transform, ItemStack stack){
		
		// Render slabs on top half
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockState state = IEBlocks.MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD).defaultBlockState();
		BakedModel model = blockRenderer.getBlockModelShaper().getBlockModel(state);
		
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		
		transform.pushPose();
		transform.translate(0.0F, 0.5F, 1.0F);
		blockRenderer.getModelRenderer().renderModel(transform.last(), buffers.getBuffer(RenderType.solid()), state, model, 1.0F, 1.0F, 1.0F, -1, -1, EmptyModelData.INSTANCE);
		transform.popPose();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void spawnSpillParticles(Level world, BlockPos pos, Fluid fluid, int particles, float yOffset, float flow){
		if(fluid == null || fluid == Fluids.EMPTY){
			return;
		}
		
		for(int i = 0;i < particles;i++){
			float xa = (world.random.nextFloat() - .5F) / 2F;
			float ya = 0.25F + (0.5F + (world.random.nextFloat() * 0.25F)) * flow / 800;
			float za = (world.random.nextFloat() - .5F) / 2F;
			
			float rx = (world.random.nextFloat() - .5F) * 0.5F;
			float rz = (world.random.nextFloat() - .5F) * 0.5F;
			
			double x = (pos.getX() + 0.5) + rx;
			double y = (pos.getY() + yOffset);
			double z = (pos.getZ() + 0.5) + rz;
			
			world.addParticle(new FluidParticleData(fluid), x, y, z, xa, ya, za);
		}
	}
	
	@Override
	public Level getClientWorld(){
		return MCUtil.getLevel();
	}
	
	@Override
	public Player getClientPlayer(){
		return MCUtil.getPlayer();
	}
	
	@Override
	public void handleEntitySound(SoundEvent soundEvent, Entity entity, boolean active, float volume, float pitch){
		// TODO Sound: Restore motorboat audio
	}
	
	@Override
	public void handleTileSound(SoundEvent soundEvent, BlockEntity te, boolean active, float volume, float pitch){
		// TODO Sound: Perhaps give some MBs some audio
	}

	public void setupManualPages(){
		handleReservoirManual(ResourceUtils.ip("reservoir"), 0);
		flarestack(ResourceUtils.ip("flarestack"), 12);
	}
	
	@SuppressWarnings({"deprecation"})
	private static void flarestack(ResourceLocation location, int priority){
		ManualInstance man = ManualHelper.getManual();
		
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(man);
		builder.readFromFile(location);
		builder.appendText(() -> {
			List<Component[]> list = new ArrayList<>();
			for(TagKey<Fluid> tag:FlarestackHandler.getSet()){
				for(Fluid fluid:ForgeRegistries.FLUIDS.getValues()){
					if(fluid.is(tag)){
						Component[] entry = new Component[]{TextComponent.EMPTY, new FluidStack(fluid, 1).getDisplayName()};
						list.add(entry);
					}
				}
			}
			
			StringBuilder additionalText = new StringBuilder();
			List<SpecialElementData> newElements = new ArrayList<>();
			int nextLine = 0;
			for(int page = 0;nextLine < list.size();++page){
				final int linesOnPage = page == 0 ? 12 : 14;
				final int endIndex = Math.min(nextLine + linesOnPage, list.size());
				List<Component[]> onPage = list.subList(nextLine, endIndex);
				nextLine = endIndex;
				final String key = "flarestack_table" + page;
				additionalText.append("<&").append(key).append(">");
				newElements.add(new SpecialElementData(key, 0, new ManualElementTable(man, onPage.toArray(Component[][]::new), false)));
			}
			return Pair.of(additionalText.toString(), newElements);
		});
		
		man.addEntry(man.getRoot().getOrCreateSubnode(ResourceUtils.ip("petroleum")), builder.create(), priority);
	}
	

	
	private static void handleReservoirManual(ResourceLocation location, int priority){
		ManualInstance man = ManualHelper.getManual();
		
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(man);
		builder.setContent(ClientProxy::createContent);
		builder.setLocation(location);
		man.addEntry(man.getRoot().addNewSubnode(ResourceUtils.ip("petroleum"), 100), builder.create(), priority);
	}
	
	protected static EntryData createContent(){
		ManualInstance man = ManualHelper.getManual();
		ArrayList<SpecialElementData> itemList = new ArrayList<>();
		final ReservoirType[] reservoirs = ReservoirType.map.values().toArray(new ReservoirType[0]);
		
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append(I18n.get("ie.manual.entry.reservoirs.oil0"));
		contentBuilder.append(I18n.get("ie.manual.entry.reservoirs.oil1"));
		
		for(int i = 0;i < reservoirs.length;i++){
			ReservoirType reservoir = reservoirs[i];
			
			ImmersivePetroleum.log.debug("Creating entry for " + reservoir);
			
			String name = "desc.immersivepetroleum.info.reservoir." + reservoir.name;
			String localizedName = I18n.get(name);
			if(localizedName.equalsIgnoreCase(name))
				localizedName = reservoir.name;
			
			char c = localizedName.toLowerCase().charAt(0);
			boolean isVowel = (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u');
			String aOrAn = I18n.get(isVowel ? "ie.manual.entry.reservoirs.vowel" : "ie.manual.entry.reservoirs.consonant");
			
			String dimBWList = "", bioBWList = "";
			
			if(reservoir.getDimensions().hasEntries()){
				StringBuilder strBuilder = new StringBuilder();
				
				reservoir.getDimensions().forEach(rl -> {
					strBuilder.append((strBuilder.length() > 0) ? ", " : "").append("<dim;").append(rl).append(">");
				});
				
				if(reservoir.getDimensions().isBlacklist()){
					dimBWList = I18n.get("ie.manual.entry.reservoirs.dim.invalid", localizedName, strBuilder.toString(), aOrAn);
				}else{
					dimBWList = I18n.get("ie.manual.entry.reservoirs.dim.valid", localizedName, strBuilder.toString(), aOrAn);
				}
			}else{
				dimBWList = I18n.get("ie.manual.entry.reservoirs.dim.any", localizedName, aOrAn);
			}
			
			if(reservoir.getBiomes().hasEntries()){
				StringBuilder strBuilder = new StringBuilder();
				
				reservoir.getBiomes().forEach(rl -> {
					Biome bio = ForgeRegistries.BIOMES.getValue(rl);
					strBuilder.append((strBuilder.length() > 0) ? ", " : "").append(bio != null ? bio.toString() : rl);
				});
				
				if(reservoir.getBiomes().isBlacklist()){
					bioBWList = I18n.get("ie.manual.entry.reservoirs.bio.invalid", strBuilder.toString());
				}else{
					bioBWList = I18n.get("ie.manual.entry.reservoirs.bio.valid", strBuilder.toString());
				}
			}else{
				bioBWList = I18n.get("ie.manual.entry.reservoirs.bio.any");
			}
			
			String fluidName = "";
			Fluid fluid = reservoir.getFluid();
			if(fluid != null){
				fluidName = new FluidStack(fluid, 1).getDisplayName().getString();
			}
			
			String repRate = "";
			if(reservoir.residual > 0){
				if (reservoir.equilibrium > 0)
					repRate = I18n.get("ie.manual.entry.reservoirs.replenish", reservoir.residual, fluidName, Utils.fDecimal(reservoir.equilibrium/1000));
				else
				    repRate = I18n.get("ie.manual.entry.reservoirs.replenish_depleted", reservoir.residual, fluidName);
			}
			contentBuilder.append("<&").append(reservoir.getId().toString()).append(">");
			contentBuilder.append(I18n.get("ie.manual.entry.reservoirs.content", dimBWList, fluidName, Utils.fDecimal(reservoir.minSize/1000), Utils.fDecimal(reservoir.maxSize/1000), repRate, bioBWList));
			
			if(i < (reservoirs.length - 1))
				contentBuilder.append("<np>");
			
			itemList.add(new SpecialElementData(reservoir.getId().toString(), 0, new ManualElementItem(man, new ItemStack(fluid.getBucket()))));
		}
		
		String translatedTitle = I18n.get("ie.manual.entry.reservoirs.title");
		String tanslatedSubtext = I18n.get("ie.manual.entry.reservoirs.subtitle");
		String formattedContent = contentBuilder.toString().replaceAll("\r\n|\r|\n", "\n");
		return new EntryData(translatedTitle, tanslatedSubtext, formattedContent, itemList);
	}
}
