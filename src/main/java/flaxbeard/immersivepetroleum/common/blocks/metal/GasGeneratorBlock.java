package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.interfaces.IPlacementReader;
import flaxbeard.immersivepetroleum.common.blocks.interfaces.IPlayerInteraction;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.GasGeneratorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GasGeneratorBlock extends IPBlockBase implements EntityBlock{
	//private static final Material material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	
	public GasGeneratorBlock(){
		super(Block.Properties.of().mapColor(MapColor.METAL).pushReaction(PushReaction.BLOCK).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
		
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder){
		builder.add(FACING);
	}
	
	@Override
	public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos){
		return 0;
	}
	
	@Override
	public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos){
		return 1.0F;
	}
	
	@Override
	public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos){
		return true;
	}
	
	// Fixes black faces apearing when a solid block is placed next to the generator
	// at the cost of not being able to put a lever on the generator anymore.
	static final VoxelShape SHAPE = Shapes.box(0.0001, 0.0001, 0.0001, 0.9999, 0.9999, 0.9999);
	
	@Override
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context){
		return SHAPE;
	}
	
	@Override
	@Nonnull
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context){
		return SHAPE;
	}
	
	@Override
	@Nonnull
	public InteractionResult use(@Nonnull BlockState state, Level worldIn, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof IPlayerInteraction inst){
			return inst.interact(hit.getDirection(), player, handIn, player.getItemInHand(handIn), (float) hit.getLocation().x, (float) hit.getLocation().y, (float) hit.getLocation().z);
		}
		return InteractionResult.FAIL;
	}
	
	@Override
	public void setPlacedBy(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack){
		if(!worldIn.isClientSide){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof IPlacementReader read){
				read.readOnPlacement(placer, stack);
			}
		}
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState){
		GasGeneratorTileEntity te = IPTileTypes.GENERATOR.get().create(pPos, pState);
		te.setFacing(pState.getValue(FACING));
		return te;
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type){
		return createCommonTicker(level.isClientSide, type, IPTileTypes.GENERATOR);
	}
}
