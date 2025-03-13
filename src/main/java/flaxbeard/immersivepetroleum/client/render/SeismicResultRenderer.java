package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.client.render.dyn.DynamicTextureWrapper;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.items.SurveyResultItem;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.survey.ISurveyInfo;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

/**
 * @author TwistedGate
 */
public class SeismicResultRenderer{
	
	@SubscribeEvent
	public void renderHandEvent(RenderHandEvent event){
		ItemStack stack = event.getItemStack();
		if(stack.getItem().equals(IPContent.Items.SURVEYRESULT.get()) && stack.hasTag() && stack.getTagElement("surveyscan") != null){
			event.setCanceled(true);
		}
	}
	
	static final Tesselator TESSELATOR = new Tesselator();
	
	private static final ResourceLocation OVERLAY = ResourceUtils.ip("textures/gui/seismicsurvey_overlay.png");
	
	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event){
		if(event.getItemStack().getItem() instanceof SurveyResultItem && MCUtil.getPlayer().distanceTo(event.getItemFrameEntity()) < 1000){
			if(ISurveyInfo.from(event.getItemStack()) instanceof SurveyScan scan){
				DynamicTextureWrapper wrapper = DynamicTextureWrapper.getOrCreate(SurveyScan.SCAN_SIZE, SurveyScan.SCAN_SIZE, scan);
				
				if(wrapper != null){
					PoseStack matrix = event.getPoseStack();
					// MultiBufferSource buffer = event.getMultiBufferSource(); // Breaks things left and right
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(TESSELATOR.getBuilder());
					
					matrix.pushPose();
					{
						int light = event.getPackedLight();
						int rot = event.getItemFrameEntity().getRotation();
						float scale = 1F / SurveyScan.SCAN_SIZE;
						matrix.mulPose(Axis.ZP.rotationDegrees(180F - -rot * 45F));
						matrix.translate(-0.5F, -0.5F, -0.00625F);
						matrix.scale(scale, scale, 1F);
						
						matrix.pushPose();
						{
							int a = wrapper.width;
							int b = wrapper.height;
							VertexConsumer builder = buffer.getBuffer(wrapper.renderType);
							Matrix4f mat = matrix.last().pose();
							
							builder.vertex(mat, 0, 0, 0).color(-1).uv(1, 1).uv2(light).endVertex();
							builder.vertex(mat, 0, b, 0).color(-1).uv(1, 0).uv2(light).endVertex();
							builder.vertex(mat, a, b, 0).color(-1).uv(0, 0).uv2(light).endVertex();
							builder.vertex(mat, a, 0, 0).color(-1).uv(0, 1).uv2(light).endVertex();
						}
						matrix.popPose();
						
						// Draw Grid-Overlay
						matrix.pushPose();
						{
							final int w = SurveyScan.SCAN_SIZE;
							final int h = SurveyScan.SCAN_SIZE;
							
							matrix.translate(0, 0, -0.002F);
							
							VertexConsumer builder = buffer.getBuffer(RenderType.text(OVERLAY));
							Matrix4f mat = matrix.last().pose();
							
							builder.vertex(mat, 0, 0, 0).color(-1).uv(0, 0).uv2(light).endVertex();
							builder.vertex(mat, 0, h, 0).color(-1).uv(0, 1).uv2(light).endVertex();
							builder.vertex(mat, w, h, 0).color(-1).uv(1, 1).uv2(light).endVertex();
							builder.vertex(mat, w, 0, 0).color(-1).uv(1, 0).uv2(light).endVertex();
						}
						matrix.popPose();
					}
					matrix.popPose();
					
					buffer.endBatch();
					event.setCanceled(true);
				}
			}
		}
	}
}
