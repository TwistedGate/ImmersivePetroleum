/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.PumpjackShape;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellPipeTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PumpjackLogic implements IMultiblockLogic<PumpjackLogic.State>, IServerTickableComponent<PumpjackLogic.State>, IClientTickableComponent<PumpjackLogic.State> {

    /** Template-Location of the Energy Input Port. (0, 1, 5) */
    public static final BlockPos REDSTONE_IN = new BlockPos(0, 1, 5);

    /** Template-Location of the Redstone Input Port. (2, 1, 5) */
    public static final CapabilityPosition ENERGY_IN = new CapabilityPosition(2, 1, 5, RelativeBlockFace.UP);

    /** Template-Location of the Eastern Fluid Output Port. (2, 0, 2) */
    public static final MultiblockFace EAST_PORT_OFFSET = new MultiblockFace(3, 0, 2, RelativeBlockFace.RIGHT);
    public static final CapabilityPosition EAST_PORT = CapabilityPosition.opposing(EAST_PORT_OFFSET);

    /** Template-Location of the Western Fluid Output Port. (0, 0, 2) */
    public static final MultiblockFace WEST_PORT_OFFSET = new MultiblockFace(-1, 0, 2, RelativeBlockFace.LEFT);
    public static final CapabilityPosition WEST_PORT = CapabilityPosition.opposing(WEST_PORT_OFFSET);

    /** Template-Location of the Bottom Fluid Output Port. (1, 0, 0) <b>(Also Master Block)</b> */
    public static final BlockPos DOWN_PORT = new BlockPos(1, 0, 0);

    @Override
    public PumpjackLogic.State createInitialState(IInitialMultiblockContext<PumpjackLogic.State> capabilitySource){
        return new PumpjackLogic.State(capabilitySource);
    }

    @Override
    public void tickClient(IMultiblockContext<State> context){
        final PumpjackLogic.State state = context.getState();

        if(state.wasActive){
            state.activeTicks++;
        }
    }

    @Override
    public void tickServer(IMultiblockContext<PumpjackLogic.State> context){
        final PumpjackLogic.State state = context.getState();
        final IMultiblockLevel level = context.getLevel();
        final boolean rsEnabled = state.rsState.isEnabled(context);

        boolean active = false;
        if(rsEnabled){
            BlockEntity teLow = level.getBlockEntity(DOWN_PORT.below());

            if(teLow instanceof WellPipeTileEntity pipe){
                WellTileEntity well = pipe.getWell();

                if(well != null){
                    int consumption = IPServerConfig.EXTRACTION.pumpjack_consumption.get();
                    int extracted = state.energy.extractEnergy(consumption, true);

                    if(extracted >= consumption){
                        // Does any island still have pressure?
                        boolean foundPressurizedIsland = false;
                        for(ColumnPos cPos:well.tappedIslands){
                            ReservoirIsland island = ReservoirHandler.getIsland(level.getRawLevel(), cPos);

                            if(island != null && island.getPressure(level.getRawLevel(), cPos.x(), cPos.z()) > 0.0F){
                                foundPressurizedIsland = true;
                                break;
                            }
                        }

                        // Skip if there is (Simulates pumpjack not being able to handle high pressures)
                        if(!foundPressurizedIsland){
                            int extractSpeed = IPServerConfig.EXTRACTION.pumpjack_speed.get();

                            IFluidHandler portEast_output = state.east_port_output.getNullable();
                            IFluidHandler portWest_output = state.west_port_output.getNullable();

                            for(ColumnPos cPos:well.tappedIslands){
                                ReservoirIsland island = ReservoirHandler.getIsland(level.getRawLevel(), cPos);

                                if(island != null){
                                    FluidStack fluid = new FluidStack(island.getFluid(), island.extract(extractSpeed, IFluidHandler.FluidAction.SIMULATE));

                                    if(portEast_output != null){

                                        int accepted = portEast_output.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
                                        if(accepted > 0){
                                            int drained = portEast_output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted)), IFluidHandler.FluidAction.EXECUTE);
                                            island.extract(drained, IFluidHandler.FluidAction.EXECUTE);
                                            fluid = FluidHelper.copyFluid(fluid, fluid.getAmount() - drained);
                                            active = true;
                                        }
                                    }

                                    if(portWest_output != null && fluid.getAmount() > 0){
                                        int accepted = portWest_output.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
                                        if(accepted > 0){
                                            int drained = portWest_output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted)), IFluidHandler.FluidAction.EXECUTE);
                                            island.extract(drained, IFluidHandler.FluidAction.EXECUTE);
                                            active = true;
                                        }
                                    }
                                }
                            }

                            if(active){
                                state.energy.extractEnergy(consumption, false);
                                state.activeTicks++;
                            }
                        }
                    }
                }
            }
        }

        if(active != state.wasActive){
            context.markMasterDirty();
            context.requestMasterBESync();
        }
        state.wasActive = active;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        final State state = ctx.getState();
        final IMultiblockLevel level = ctx.getLevel();

        if(cap == ForgeCapabilities.FLUID_HANDLER){
            // East Port
            if(position.equalsOrNullFace(EAST_PORT)){
                return state.fakeFluidHandler.cast(ctx);

            }
            // West Port
            if(position.equalsOrNullFace(WEST_PORT)){
                return state.fakeFluidHandler.cast(ctx);
            }
        }

        if(cap == ForgeCapabilities.ENERGY)
        {
            if (ENERGY_IN.equalsOrNullFace(position))
            {
                return state.energyStorage.cast(ctx);
            }
        }

        return LazyOptional.empty();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
        return PumpjackShape.GETTER;
    }

    public static class State implements IMultiblockState {
        public static final FluidTank FAKE_TANK = new FluidTank(0);
        private final StoredCapability<IFluidHandler> fakeFluidHandler = new StoredCapability<>(FAKE_TANK);

        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
        private final StoredCapability<IEnergyStorage> energyStorage = new StoredCapability<>(energy);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public boolean wasActive = false;
        public float activeTicks = 0;

        private final CapabilityReference<@Nullable IFluidHandler> east_port_output;
        private final CapabilityReference<@Nullable IFluidHandler> west_port_output;
        public State(IInitialMultiblockContext<State> context){
            this.east_port_output = context.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, EAST_PORT_OFFSET);
            this.west_port_output = context.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, WEST_PORT_OFFSET);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt){
            nbt.putBoolean("wasActive", this.wasActive);
            nbt.put("energy", energy.serializeNBT());
            rsState.writeSaveNBT(nbt);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt){
            boolean lastActive = this.wasActive;
            this.wasActive = nbt.getBoolean("wasActive");
            if(!this.wasActive && lastActive){
                this.activeTicks++;
            }
            energy.deserializeNBT(nbt.getCompound("energy"));
            rsState.readSaveNBT(nbt);
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("wasActive", this.wasActive);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            boolean lastActive = this.wasActive;
            this.wasActive = nbt.getBoolean("wasActive");
            if(!this.wasActive && lastActive){
                this.activeTicks++;
            }
        }
    }
}
