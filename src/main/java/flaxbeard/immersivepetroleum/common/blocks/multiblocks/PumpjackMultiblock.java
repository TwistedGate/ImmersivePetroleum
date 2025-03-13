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
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.PumpjackTileEntity;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class PumpjackMultiblock extends IPTemplateMultiblock{
    public static final PumpjackMultiblock INSTANCE = new PumpjackMultiblock();

    private PumpjackMultiblock(){
        super(ResourceUtils.ip("multiblocks/pumpjack"), new BlockPos(1, 0, 0), new BlockPos(1, 1, 4), new BlockPos(3, 4, 6), IPContent.Multiblock.PUMPJACK);
    }

    @Override
    public float getManualScale(){
        return 12;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
        consumer.accept(new IPClientMultiblockProperties(this, 1.5, 0.5, 0.5));
    }

    /*public class PumpjackClientData extends IPClientMultiblockProperties{
        //private PumpjackTileEntity te;
        //private List<BakedQuad> list;
        public PumpjackClientData(){
            super(PumpjackMultiblock.INSTANCE, 0, 0, 0);
        }

        @Override
        protected boolean usingCustomRendering(){
            return true;
        }

        @Override
        public boolean canRenderFormedStructure(){
            return true;
        }

        final Quaternionf rot = new Quaternionf(new AxisAngle4f(90F, new Vector3f(0F, 1F, 0F)));
        @Override
        public void renderCustomFormedStructure(PoseStack matrix, MultiBufferSource buffer){
			if(this.te == null){
				this.te = new PumpjackTileEntity(IPTileTypes.PUMP.master(), BlockPos.ZERO, IPContent.Multiblock.PUMPJACK.get().defaultBlockState());
			}

			if(this.list == null){
				BlockState state = this.te.getBlockState();
				BakedModel model = MCUtil.getBlockRenderer().getBlockModel(state);
				this.list = model.getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
			}

			if(this.list != null && this.list.size() > 0){
				Level world = MCUtil.getLevel();
				if(world != null){
					matrix.pushPose();
					{
						matrix.translate(1, 0, 0);

						RenderUtils.renderModelTESRFast(this.list, buffer.getBuffer(RenderType.solid()), matrix, 0xF000F0, OverlayTexture.NO_OVERLAY);

						matrix.pushPose();
						{
							matrix.mulPose(this.rot);
							matrix.translate(-2, -1, -1);
							ImmersivePetroleum.proxy.renderTile(this.te, buffer.getBuffer(RenderType.solid()), matrix, buffer);
						}
						matrix.popPose();
					}
					matrix.popPose();
				}
			}
        }
    }*/
}
