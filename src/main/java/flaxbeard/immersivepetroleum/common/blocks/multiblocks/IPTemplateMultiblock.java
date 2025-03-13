/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public abstract class IPTemplateMultiblock extends IETemplateMultiblock {
    //private final MultiblockRegistration<?> logic;

    public IPTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, MultiblockRegistration<?> logic){
        super(loc, masterFromOrigin, triggerFromOrigin, size, logic);
        //this.logic = logic;
    }

    //@Override
    //protected void replaceStructureBlock(StructureTemplate.StructureBlockInfo info, Level world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster){
    //}

    /*public ResourceLocation getBlockName(){
        return this.logic.id();
    }

    @Override
    public Component getDisplayName(){
        return this.logic.block().get().getName();
    }

    @Override
    public Block getBlock(){
        return this.logic.block().get();
    }

    @Deprecated(forRemoval = true)
    public Block getBaseBlock(){
        throw new UnsupportedOperationException();
    }*/
}