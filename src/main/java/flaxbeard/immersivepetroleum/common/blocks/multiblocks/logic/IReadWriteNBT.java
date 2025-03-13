/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

import net.minecraft.nbt.CompoundTag;

public interface IReadWriteNBT{
    public CompoundTag writeNBT();
    public void readNBT(CompoundTag nbt);
}