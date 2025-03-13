package flaxbeard.immersivepetroleum.common.blocks.wooden;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AutoLubricatorBlock extends IPBlockBase implements EntityBlock{
	//private static final Material material = new Material(MaterialColor.WOOD, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty SLAVE = BooleanProperty.create("slave");
	
	public AutoLubricatorBlock(){
		super(Block.Properties.of().
				strength(5.0F, 6.0F).
				sound(SoundType.WOOD).
				requiresCorrectToolForDrops().
				noOcclusion().
				mapColor(MapColor.COLOR_GRAY).
				instrument(NoteBlockInstrument.BASS).
				sound(SoundType.CHERRY_WOOD).
				pushReaction(PushReaction.BLOCK));
		
		registerDefaultState(getStateDefinition().any()
				.setValue(FACING, Direction.NORTH)
				.setValue(SLAVE, false));
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		return () -> new AutoLubricatorBlockItem(this);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder){
		builder.add(FACING, SLAVE);
	}
	
	@Override
	public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos){
		return 0;
	}
	
	@Override
	public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos){
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos){
		return 1.0F;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack){
		if(!pLevel.isClientSide){
			pLevel.setBlockAndUpdate(pPos.offset(0, 1, 0), pState.setValue(SLAVE, true));
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState){
		AutoLubricatorTileEntity te = IPTileTypes.AUTOLUBE.get().create(pPos, pState);
		te.isSlave = pState.getValue(SLAVE);
		te.facing = pState.getValue(FACING);
		return te;
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type){
		return createCommonTicker(level.isClientSide, type, IPTileTypes.AUTOLUBE);
	}
	
	@Override
	public void playerWillDestroy(@Nonnull Level worldIn, @Nonnull BlockPos pos, BlockState state, @Nonnull Player player){
		if(state.getValue(SLAVE)){
			worldIn.destroyBlock(pos.offset(0, -1, 0), !player.isCreative());
		}else{
			worldIn.destroyBlock(pos.offset(0, 1, 0), false);
		}
		
		super.playerWillDestroy(worldIn, pos, state, player);
	}
	
	static final VoxelShape SHAPE_SLAVE = Shapes.box(.1875F, 0, .1875F, .8125f, 1, .8125f);
	static final VoxelShape SHAPE_MASTER = Shapes.box(.0625f, 0, .0625f, .9375f, 1, .9375f);
	
	@Override
	@Nonnull
	public VoxelShape getShape(BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context){
		return state.getValue(SLAVE) ? SHAPE_SLAVE : SHAPE_MASTER;
	}
	
	public static class AutoLubricatorBlockItem extends IPBlockItemBase{
		public AutoLubricatorBlockItem(Block blockIn){
			super(blockIn, new Item.Properties());
		}
		
		@Override
		protected boolean canPlace(@Nonnull BlockPlaceContext con, @Nonnull BlockState state){
			if(super.canPlace(con, state)){
				BlockPos otherPos = con.getClickedPos().relative(Direction.UP);
				BlockState otherState = con.getLevel().getBlockState(otherPos);
				
				return otherState.isAir();
			}
			return false;
		}
	}
}
