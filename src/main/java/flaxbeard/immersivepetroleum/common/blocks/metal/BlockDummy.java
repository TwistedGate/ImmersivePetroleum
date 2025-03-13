package flaxbeard.immersivepetroleum.common.blocks.metal;

import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class BlockDummy extends IPBlockBase{
	//private static final Material Material = new Material(MaterialColor.METAL, false, false, true, true, false, false, PushReaction.BLOCK);
	
	public BlockDummy(){
		super(Block.Properties.of().
				mapColor(MapColor.METAL).
				pushReaction(PushReaction.BLOCK).
				noOcclusion());
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		return () -> new BlockItem(this, new Item.Properties());
	}
}
