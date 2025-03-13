package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.render.tile.IEBlockEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.List;

public class DerrickRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<DerrickLogic.State>> {
	//static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
	
	public static final ResourceLocation DRILL = ResourceUtils.ip("multiblock/dyn/derrick_drill");
	public static final ResourceLocation PIPE_SEGMENT = ResourceUtils.ip("multiblock/dyn/derrick_pipe_segment");
	public static final ResourceLocation PIPE_TOP = ResourceUtils.ip("multiblock/dyn/derrick_pipe_top");
	
	@Override
	public boolean shouldRenderOffScreen(@Nonnull MultiblockBlockEntityMaster<DerrickLogic.State> te){
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MultiblockBlockEntityMaster<DerrickLogic.State> te, float partialTicks, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn){
		if(te.isRemoved() || !te.getLevel().hasChunkAt(te.getBlockPos())){
			return;
		}
		
		matrix.pushPose();
		{
			float rot = te.getHelper().getState().rotation + (te.getHelper().getState().drilling? 10 * partialTicks : 0);
			
			matrix.translate(0.5, 1.0, 0.5);
			matrix.mulPose(Axis.YP.rotationDegrees(rot));
			renderObj(DRILL, bufferIn, matrix, combinedLightIn, combinedOverlayIn);
			
			float pipeHeight = -(rot / 360F);
			
			for(int i = 0;i < 6;i++){
				float y = pipeHeight + i;
				if(y > -1.0){
					matrix.pushPose();
					{
						matrix.translate(0, y + 0.75, 0);
						renderObj(i < 5 ? PIPE_SEGMENT : PIPE_TOP, bufferIn, matrix, combinedLightIn, combinedOverlayIn);
					}
					matrix.popPose();
				}
			}
			
		}
		matrix.popPose();
	}
	
	private void renderObj(ResourceLocation modelRL, @Nonnull MultiBufferSource bufferIn, @Nonnull PoseStack matrix, int light, int overlay){
		List<BakedQuad> quads = MCUtil.getModel(modelRL).getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
		Pose last = matrix.last();
		VertexConsumer solid = bufferIn.getBuffer(RenderType.solid());
		for(BakedQuad quad:quads){
			solid.putBulkData(last, quad, 1.0F, 1.0F, 1.0F, light, overlay);
		}
	}
}
