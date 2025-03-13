package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class AutoLubricatorRenderer implements BlockEntityRenderer<AutoLubricatorTileEntity>{
	
	@Override
	public boolean shouldRenderOffScreen(@Nonnull AutoLubricatorTileEntity te){
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	@SuppressWarnings("unchecked, rawtypes")
	public void render(@Nonnull AutoLubricatorTileEntity te, float partialTicks, @Nonnull PoseStack transform, @Nonnull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn){
		if(te.isSlave)
			return;
		
		FluidStack fs = te.tank.getFluid();
		float level = 0;
		if(!fs.isEmpty()){
			level = fs.getAmount() / (float) te.tank.getCapacity();
		}
		
		if(level > 0){
			float height = 16;
			
			transform.pushPose();
			{
				float scale = 0.0625f;
				transform.translate(0.25, 0.875, 0.25);
				transform.scale(scale, scale, scale);
				
				VertexConsumer builder = bufferIn.getBuffer(RenderType.solid());
				
				float h = height * level;
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				transform.mulPose(Axis.YP.rotationDegrees(90));
				transform.translate(-7.98, 0, 0);
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				transform.mulPose(Axis.YP.rotationDegrees(90));
				transform.translate(-7.98, 0, 0);
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				transform.mulPose(Axis.YP.rotationDegrees(90));
				transform.translate(-7.98, 0, 0);
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				if(h < height){
					transform.mulPose(Axis.XP.rotationDegrees(90));
					transform.translate(0, 0, -h);
					GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, 8);
				}
			}
			transform.popPose();
		}
		
		transform.pushPose();
		{
			BlockPos target = te.getBlockPos().relative(te.getFacing());
			BlockEntity test = te.getLevel().getBlockEntity(target);

			if (test instanceof IMultiblockBE<?> tile)
			{
				ILubricationHandler handler = LubricatedHandler.getHandlerForTile(tile.getHelper());
				if(handler != null){
					BlockEntity master = handler.isPlacedCorrectly(te.getLevel(), te, te.getFacing());
					if(master instanceof IMultiblockBE<?> newTile){
						handler.renderPipes(te, newTile.getHelper(), transform, bufferIn, combinedLightIn, combinedOverlayIn);
					}
				}
			}
		}
		transform.popPose();
	}
}
