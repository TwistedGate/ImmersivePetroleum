/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.OilTankShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public class OilTankLogic implements IMultiblockLogic<OilTankLogic.State>, IServerTickableComponent<OilTankLogic.State>, IClientTickableComponent<OilTankLogic.State> {

    public enum PortState implements StringRepresentable {
        INPUT, OUTPUT;

        @Override
        @Nonnull
        public String getSerializedName(){
            return this.toString().toLowerCase(Locale.ENGLISH);
        }

        public Component getText(){
            return Component.translatable("desc.immersivepetroleum.info.oiltank." + getSerializedName());
        }

        public PortState next(){
            return this == INPUT ? OUTPUT : INPUT;
        }
    }

    public enum Port implements StringRepresentable{
        TOP(new CapabilityPosition(2, 2, 3, RelativeBlockFace.UP)),
        BOTTOM(new CapabilityPosition(2, 0, 3, RelativeBlockFace.DOWN)),
        DYNAMIC_A(new CapabilityPosition(0, 1, 2, RelativeBlockFace.RIGHT)),
        DYNAMIC_B(new CapabilityPosition(4, 1, 2, RelativeBlockFace.LEFT)),
        DYNAMIC_C(new CapabilityPosition(0, 1, 4, RelativeBlockFace.RIGHT)),
        DYNAMIC_D(new CapabilityPosition(4, 1, 4, RelativeBlockFace.LEFT));

        public static final Port[] DYNAMIC_PORTS = {DYNAMIC_A, DYNAMIC_B, DYNAMIC_C, DYNAMIC_D};

        public final CapabilityPosition posInMultiblock;
        Port(CapabilityPosition posInMultiblock){
            this.posInMultiblock = posInMultiblock;
        }

        public boolean matches(BlockPos posInMultiblock){
            return posInMultiblock.equals(this.posInMultiblock.posInMultiblock());
        }

        @Override
        @Nonnull
        public String getSerializedName(){
            return this.toString().toLowerCase(Locale.ENGLISH);
        }

        static Set<CapabilityPosition> toSet(Port... ports){
            ImmutableSet.Builder<CapabilityPosition> builder = ImmutableSet.builder();
            for(Port port:ports){
                builder.add(port.posInMultiblock);
            }
            return builder.build();
        }
    }

    /**
     * Template-Location of the Redstone Input Port. (2 2 5 & 2 2 2)<br>
     */
    public static final BlockPos[] Redstone_IN = new BlockPos[]{new BlockPos(2, 2, 5), new BlockPos(2, 2, 2)};

    @Override
    public State createInitialState(IInitialMultiblockContext<OilTankLogic.State> capabilitySource){
        return new OilTankLogic.State(capabilitySource);
    }

    @Override
    public void tickClient(IMultiblockContext<State> context){
    }

    @Override
    public void tickServer(IMultiblockContext<OilTankLogic.State> ctx)
    {
        int threshold = 1;

        OilTankLogic.State state = ctx.getState();
        IMultiblockLevel level = ctx.getLevel();

        PortState portStateA = state.getPortStateFor(Port.DYNAMIC_A),
                portStateB = state.getPortStateFor(Port.DYNAMIC_B),
                portStateC = state.getPortStateFor(Port.DYNAMIC_C),
                portStateD = state.getPortStateFor(Port.DYNAMIC_D);

        boolean wasBalancing = false;
        if((portStateA == PortState.OUTPUT && portStateC == PortState.INPUT) || (portStateA == PortState.INPUT && portStateC == PortState.OUTPUT)){
            wasBalancing |= state.equalize(ctx, Port.DYNAMIC_A, threshold, FluidType.BUCKET_VOLUME);
        }

        if((portStateB == PortState.OUTPUT && portStateD == PortState.INPUT) || (portStateB == PortState.INPUT && portStateD == PortState.OUTPUT)){
            wasBalancing |= state.equalize(ctx, Port.DYNAMIC_B, threshold, FluidType.BUCKET_VOLUME);
        }

        if(state.rsState.isEnabled(ctx)){
            for(Port port: Port.values()){
                if((!wasBalancing && state.getPortStateFor(port) == PortState.OUTPUT) || (wasBalancing && port == Port.BOTTOM)){
                    Direction facing = state.getPortDirection(level.getOrientation(), port);
                    BlockPos pos = level.toAbsolute(port.posInMultiblock.posInMultiblock()).relative(facing);

                    FluidUtil.getFluidHandler(level.getRawLevel(), pos, facing.getOpposite()).ifPresent(out -> {
                        if(state.tank.getFluidAmount() > 0){
                            FluidStack fs = FluidHelper.copyFluid(state.tank.getFluid(), Math.min(state.tank.getFluidAmount(), 432), false);
                            int accepted = out.fill(fs, IFluidHandler.FluidAction.SIMULATE);
                            if(accepted > 0){
                                int drained = out.fill(FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), accepted), false), IFluidHandler.FluidAction.EXECUTE);
                                state.tank.drain(Utils.copyFluidStackWithAmount(state.tank.getFluid(), drained, false), IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    });
                }
            }
        }

        ctx.markDirtyAndSync();
        state.comparatorHelper.update(ctx, state.tank.getFluidAmount());
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        final OilTankLogic.State state = ctx.getState();

        if(cap == ForgeCapabilities.FLUID_HANDLER){
            for(Port port: Port.values()){
                if(port.matches(position.posInMultiblock())){
                    return switch(state.portConfig.get(port)){
                        case INPUT -> state.fluidInput.cast(ctx);
                        case OUTPUT -> state.fluidOutput.cast(ctx);
                    };
                }
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
        return OilTankShape.GETTER;
    }

    public static class State implements IMultiblockState {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public final FluidTank tank = new FluidTank(1024 * FluidType.BUCKET_VOLUME, f -> !f.getFluid().getFluidType().isLighterThanAir());
        public final EnumMap<Port, PortState> portConfig = new EnumMap<>(Port.class);

        private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;

        private final StoredCapability<IFluidHandler> fluidInput;
        private final StoredCapability<IFluidHandler> fluidOutput;
        //private final List<CapabilityReference<IFluidHandler>>
        public State(IInitialMultiblockContext<State> context)
        {
            final BlockPos masterPos = IPContent.Multiblock.OILTANK.masterPosInMB();
            final Updater update = (ctx, layer, value) ->
            {
                final IMultiblockLevel level = ctx.getLevel();
                ctx.setComparatorOutputFor(masterPos, value);
                final BlockPos absPos = level.toAbsolute(masterPos);
                final BlockState stateAt = level.getBlockState(masterPos);
                level.getRawLevel().updateNeighborsAt(absPos, stateAt.getBlock());
            };

            this.comparatorHelper = new LayeredComparatorOutput<>(
                    this.tank.getCapacity(),
                    3,
                    (ctx, value) -> update.update(ctx, masterPos, value),
                    (ctx, layer, value) -> {
                        for(int z = -2;z <= 2;z++){
                            for(int x = -2;x <= 2;x++){
                                BlockPos pos = masterPos.offset(x, layer, z);
                                update.update(ctx, pos, value);
                            }
                        }
                    });

            for(Port port: Port.values()){
                if(port == Port.DYNAMIC_B || port == Port.DYNAMIC_C || port == Port.BOTTOM){
                    portConfig.put(port, PortState.OUTPUT);
                }else{
                    portConfig.put(port, PortState.INPUT);
                }
            }

            this.fluidInput = new StoredCapability<>(ArrayFluidHandler.fillOnly(tank, context.getMarkDirtyRunnable()));
            this.fluidOutput = new StoredCapability<>(ArrayFluidHandler.drainOnly(tank, context.getMarkDirtyRunnable()));
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt){
            nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));

            for(Port port: Port.DYNAMIC_PORTS){
                nbt.putInt(port.getSerializedName(), getPortStateFor(port).ordinal());
            }
            rsState.writeSaveNBT(nbt);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt){
            this.tank.readFromNBT(nbt.getCompound("tank"));

            for(Port port: Port.DYNAMIC_PORTS){
                portConfig.put(port, PortState.values()[nbt.getInt(port.getSerializedName())]);
            }
            rsState.readSaveNBT(nbt);
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            writeSaveNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            readSaveNBT(nbt);
        }

        public PortState getPortStateFor(Port port){
            return this.portConfig.get(port);
        }

        private boolean equalize(IMultiblockContext<OilTankLogic.State> ctx, Port port, int threshold, int maxTransfer){
            IMultiblockLevel level = ctx.getLevel();
            Direction facing = getPortDirection(level.getOrientation(), port);
            BlockPos pos = level.toAbsolute(port.posInMultiblock.posInMultiblock()).relative(facing);
            BlockEntity te = level.getRawLevel().getBlockEntity(pos);

           if(te instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper().getContext().getState() instanceof OilTankLogic.State){

               IMultiblockContext<OilTankLogic.State> otherState = multiblockBE.getHelper().asType(IPContent.Multiblock.OILTANK).getContext();

               int diff = otherState.getState().tank.getFluidAmount() - this.tank.getFluidAmount();
               int amount = Math.min(Math.abs(diff) / 2, maxTransfer);

                return (diff <= -threshold && transfer(ctx, otherState, amount)) || (diff >= threshold && transfer(otherState, ctx, amount));
            }

            return false;
        }

        private boolean transfer(IMultiblockContext<OilTankLogic.State> src, IMultiblockContext<OilTankLogic.State> dst, int amount){

            State srcState = src.getState();
            State dstState = dst.getState();

            FluidStack fs = new FluidStack(srcState.tank.getFluid(), amount);
            int accepted = dstState.tank.fill(fs, IFluidHandler.FluidAction.SIMULATE);
            if(accepted > 0){
                fs = new FluidStack(srcState.tank.getFluid(), accepted);
                dstState.tank.fill(fs, IFluidHandler.FluidAction.EXECUTE);
                srcState.tank.drain(fs, IFluidHandler.FluidAction.EXECUTE);

                src.markDirtyAndSync();
                dst.markDirtyAndSync();
                return true;
            }

            return false;
        }

        private Direction getPortDirection(MultiblockOrientation orientation, Port port){

            boolean isMirrored = orientation.mirrored();
            Direction front = orientation.front();
            switch (port) {
                case DYNAMIC_B, DYNAMIC_D -> {
                    return isMirrored ? front.getCounterClockWise() : front.getClockWise();
                }
                case DYNAMIC_A, DYNAMIC_C -> {
                    return isMirrored ? front.getClockWise() : front.getCounterClockWise();
                }
                case TOP -> {
                    return Direction.UP;
                }
                default -> {
                    return Direction.DOWN;
                }
            }
        }

        /*public boolean isLadder(){
            int x = posInMultiblock.getX();
            int z = posInMultiblock.getZ();

            return x == 3 && z == 0;
        }*/

        interface Updater
        {
            void update(IMultiblockContext<?> ctx, BlockPos pos, int value);
        }
    }
}