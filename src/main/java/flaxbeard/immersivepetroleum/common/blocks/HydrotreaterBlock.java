package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.blocks.tileentities.HydrotreaterTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class HydrotreaterBlock extends IPMetalMultiblock<HydrotreaterTileEntity>{
	public HydrotreaterBlock(){
		super("hydrotreater", () -> HydrotreaterTileEntity.TYPE);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}
}
