/**
 * @author ArcAnc
 * Created at: 27.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import java.util.Optional;

public class IPMultiblockTextutesAttach extends SpriteSourceProvider {
    public IPMultiblockTextutesAttach(PackOutput output, ExistingFileHelper fileHelper)
    {
        super(output, fileHelper, ImmersivePetroleum.MODID);
    }

    @Override
    protected void addSources()
    {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/cokerunit"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/derrick"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillation_tower"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/distillation_tower_active"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/hydrotreater"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/oiltank"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "multiblock/pumpjack_base"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "models/lubricator"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "models/pumpjack_armature"), Optional.empty()));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(ImmersivePetroleum.MODID, "projectors/projector"), Optional.empty()));
    }
}
