package flaxbeard.immersivepetroleum.common.fluids;

import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;

public class CrudeOilFluid extends IPFluid{
	//public static final Material MATERIAL = createMaterial(MaterialColor.COLOR_BLACK);
	
	public CrudeOilFluid(IPFluidEntry entry){
		super(entry);
	}
	
	@Override
	public int getTickDelay(@Nonnull LevelReader p_205569_1_){
		return 20;
	}
	
	@Override
	public boolean hasCustomSlowdown(){
		return true;
	}
	
	@Override
	public double getEntitySlowdown(){
		return 0.4;
	}
	
	public static class CrudeOilBlock extends IPFluidBlock{
		public CrudeOilBlock(IPFluidEntry entry, BlockBehaviour.Properties props){
			super(entry, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_BLACK).noCollission().strength(100.0F).noLootTable());
		}
	}
}
