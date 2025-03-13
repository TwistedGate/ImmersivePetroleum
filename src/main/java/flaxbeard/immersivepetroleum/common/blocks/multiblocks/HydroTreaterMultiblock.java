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

public class HydroTreaterMultiblock extends IPTemplateMultiblock {
    public static final HydroTreaterMultiblock INSTANCE = new HydroTreaterMultiblock();

    public HydroTreaterMultiblock(){
        super(ResourceUtils.ip("multiblocks/hydrotreater"),
                new BlockPos(1, 0, 2), new BlockPos(1, 1, 3), new BlockPos(3, 3, 4),
                IPContent.Multiblock.HYDROTREATER);
    }

    @Override
    public float getManualScale(){
        return 12.0F;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
        consumer.accept(new IPClientMultiblockProperties(this, 1.5, 0.5, 2.5));
    }
}