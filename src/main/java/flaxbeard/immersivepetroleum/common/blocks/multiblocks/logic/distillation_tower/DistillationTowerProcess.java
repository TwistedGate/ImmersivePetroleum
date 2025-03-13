/**
 * @author ArcAnc
 * Created at: 25.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class DistillationTowerProcess extends MultiblockProcessInMachine<DistillationTowerRecipe>
{
    private final DistillationTowerLogic.Tanks tanks;

    public DistillationTowerProcess(DistillationTowerRecipe recipe, DistillationTowerLogic.Tanks tanks, int... inputSlots) {
        super(recipe, inputSlots);
        this.tanks = tanks;
    }

    public DistillationTowerProcess(BiFunction<Level, ResourceLocation, DistillationTowerRecipe> recipe, CompoundTag data, DistillationTowerLogic.Tanks tanks) {
        super(recipe, data);
        this.tanks = tanks;
    }

    @Override
    protected List<IngredientWithSize> getRecipeItemInputs(ProcessContext.ProcessContextInMachine<DistillationTowerRecipe> context, Level level) {
        return Collections.emptyList();
    }

//    @Override
//    public boolean canProcess(ProcessContext.ProcessContextInMachine<DistillationTowerRecipe> context, Level level) {

//    }

    @Override
    protected void outputItem(ProcessContext.ProcessContextInMachine<DistillationTowerRecipe> context, ItemStack output, IMultiblockLevel level)
    {
        MultiblockOrientation orientation = level.getOrientation();

        Direction outputdir = orientation.mirrored() ? orientation.front().getClockWise() : orientation.front().getCounterClockWise();
        BlockPos outputpos = level.toAbsolute(DistillationTowerLogic.Item_OUT).relative(outputdir);

        BlockEntity te = level.getBlockEntity(outputpos);
        if(te != null){
            LazyOptional<IItemHandler> handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER, outputdir.getOpposite());
            ItemStack finalOutput = output;
            output = handler.map(outputHandler -> ItemHandlerHelper.insertItem(outputHandler, finalOutput, false)).orElse(ItemStack.EMPTY);
        }

        if(!output.isEmpty()) {
            double x = outputpos.getX() + 0.5;
            double y = outputpos.getY() + 0.25;
            double z = outputpos.getZ() + 0.5;

            Direction facing = orientation.mirrored() ? orientation.front().getOpposite() : orientation.front();
            if (facing != Direction.EAST && facing != Direction.WEST) {
                x = outputpos.getX() + (facing == Direction.SOUTH ? 0.15 : 0.85);
            }
            if (facing != Direction.NORTH && facing != Direction.SOUTH) {
                z = outputpos.getZ() + (facing == Direction.WEST ? 0.15 : 0.85);
            }

            ItemEntity ei = new ItemEntity(level.getRawLevel(), x, y, z, output.copy());
            ei.setDeltaMovement(0.075 * outputdir.getStepX(), 0.025, 0.075 * outputdir.getStepZ());
            level.getRawLevel().addFreshEntity(ei);
        }
    }
}
