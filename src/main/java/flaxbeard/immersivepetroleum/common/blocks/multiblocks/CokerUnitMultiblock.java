/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class CokerUnitMultiblock extends IPTemplateMultiblock {
    public static final CokerUnitMultiblock INSTANCE = new CokerUnitMultiblock();

    public CokerUnitMultiblock(){
        super(ResourceUtils.ip("multiblocks/cokerunit"),
                new BlockPos(4, 0, 2), new BlockPos(4, 1, 4), new BlockPos(9, 23, 5),
                IPContent.Multiblock.COKERUNIT);
    }

    @Override
    public float getManualScale(){
        return 4.0F;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
        consumer.accept(new IPClientMultiblockProperties(this, 4.5, 0.5, 2.5));
    }
}