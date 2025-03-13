package flaxbeard.immersivepetroleum.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nonnull;

//This serves to replace the "default" registered block that makes up the multiblocks.
public class IPMultiblockBase<T extends IMultiblockState> extends MultiblockPartBlock<T> {

    //the multiblock field is private so we save it here to reference it later on.
    private final MultiblockRegistration<T> multiblock;

    public IPMultiblockBase(BlockBehaviour.Properties properties, MultiblockRegistration<T> multiblock){
        super(properties, multiblock);
        this.multiblock = multiblock;
    }

    /*
    Normally this would be split up between the block and the block entities
    Instead we do everything here. You can't even register custom tile entities anymore.
     */
    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity){
        BlockEntity be = level.getBlockEntity(pos);
        //This is kinda ugly, but it's all we need to get closest to the original implementation.
        if(!(be instanceof IMultiblockBE<?> multiblockBE)) return false;
        BlockPos posMb = multiblockBE.getHelper().getPositionInMB();
        int bX = posMb.getX();
        int bY = posMb.getY();
        int bZ = posMb.getZ();
        //No, a switch won't accept this.
        if(multiblock == IPContent.Multiblock.COKERUNIT){
            return ((bX == 8 && bZ == 2) && (bY >= 5 && bY <= 13)) //Primary Ladder
                    || ((bX == 7 && bZ == 2) && (bY >= 15 && bY <= 21)); //Secondary Ladder
        }
        if(multiblock == IPContent.Multiblock.DERRICK){
            return (bY >= 0 && bY <= 2 && bX == 0 && bZ == 2);
        }
        if(multiblock == IPContent.Multiblock.DISTILLATIONTOWER){
            return (bY > 0 && bX == 2 && bZ == 0);
        }
        if(multiblock == IPContent.Multiblock.OILTANK){
            return (bX == 3 && bZ == 0);
        }
        return false;
    }

    //Without this, the constructor of MultiblockPartBlock will throw an exception and initialization will actually explode.
    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(IEProperties.MIRRORED);
    }


}
