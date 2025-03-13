package flaxbeard.immersivepetroleum.common.lubehandlers;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import com.mojang.blaze3d.vertex.PoseStack;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.PumpjackLogic;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class PumpjackLubricationHandler implements ILubricationHandler<IMultiblockBEHelper<PumpjackLogic.State>, PumpjackLogic.State>{
	private static final Vec3i size = new Vec3i(4, 6, 3);
	
	@Override
	public Vec3i getStructureDimensions(){
		return size;
	}
	
	@Override
	public boolean isMachineEnabled(Level world, IMultiblockBEHelper<PumpjackLogic.State> mbte){
		return mbte.getState().wasActive;
	}
	
	@Override
	public BlockEntity isPlacedCorrectly(Level world, AutoLubricatorTileEntity lubricator, Direction facing){
		BlockPos target = lubricator.getBlockPos().relative(facing);
		BlockEntity te = world.getBlockEntity(target);

		if(te instanceof MultiblockBlockEntityMaster<?> master){

			if(master != null){
				Direction f = master.getHelper().getContext().getLevel().getOrientation().mirrored() ? facing : facing.getOpposite();
				if(master.getHelper().getContext().getLevel().getOrientation().front().getClockWise() == f){
					return master;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void lubricateClient(ClientLevel world, Fluid lubricant, int ticks, IMultiblockBEHelper<PumpjackLogic.State> mbte){
		mbte.getState().activeTicks += 1F / 4F;
	}
	
	@Override
	public void lubricateServer(ServerLevel world, Fluid lubricant, int ticks, IMultiblockBEHelper<PumpjackLogic.State> mbte){
		if(ticks % 4 == 0){
			if (mbte instanceof IMultiblockBEHelperMaster<PumpjackLogic.State> master)
				master.tickServer();
		}
	}
	
	@Override
	public void spawnLubricantParticles(ClientLevel world, AutoLubricatorTileEntity lubricator, Direction facing, IMultiblockBEHelper<PumpjackLogic.State> mbte){
		Direction f = mbte.getContext().getLevel().getOrientation().mirrored() ? facing : facing.getOpposite();
		float location = world.random.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getContext().getLevel().getOrientation().mirrored();
		float xO = 2.5F;
		float zO = -.15F;
		float yO = 2.25F;
		
		if(location > .5F){
			xO = 1.7F;
			yO = 2.9F;
			zO = -1.5F;
			
		}
		
		if(facing.getAxisDirection() == AxisDirection.NEGATIVE)
			xO = -xO + 1;
		if(!flip)
			zO = -zO + 1;
		
		float x = lubricator.getBlockPos().getX() + (f.getAxis() == Axis.X ? xO : zO);
		float y = lubricator.getBlockPos().getY() + yO;
		float z = lubricator.getBlockPos().getZ() + (f.getAxis() == Axis.X ? zO : xO);
		
		for(int i = 0;i < 3;i++){
			float r1 = (world.random.nextFloat() - .5F) * 2F;
			float r2 = (world.random.nextFloat() - .5F) * 2F;
			float r3 = world.random.nextFloat();
			
			world.addParticle(ParticleTypes.FALLING_HONEY, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	@Override
	public Tuple<BlockPos, Direction> getGhostBlockPosition(Level world, IMultiblockBEHelper<PumpjackLogic.State> mbte){
		MultiblockOrientation orientation = mbte.getContext().getLevel().getOrientation();
		Direction mbFacing = orientation.front().getOpposite();
		BlockPos pos = mbte.getPositionInMB()
				.relative(Direction.UP)
				.relative(mbFacing, 4)
				.relative(orientation.mirrored() ? mbFacing.getClockWise() : mbFacing.getCounterClockWise(), 2);
			
		Direction f = (orientation.mirrored() ? orientation.front().getOpposite() : orientation.front()).getCounterClockWise();
		return new Tuple<>(pos, f);
	}
	
	private static final ResourceLocation TEXTURE = ResourceUtils.ip("textures/models/lube_pipe.png");
	
	@OnlyIn(Dist.CLIENT)
	private static Supplier<IPModel> pipes_normal;
	
	@OnlyIn(Dist.CLIENT)
	private static Supplier<IPModel> pipes_mirrored;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(AutoLubricatorTileEntity lubricator, IMultiblockBEHelper<PumpjackLogic.State> mbte, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		matrix.translate(0, -1, 0);
		Vec3i offset = mbte.getPositionInMB().subtract(lubricator.getBlockPos());
		matrix.translate(offset.getX(), offset.getY(), offset.getZ());

		MultiblockOrientation orientation = mbte.getContext().getLevel().getOrientation();
		Direction rotation = orientation.front();
		switch(rotation){
			case NORTH -> {
				matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90F));
				matrix.translate(-6, 1, -1);
			}
			case SOUTH -> {
				matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270F));
				matrix.translate(-5, 1, -2);
			}
			case EAST -> {
				matrix.translate(-5, 1, -1);
			}
			case WEST -> {
				matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
				matrix.translate(-6, 1, -2);
			}
			default -> {
			}
		}
		
		IPModel model;
		if(orientation.mirrored()){
			if(pipes_mirrored == null)
				pipes_mirrored = IPModels.getSupplier(ModelLubricantPipes.Pumpjack.ID_MIRRORED);
			
			model = pipes_mirrored.get();
		}else{
			if(pipes_normal == null)
				pipes_normal = IPModels.getSupplier(ModelLubricantPipes.Pumpjack.ID_NORMAL);
			
			model = pipes_normal.get();
		}
		
		if(model != null){
			model.renderToBuffer(matrix, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
