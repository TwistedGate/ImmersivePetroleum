package flaxbeard.immersivepetroleum.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.FlarestackTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.shapes.BooleanOp;
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

public class FlarestackBlock extends IPBlockBase implements EntityBlock{
	//private static final Material material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public static final BooleanProperty SLAVE = BooleanProperty.create("slave");
	
	public FlarestackBlock(){
		super(Block.Properties.of().mapColor(MapColor.METAL).pushReaction(PushReaction.BLOCK).strength(3.0F, 15.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
		
		registerDefaultState(getStateDefinition().any().setValue(SLAVE, false));
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		return () -> new FlarestackBlockItem(this);
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
	@Nonnull
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit){
		if(Utils.isScrewdriver(player.getItemInHand(handIn))){
			if(state.getValue(SLAVE)){
				pos = pos.relative(Direction.DOWN);
			}
			
			if(!worldIn.isClientSide){
				BlockEntity te = worldIn.getBlockEntity(pos);
				if(te instanceof FlarestackTileEntity flare){
					flare.invertRedstone();
					
					ChatUtils.sendServerNoSpamMessages(player, Component.translatable(Lib.CHAT_INFO + "rsControl." + (flare.isRedstoneInverted() ? "invertedOn" : "invertedOff")));
				}
			}
			
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
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
	
	@Override
	public void setPlacedBy(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack){
		if(!worldIn.isClientSide){
			worldIn.setBlockAndUpdate(pos.relative(Direction.UP), state.setValue(SLAVE, true));
		}
	}
	
	@Override
	public void entityInside(BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn){
		if(state.getValue(SLAVE) && !entityIn.fireImmune()){
			entityIn.hurt(worldIn.damageSources().hotFloor(), 1.0F);
		}
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
	
	static VoxelShape SHAPE_SLAVE;
	static VoxelShape SHAPE_MASTER;
	
	@Override
	@Nonnull
	public VoxelShape getShape(BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context){
		if(state.getValue(SLAVE)){
			if(SHAPE_SLAVE == null){
				VoxelShape s0 = Shapes.box(0.125, 0.0, 0.125, 0.875, 0.75, 0.875);
				VoxelShape s1 = Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.375, 0.9375);
				SHAPE_SLAVE = Shapes.join(s0, s1, BooleanOp.OR);
			}
			
			return SHAPE_SLAVE;
		}else{
			if(SHAPE_MASTER == null){
				VoxelShape s0 = Shapes.box(0.125, 0.0, 0.125, 0.875, 0.75, 0.875);
				VoxelShape s1 = Shapes.box(0.0625, 0.5, 0.0625, 0.9375, 1.0, 0.9375);
				SHAPE_MASTER = Shapes.join(s0, s1, BooleanOp.OR);
			}
			
			return SHAPE_MASTER;
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, BlockState pState){
		if(pState.getValue(SLAVE))
			return null;
		
		return IPTileTypes.FLARE.get().create(pPos, pState);
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type){
		if(state.getValue(SLAVE))
			return null;
		
		return createCommonTicker(level.isClientSide, type, IPTileTypes.FLARE);
	}
	
	public static class FlarestackBlockItem extends IPBlockItemBase{
		public FlarestackBlockItem(Block blockIn){
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
