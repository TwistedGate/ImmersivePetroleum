package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellPipeTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class WellPipeBlock extends IPBlockBase implements EntityBlock{
	
	public static final BooleanProperty BROKEN = BooleanProperty.create("broken");
	
	public WellPipeBlock(){
		super(Block.Properties.copy(Blocks.STONE).mapColor(MapColor.PODZOL).strength(75.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().noLootTable());
		
		registerDefaultState(getStateDefinition().any()
				.setValue(BROKEN, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder){
		builder.add(BROKEN);
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		// Nobody is supposed to have this in their inventory
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor){
//		int d = pos.getY() - neighbor.getY();
//		if(d > 0 && world.getBlockState(pos.up()).getBlock() != this){
//		}
	}
	
	@Override
	public void onPlace(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving){
	}
	
	@Override
	public float getDestroyProgress(BlockState state, @Nonnull Player player, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos){
		float f = state.getDestroySpeed(worldIn, pos);
		
		if(state.getValue(BROKEN)){
			f /= 5F;
		}
		
		if(f == -1.0F){
			return 0.0F;
		}else{
			int i = net.minecraftforge.common.ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
			return player.getDigSpeed(state, pos) / f / (float) i;
		}
	}
	
	@Override
	public void onRemove(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving){
		if(state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())){
			removed(state, world, pos);
			world.removeBlockEntity(pos);
		}
	}
	
	private void removed(BlockState state, Level world, BlockPos pos){
		if(world.isClientSide || state.getValue(BROKEN)){
			return;
		}
		
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof WellPipeTileEntity){
			WellTileEntity well = null;
			
			for(int y = pos.getY() - 1;y >= world.getMinBuildHeight();y--){
				BlockEntity teLow = world.getBlockEntity(new BlockPos(pos.getX(), y, pos.getZ()));
				
				if(teLow instanceof WellTileEntity w){
					well = w;
					break;
				}
			}
			
			if(well != null && !well.pastPhysicalPart){
				well.phyiscalPipesList.remove(Integer.valueOf(pos.getY()));
				if(well.wellPipeLength > 0){
					well.wellPipeLength -= 1;
				}
				well.setChanged();
			}
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState){
		return IPTileTypes.WELL_PIPE.get().create(pPos, pState);
	}
}
