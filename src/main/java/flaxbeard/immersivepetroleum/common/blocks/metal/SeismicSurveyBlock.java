package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.SeismicSurveyTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author TwistedGate
 */
public class SeismicSurveyBlock extends IPBlockBase implements EntityBlock{
	//private static final Material material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final BooleanProperty SLAVE = BooleanProperty.create("slave");
	
	public SeismicSurveyBlock(){
		super(Block.Properties.of().mapColor(MapColor.METAL).pushReaction(PushReaction.BLOCK).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
		
		registerDefaultState(getStateDefinition().any()
				.setValue(SLAVE, false));
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		return () -> new SeismicSurveyBlockItem(this);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder){
		builder.add(SLAVE);
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
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState){
		SeismicSurveyTileEntity te = IPTileTypes.SEISMIC_SURVEY.get().create(pPos, pState);
		te.isSlave = pState.getValue(SLAVE);
		return te;
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type){
		if(state.getValue(SLAVE)){
			return null;
		}
		return createCommonTicker(level.isClientSide, type, IPTileTypes.SEISMIC_SURVEY);
	}
	
	@Override
	public void playerWillDestroy(@Nonnull Level world, @Nonnull BlockPos pos, BlockState state, @Nonnull Player player){
		if(state.getValue(SLAVE)){
			// Find the master block
			for(int i = 1;i < 3;i++){
				BlockPos p = pos.offset(0, -i, 0);
				BlockState stateDown = world.getBlockState(p);
				
				if(!stateDown.isAir() && stateDown.getBlock().equals(this) && !stateDown.getValue(SLAVE)){
					world.destroyBlock(p, !player.isCreative());
					world.destroyBlock(p.offset(0, 1, 0), false);
					world.destroyBlock(p.offset(0, 2, 0), false);
					break;
				}
			}
		}else{
			world.destroyBlock(pos.offset(0, 1, 0), false);
			world.destroyBlock(pos.offset(0, 2, 0), false);
		}
		
		super.playerWillDestroy(world, pos, state, player);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Nonnull
	public List<ItemStack> getDrops(BlockState state, @Nonnull LootParams.Builder builder){
		if(state.getValue(SLAVE)){
			// TODO Don't know how else i would do this yet
			return Collections.emptyList();
		}
		
		return super.getDrops(state, builder);
	}
	
	@Override
	public void onRemove(BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pNewState, boolean pIsMoving){
		if(pState.hasBlockEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasBlockEntity())){
			if(!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof SeismicSurveyTileEntity survey && !survey.isSlave){
				if(!survey.stack.isEmpty()){
					Block.popResource(pLevel, pPos, survey.stack);
				}
			}
			pLevel.removeBlockEntity(pPos);
		}
	}
	
	@Override
	@Nonnull
	public InteractionResult use(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof SeismicSurveyTileEntity survey){
			survey = survey.master();
			
			if(survey != null && survey.interact(state, world, survey.getBlockPos(), player, hand)){
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
	
	@Override
	public void setPlacedBy(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack){
		if(!worldIn.isClientSide){
			worldIn.setBlockAndUpdate(pos.offset(0, 1, 0), state.setValue(SLAVE, true));
			worldIn.setBlockAndUpdate(pos.offset(0, 2, 0), state.setValue(SLAVE, true));
		}
	}
	
	static final VoxelShape SHAPE_MASTER = Shapes.box(0.001, 0.001, 0.001, 0.999, 0.999, 0.999);
	
	@Override
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context){
		return SHAPE_MASTER;
	}
	
	public static class SeismicSurveyBlockItem extends IPBlockItemBase{
		public SeismicSurveyBlockItem(Block blockIn){
			super(blockIn, new Item.Properties());
		}
		
		@Override
		protected boolean canPlace(@Nonnull BlockPlaceContext con, @Nonnull BlockState state){
			if(super.canPlace(con, state)){
				BlockPos posA = con.getClickedPos().relative(Direction.UP, 1);
				BlockState stateA = con.getLevel().getBlockState(posA);
				if(stateA.isAir()){
					BlockPos posB = con.getClickedPos().relative(Direction.UP, 2);
					BlockState stateB = con.getLevel().getBlockState(posB);
					
					return stateB.isAir();
				}
			}
			return false;
		}
	}
}
