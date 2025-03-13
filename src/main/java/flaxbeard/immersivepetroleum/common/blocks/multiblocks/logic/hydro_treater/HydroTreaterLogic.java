/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.HydroTreaterShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Function;

// TODO
public class HydroTreaterLogic implements IMultiblockLogic<HydroTreaterLogic.State>, IServerTickableComponent<HydroTreaterLogic.State>, IClientTickableComponent<HydroTreaterLogic.State> {
    /** Primary Fluid Input Tank<br> */
    public static final int TANK_INPUT_A = 0;

    /** Secondary Fluid Input Tank<br> */
    public static final int TANK_INPUT_B = 1;

    /** Output Fluid Tank<br> */
    public static final int TANK_OUTPUT = 2;

    /** Template-Location of the Fluid Input Port. (1 0 3)<br> */
    public static final CapabilityPosition Fluid_IN_A = new CapabilityPosition(1, 0, 3, RelativeBlockFace.BACK);

    /** Template-Location of the Fluid Input Port. (2 2 1)<br> */
    public static final CapabilityPosition Fluid_IN_B = new CapabilityPosition(2, 2, 1, RelativeBlockFace.UP);

    /** Template-Location of the Fluid Output Port. (0 1 2)<br> */
    public static final MultiblockFace FLUID_OUT = new MultiblockFace(0, 1, 2, RelativeBlockFace.UP);
    public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP);

    /** Template-Location of the Item Output Port. (0 0 2)<br> */
    public static final BlockPos Item_OUT = new BlockPos(0, 0, 2);

    /** Template-Location of the Energy Input Ports. (2 2 3)<br> */
    public static final CapabilityPosition Energy_IN = new CapabilityPosition(2, 2, 3, RelativeBlockFace.UP);

    /** Template-Location of the Redstone Input Port. (0 1 3)<br> */
    public static final BlockPos Redstone_IN = new BlockPos(0, 1, 3);

    @Override
    public State createInitialState(IInitialMultiblockContext<HydroTreaterLogic.State> capabilitySource){
        return new HydroTreaterLogic.State(capabilitySource);
    }

    @Override
    public void tickClient(IMultiblockContext<State> context){
    }

    @Override
    public void tickServer(IMultiblockContext<HydroTreaterLogic.State> context)
    {
        boolean update = false;

        State state = context.getState();
        Level level = context.getLevel().getRawLevel();

        if(state.rsState.isEnabled(context)){
            if(state.energy.getEnergyStored() > 0 && state.processor.getQueueSize() < state.processor.getMaxQueueSize()){
                if(state.tanks.primary().getFluidAmount() > 0 || state.tanks.secondary().getFluidAmount() > 0){
                    HighPressureRefineryRecipe recipe = HighPressureRefineryRecipe.findRecipe(state.tanks.primary().getFluid(), state.tanks.secondary().getFluid());
                    if(recipe != null && state.energy.getEnergyStored() >= recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()){
                        if(state.tanks.primary().getFluidAmount() >= recipe.getInputFluid().getAmount() && (recipe.getSecondaryInputFluid() == null || (state.tanks.secondary().getFluidAmount() >= recipe.getSecondaryInputFluid().getAmount()))){
                            int[] inputs, inputAmounts;

                            if(recipe.getSecondaryInputFluid() != null){
                                inputs = new int[]{TANK_INPUT_A, TANK_INPUT_B};
                                inputAmounts = new int[]{recipe.getInputFluid().getAmount(), recipe.getSecondaryInputFluid().getAmount()};
                            }else{
                                inputs = new int[]{TANK_INPUT_A};
                                inputAmounts = new int[]{recipe.getInputFluid().getAmount()};
                            }

                            MultiblockProcessInMachine<HighPressureRefineryRecipe> process = new HydroTreaterProcess(recipe, state.tanks)
                                    .setInputTanks(inputs)
                                    .setInputAmounts(inputAmounts);
                            if(state.processor.addProcessToQueue(process, level, true)){
                                state.processor.addProcessToQueue(process, level, false);
                                update = true;
                            }
                        }
                    }
                }
            }
        }

        if(!state.processor.getQueue().isEmpty()){
            update = true;
            state.processor.tickServer(state, context.getLevel(), true);
        }

        if(state.tanks.output().getFluidAmount() > 0){

            BlockPos outPos = context.getLevel().toAbsolute(Fluid_OUT.posInMultiblock()).above();
            update |= FluidUtil.getFluidHandler(level, outPos, Direction.DOWN).map(output -> {
                boolean ret = false;
                FluidStack target = state.tanks.output().getFluid();
                target = FluidHelper.copyFluid(target, Math.min(target.getAmount(), 1000));

                int accepted = output.fill(target, IFluidHandler.FluidAction.SIMULATE);
                if(accepted > 0){
                    int drained = output.fill(FluidHelper.copyFluid(target, Math.min(target.getAmount(), accepted)), IFluidHandler.FluidAction.EXECUTE);

                    state.tanks.output().drain(new FluidStack(target.getFluid(), drained), IFluidHandler.FluidAction.EXECUTE);
                    ret = true;
                }

                return ret;
            }).orElse(false);
        }

        if(update){
            context.markDirtyAndSync();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();

        if(cap==ForgeCapabilities.ENERGY&&Energy_IN.equalsOrNullFace(position))
            return state.energyCap.cast(ctx);
        else if (cap == ForgeCapabilities.FLUID_HANDLER)
        {
            if (Fluid_IN_A.equalsOrNullFace(position))
            {
                return state.fluidInputMain.cast(ctx);
            } else if (Fluid_IN_B.equalsOrNullFace(position))
            {
                return state.fluidInputSecondary.cast(ctx);
            } else if (Fluid_OUT.equalsOrNullFace(position))
            {
                return state.fluidOutput.cast(ctx);
            }
        }

        return  LazyOptional.empty();
    }

    public static HighPressureRefineryRecipe getRecipeForId(Level level, ResourceLocation id){
        return HighPressureRefineryRecipe.recipes.get(id);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
        return HydroTreaterShape.GETTER;
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<HighPressureRefineryRecipe> {

        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(8000);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public final Tanks tanks = new Tanks();

        public final MultiblockProcessor.InMachineProcessor<HighPressureRefineryRecipe> processor;

        private final CapabilityReference<IFluidHandler> outputRef;
        private final StoredCapability<IFluidHandler> fluidInputMain;
        private final StoredCapability<IFluidHandler> fluidInputSecondary;
        private final StoredCapability<IFluidHandler> fluidOutput;
        private final StoredCapability<IEnergyStorage> energyCap;
        public State(IInitialMultiblockContext<State> context)
        {
            this.processor = new MultiblockProcessor.InMachineProcessor<>(
                    1, 0, 1, context.getMarkDirtyRunnable(),  HydroTreaterLogic :: getRecipeForId);

            this.outputRef = context.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, FLUID_OUT);
            this.fluidInputMain = new StoredCapability<>(ArrayFluidHandler.fillOnly(tanks.primary(), context.getMarkDirtyRunnable()));
            this.fluidInputSecondary = new StoredCapability<>(ArrayFluidHandler.fillOnly(tanks.secondary(), context.getMarkDirtyRunnable()));
            this.fluidOutput = new StoredCapability<>(ArrayFluidHandler.drainOnly(tanks.output(), context.getMarkDirtyRunnable()));
            this.energyCap = new StoredCapability<>(this.energy);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt){
            nbt.put("tanks", this.tanks.writeNBT());
            nbt.put("energy", this.energy.serializeNBT());
            nbt.put("processor", this.processor.toNBT());
            rsState.writeSaveNBT(nbt);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt){
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.energy.deserializeNBT(nbt.getCompound("energy"));
            this.processor.fromNBT(nbt.get("processor"), (getRecipe, data) -> new HydroTreaterProcess(getRecipe, data, tanks));
            rsState.readSaveNBT(nbt);
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.writeNBT());
            nbt.put("energy", this.energy.serializeNBT());
            nbt.put("processor", this.processor.toNBT());
            rsState.writeSyncNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.energy.deserializeNBT(nbt.getCompound("energy"));
            this.processor.fromNBT(nbt.get("processor"), (getRecipe, data) -> new HydroTreaterProcess(getRecipe, data, tanks));
            rsState.readSyncNBT(nbt);
        }

        @Override
        public AveragingEnergyStorage getEnergy() {
            return energy;
        }

        @Override
        public IFluidTank[] getInternalTanks() {
            return tanks.asArray();
        }

        @Override
        public int[] getOutputTanks() {
            return new int[]{HydroTreaterLogic.TANK_OUTPUT};
        }

        @Override
        public boolean additionalCanProcessCheck(MultiblockProcess<HighPressureRefineryRecipe, ?> process, Level level)
        {
            int outputAmount = 0;
            for(FluidStack outputFluid : process.getRecipe(level).getFluidOutputs()){
                outputAmount += outputFluid.getAmount();
            }

            return this.tanks.output().getCapacity() >= (this.tanks.output().getFluidAmount() + outputAmount);
        }
    }



    public static record Tanks(FluidTank primary, FluidTank secondary, FluidTank output) implements IReadWriteNBT {
        public Tanks(){
            this(new FluidTank(12000, fluidStack -> HighPressureRefineryRecipe.hasRecipeWithInput(fluidStack, true)),
                    new FluidTank(12000, fluidStack -> HighPressureRefineryRecipe.hasRecipeWithSecondaryInput(fluidStack, true)),
                    new FluidTank(12000));
        }

        public IFluidTank[] asArray(){
            return new IFluidTank[]{primary(), secondary(), output()};
        }

        @Override
        public void readNBT(CompoundTag nbt){
            this.primary.readFromNBT(nbt.getCompound("primary"));
            this.secondary.readFromNBT(nbt.getCompound("secondary"));
            this.output.readFromNBT(nbt.getCompound("output"));
        }

        @Override
        public CompoundTag writeNBT(){
            CompoundTag nbt = new CompoundTag();
            nbt.put("primary", this.primary.writeToNBT(new CompoundTag()));
            nbt.put("secondary", this.secondary.writeToNBT(new CompoundTag()));
            nbt.put("output", this.output.writeToNBT(new CompoundTag()));
            return nbt;
        }
    }
}