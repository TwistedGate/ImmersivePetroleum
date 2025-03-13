package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.render.tile.IEBlockEntityRenderer;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.OilTankLogic;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class OilTankRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<OilTankLogic.State>> {
	@Override
	public boolean shouldRenderOffScreen(@Nonnull MultiblockBlockEntityMaster<OilTankLogic.State> te){
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MultiblockBlockEntityMaster<OilTankLogic.State> te, float partialTicks, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		if(te.isRemoved() || !te.getLevel().hasChunkAt(te.getBlockPos()))
			return;
		
		combinedOverlay = OverlayTexture.NO_OVERLAY;
		
		matrix.pushPose();
		{
			switch(te.getHelper().getContext().getLevel().getOrientation().front()){
				case EAST -> {
					matrix.mulPose(Axis.YP.rotationDegrees(270F));
					matrix.translate(0, 0, -1);
				}
				case SOUTH -> {
					matrix.mulPose(Axis.YP.rotationDegrees(180F));
					matrix.translate(-1, 0, -1);
				}
				case WEST -> {
					matrix.mulPose(Axis.YP.rotationDegrees(90F));
					matrix.translate(-1, 0, 0);
				}
				default -> {
				}
			}
			
			// Tank Display
			matrix.pushPose();
			{
				matrix.translate(1, 2, 2.995F);
				
				// Background
				Matrix4f mat = matrix.last().pose();
				VertexConsumer builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_POSITION_COLOR);
				builder.vertex(mat, 1.5F, -0.5F, 0.0F).color(34, 34, 34, 255).endVertex();
				builder.vertex(mat, 1.5F, 1F, 0.0F).color(34, 34, 34, 255).endVertex();
				builder.vertex(mat, 0F, 1F, 0.0F).color(34, 34, 34, 255).endVertex();
				builder.vertex(mat, 0F, -0.5F, 0.0F).color(34, 34, 34, 255).endVertex();
				
					FluidStack fs = te.getHelper().getState().tank.getFluid();
					if(!fs.isEmpty()){
						matrix.pushPose();
						{
							matrix.translate(0.25, 0.875, 0.0025F);
							matrix.scale(0.0625F, -0.0625F, 0.0625F);
							
							float h = fs.getAmount() / (float) te.getHelper().getState().tank.getCapacity();
							GuiHelper.drawRepeatedFluidSprite(buffer.getBuffer(RenderType.solid()), matrix, fs, 0, 0 + (1 - h) * 16, 16, h * 16);
						}
						matrix.popPose();
					}

			}
			matrix.popPose();
			
			matrix.pushPose();
			{
				// Dynamic Fluid IO Ports
				if(te.getHelper().getContext().getLevel().getOrientation().mirrored()){
						for(OilTankLogic.Port port:OilTankLogic.Port.DYNAMIC_PORTS){
							matrix.pushPose();
							{
								BlockPos p = port.posInMultiblock.posInMultiblock().subtract(te.getHelper().getPositionInMB());
								matrix.mulPose(Axis.YP.rotationDegrees(180F));
								matrix.translate(p.getX() - 1, p.getY(), -p.getZ() - 1);
								quad(matrix, buffer, te.getHelper().getState().getPortStateFor(port), port.posInMultiblock.posInMultiblock().getX() == 4, combinedLight, combinedOverlay);
							}
							matrix.popPose();
						}

				}else{
						for(OilTankLogic.Port port:OilTankLogic.Port.DYNAMIC_PORTS){
							matrix.pushPose();
							{
								BlockPos p = port.posInMultiblock.posInMultiblock().subtract(te.getHelper().getPositionInMB());
								matrix.translate(p.getX(), p.getY(), p.getZ());
								quad(matrix, buffer, te.getHelper().getState().getPortStateFor(port), port.posInMultiblock.posInMultiblock().getX() == 4, combinedLight, combinedOverlay);
							}
							matrix.popPose();
						}

				}
			}
			matrix.popPose();
		}
		matrix.popPose();
	}
	
	public void quad(PoseStack matrix, MultiBufferSource buffer, OilTankLogic.PortState portState, boolean flip, int combinedLight, int combinedOverlay){
		Matrix4f mat = matrix.last().pose();
		VertexConsumer builder = buffer.getBuffer(IPRenderTypes.OIL_TANK);
		
		boolean input = portState == OilTankLogic.PortState.INPUT;
		float u0 = input ? 0.0F : 0.1F, v0 = 0.5F;
		float u1 = u0 + 0.1F, v1 = v0 + 0.1F;
		if(flip){
			builder.vertex(mat, 1.001F, 0F, 0F).color(1F, 1F, 1F, 1F).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
			builder.vertex(mat, 1.001F, 1F, 0F).color(1F, 1F, 1F, 1F).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
			builder.vertex(mat, 1.001F, 1F, 1F).color(1F, 1F, 1F, 1F).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
			builder.vertex(mat, 1.001F, 0F, 1F).color(1F, 1F, 1F, 1F).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
		}else{
			builder.vertex(mat, -0.001F, 0F, 0F).color(1F, 1F, 1F, 1F).uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
			builder.vertex(mat, -0.001F, 0F, 1F).color(1F, 1F, 1F, 1F).uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
			builder.vertex(mat, -0.001F, 1F, 1F).color(1F, 1F, 1F, 1F).uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
			builder.vertex(mat, -0.001F, 1F, 0F).color(1F, 1F, 1F, 1F).uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight).normal(1, 1, 1).endVertex();
		}
	}
}
