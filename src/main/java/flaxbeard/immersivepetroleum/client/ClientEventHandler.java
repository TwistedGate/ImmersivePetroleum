package flaxbeard.immersivepetroleum.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.ItemOverlayUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.wooden.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

public class ClientEventHandler{
	
	@SubscribeEvent
	public void renderLevelStage(RenderLevelStageEvent event){
		if(event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS){
			renderAutoLubricatorGhost(event);
		}
	}
	
	private void renderAutoLubricatorGhost(RenderLevelStageEvent event){
		PoseStack matrix = event.getPoseStack();
		Minecraft mc = Minecraft.getInstance();
		
		matrix.pushPose();
		{
			if(mc.player != null){
				ItemStack mainItem = mc.player.getMainHandItem();
				ItemStack secondItem = mc.player.getOffhandItem();
				
				boolean main = (!mainItem.isEmpty()) && mainItem.getItem() == IPContent.Blocks.AUTO_LUBRICATOR.get().asItem();
				boolean off = (!secondItem.isEmpty()) && secondItem.getItem() == IPContent.Blocks.AUTO_LUBRICATOR.get().asItem();
				
				if(main || off){
					BlockRenderDispatcher blockDispatcher = Minecraft.getInstance().getBlockRenderer();
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					
					// Anti-Jiggle when moving
					Vec3 renderView = MCUtil.getGameRenderer().getMainCamera().getPosition();
					matrix.translate(-renderView.x, -renderView.y, -renderView.z);
					
					BlockPos base = mc.player.blockPosition();
					for(int x = -16;x <= 16;x++){
						for(int z = -16;z <= 16;z++){
							for(int y = -16;y <= 16;y++){
								BlockPos pos = base.offset(x, y, z);
								BlockEntity te = mc.player.level.getBlockEntity(pos);
								
								if(te != null){
									ILubricationHandler<BlockEntity> handler = LubricatedHandler.getHandlerForTile(te);
									if(handler != null){
										Tuple<BlockPos, Direction> target = handler.getGhostBlockPosition(mc.player.level, te);
										if(target != null){
											BlockPos targetPos = target.getA();
											Direction targetFacing = target.getB();
											BlockState targetState = mc.player.level.getBlockState(targetPos);
											BlockState targetStateUp = mc.player.level.getBlockState(targetPos.above());
											if(targetState.getMaterial().isReplaceable() && targetStateUp.getMaterial().isReplaceable()){
												VertexConsumer vBuilder = buffer.getBuffer(RenderType.translucent());
												matrix.pushPose();
												{
													matrix.translate(targetPos.getX(), targetPos.getY() - 1, targetPos.getZ());
													
													BlockState state = IPContent.Blocks.AUTO_LUBRICATOR.get().defaultBlockState().setValue(AutoLubricatorBlock.FACING, targetFacing);
													BakedModel model = blockDispatcher.getBlockModel(state);
													blockDispatcher.getModelRenderer().renderModel(matrix.last(), vBuilder, null, model, 1.0F, 1.0F, 1.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
													
												}
												matrix.popPose();
												
												buffer.endBatch();
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		matrix.popPose();
	}
	
	@SubscribeEvent
	public void reservoirDebuggingOverlayText(RenderGameOverlayEvent.Post event){
		if(ReservoirHandler.getGenerator() == null){
			return;
		}
		
		Player player = MCUtil.getPlayer();
		
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		
		if((main != ItemStack.EMPTY && main.getItem() == IPContent.DEBUGITEM.get()) || (off != ItemStack.EMPTY && off.getItem() == IPContent.DEBUGITEM.get())){
			if(!((DebugItem.getMode(main) == DebugItem.Modes.SEEDBASED_RESERVOIR) || (DebugItem.getMode(off) == DebugItem.Modes.SEEDBASED_RESERVOIR))){
				return;
			}
			
			List<Component> debugOut = new ArrayList<>();
			
			if(!debugOut.isEmpty()){
				PoseStack matrix = event.getMatrixStack();
				matrix.pushPose();
				MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				for(int i = 0;i < debugOut.size();i++){
					int w = ClientUtils.font().width(debugOut.get(i).getString());
					int yOff = i * (ClientUtils.font().lineHeight + 2);
					
					matrix.pushPose();
					matrix.translate(0, 0, 1);
					GuiHelper.drawColouredRect(1, 1 + yOff, w + 1, 10, 0xAF_000000, buffer, matrix);
					buffer.endBatch();
					// Draw string without shadow
					ClientUtils.font().draw(matrix, debugOut.get(i), 2, 2 + yOff, -1);
					matrix.popPose();
				}
				matrix.popPose();
			}
		}
	}
	
	@SubscribeEvent
	public void renderInfoOverlays(RenderGameOverlayEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			Player player = MCUtil.getPlayer();
			
			if(MCUtil.getHitResult() != null){
				HitResult result = MCUtil.getHitResult();
				
				if(result.getType() == HitResult.Type.ENTITY){
					if(result instanceof EntityHitResult eHit){
						if(eHit.getEntity() instanceof MotorboatEntity motorboat){
							String[] text = motorboat.getOverlayText(player, result);
							
							if(text != null && text.length > 0){
								Font font = ClientUtils.font();
								int col = 0xffffff;
								for(int i = 0;i < text.length;i++){
									if(text[i] != null){
										int fx = event.getWindow().getGuiScaledWidth() / 2 + 8;
										int fy = event.getWindow().getGuiScaledHeight() / 2 + 8 + i * font.lineHeight;
										font.drawShadow(event.getMatrixStack(), text[i], fx, fy, col);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			Player player = MCUtil.getPlayer();
			PoseStack matrix = event.getMatrixStack();
			
			if(player.getVehicle() instanceof MotorboatEntity motorboat){
				int offset = 0;
				boolean holdingDebugItem = false;
				for(InteractionHand hand:InteractionHand.values()){
					if(!player.getItemInHand(hand).isEmpty()){
						ItemStack equipped = player.getItemInHand(hand);
						if((equipped.getItem() instanceof DrillItem) || (equipped.getItem() instanceof ChemthrowerItem) || (equipped.getItem() instanceof BuzzsawItem)){
							offset -= 85;
						}else if((equipped.getItem() instanceof RevolverItem) || (equipped.getItem() instanceof SpeedloaderItem)){
							offset -= 65;
						}else if(equipped.getItem() instanceof RailgunItem){
							offset -= 50;
						}else if(equipped.getItem() instanceof IEShieldItem){
							offset -= 40;
						}
						
						if(equipped.getItem() instanceof DebugItem){
							holdingDebugItem = true;
						}
					}
				}
				
				matrix.pushPose();
				{
					int scaledWidth = MCUtil.getWindow().getGuiScaledWidth();
					int scaledHeight = MCUtil.getWindow().getGuiScaledHeight();
					
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					VertexConsumer builder = ItemOverlayUtils.getHudElementsBuilder(buffer);
					
					int rightOffset = 0;
					if(MCUtil.getOptions().showSubtitles)
						rightOffset += 100;
					float dx = scaledWidth - rightOffset - 16;
					float dy = scaledHeight + offset;
					matrix.pushPose();
					{
						matrix.translate(dx, dy, 0);
						GuiHelper.drawTexturedRect(builder, matrix, -24, -68, 31, 62, 256f, 179, 210, 9, 71);
						
						matrix.translate(-23, -37, 0);
						float capacity = motorboat.getMaxFuel();
						if(capacity > 0){
							FluidStack fuel = motorboat.getContainedFluid();
							int amount = fuel.getAmount();
							float angle = 83 - (166 * amount / capacity);
							matrix.pushPose();
							matrix.mulPose(new Quaternion(0, 0, angle, true));
							GuiHelper.drawTexturedRect(builder, matrix, 6, -2, 24, 4, 256f, 91, 123, 80, 87);
							matrix.popPose();
							matrix.translate(23, 37, 0);
							
							GuiHelper.drawTexturedRect(builder, matrix, -41, -73, 53, 72, 256f, 8, 61, 4, 76);
						}
					}
					matrix.popPose();
					
					buffer.endBatch();
					
					if(holdingDebugItem && MCUtil.getFont() != null){
						matrix.pushPose();
						{
							Font font = MCUtil.getFont();
							
							int capacity = motorboat.getMaxFuel();
							FluidStack fs = motorboat.getContainedFluid();
							int amount = (fs == FluidStack.EMPTY || fs.getFluid() == null) ? 0 : fs.getAmount();
							
							Vec3 vec = motorboat.getDeltaMovement();
							float speed = (float) Math.sqrt(vec.x * vec.x + vec.z * vec.z);
							
							String[] array = {
									String.format(Locale.US, "Fuel: %05d/%d mB (%s)", amount, capacity, fs.getDisplayName().getString()),
									String.format(Locale.US, "Speed: %.3f", speed),
									String.format(Locale.US, "PropXRot: %07.3f° (%.3frad)", motorboat.propellerXRot, motorboat.propellerXRot * Mth.DEG_TO_RAD),
									String.format(Locale.US, "PropSpeed: %06.3f°", motorboat.propellerXRotSpeed),
							};
							int w = 3, h = 3;
							for(int i = 0;i < array.length;i++){
								font.drawShadow(matrix, array[i], w, h + (9 * i), -1);
							}
						}
						matrix.popPose();
					}
				}
				matrix.popPose();
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(RenderBlockOverlayEvent event){
		Player entity = event.getPlayer();
		if(event.getOverlayType() == OverlayType.FIRE && entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity boat){
			if(boat.isFireproof){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleFireRender(RenderPlayerEvent.Pre event){
		Player entity = event.getPlayer();
		if(entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity boat){
			if(boat.isFireproof){
				entity.clearFire();
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesClient(ClientTickEvent event){
		if(event.phase == Phase.END && MCUtil.getLevel() != null){
			CommonEventHandler.handleLubricatingMachines(MCUtil.getLevel());
		}
	}
}
