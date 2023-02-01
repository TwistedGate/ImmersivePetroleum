package flaxbeard.immersivepetroleum.common.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.event.ProjectorEvent;
import flaxbeard.immersivepetroleum.client.IPShaders;
import flaxbeard.immersivepetroleum.client.gui.ProjectorScreen;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Items;
import flaxbeard.immersivepetroleum.common.util.IPItemStackHandler;
import flaxbeard.immersivepetroleum.common.util.projector.MultiblockProjection;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import flaxbeard.immersivepetroleum.common.util.projector.Settings.Mode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

public class ProjectorItem extends IPItemBase implements IUpgradeableTool{
	public ProjectorItem(){
		super(new Item.Properties().stacksTo(1).tab(ImmersivePetroleum.creativeTab));
	}
	
	@Override
	@Nonnull
	public Component getName(@Nonnull ItemStack stack){
		String selfKey = getDescriptionId(stack);
		if(stack.hasTag()){
			Settings settings = getSettings(stack);
			if(settings.getMultiblock() != null){
				Component name = settings.getMultiblock().getDisplayName(); //new TranslatableComponent("desc.immersiveengineering.info.multiblock.IE:" + getActualMBName(settings.getMultiblock()));
				
				return new TranslatableComponent(selfKey + ".specific", name).withStyle(ChatFormatting.GOLD);
			}
		}
		return new TranslatableComponent(selfKey).withStyle(ChatFormatting.GOLD);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@Nonnull ItemStack stack, Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn){
		Settings settings = getSettings(stack);
		if(settings.getMultiblock() != null){
			Vec3i size = settings.getMultiblock().getSize(worldIn);
			
			tooltip.add(new TranslatableComponent("desc.immersivepetroleum.info.projector.build0"));
			tooltip.add(new TranslatableComponent("desc.immersivepetroleum.info.projector.build1", settings.getMultiblock().getDisplayName()));
			
			if(isPressing(GLFW.GLFW_KEY_LEFT_SHIFT) || isPressing(GLFW.GLFW_KEY_RIGHT_SHIFT)){
				Component title = new TranslatableComponent("desc.immersivepetroleum.info.projector.holdshift.text").withStyle(ChatFormatting.DARK_AQUA);
				tooltip.add(title);
				
				Component mbSize = new TranslatableComponent("desc.immersivepetroleum.info.projector.size", size.getX(), size.getY(), size.getZ()).withStyle(ChatFormatting.DARK_GRAY);
				tooltip.add(indent(mbSize));
				
				Direction dir = Direction.from2DDataValue(settings.getRotation().ordinal());
				Component rotation = new TranslatableComponent("desc.immersivepetroleum.info.projector.rotated." + dir).withStyle(ChatFormatting.DARK_GRAY);
				
				Component flip;
				if(settings.isMirrored()){
					flip = new TranslatableComponent("desc.immersivepetroleum.info.projector.flipped.true").withStyle(ChatFormatting.DARK_GRAY);
				}else{
					flip = new TranslatableComponent("desc.immersivepetroleum.info.projector.flipped.false").withStyle(ChatFormatting.DARK_GRAY);
				}
				
				if(settings.getPos() != null){
					int x = settings.getPos().getX();
					int y = settings.getPos().getY();
					int z = settings.getPos().getZ();
					
					Component centerText = new TranslatableComponent("desc.immersivepetroleum.info.projector.center", x, y, z).withStyle(ChatFormatting.DARK_GRAY);
					tooltip.add(indent(centerText));
				}
				
				tooltip.add(indent(rotation));
				tooltip.add(indent(flip));
			}else{
				Component text = new TextComponent("[")
						.append(new TranslatableComponent("desc.immersivepetroleum.info.projector.holdshift"))
						.append("] ")
						.append(new TranslatableComponent("desc.immersivepetroleum.info.projector.holdshift.text"))
						.withStyle(ChatFormatting.DARK_AQUA);
				tooltip.add(text);
			}
			
			if(isPressing(GLFW.GLFW_KEY_LEFT_CONTROL) || isPressing(GLFW.GLFW_KEY_RIGHT_CONTROL)){
				Component title = new TranslatableComponent("desc.immersivepetroleum.info.projector.holdctrl.text").withStyle(ChatFormatting.DARK_PURPLE);
				Component ctrl0 = new TranslatableComponent("desc.immersivepetroleum.info.projector.control1").withStyle(ChatFormatting.DARK_GRAY);
				Component ctrl1 = new TranslatableComponent("desc.immersivepetroleum.info.projector.control2", ClientInputHandler.keybind_preview_flip.getTranslatedKeyMessage()).withStyle(ChatFormatting.DARK_GRAY);
				Component ctrl2 = new TranslatableComponent("desc.immersivepetroleum.info.projector.control3").withStyle(ChatFormatting.DARK_GRAY);
				
				tooltip.add(title);
				tooltip.add(indent(ctrl0));
				tooltip.add(indent(ctrl1));
				tooltip.add(indent(ctrl2));
			}else{
				Component text = new TextComponent("[")
						.append(new TranslatableComponent("desc.immersivepetroleum.info.projector.holdctrl"))
						.append("] ")
						.append(new TranslatableComponent("desc.immersivepetroleum.info.projector.holdctrl.text"))
						.withStyle(ChatFormatting.DARK_PURPLE);
				tooltip.add(text);
			}
		}else{
			tooltip.add(new TranslatableComponent("desc.immersivepetroleum.info.projector.noMultiblock"));
		}
	}
	
	private Component indent(Component text){
		return new TextComponent("  ").append(text);
	}
	
	/** Find the key that is being pressed while minecraft is in focus */
	@OnlyIn(Dist.CLIENT)
	private boolean isPressing(int key){
		long window = Minecraft.getInstance().getWindow().getWindow();
		return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
	}
	
	/** Name cache for {@link ProjectorItem#getActualMBName(IMultiblock)} */
	static final Map<Class<? extends IMultiblock>, String> nameCache = new HashMap<>();
	/** Gets the name of the class */
	public static String getActualMBName(IMultiblock multiblock){
		if(!nameCache.containsKey(multiblock.getClass())){
			String name = multiblock.getClass().getSimpleName();
			name = name.substring(0, name.indexOf("Multiblock"));
			
			name = switch(name){
				case "LightningRod" -> "Lightningrod";
				case "ImprovedBlastfurnace" -> "BlastFurnaceAdvanced";
				default -> name;
			};
			
			nameCache.put(multiblock.getClass(), name);
		}
		
		return nameCache.get(multiblock.getClass());
	}
	
	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items){
		if(this.allowdedIn(group)){
			items.add(new ItemStack(this, 1));
		}
	}
	
	@Override
	@Nonnull
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand){
		ItemStack held = player.getItemInHand(hand);
		
		if(world.isClientSide){
			boolean changeMode = false;
			Settings settings = getSettings(held);
			switch(settings.getMode()){
				case PROJECTION -> {
					if(player.isShiftKeyDown()){
						if(settings.getPos() != null){
							settings.setPos(null);
							settings.sendPacketToServer(hand);
						}else{
							changeMode = true;
						}
					}
				}
				case MULTIBLOCK_SELECTION -> {
					if(!player.isShiftKeyDown()){
						openGUI(hand, held);
					}else{
						changeMode = true;
					}
				}
				default -> {
				}
			}
			
			if(changeMode){
				int modeId = settings.getMode().ordinal() + 1;
				settings.setMode(Mode.values()[modeId >= Mode.values().length ? 0 : modeId]);
				settings.applyTo(held);
				settings.sendPacketToServer(hand);
				player.displayClientMessage(settings.getMode().getTranslated(), true);
			}
		}
		
		return InteractionResultHolder.success(held);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static void openGUI(InteractionHand hand, ItemStack held){
		Minecraft.getInstance().setScreen(new ProjectorScreen(hand, held));
	}
	
	@Override
	@Nonnull
	public InteractionResult useOn(UseOnContext context){
		Level world = context.getLevel();
		Player playerIn = context.getPlayer();
		InteractionHand hand = context.getHand();
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();
		
		ItemStack stack = playerIn.getItemInHand(hand);
		final Settings settings = ProjectorItem.getSettings(stack);
		if(playerIn.isShiftKeyDown() && settings.getPos() != null){
			if(world.isClientSide){
				settings.setPos(null);
				settings.applyTo(stack);
				settings.sendPacketToServer(hand);
			}
			
			return InteractionResult.SUCCESS;
		}
		
		if(settings.getMode() == Mode.PROJECTION && settings.getPos() == null && settings.getMultiblock() != null){
			BlockState state = world.getBlockState(pos);
			
			final MutableBlockPos hit = pos.mutable();
			if(!state.getMaterial().isReplaceable() && facing == Direction.UP){
				hit.setWithOffset(hit, 0, 1, 0);
			}
			
			Vec3i size = settings.getMultiblock().getSize(world);
			alignHit(hit, playerIn, size, settings.getRotation(), settings.isMirrored());
			
			if(playerIn.isShiftKeyDown() && playerIn.isCreative()){
				if(!world.isClientSide){
					if(settings.getMultiblock().getUniqueName().getPath().contains("excavator_demo") || settings.getMultiblock().getUniqueName().getPath().contains("bucket_wheel")){
						hit.setWithOffset(hit, 0, -2, 0);
					}
					
					// Creative Placement
					BiPredicate<Integer, MultiblockProjection.Info> pred = (layer, info) -> {
						BlockPos realPos = info.tPos.offset(hit);
						BlockState tstate0 = info.getModifiedState(world, realPos);
						
						ProjectorEvent.PlaceBlock event = new ProjectorEvent.PlaceBlock(info.multiblock, info.templateWorld, info.tBlockInfo.pos, world, realPos, tstate0, settings.getRotation());
						if(!MinecraftForge.EVENT_BUS.post(event)){
							BlockState tstate1 = event.getState();
							
							if(world.setBlockAndUpdate(realPos, tstate1)){
								ProjectorEvent.PlaceBlockPost postEvent = new ProjectorEvent.PlaceBlockPost(info.multiblock, info.templateWorld, event.getTemplatePos(), world, realPos, tstate1, settings.getRotation());
								MinecraftForge.EVENT_BUS.post(postEvent);
							}
						}
						
						return false; // Don't ever skip a step.
					};
					
					MultiblockProjection projection = new MultiblockProjection(world, settings.getMultiblock());
					projection.setFlip(settings.isMirrored());
					projection.setRotation(settings.getRotation());
					projection.processAll(pred);
				}
				
				return InteractionResult.SUCCESS;
				
			}else{
				if(world.isClientSide){
					settings.setPos(hit);
					settings.applyTo(stack);
					settings.sendPacketToServer(hand);
				}
				
				return InteractionResult.SUCCESS;
			}
		}
		
		return InteractionResult.PASS;
	}
	
	// STATIC METHODS
	
	public static Settings getSettings(@Nullable ItemStack stack){
		return new Settings(stack);
	}
	
	private static void alignHit(MutableBlockPos hit, Player playerIn, Vec3i size, Rotation rotation, boolean mirror){
		int x = ((rotation.ordinal() % 2 == 0) ? size.getX() : size.getZ()) / 2;
		int z = ((rotation.ordinal() % 2 == 0) ? size.getZ() : size.getX()) / 2;
		Direction facing = playerIn.getDirection();
		
		switch(facing){
			case NORTH -> hit.setWithOffset(hit, 0, 0, -z);
			case SOUTH -> hit.setWithOffset(hit, 0, 0, z);
			case EAST -> hit.setWithOffset(hit, x, 0, 0);
			case WEST -> hit.setWithOffset(hit, -x, 0, 0);
			default -> {
			}
		}
	}
	
	public static boolean hasKey(ItemStack stack, String key, int tagId){
		return stack.hasTag() && stack.getTag().contains(key, tagId);
	}
	
	// STATIC SUPPORT CLASSES
	
	/** Client Rendering Stuff */
	@OnlyIn(Dist.CLIENT)
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class ClientRenderHandler{
		@SubscribeEvent
		public static void renderLevelStage(RenderLevelStageEvent event){
			if(event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS){
				renderProjection(event);
			}
		}
		
		private static void renderProjection(RenderLevelStageEvent event){
			Minecraft mc = Minecraft.getInstance();
			
			if(mc.player != null){
				PoseStack matrix = event.getPoseStack();
				matrix.pushPose();
				{
					// Anti-Jiggle when moving
					Vec3 renderView = MCUtil.getGameRenderer().getMainCamera().getPosition();
					matrix.translate(-renderView.x, -renderView.y, -renderView.z);
					
					ItemStack secondItem = mc.player.getOffhandItem();
					boolean off = secondItem.is(Items.PROJECTOR.get()) && hasKey(secondItem, "settings", Tag.TAG_COMPOUND);
					
					for(int i = 0;i <= 10;i++){
						ItemStack stack = (i == 10 ? secondItem : mc.player.getInventory().getItem(i));
						if(stack.is(Items.PROJECTOR.get()) && hasKey(stack, "settings", Tag.TAG_COMPOUND)){
							Settings settings = getSettings(stack);
							matrix.pushPose();
							{
								boolean renderMoving = i == mc.player.getInventory().selected || (i == 10 && off);
								renderSchematic(matrix, settings, mc.player, mc.player.level, event.getPartialTick(), renderMoving);
							}
							matrix.popPose();
						}
					}
				}
				matrix.popPose();
			}
		}
		
		static final MutableBlockPos FULL_MAX = new MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		public static void renderSchematic(PoseStack matrix, Settings settings, Player player, Level world, float partialTicks, boolean renderMoving){
			if(settings.getMultiblock() == null)
				return;
			
			Vec3i size = settings.getMultiblock().getSize(world);
			final MutableBlockPos hit = new MutableBlockPos(FULL_MAX.getX(), FULL_MAX.getY(), FULL_MAX.getZ());
			final MutableBoolean isPlaced = new MutableBoolean(false);
			if(settings.getPos() != null){
				hit.set(settings.getPos());
				isPlaced.setTrue();
				
			}else if(renderMoving && MCUtil.getHitResult() != null && MCUtil.getHitResult().getType() == Type.BLOCK){
				BlockHitResult blockRTResult = (BlockHitResult) MCUtil.getHitResult();
				
				BlockPos pos = blockRTResult.getBlockPos();
				
				BlockState state = world.getBlockState(pos);
				if(state.getMaterial().isReplaceable() || blockRTResult.getDirection() != Direction.UP){
					hit.set(pos);
				}else{
					hit.setWithOffset(pos, 0, 1, 0);
				}
				
				alignHit(hit, player, size, settings.getRotation(), settings.isMirrored());
			}
			
			if(!hit.equals(FULL_MAX)){
				ResourceLocation name = settings.getMultiblock().getUniqueName();
				if(name.getPath().contains("excavator_demo") || name.getPath().contains("bucket_wheel")){
					hit.setWithOffset(hit, 0, -2, 0);
				}
				
				MultiblockProjection projection = new MultiblockProjection(world, settings.getMultiblock());
				projection.setRotation(settings.getRotation());
				projection.setFlip(settings.isMirrored());
				
				final List<Pair<RenderLayer, MultiblockProjection.Info>> toRender = new ArrayList<>();
				final MutableInt currentLayer = new MutableInt();
				final MutableInt badBlocks = new MutableInt();
				final MutableInt goodBlocks = new MutableInt();
				BiPredicate<Integer, MultiblockProjection.Info> bipred = (layer, info) -> {
					// Slice handling
					if(badBlocks.getValue() == 0 && layer > currentLayer.getValue()){
						currentLayer.setValue(layer);
					}else if(!Objects.equals(layer, currentLayer.getValue())){
						return true; // breaks the internal loop
					}
					
					if(isPlaced.booleanValue()){ // Render only slices when placed
						if(Objects.equals(layer, currentLayer.getValue())){
							BlockPos realPos = info.tPos.offset(hit);
							BlockState toCompare = world.getBlockState(realPos);
							BlockState tState = info.getModifiedState(world, realPos);
							
							boolean skip = false;
							if(tState == toCompare){
								toRender.add(Pair.of(RenderLayer.PERFECT, info));
								goodBlocks.increment();
								skip = true;
							}else{
								// Making it this far only needs an air check,
								// the other already proved to be false.
								if(!toCompare.isAir()){
									toRender.add(Pair.of(RenderLayer.BAD, info));
									skip = true;
								}else{
									badBlocks.increment();
								}
							}
							
							if(skip){
								return false;
							}
						}
					}
					
					toRender.add(Pair.of(RenderLayer.ALL, info));
					return false;
				};
				projection.processAll(bipred);
				
				boolean perfect = (goodBlocks.getValue() == projection.getBlockCount());
				
				MutableBlockPos min = new MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
				MutableBlockPos max = new MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
				float flicker = world.random.nextFloat() / 2F + 0.25F;
				//float flicker = (world.random.nextInt(10) == 0) ? 0.75F : (world.random.nextInt(20) == 0 ? 0.5F : 1F);
				matrix.translate(hit.getX(), hit.getY(), hit.getZ());
				
				toRender.sort((a, b) -> {
					int ao = a.getLeft().ordinal();
					int bo = b.getLeft().ordinal();
					if(ao > bo){
						return 1;
					}else if(ao < bo){
						return -1;
					}
					return 0;
				});
				
				MultiBufferSource.BufferSource mainBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				
				ItemStack heldStack = player.getMainHandItem();
				for(Pair<RenderLayer, MultiblockProjection.Info> pair:toRender){
					MultiblockProjection.Info rInfo = pair.getRight();
					
					switch(pair.getLeft()){
						case ALL -> { // All / Slice
							boolean held = heldStack.getItem() == rInfo.getRawState().getBlock().asItem();
							float alpha = held ? 1.0F : 0.50F;
							
							matrix.pushPose();
							{
								// This can NOT use mainBuffer, otherwise highlighting held blocks does not work at all.
								// While this may not be the most efficient thing to do, it's the one thing i have yet to find an alternative to
								// It has it's own Tesselator for that reason.
								renderPhantom(matrix, world, rInfo, settings.isMirrored(), flicker, alpha, partialTicks);
								
								if(held){
									renderCenteredOutlineBox(mainBuffer, matrix, 0xAFAFAF, flicker);
								}
							}
							matrix.popPose();
						}
						case BAD -> { // Bad block
							matrix.pushPose();
							{
								matrix.translate(rInfo.tPos.getX(), rInfo.tPos.getY(), rInfo.tPos.getZ());
								
								renderCenteredOutlineBox(mainBuffer, matrix, 0xFF0000, flicker);
							}
							matrix.popPose();
						}
						case PERFECT -> {
							int x = rInfo.tPos.getX();
							int y = rInfo.tPos.getY();
							int z = rInfo.tPos.getZ();
							
							min.set(Math.min(x, min.getX()), Math.min(y, min.getY()), Math.min(z, min.getZ()));
							max.set(Math.max(x, max.getX()), Math.max(y, max.getY()), Math.max(z, max.getZ()));
						}
					}
				}
				
				if(perfect){
					// Multiblock Correctly Built
					matrix.pushPose();
					{
						renderOutlineBox(mainBuffer, matrix, min, max, 0x00BF00, flicker);
					}
					matrix.popPose();
					
					// Debugging Stuff
					if(!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() == IPContent.DEBUGITEM.get()){
						matrix.pushPose();
						{
							// Min (Red)
							matrix.translate(min.getX(), min.getY(), min.getZ());
							renderCenteredOutlineBox(mainBuffer, matrix, 0xFF0000, flicker);
						}
						matrix.popPose();
						
						matrix.pushPose();
						{
							// Max (Green)
							matrix.translate(max.getX(), max.getY(), max.getZ());
							renderCenteredOutlineBox(mainBuffer, matrix, 0x00FF00, flicker);
						}
						matrix.popPose();
						
						matrix.pushPose();
						{
							// Center (Blue)
							BlockPos center = min.immutable().offset(max);
							matrix.translate(center.getX() / 2, center.getY() / 2, center.getZ() / 2);
							renderCenteredOutlineBox(mainBuffer, matrix, 0x0000FF, flicker);
						}
						matrix.popPose();
					}
				}
				
				mainBuffer.endBatch();
			}
		}
		
		private static final Tesselator PHANTOM_TESSELATOR = new Tesselator();
		private static void renderPhantom(PoseStack matrix, Level realWorld, MultiblockProjection.Info rInfo, boolean mirror, float flicker, float alpha, float partialTicks){
			BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
			ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
			BlockColors blockColors = Minecraft.getInstance().getBlockColors();
			
			// Centers the preview block
			matrix.translate(rInfo.tPos.getX(), rInfo.tPos.getY(), rInfo.tPos.getZ());
			
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(PHANTOM_TESSELATOR.getBuilder());
			
			BlockState state = rInfo.getModifiedState(realWorld, rInfo.tPos);
			
			ProjectorEvent.RenderBlock renderEvent = new ProjectorEvent.RenderBlock(rInfo.multiblock, rInfo.templateWorld, rInfo.tBlockInfo.pos, realWorld, rInfo.tPos, state, rInfo.settings.getRotation());
			if(!MinecraftForge.EVENT_BUS.post(renderEvent)){
				state = renderEvent.getState();
				state.updateNeighbourShapes(realWorld, rInfo.tPos, 3);
				
				IModelData modelData = EmptyModelData.INSTANCE;
				BlockEntity te = rInfo.templateWorld.getBlockEntity(rInfo.tBlockInfo.pos);
				if(te != null){
					te.blockState = state;
					modelData = te.getModelData();
				}
				
				RenderShape blockrendertype = state.getRenderShape();
				switch(blockrendertype){
					case MODEL -> {
						BakedModel ibakedmodel = dispatcher.getBlockModel(state);
						int i = blockColors.getColor(state, null, null, 0);
						float red = (i >> 16 & 0xFF) / 255F;
						float green = (i >> 8 & 0xFF) / 255F;
						float blue = (i & 0xFF) / 255F;
						
						modelData = ibakedmodel.getModelData(rInfo.templateWorld, rInfo.tBlockInfo.pos, state, modelData);
						
						IPShaders.projNoise(flicker * alpha, MCUtil.getPlayer().tickCount + partialTicks);
						
						VertexConsumer vc = buffer.getBuffer(IPRenderTypes.PROJECTION);
						//vc = buffer.getBuffer(RenderType.translucent());
						blockRenderer.renderModel(matrix.last(), vc, state, ibakedmodel, red, green, blue, 0xF000F0, OverlayTexture.NO_OVERLAY, modelData);
					}
					case ENTITYBLOCK_ANIMATED -> {
						ItemStack stack = new ItemStack(state.getBlock());
						
						MCUtil.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, matrix, buffer, 0);
					}
					default -> {}
				}
			}
			
			buffer.endBatch();
		}
		
		private static void renderOutlineBox(MultiBufferSource buffer, PoseStack matrix, Vec3i min, Vec3i max, int rgb, float flicker){
			renderBox(buffer, matrix, Vec3.atLowerCornerOf(min), Vec3.atLowerCornerOf(max).add(1, 1, 1), rgb, flicker);
		}
		
		private static void renderBox(MultiBufferSource buffer, PoseStack matrix, Vec3 min, Vec3 max, int rgb, float flicker){
			VertexConsumer builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINE);
			
			float alpha = 0.25F + (0.5F * flicker);
			
			int rgba = rgb | (((int) (alpha * 255)) << 24);
			
			line(builder, matrix, min, max, 0b010, 0b110, rgba);
			line(builder, matrix, min, max, 0b110, 0b111, rgba);
			line(builder, matrix, min, max, 0b111, 0b011, rgba);
			line(builder, matrix, min, max, 0b011, 0b010, rgba);
			
			line(builder, matrix, min, max, 0b010, 0b000, rgba);
			line(builder, matrix, min, max, 0b110, 0b100, rgba);
			line(builder, matrix, min, max, 0b011, 0b001, rgba);
			line(builder, matrix, min, max, 0b111, 0b101, rgba);
			
			line(builder, matrix, min, max, 0b000, 0b100, rgba);
			line(builder, matrix, min, max, 0b100, 0b101, rgba);
			line(builder, matrix, min, max, 0b101, 0b001, rgba);
			line(builder, matrix, min, max, 0b001, 0b000, rgba);
		}
		
		private static void renderCenteredOutlineBox(MultiBufferSource buffer, PoseStack matrix, int rgb, float flicker){
			renderBox(buffer, matrix, Vec3.ZERO, new Vec3(1, 1, 1), rgb, flicker);
		}
		
		private static Vector3f combine(Vec3 start, Vec3 end, int mixBits){
			final float eps = 0.01f;
			return new Vector3f(
					(float) ((mixBits & 4) != 0 ? end.x + eps : start.x - eps),
					(float) ((mixBits & 2) != 0 ? end.y + eps : start.y - eps),
					(float) ((mixBits & 1) != 0 ? end.z + eps : start.z - eps)
			);
		}
		
		private static void line(VertexConsumer out, PoseStack mat, Vec3 min, Vec3 max, int startBits, int endBits, int rgba){
			Vector3f start = combine(min, max, startBits);
			Vector3f end = combine(min, max, endBits);
			Vector3f delta = end.copy();
			delta.sub(start);
			out.vertex(mat.last().pose(), start.x(), start.y(), start.z())
					.color(rgba)
					.normal(mat.last().normal(), delta.x(), delta.y(), delta.z())
					.endVertex();
			out.vertex(mat.last().pose(), end.x(), end.y(), end.z())
					.color(rgba)
					.normal(mat.last().normal(), delta.x(), delta.y(), delta.z())
					.endVertex();
		}
	}
	
	/** Client Input Stuff */
	@OnlyIn(Dist.CLIENT)
	@Mod.EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
	public static class ClientInputHandler{
		public static final KeyMapping keybind_preview_flip = new KeyMapping("key.immersivepetroleum.projector.flip", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.immersivepetroleum");
		
		static boolean shiftHeld = false;
		
		@SubscribeEvent
		public static void onPlayerTick(TickEvent.PlayerTickEvent event){
			if(event.side == LogicalSide.CLIENT && event.player != null && event.player == Minecraft.getInstance().getCameraEntity()){
				if(event.phase == Phase.END){
					if(!ClientInputHandler.keybind_preview_flip.isUnbound() && ClientInputHandler.keybind_preview_flip.consumeClick()){
						doAFlip();
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void handleScroll(InputEvent.MouseScrollEvent event){
			double delta = event.getScrollDelta();
			
			if(shiftHeld && delta != 0.0){
				Player player = MCUtil.getPlayer();
				ItemStack mainItem = player.getMainHandItem();
				ItemStack secondItem = player.getOffhandItem();
				
				boolean main = mainItem.is(Items.PROJECTOR.get()) && hasKey(mainItem, "settings", Tag.TAG_COMPOUND);
				boolean off = secondItem.is(Items.PROJECTOR.get()) && hasKey(secondItem, "settings", Tag.TAG_COMPOUND);
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					if(shiftHeld){
						Settings settings = getSettings(target);
						
						if(delta > 0){
							settings.rotateCCW();
						}else{
							settings.rotateCW();
						}
						
						settings.applyTo(target);
						settings.sendPacketToServer(main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
						
						Direction facing = Direction.from2DDataValue(settings.getRotation().ordinal());
						player.displayClientMessage(new TranslatableComponent("desc.immersivepetroleum.info.projector.rotated." + facing), true);
						
						event.setCanceled(true);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void handleKey(InputEvent.KeyInputEvent event){
			if(event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT || event.getKey() == GLFW.GLFW_KEY_LEFT_SHIFT){
				switch(event.getAction()){
					case GLFW.GLFW_PRESS -> {
						shiftHeld = true;
					}
					case GLFW.GLFW_RELEASE -> {
						shiftHeld = false;
					}
				}
			}
		}
		
		private static void doAFlip(){
			Player player = MCUtil.getPlayer();
			ItemStack mainItem = player.getMainHandItem();
			ItemStack secondItem = player.getOffhandItem();
			
			boolean main = mainItem.is(Items.PROJECTOR.get()) && hasKey(mainItem, "settings", Tag.TAG_COMPOUND);
			boolean off = secondItem.is(Items.PROJECTOR.get()) && hasKey(mainItem, "settings", Tag.TAG_COMPOUND);
			ItemStack target = main ? mainItem : secondItem;
			
			if(main || off){
				Settings settings = ProjectorItem.getSettings(target);
				
				settings.flip();
				settings.applyTo(target);
				settings.sendPacketToServer(main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
				
				Component flip;
				if(settings.isMirrored()){
					flip = new TranslatableComponent("desc.immersivepetroleum.info.projector.flipped.true");
				}else{
					flip = new TranslatableComponent("desc.immersivepetroleum.info.projector.flipped.false");
				}
				player.displayClientMessage(flip, true);
			}
		}
	}
	
	public enum RenderLayer{
		ALL, BAD, PERFECT
	}

	@Override
	public CompoundTag getUpgrades(ItemStack stack){
		return stack.hasTag() ? stack.getOrCreateTag().getCompound("upgrades") : new CompoundTag();
	}

	@Override
	public void clearUpgrades(ItemStack stack){
		ItemUtils.removeTag(stack, "upgrades");
	}

	@Override
	public boolean canTakeFromWorkbench(ItemStack stack){
		return true;
	}

	@Override
	public boolean canModify(ItemStack stack){
		return true;
	}

	@Override
	public void recalculateUpgrades(ItemStack stack, Level w, Player player){
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack){
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack){
	}
	
	private static final Slot[] NONE = new Slot[0];
	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory){
		return NONE;
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt){
		if(!stack.isEmpty()){
			return new IPItemStackHandler(0){
				private final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(new ShaderWrapper_Item(getRegistryName(), stack));
				
				@Override
				public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing){
					if(capability == CapabilityShader.SHADER_CAPABILITY){
						return shaders.cast();
					}
					return super.getCapability(capability, facing);
				}
			};
		}
		return null;
	}
}
