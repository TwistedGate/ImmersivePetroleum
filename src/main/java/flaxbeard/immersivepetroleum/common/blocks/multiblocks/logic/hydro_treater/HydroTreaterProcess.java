/**
 * @author ArcAnc
 * Created at: 25.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
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

import java.util.function.BiFunction;

public class HydroTreaterProcess extends MultiblockProcessInMachine<HighPressureRefineryRecipe> {

    private final HydroTreaterLogic.Tanks tanks;

    public HydroTreaterProcess(HighPressureRefineryRecipe recipe, HydroTreaterLogic.Tanks tanks, int... inputSlots) {
        super(recipe, inputSlots);
        this.tanks = tanks;
    }

    public HydroTreaterProcess(BiFunction<Level, ResourceLocation, HighPressureRefineryRecipe> recipe, CompoundTag data, HydroTreaterLogic.Tanks tanks) {
        super(recipe, data);
        this.tanks = tanks;
    }


    /*@Override
    public boolean canProcess(ProcessContext.ProcessContextInMachine<HighPressureRefineryRecipe> context, Level level) {

    }*/

    @Override
    protected void outputItem(ProcessContext.ProcessContextInMachine<HighPressureRefineryRecipe> ctx, ItemStack output, IMultiblockLevel level)
    {
        if(output == null || output.isEmpty())
            return;

        MultiblockOrientation orientation = level.getOrientation();

        Direction outputdir = (orientation.mirrored() ? orientation.front().getClockWise() : orientation.front().getCounterClockWise());
        BlockPos outputpos = level.toAbsolute(HydroTreaterLogic.Item_OUT).relative(outputdir);

        BlockEntity te = level.getBlockEntity(outputpos);
        if(te != null){
            LazyOptional<IItemHandler> handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER, outputdir.getOpposite());
            ItemStack finalOutput = output;
            output = handler.map(itemHandler -> ItemHandlerHelper.insertItem(itemHandler, finalOutput, false)).orElse(ItemStack.EMPTY);
        }

        if(!output.isEmpty()){
            double x = outputpos.getX() + 0.5;
            double y = outputpos.getY() + 0.25;
            double z = outputpos.getZ() + 0.5;

            Direction facing = orientation.mirrored() ? orientation.front().getOpposite() : orientation.front();
            if(facing != Direction.EAST && facing != Direction.WEST){
                x = outputpos.getX() + (facing == Direction.SOUTH ? 0.15 : 0.85);
            }
            if(facing != Direction.NORTH && facing != Direction.SOUTH){
                z = outputpos.getZ() + (facing == Direction.WEST ? 0.15 : 0.85);
            }

            ItemEntity ei = new ItemEntity(level.getRawLevel(), x, y, z, output.copy());
            ei.setDeltaMovement(0.075 * outputdir.getStepX(), 0.025, 0.075 * outputdir.getStepZ());
            level.getRawLevel().addFreshEntity(ei);
        }
    }
}
