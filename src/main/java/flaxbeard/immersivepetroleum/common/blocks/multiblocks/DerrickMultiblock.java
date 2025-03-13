/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

import static flaxbeard.immersivepetroleum.client.render.DerrickRenderer.*;

public class DerrickMultiblock extends IPTemplateMultiblock {
    public static final DerrickMultiblock INSTANCE = new DerrickMultiblock();

    public DerrickMultiblock(){
        super(ResourceUtils.ip("multiblocks/derrick"),
                new BlockPos(2, 0, 2), new BlockPos(2, 0, 4), new BlockPos(5, 8, 5), IPContent.Multiblock.DERRICK);
    }

    @Override
    public float getManualScale(){
        return 10.0F;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
        consumer.accept(new DerrickMultiblockProperties());
    }

    public static class DerrickMultiblockProperties extends IPClientMultiblockProperties {
        public DerrickMultiblockProperties(){
            super(INSTANCE, 2.5, 0.5, 2.5);
        }

        @Override
        public void renderExtras(PoseStack matrix, MultiBufferSource buffer){
            matrix.translate(0.0, 0.5, 0.0);
            renderObj(DRILL, buffer, matrix);
            for(int i = 0;i < 6;i++){
                matrix.pushPose();
                {
                    matrix.translate(0, i + 0.75, 0);
                    renderObj(i < 5 ? PIPE_SEGMENT : PIPE_TOP, buffer, matrix);
                }
                matrix.popPose();
            }
        }

        private static void renderObj(ResourceLocation modelRL, @Nonnull MultiBufferSource bufferIn, @Nonnull PoseStack matrix){
            List<BakedQuad> quads = MCUtil.getModel(modelRL).getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
            PoseStack.Pose last = matrix.last();
            VertexConsumer solid = bufferIn.getBuffer(RenderType.solid());
            for(BakedQuad quad:quads){
                solid.putBulkData(last, quad, 1.0F, 1.0F, 1.0F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}