/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.DistillationTowerShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DistillationTowerLogic implements IMultiblockLogic<DistillationTowerLogic.State>, IServerTickableComponent<DistillationTowerLogic.State>, IClientTickableComponent<DistillationTowerLogic.State> {

    /** Input Tank ID */
    public static final int TANK_INPUT = 0;

    /** Output Tank ID */
    public static final int TANK_OUTPUT = 1;

    /** Inventory Fluid Input (Filled Bucket) */
    public static final int INV_0 = 0;

    /** Inventory Fluid Input (Empty Bucket) */
    public static final int INV_1 = 1;

    /** Inventory Fluid Output (Empty Bucket) */
    public static final int INV_2 = 2;

    /** Inventory Fluid Output (Filled Bucket) */
    public static final int INV_3 = 3;

    /** Template-Location of the Fluid Input Port. (3 0 3) */
    public static final CapabilityPosition Fluid_IN = new CapabilityPosition(3, 0, 3, RelativeBlockFace.LEFT);

    /** Template-Location of the Fluid Output Port. (1 0 3) */
    public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(1, 0, 3, RelativeBlockFace.BACK);

    /** Template-Location of the Item Output Port. (0 0 1) */
    public static final BlockPos Item_OUT = new BlockPos(0, 0, 1);

    /** Template-Location of the Energy Input Port. (3 1 3) */
    public static final CapabilityPosition ENERGY_IN = new CapabilityPosition(3, 1, 3, RelativeBlockFace.UP);

    /** Template-Location of the Redstone Input Port. (0 1 3) */
    public static final BlockPos REDSTONE_IN = new BlockPos(0, 1, 3);

    @Override
    public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
        return new DistillationTowerLogic.State(capabilitySource);
    }

    @Override
    public void tickClient(IMultiblockContext<State> context){
    }

    @Override
    public void tickServer(IMultiblockContext<DistillationTowerLogic.State> context){
        final DistillationTowerLogic.State state = context.getState();
        final IMultiblockLevel level = context.getLevel();
        final boolean rsEnabled = state.rsState.isEnabled(context);

        if(state.cooldownTicks > 0){
            state.cooldownTicks--;
        }

        boolean update = false;


        if(rsEnabled){
            if(state.energy.getEnergyStored() > 0 && state.processor.getQueueSize() < state.processor.getMaxQueueSize()){
                if(state.tanks.input().getFluidAmount() > 0){
                    DistillationTowerRecipe recipe = DistillationTowerRecipe.findRecipe(state.tanks.input().getFluid());
                    if(recipe != null && state.tanks.input().getFluidAmount() >= recipe.getInputFluid().getAmount() && state.energy.getEnergyStored() >= recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()){
                        MultiblockProcessInMachine<DistillationTowerRecipe> process = new DistillationTowerProcess(recipe, state.tanks).setInputTanks(TANK_INPUT);
                        if(state.processor.addProcessToQueue(process, level.getRawLevel(),true)){
                            state.processor.addProcessToQueue(process, level.getRawLevel(), false);
                            update = true;
                        }
                    }
                }
            }

            if(!state.processor.getQueue().isEmpty()){
                state.wasActive = true;
                state.cooldownTicks = 10;
                update = true;
            }else if(state.wasActive){
                state.wasActive = false;
                update = true;
            }
            state.processor.tickServer(state, level, state.wasActive);
        }

        if(state.inventory.get(INV_0) != ItemStack.EMPTY && state.tanks.input().getFluidAmount() < state.tanks.input().getCapacity()){
            ItemStack emptyContainer = Utils.drainFluidContainer(state.tanks.input(), state.inventory.get(INV_0), state.inventory.get(INV_1));
            if(!emptyContainer.isEmpty()){
                if(!state.inventory.get(INV_1).isEmpty() && ItemHandlerHelper.canItemStacksStack(state.inventory.get(INV_1), emptyContainer)){
                    state.inventory.get(INV_1).grow(emptyContainer.getCount());
                }else if(state.inventory.get(INV_1).isEmpty()){
                    state.inventory.set(INV_1, emptyContainer.copy());
                }

                state.inventory.get(INV_0).shrink(1);
                if(state.inventory.get(INV_0).getCount() <= 0){
                    state.inventory.set(INV_0, ItemStack.EMPTY);
                }
                update = true;
            }
        }

        if(state.tanks.output().getFluidAmount() > 0){
            if(state.inventory.get(INV_2) != ItemStack.EMPTY){

                if(state.tanks.output().getFluidTypes() > 0){
                    MultiFluidTank outTank = state.tanks.output();

                    for(int i = outTank.getFluidTypes() - 1;i >= 0;i--){
                        FluidStack fs = outTank.getFluidInTank(i);

                        if(fs.getAmount() > 0){
                            ItemStack filledContainer = FluidHelper.fillFluidContainer(outTank, fs, state.inventory.get(INV_2), state.inventory.get(INV_3));
                            if(!filledContainer.isEmpty()){
                                if(state.inventory.get(INV_3).getCount() == 1 && !FluidHelper.isFluidContainerFull(filledContainer)){
                                    state.inventory.set(INV_3, filledContainer.copy());
                                }else{
                                    if(!state.inventory.get(INV_3).isEmpty() && ItemHandlerHelper.canItemStacksStack(state.inventory.get(INV_3), filledContainer)){
                                        state.inventory.get(INV_3).grow(filledContainer.getCount());
                                    }else if(state.inventory.get(INV_3).isEmpty()){
                                        state.inventory.set(INV_3, filledContainer.copy());
                                    }

                                    state.inventory.get(INV_2).shrink(1);
                                    if(state.inventory.get(INV_2).getCount() <= 0){
                                       state.inventory.set(INV_2, ItemStack.EMPTY);
                                    }
                                }

                                update = true;
                                break;
                            }
                        }
                    }
                }
            }

            MultiblockOrientation orientation = level.getOrientation();

            BlockPos outPos = level.toAbsolute(Fluid_OUT.posInMultiblock()).relative(orientation.front().getOpposite());
            update |= FluidUtil.getFluidHandler(level.getRawLevel(), outPos, orientation.front()).map(output -> {
                boolean ret = false;
                if(state.tanks.input().fluids.size() > 0){
                    List<FluidStack> toDrain = new ArrayList<>();
                    boolean iePipe = level.getRawLevel().getBlockEntity(outPos) instanceof IFluidPipe;

                    // Tries to Output the output-fluids in parallel
                    for(FluidStack target:state.tanks.output().fluids){
                        FluidStack outStack = FluidHelper.copyFluid(target, Math.min(target.getAmount(), 100), iePipe);

                        int accepted = output.fill(outStack, IFluidHandler.FluidAction.SIMULATE);
                        if(accepted > 0){
                            int drained = output.fill(FluidHelper.copyFluid(outStack, Math.min(outStack.getAmount(), accepted), iePipe), IFluidHandler.FluidAction.EXECUTE);

                            toDrain.add(new FluidStack(target.getFluid(), drained));
                            ret = true;
                        }
                    }

                    // If this were to be done in the for-loop it would throw a concurrent exception
                    toDrain.forEach(fluid -> state.tanks.output().drain(fluid, IFluidHandler.FluidAction.EXECUTE));
                }

                return ret;
            }).orElse(false);
        }

        if(update){
            context.markDirtyAndSync();
        }
    }

    public static DistillationTowerRecipe getRecipeForId(Level level, ResourceLocation id){
        return DistillationTowerRecipe.recipes.get(id);
    }


    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        DistillationTowerLogic.State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER)
        {
            if (position.equalsOrNullFace(Fluid_IN))
                return state.fluidInput.cast(ctx);
            else if (position.equalsOrNullFace(Fluid_OUT))
                    return state.fluidOutput.cast(ctx);
        }
        else if (cap == ForgeCapabilities.ENERGY)
            if (position.equalsOrNullFace(ENERGY_IN))
                return state.energyHandler.cast(ctx);

        return LazyOptional.empty();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
        return DistillationTowerShape.GETTER;
    }

    public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillationTowerRecipe>
    {
        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public final MultiblockProcessor.InMachineProcessor<DistillationTowerRecipe> processor;

        public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
        public final Tanks tanks = new Tanks();
        public int cooldownTicks = 0;
        public boolean wasActive = false;

        private final StoredCapability<IFluidHandler> fluidInput;
        private final StoredCapability<IFluidHandler> fluidOutput;
        private final StoredCapability<IEnergyStorage> energyHandler;

        public State(IInitialMultiblockContext<State> context) {

            processor = new MultiblockProcessor.InMachineProcessor<>(
                    1, 0, 1, context.getMarkDirtyRunnable(), DistillationTowerLogic::getRecipeForId);

            fluidInput = new StoredCapability<>(ArrayFluidHandler.fillOnly(tanks.input(), context.getMarkDirtyRunnable()));
            fluidOutput = new StoredCapability<>(ArrayFluidHandler.drainOnly(tanks.output(), context.getMarkDirtyRunnable()));
            energyHandler = new StoredCapability<>(energy);
        }

        @Override
        public AveragingEnergyStorage getEnergy() {
            return energy;
        }

        @Override
        public MultiFluidTankFiltered[] getInternalTanks(){
            return this.tanks.asArray();
        }

        @Override
        public int[] getOutputTanks(){
            return new int[]{1};
        }

        @Override
        public boolean additionalCanProcessCheck(MultiblockProcess<DistillationTowerRecipe, ?> process, Level level) {
            int outputAmount = 0;
            for(FluidStack outputFluid : process.getRecipe(level).getFluidOutputs()){
                outputAmount += outputFluid.getAmount();
            }

            return this.tanks.output().getCapacity() >= (this.tanks.output().getFluidAmount() + outputAmount);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt){
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.energy.deserializeNBT(nbt.getCompound("energy"));
            this.cooldownTicks = nbt.getInt("cooldownTicks");
            this.processor.fromNBT(nbt.getCompound("recipeworker"), (getRecipe, data) -> new DistillationTowerProcess(getRecipe, data, tanks));

            this.inventory = readInventory(nbt.getCompound("inventory"));
            rsState.readSaveNBT(nbt);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt){
            nbt.put("tanks", this.tanks.writeNBT());
            nbt.put("energy", this.energy.serializeNBT());
            nbt.putInt("cooldownTicks", this.cooldownTicks);
            nbt.put("recipeworker", this.processor.toNBT());

            nbt.put("inventory", writeInventory(this.inventory));
            rsState.writeSaveNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.energy.deserializeNBT(nbt.getCompound("energy"));
            this.cooldownTicks = nbt.getInt("cooldownTicks");
            this.processor.fromNBT(nbt.getCompound("recipeworker"), (getRecipe, data) -> new DistillationTowerProcess(getRecipe, data, tanks));

            this.inventory = readInventory(nbt.getCompound("inventory"));
            rsState.readSyncNBT(nbt);
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.writeNBT());
            nbt.put("energy", this.energy.serializeNBT());
            nbt.putInt("cooldownTicks", this.cooldownTicks);
            nbt.put("recipeworker", this.processor.toNBT());

            nbt.put("inventory", writeInventory(this.inventory));
            rsState.writeSyncNBT(nbt);
        }

        protected NonNullList<ItemStack> readInventory(CompoundTag nbt){
            NonNullList<ItemStack> list = NonNullList.create();
            ContainerHelper.loadAllItems(nbt, list);

            if(list.size() == 0){ // Incase it loaded none
                list = this.inventory.size() == 4 ? this.inventory : NonNullList.withSize(4, ItemStack.EMPTY);
            }else if(list.size() < 4){ // Padding incase it loaded less than 4
                while(list.size() < 4)
                    list.add(ItemStack.EMPTY);
            }
            return list;
        }

        protected CompoundTag writeInventory(NonNullList<ItemStack> list){
            return ContainerHelper.saveAllItems(new CompoundTag(), list);
        }
    }

    public static record Tanks(MultiFluidTankFiltered input, MultiFluidTankFiltered output) implements IReadWriteNBT {
        public static final int CAPACITY = 24 * FluidType.BUCKET_VOLUME;

        public Tanks(){
            this(new MultiFluidTankFiltered(CAPACITY, fs -> DistillationTowerRecipe.findRecipe(fs) != null), new MultiFluidTankFiltered(CAPACITY));
        }

        public MultiFluidTankFiltered[] asArray(){
            return new MultiFluidTankFiltered[]{input, output};
        }

        @Override
        public void readNBT(CompoundTag nbt){
            this.input.readFromNBT(nbt.getCompound("input"));
            this.output.readFromNBT(nbt.getCompound("output"));
        }

        @Override
        public CompoundTag writeNBT(){
            CompoundTag nbt = new CompoundTag();
            nbt.put("input", this.input.writeToNBT(new CompoundTag()));
            nbt.put("output", this.output.writeToNBT(new CompoundTag()));
            return nbt;
        }
    }
}
