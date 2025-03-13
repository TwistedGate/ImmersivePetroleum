package flaxbeard.immersivepetroleum.common.fluids;

import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NapalmFluid extends IPFluid{
	
	public NapalmFluid(IPFluidEntry entry){
		super(entry);
	}
	
	@Override
	public int getTickDelay(@Nonnull LevelReader p_205569_1_){
		return 10;
	}
	
	@Override
	public boolean hasCustomSlowdown(){
		return true;
	}
	
	@Override
	public double getEntitySlowdown(){
		return 0.7;
	}
	
	public static void processFire(IPFluidEntry entry, Level world, BlockPos pos){
		ResourceLocation d = world.dimension().location();
		
		List<BlockPos> list = CommonEventHandler.napalmPositions.computeIfAbsent(d, f -> new ArrayList<>());
		
		list.add(pos);
		world.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
		
		for(Direction facing:Direction.values()){
			BlockPos notifyPos = pos.relative(facing);
			if(world.getBlockState(notifyPos).is(entry.block().get())){
				list.add(notifyPos);
			}
		}
	}
	
	public static class NapalmFluidBlock extends IPFluidBlock{
		public NapalmFluidBlock(IPFluidEntry entry, Properties props){
			super(entry, props);
		}
		
		@Override
		public void onPlace(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving){
			for(Direction facing:Direction.values()){
				BlockPos notifyPos = pos.relative(facing);
				if(world.getBlockState(notifyPos).getBlock() instanceof FireBlock || world.getBlockState(notifyPos).is(BlockTags.FIRE)){
					world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
					break;
				}
			}
			super.onPlace(state, world, pos, oldState, isMoving);
		}
		
		@Override
		public void neighborChanged(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving){
			if(world.getBlockState(fromPos).getBlock() instanceof FireBlock || world.getBlockState(fromPos).is(BlockTags.FIRE)){
				ResourceLocation d = world.dimension().location();
				if(!CommonEventHandler.napalmPositions.containsKey(d) || !CommonEventHandler.napalmPositions.get(d).contains(fromPos)){
					processFire(this.entry, world, pos);
				}
			}
			
			super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
		}
	}
}
