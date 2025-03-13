package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WellBlock extends IPBlockBase implements EntityBlock{
	public WellBlock(){
		super(Block.Properties.copy(Blocks.STONE).strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn((s, r, p, e) -> false).requiresCorrectToolForDrops());
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState){
		return IPTileTypes.WELL.get().create(pPos, pState);
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type){
		return createCommonTicker(level.isClientSide, type, IPTileTypes.WELL);
	}
}
