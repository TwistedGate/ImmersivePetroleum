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
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.client.gui.elements.PipeConfig;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.DerrickShape;
import flaxbeard.immersivepetroleum.common.blocks.stone.WellPipeBlock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DerrickLogic implements IMultiblockLogic<DerrickLogic.State>, IServerTickableComponent<DerrickLogic.State>, IClientTickableComponent<DerrickLogic.State> {
    public static final int REQUIRED_WATER_AMOUNT = 125;
    public static final int REQUIRED_CONCRETE_AMOUNT = 125;

    public enum Inventory{
        /** Item Pipe Input */
        INPUT;

        public int id(){
            return ordinal();
        }
    }

    public static final FluidTank DUMMY_TANK = new FluidTank(0);

    /** Template-Location of the Fluid Input Port. (2 0 4)<br> */
    public static final CapabilityPosition Fluid_IN = new CapabilityPosition(2, 0, 4, RelativeBlockFace.BACK);

    /** Template-Location of the Fluid Output Port. (4 0 2)<br> */
    //public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(4, 0, 2, RelativeBlockFace.LEFT);
    public static final CapabilityPosition FLUID_OUT = new CapabilityPosition(4,0, 2 , RelativeBlockFace.LEFT);

    /** Template-Location of the Energy Input Ports.<br><pre>2 1 0</pre><br> */
    public static final CapabilityPosition Energy_IN = new CapabilityPosition(2, 1, 0, RelativeBlockFace.UP);

    /** Template-Location of the Redstone Input Port. (0 1 1)<br> */
    public static final BlockPos Redstone_IN = new BlockPos(0, 1, 1);

    @Override
    public State createInitialState(IInitialMultiblockContext<DerrickLogic.State> capabilitySource){
        InitialMultiblockContext<DerrickLogic.State> capSource = (InitialMultiblockContext<DerrickLogic.State>)capabilitySource;
        return new DerrickLogic.State(capabilitySource, capSource.masterBE().getBlockPos());
    }

    private static final BlockState[] PARTICLESTATES = new BlockState[]{
            Blocks.STONE.defaultBlockState(),
            Blocks.GRANITE.defaultBlockState(),
            Blocks.GRAVEL.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.DIORITE.defaultBlockState(),
            Blocks.SAND.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
    };
    @Override
    public void tickClient(IMultiblockContext<State> context){

        final DerrickLogic.State state = context.getState();
        final IMultiblockLevel level = context.getLevel();

        //if (state.level == null)
        //    state.level = level.getRawLevel();
        //if (state.originPos == null)
        //    state.originPos = level.getAbsoluteOrigin();

        if(state.drilling)
        {
            state.rotation += 10;
            state.rotation %= 2160; // 360 * 6

            double x = (level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()).getX() + 0.5);
            double y = (level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()).getY() + 1.0);
            double z = (level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()).getZ() + 0.5);
            int r = level.getRawLevel().random.nextInt(PARTICLESTATES.length);
            for(int i = 0;i < 5;i++){
                float xa = (level.getRawLevel().random.nextFloat() - 0.5F) * 10.0F;
                float ya = 5.0F;
                float za = (level.getRawLevel().random.nextFloat() - 0.5F) * 10.0F;

                level.getRawLevel().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, PARTICLESTATES[r]), x, y, z, xa, ya, za);
            }
        }
        if(state.spilling){
            ClientProxy.spawnSpillParticles(level.getRawLevel(), level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()), state.fluidSpilled, 5, 1.25F, state.clientFlow);
        }
    }

    @Override
    public void tickServer(IMultiblockContext<DerrickLogic.State> context)
    {
        final DerrickLogic.State state = context.getState();
        final IMultiblockLevel level = context.getLevel();
        final boolean rsEnabled = state.rsState.isEnabled(context);

        if (state.level == null)
            state.level = () -> level.getRawLevel();
        if (state.originPos == null)
            state.originPos = level.getAbsoluteOrigin();

        boolean wasActive = false;
        boolean lastDrilling = state.drilling;
        boolean lastSpilling = state.spilling;
        state.drilling = state.spilling = false;

        if(level.getAbsoluteOrigin().getY() < level.getRawLevel().getSeaLevel()) {
            if (state.fluidSpilled == Fluids.EMPTY) {
                state.fluidSpilled = Fluids.WATER;
            }
            state.spilling = true;
        }
        else
        {
            WellTileEntity well = createAndGetWell(state.level, state, level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()), getInventory(state, Inventory.INPUT) != ItemStack.EMPTY);
            if(rsEnabled){
               if(state.energy.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), true) >= IPServerConfig.EXTRACTION.derrick_consumption.get()){
                   if(well != null){
                       if(well.wellPipeLength < well.getMaxPipeLength()){
                           if(well.pipes <= 0 && getInventory(state, Inventory.INPUT) != ItemStack.EMPTY){
                               ItemStack stack = getInventory(state, Inventory.INPUT);
                                    if(stack.getCount() > 0){
                                        stack.shrink(1);
                                        well.pipes = WellTileEntity.PIPE_WORTH;

                                        if(stack.getCount() <= 0){
                                            setInventory(state, Inventory.INPUT, ItemStack.EMPTY);
                                        }

                                        well.setChanged();
                                        wasActive = true;
                                    }
                               }

                               if(well.pipes > 0){
                                   final BlockPos dPos = level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB());
                                   final BlockPos wPos = well.getBlockPos();
                                   int realPipeLength = ((dPos.getY() - 1) - wPos.getY());

                                   if(well.phyiscalPipesList.size() < realPipeLength && well.wellPipeLength < realPipeLength){
                                       if(state.tank.drain(REQUIRED_CONCRETE_AMOUNT, IFluidHandler.FluidAction.SIMULATE).getAmount() >= REQUIRED_CONCRETE_AMOUNT){
                                           state.energy.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);

                                           if(advanceTimer(state)){
                                               Level world = level.getRawLevel();
                                               int y = dPos.getY() - 1;
                                               for(;y > wPos.getY();y--){
                                                   BlockPos current = new BlockPos(dPos.getX(), y, dPos.getZ());
                                                   BlockState blockState = world.getBlockState(current);

                                                   if(blockState.getBlock() == Blocks.BEDROCK || blockState.getBlock() == IPContent.Blocks.WELL.get()){
                                                       break;
                                                   }else if(!(blockState.getBlock() == IPContent.Blocks.WELL_PIPE.get() && !blockState.getValue(WellPipeBlock.BROKEN))){
                                                       world.destroyBlock(current, false);
                                                       world.setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());

                                                       well.phyiscalPipesList.add(y);

                                                       state.tank.drain(REQUIRED_CONCRETE_AMOUNT, IFluidHandler.FluidAction.EXECUTE);

                                                       well.usePipe();
                                                       break;
                                                   }
                                               }

                                               if(well.phyiscalPipesList.size() >= realPipeLength && well.wellPipeLength >= realPipeLength){
                                                   well.pastPhysicalPart = true;
                                                   well.setChanged();
                                               }
                                           }

                                           wasActive = true;
                                           state.drilling = true;
                                       }
                                   }else{
                                       if(!state.tank.getFluid().isEmpty() && state.tank.getFluid().getFluid() == ExternalModContent.getIEFluid_Concrete()){
                                           // FIXME ! This happens every now and then, and i have not yet nailed down HOW this happens.
                                           // Void excess concrete.
                                           state.tank.drain(state.tank.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
                                           wasActive = true;
                                       }
                                       if(state.tank.drain(REQUIRED_WATER_AMOUNT, IFluidHandler.FluidAction.SIMULATE).getAmount() >= REQUIRED_WATER_AMOUNT){
                                           state.energy.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);

                                           if(advanceTimer(state)){
                                               restorePhysicalPipeProgress(well, dPos, realPipeLength);

                                               state.tank.drain(REQUIRED_WATER_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
                                               well.usePipe();
                                           }

                                           wasActive = true;
                                           state.drilling = true;
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
               if(well != null && well.wellPipeLength == well.getMaxPipeLength())
                   outputReservoirFluid(level, state, level.toAbsolute(FLUID_OUT.posInMultiblock()), context);
        }

        if(state.spilling && state.fluidSpilled == Fluids.EMPTY){
            state.fluidSpilled = IPContent.Fluids.CRUDEOIL.get();
        }
        if(!state.spilling && state.fluidSpilled != Fluids.EMPTY){
            state.fluidSpilled = Fluids.EMPTY;
        }

        if(wasActive || lastDrilling != state.drilling || lastSpilling != state.spilling){
            context.markMasterDirty();
            context.requestMasterBESync();
        }
    }

    // Only accept as much Concrete and Water as needed
    private static boolean acceptsFluid(Supplier<Level> level, DerrickLogic.State state, BlockPos inPos, FluidStack fs){
        if(fs.isEmpty())
            return false;

        WellTileEntity well = createAndGetWell(level, state, inPos,false);
        if(well == null){
            return false;
        }

        final Fluid inFluid = fs.getFluid();
        final boolean isConcrete = inFluid == ExternalModContent.getIEFluid_Concrete();
        final boolean isWater = inFluid == Fluids.WATER;

        if(!isConcrete && !isWater)
            return false;

        int realPipeLength = (inPos.getY() - 1) - well.getBlockPos().getY();
        int concreteNeeded = (REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
        if(concreteNeeded > 0 && isConcrete){
            FluidStack tankFluidStack = state.tank.getFluid();

            if((!tankFluidStack.isEmpty() && inFluid != tankFluidStack.getFluid()) || tankFluidStack.getAmount() >= concreteNeeded){
                return false;
            }

            return concreteNeeded >= fs.getAmount();
        }

        if(concreteNeeded <= 0){
            int waterNeeded = REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
            if(waterNeeded > 0 && isWater){
                FluidStack tankFluidStack = state.tank.getFluid();

                if((!tankFluidStack.isEmpty() && inFluid != tankFluidStack.getFluid()) || tankFluidStack.getAmount() >= waterNeeded){
                    return false;
                }

                return waterNeeded >= fs.getAmount();
            }
        }

        return false;
    }

    public static WellTileEntity createAndGetWell(Supplier<Level> level, DerrickLogic.State state, BlockPos inPos, boolean popList){
        Level rawLevel = level.get();

        if(state.wellCache != null && state.wellCache.isRemoved()){
            state.wellCache = null;
        }

        if(state.wellCache == null){
            WellTileEntity well = null;

            for(int y = inPos.below().getY();y >= rawLevel.getMinBuildHeight() - 1;y--){
                BlockPos current = new BlockPos(inPos.getX(), y, inPos.getZ());
                BlockState blockState = rawLevel.getBlockState(current);

                if(blockState.getBlock() == IPContent.Blocks.WELL.get()){
                    well = (WellTileEntity) rawLevel.getBlockEntity(current);
                    break;
                }else if(blockState.getBlock() == Blocks.BEDROCK){
                    rawLevel.setBlockAndUpdate(current, IPContent.Blocks.WELL.get().defaultBlockState());
                    well = (WellTileEntity) rawLevel.getBlockEntity(current);
                    break;
                }
            }

            state.wellCache = well;
        }

        if(popList && state.wellCache != null && state.wellCache.tappedIslands.isEmpty()){
            if(state.gridStorage != null){
                transferGridDataToWell(inPos, state, state.wellCache);
            }else{
                state.wellCache.tappedIslands.add(Utils.toColumnPos(inPos));
                state.wellCache.setChanged();
            }
        }

        if(state.wellCache != null){
            state.wellCache.abortSelfDestructSequence();
        }

        return state.wellCache;
    }

    public ItemStack getInventory(DerrickLogic.State state, DerrickLogic.Inventory inv){
        return state.inventory.get(inv.id());
    }

    public ItemStack setInventory(DerrickLogic.State state, DerrickLogic.Inventory inv, ItemStack stack){
        return state.inventory.set(inv.id(), stack);
    }

    private boolean advanceTimer(DerrickLogic.State state){
        if(state.timer-- <= 0){
            state.timer = 10;
            return true;
        }
        return false;
    }

    public void restorePhysicalPipeProgress(@Nonnull WellTileEntity well, BlockPos dPos, int realPipeLength){
        int min = Math.min(well.wellPipeLength, realPipeLength);
        for(int i = 1;i < min;i++){
            BlockPos current = new BlockPos(dPos.getX(), dPos.getY() - i, dPos.getZ());
            BlockState blockState = well.getLevel().getBlockState(current);
            if(!(blockState.getBlock() instanceof WellPipeBlock)){
                well.getLevel().destroyBlock(current, false);
                well.getLevel().setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
            }
        }
    }

    private void outputReservoirFluid(IMultiblockLevel level, DerrickLogic.State state, BlockPos inPos, IMultiblockContext<DerrickLogic.State> ctx){
        WellTileEntity well = createAndGetWell(() -> level.getRawLevel(), state, inPos, true);
        boolean mirrored = level.getOrientation().mirrored();
        Direction front = level.getOrientation().front();
        if(well == null){
            return;
        }

        FluidStack extracted = getExtractedFluidStack(well);
        if(!extracted.isEmpty()){
            Direction facing = mirrored ? front.getCounterClockWise() : front.getClockWise();
            BlockPos outPos = level.toAbsolute(FLUID_OUT.posInMultiblock()).relative(facing, 1);
            BlockEntity target = level.getRawLevel().getBlockEntity(outPos);
            if (target != null)
            {
                boolean iePipe = level.getRawLevel().getBlockEntity(outPos) instanceof IFluidPipe;
                LazyOptional<IFluidHandler> output = target.getCapability(ForgeCapabilities.FLUID_HANDLER, mirrored ? front.getClockWise() : front.getCounterClockWise());
                state.spilling = output.map(out -> {
                    FluidStack fluid = FluidHelper.copyFluid(extracted, extracted.getAmount(), iePipe);
                    int accepted = out.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
                    if(accepted > 0){
                        int drained = out.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted), iePipe), IFluidHandler.FluidAction.EXECUTE);
                        return fluid.getAmount() - drained > 0;
                    }else{
                        return true;
                    }
                }).orElse(true);
            }
            else
                state.spilling = true;
        }

        if(state.spilling && !extracted.isEmpty() && state.fluidSpilled != extracted.getFluid()){
            state.fluidSpilled = extracted.getFluid();
        }
        if(!state.spilling && state.fluidSpilled != Fluids.EMPTY){
            state.fluidSpilled = Fluids.EMPTY;
        }
    }

    public static void transferGridDataToWell(BlockPos masterPos, DerrickLogic.State state, @Nullable WellTileEntity well){
        if(well != null){
            int additionalPipes = 0;
            List<ColumnPos> list = new ArrayList<>();
            PipeConfig.Grid grid = state.gridStorage;
            for(int j = 0;j < grid.getHeight();j++){
                for(int i = 0;i < grid.getWidth();i++){
                    int type = grid.get(i, j);

                    if(type > 0){
                        switch(type){
                            case PipeConfig.PIPE_PERFORATED:
                            case PipeConfig.PIPE_PERFORATED_FIXED:
                            {
                                int x = i - (grid.getWidth() / 2);
                                int z = j - (grid.getHeight() / 2);
                                ColumnPos pos = new ColumnPos(masterPos.getX() + x, masterPos.getZ() + z);
                                list.add(pos);
                            }
                            case PipeConfig.PIPE_NORMAL:{
                                additionalPipes++;
                            }
                        }
                    }
                }
            }

            well.tappedIslands = list;
            well.additionalPipes = additionalPipes;
            well.setChanged();
        }
    }

    private FluidStack getExtractedFluidStack(@Nonnull WellTileEntity well){
        Fluid extractedFluid = Fluids.EMPTY;
        int extractedAmount = 0;
        for(ColumnPos cPos:well.tappedIslands){
            ReservoirIsland island = ReservoirHandler.getIsland(well.getLevel(), cPos);
            if(island != null){
                if(extractedFluid == Fluids.EMPTY){
                    extractedFluid = island.getFluid();
                }else if(island.getFluid() != extractedFluid){
                    continue;
                }

                extractedAmount += island.extractWithPressure(well.getLevel(), cPos.x(), cPos.z());
            }
        }

        return new FluidStack(extractedFluid, extractedAmount);
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER)
        {
           if (position.equalsOrNullFace(Fluid_IN))
           {
               return state.fluidHandler.cast(ctx);
           }
           else if (position.equalsOrNullFace(FLUID_OUT))
               return state.emptyHandler.cast(ctx);
        }
        else if (cap == ForgeCapabilities.ENERGY)
            if (position.equalsOrNullFace(Energy_IN))
                return state.energyHandler.cast(ctx);
        return LazyOptional.empty();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
        return DerrickShape.GETTER;
    }

    // TODO
    public static class State implements IMultiblockState {

        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public int timer = 0;
        public int rotation = 0;
        public boolean drilling;
        public boolean spilling;
        private Fluid fluidSpilled = Fluids.EMPTY;
        public final FluidTank tank; //new FluidTank(8000, fluidStack -> fluidStack.getFluid().is(FluidTags.WATER) || fluidStack.getFluid().is(IETags.fluidConcrete));
        public final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        private WellTileEntity wellCache = null;

        /** Stores the current derrick configuration. */
        @Nullable
        public PipeConfig.Grid gridStorage;
        private int clientFlow;
        private Supplier<Level> level;
        public BlockPos originPos;

        private final StoredCapability<IEnergyStorage> energyHandler;
        private final StoredCapability<IFluidHandler> fluidHandler;
        private final StoredCapability<IFluidHandler> emptyHandler;
        private final StoredCapability<IItemHandler> itemHandler;

        public State(IInitialMultiblockContext<State> context, BlockPos pos)
        {
            this.energyHandler = new StoredCapability<>(energy);
            this.emptyHandler = new StoredCapability<>(ArrayFluidHandler.drainOnly(DUMMY_TANK, context.getMarkDirtyRunnable()));
            this.itemHandler = new StoredCapability<>(new ItemStackHandler(inventory));
            this.level = context.levelSupplier();
            this.originPos = pos;
            this.tank = new FluidTank(8000, fluidStack -> DerrickLogic.acceptsFluid(level,this, pos, fluidStack));
            this.fluidHandler = new StoredCapability<>(ArrayFluidHandler.fillOnly(tank, context.getMarkDirtyRunnable()));

        }

        @Override
        public void readSaveNBT(CompoundTag nbt){
            this.drilling = nbt.getBoolean("drilling");
            this.spilling = nbt.getBoolean("spilling");
            this.clientFlow = nbt.getInt("spillflow");
            try{
                this.fluidSpilled = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("spillingfluid")));
            }catch(ResourceLocationException rle){
                this.fluidSpilled = Fluids.EMPTY;
            }

            if(nbt.contains("grid", Tag.TAG_COMPOUND)){
                this.gridStorage = PipeConfig.Grid.fromCompound(nbt.getCompound("grid"));
            }

            this.tank.readFromNBT(nbt.getCompound("tank"));

            ContainerHelper.loadAllItems(nbt, this.inventory);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt){
            nbt.putBoolean("drilling", this.drilling);
            nbt.putBoolean("spilling", this.spilling);
            nbt.putInt("spillflow", getReservoirFlow());
            nbt.putString("spillingfluid", RegistryUtils.getRegistryNameOf(this.fluidSpilled).toString());

            nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));

            if(this.gridStorage != null){
                nbt.put("grid", this.gridStorage.toCompound());
            }

            ContainerHelper.saveAllItems(nbt, this.inventory);
        }
        //TODO: I don't think this is the proper method. But looks like it works.
        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            readSaveNBT(nbt);
            //this.drilling = nbt.getBoolean("drilling");
            //this.spilling = nbt.getBoolean("spilling");
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt)
        {
            writeSaveNBT(nbt);
            //nbt.putBoolean("drilling", this.drilling);
            //nbt.putBoolean("spilling", this.spilling);
        }

        private int getReservoirFlow(){
            ReservoirIsland island = ReservoirHandler.getIsland(level.get(), originPos);
            if(island == null || this.originPos.getY() < level.get().getSeaLevel())
                return 10;

            return island.getFlowFromPressure(level.get(), originPos);
        }

        public WellTileEntity getWell(IMultiblockLevel level, BlockPos inPos){
            if(this.wellCache != null && this.wellCache.isRemoved()){
                this.wellCache = null;
            }

            if(this.wellCache == null){
                Level world = level.getRawLevel();
                WellTileEntity well = null;

                for(int y = inPos.below().getY();y >= world.getMinBuildHeight();y--){
                    BlockPos current = new BlockPos(inPos.getX(), y, inPos.getZ());
                    BlockState blockState = world.getBlockState(current);

                    if(blockState.getBlock() == IPContent.Blocks.WELL.get()){
                        well = (WellTileEntity) world.getBlockEntity(current);
                        break;
                    }
                }

                this.wellCache = well;
            }

            return this.wellCache;
        }
    }
}