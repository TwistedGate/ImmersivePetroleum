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
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.CokerShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

// TODO
public class CokerUnitLogic implements IMultiblockLogic<CokerUnitLogic.State>, IServerTickableComponent<CokerUnitLogic.State>, IClientTickableComponent<CokerUnitLogic.State> {

    public enum Inventory{
        /** Inventory Item Input */
        INPUT,
        /** Inventory Fluid Input (Filled Bucket) */
        INPUT_FILLED,
        /** Inventory Fluid Input (Empty Bucket) */
        INPUT_EMPTY,
        /** Inventory Fluid Output (Empty Bucket) */
        OUTPUT_EMPTY,
        /** Inventory Fluid Output (Filled Bucket) */
        OUTPUT_FILLED;

        public int id(){
            return ordinal();
        }
    }

    /** Input Fluid Tank<br> */
    public static final int TANK_INPUT = 0;

    /** Output Fluid Tank<br> */
    public static final int TANK_OUTPUT = 1;

    /** Coker Chamber A<br> */
    public static final int CHAMBER_A = 0;

    /** Coker Chamber B<br> */
    public static final int CHAMBER_B = 1;

    /** Template-Location of the Chamber A Item Output */
    public static final CapabilityPosition Chamber_A_OUT = new CapabilityPosition(2, 0, 2, RelativeBlockFace.BACK);

    /** Template-Location of the Chamber B Item Output */
    public static final CapabilityPosition Chamber_B_OUT = new CapabilityPosition(6, 0, 2, RelativeBlockFace.BACK);

    /** Template-Location of the Fluid Input Port. (2 0 4)<br> */
    public static final CapabilityPosition Fluid_IN = new CapabilityPosition(2, 0, 4, RelativeBlockFace.BACK);

    /** Template-Location of the Fluid Output Port. (5 0 4)<br> */
    public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(5, 0, 4, RelativeBlockFace.BACK);

    /** Template-Location of the Item Input Port. (3 0 4)<br> */
    public static final CapabilityPosition Item_IN = new CapabilityPosition(3, 0, 4, RelativeBlockFace.BACK);

    /** Template-Location of the Energy Input Ports.<br><pre>1 1 0<br>2 1 0<br>3 1 0</pre><br> */
    public static final CapabilityPosition[] Energy_IN = new CapabilityPosition[]{
            new CapabilityPosition(6, 1, 4, RelativeBlockFace.BACK),
            new CapabilityPosition(7, 1, 4, RelativeBlockFace.BACK)
    };

    /** Template-Location of the Redstone Input Port. (6 1 4)<br> */
    public static final BlockPos Redstone_IN = new BlockPos(6, 1, 4);

    @Override
    public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
        InitialMultiblockContext<CokerUnitLogic.State> capSource = (InitialMultiblockContext<CokerUnitLogic.State>)capabilitySource;
        return new CokerUnitLogic.State(capabilitySource, capSource.masterBE().getBlockPos());
    }

    @Override
    public void tickClient(IMultiblockContext<State> context)
    {
        State state = context.getState();
        IMultiblockLevel level = context.getLevel();

        if(!state.rsState.isEnabled(context)){
            return;
        }

        CokingChamber[] chambers = new CokingChamber[]{state.chambers.primary(), state.chambers.secondary()};

        boolean debug = false;
        for(int i = 0; i < chambers.length; i++)
        {
            if(debug || chambers[i].getState() == CokingState.DUMPING)
            {
                BlockPos cOutPos = level.toAbsolute(i == 0 ? Chamber_A_OUT.posInMultiblock() : Chamber_B_OUT.posInMultiblock());
                Vec3 origin = new Vec3(cOutPos.getX() + 0.5, cOutPos.getY() + 2.125, cOutPos.getZ() + 0.5);
                for(int j = 0; j < 10; j++){
                    double rX = (Math.random() - 0.5) * 0.4;
                    double rY = (Math.random() - 0.5) * 0.5;
                    double rdx = (Math.random() - 0.5) * 0.10;
                    double rdy = (Math.random() - 0.5) * 0.10;

                    level.getRawLevel().addParticle(ParticleTypes.SMOKE,
                            origin.x + rX, origin.y, origin.z + rY,
                            rdx, -(Math.random() * 0.06 + 0.11), rdy);
                }
            }
        }
    }

    @Override
    public void tickServer(IMultiblockContext<CokerUnitLogic.State> context){
        final CokerUnitLogic.State state = context.getState();
        final IMultiblockLevel level = context.getLevel();
        final boolean rsEnabled = state.rsState.isEnabled(context);
        final CokingChamber[] chambers = new CokingChamber[]{state.chambers.primary(), state.chambers.secondary()};

        boolean update = false;

        if(rsEnabled){
            ItemStack inputStack = state.getInventory(Inventory.INPUT);
            FluidStack inputFluid = state.bufferTanks.input().getFluid();

            if(!inputStack.isEmpty() && inputFluid.getAmount() > 0 && CokerUnitRecipe.hasRecipeWithInput(inputStack, inputFluid)){
                CokerUnitRecipe recipe = CokerUnitRecipe.findRecipe(inputStack, inputFluid);

                if(recipe != null && inputStack.getCount() >= recipe.inputItem.getCount() && inputFluid.getAmount() >= recipe.inputFluid.getAmount()){
                    for(CokingChamber chamber : chambers){
                        boolean skipNext = false;

                        switch(chamber.getState()){
                            case STANDBY -> {
                                if(chamber.setRecipe(recipe)){
                                    update = true;
                                    skipNext = true;
                                }
                            }
                            case PROCESSING -> {
                                int acceptedStack = chamber.addStack(state.copyStack(inputStack, recipe.inputItem.getCount()), true);
                                if(acceptedStack >= recipe.inputItem.getCount()){
                                    acceptedStack = Math.min(acceptedStack, inputStack.getCount());

                                    chamber.addStack(state.copyStack(inputStack, acceptedStack), false);
                                    inputStack.shrink(acceptedStack);

                                    skipNext = true;
                                    update = true;
                                }
                            }
                            default -> {
                            }
                        }

                        if(skipNext){
                            break;
                        }
                    }
                }
            }

            for(int i = 0;i < chambers.length;i++){
                update |= chambers[i].tick(context, i);
            }
        }

        if(!state.getInventory(Inventory.INPUT_FILLED).isEmpty() && state.bufferTanks.input().getFluidAmount() < state.bufferTanks.input().getCapacity()){
            ItemStack container = Utils.drainFluidContainer(state.bufferTanks.input(), state.getInventory(Inventory.INPUT_FILLED), state.getInventory(Inventory.INPUT_EMPTY));
            if(!container.isEmpty()){
                if(!state.getInventory(Inventory.INPUT_EMPTY).isEmpty() && ItemHandlerHelper.canItemStacksStack(state.getInventory(Inventory.INPUT_EMPTY), container)){
                    state.getInventory(Inventory.INPUT_EMPTY).grow(container.getCount());
                }else if(state.getInventory(Inventory.INPUT_EMPTY).isEmpty()){
                    state.setInventory(Inventory.INPUT_EMPTY, container.copy());
                }

                state.getInventory(Inventory.INPUT_FILLED).shrink(1);
                if(state.getInventory(Inventory.INPUT_FILLED).getCount() <= 0){
                    state.setInventory(Inventory.INPUT_FILLED, ItemStack.EMPTY);
                }

                update = true;
            }
        }

        if(state.bufferTanks.output().getFluidAmount() > 0){
            if(!state.getInventory(Inventory.OUTPUT_EMPTY).isEmpty()){
                ItemStack filledContainer = FluidHelper.fillFluidContainer(state.bufferTanks.output(), state.getInventory(Inventory.OUTPUT_EMPTY), state.getInventory(Inventory.OUTPUT_FILLED), null);
                if(!filledContainer.isEmpty()){

                    if(state.getInventory(Inventory.OUTPUT_FILLED).getCount() == 1 && !FluidHelper.isFluidContainerFull(filledContainer)){
                        state.setInventory(Inventory.OUTPUT_FILLED, filledContainer.copy());
                    }else{
                        if(!state.getInventory(Inventory.OUTPUT_FILLED).isEmpty() && ItemHandlerHelper.canItemStacksStack(state.getInventory(Inventory.OUTPUT_FILLED), filledContainer)){
                           state.getInventory(Inventory.OUTPUT_FILLED).grow(filledContainer.getCount());
                        }else if(state.getInventory(Inventory.OUTPUT_FILLED).isEmpty()){
                           state.setInventory(Inventory.OUTPUT_FILLED, filledContainer.copy());
                        }

                        state.getInventory(Inventory.OUTPUT_EMPTY).shrink(1);
                        if(state.getInventory(Inventory.OUTPUT_EMPTY).getCount() <= 0){
                            state.setInventory(Inventory.OUTPUT_EMPTY, ItemStack.EMPTY);
                        }
                    }

                    update = true;
                }
            }

            BlockPos outPos = level.toAbsolute(Fluid_OUT.posInMultiblock()).relative(level.getOrientation().front().getOpposite());
            update |= FluidUtil.getFluidHandler(level.getRawLevel(), outPos, level.getOrientation().front()).map(out -> {
                if(state.bufferTanks.output().getFluidAmount() > 0){
                    FluidStack fs = state.bufferTanks.output().getFluid();
                    fs = FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), 250));
                    int accepted = out.fill(fs, IFluidHandler.FluidAction.SIMULATE);
                    if(accepted > 0){
                        boolean iePipe = level.getBlockEntity(outPos) instanceof IFluidPipe;
                        int drained = out.fill(FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), accepted), iePipe), IFluidHandler.FluidAction.EXECUTE);
                        state.bufferTanks.output().drain(FluidHelper.copyFluid(fs, drained), IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }

        if(update){
            context.markDirtyAndSync();
        }

        updateComparatorOutput(context);

    }

    private void updateComparatorOutput(IMultiblockContext<State> context){
        boolean update = false;
        State state = context.getState();


        ItemStack stack = state.getInventory(Inventory.INPUT);
        if(!stack.isEmpty()){
            int compared = Mth.clamp(Mth.floor(stack.getCount() / (float) Math.min(64, stack.getMaxStackSize()) * 15), 0, 15);
            if(compared != state.lastCompared){
                state.lastCompared = compared;
                update = true;
            }
        }else if(state.lastCompared != 0){
            state.lastCompared = 0;
            update = true;
        }

        if(update)
        {
            BlockPos p = context.getLevel().toAbsolute(Redstone_IN);
            context.getLevel().getRawLevel().updateNeighborsAt(p, context.getLevel().getBlockState(p).getBlock());
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
    {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ITEM_HANDLER)
        {
            if (position.equalsOrNullFace(Item_IN))
            {
               return state.itemInput.cast(ctx);
            }
        }
        else if (cap == ForgeCapabilities.FLUID_HANDLER)
        {
            if (position.equalsOrNullFace(Fluid_OUT))
                return state.fluidOutput.cast(ctx);
            else if (position.equalsOrNullFace(Fluid_IN))
                return state.fluidInput.cast(ctx);
        } else if (cap == ForgeCapabilities.ENERGY)
        {
            if (position.equalsOrNullFace(Energy_IN[0]) || position.equalsOrNullFace(Energy_IN[1]))
                return state.energyCap.cast(ctx);
        }

        return LazyOptional.empty();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
        return CokerShape.GETTER;
    }

    // TODO
    public static class State implements IMultiblockState {

        public final AveragingEnergyStorage energy = new AveragingEnergyStorage(24000);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        int lastCompared = 0;

        public final NonNullList<ItemStack> inventory = NonNullList.withSize(Inventory.values().length, ItemStack.EMPTY);
        public final BufferTanks bufferTanks = new BufferTanks();
        public final Chambers chambers = new Chambers();

        private final StoredCapability<IItemHandler> itemInput;
        private final StoredCapability<IFluidHandler> fluidInput;
        private final StoredCapability<IFluidHandler> fluidOutput;
        private final StoredCapability<IEnergyStorage> energyCap;
        public BlockPos masterPos;
        public State(IInitialMultiblockContext<State> context, BlockPos pos)
        {
            masterPos = pos;
            itemInput = new StoredCapability<>(new ItemStackHandler(inventory));
            energyCap = new StoredCapability<>(this.energy);
            fluidInput = new StoredCapability<>(ArrayFluidHandler.fillOnly(bufferTanks.input(), context.getMarkDirtyRunnable()));
            fluidOutput = new StoredCapability<>(ArrayFluidHandler.drainOnly(bufferTanks.output(), context.getMarkDirtyRunnable()));
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt){
            nbt.put("buffertanks", this.bufferTanks.writeNBT());
            nbt.put("chambers", this.chambers.writeNBT());
            nbt.put("energy", this.energy.serializeNBT());
            nbt.put("inventory", writeInventory(this.inventory));
            rsState.writeSaveNBT(nbt);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt){
            this.bufferTanks.readNBT(nbt.getCompound("buffertanks"));
            this.chambers.readNBT(nbt.getCompound("chambers"));
            readInventory(nbt.getCompound("inventory"));
            this.energy.deserializeNBT(nbt.getCompound("energy"));
            rsState.readSaveNBT(nbt);
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.put("buffertanks", this.bufferTanks.writeNBT());
            nbt.put("chambers", this.chambers.writeNBT());
            nbt.put("energy", this.energy.serializeNBT());
            nbt.put("inventory", writeInventory(this.inventory));
            rsState.writeSyncNBT(nbt);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt)
        {
            this.bufferTanks.readNBT(nbt.getCompound("buffertanks"));
            this.chambers.readNBT(nbt.getCompound("chambers"));
            readInventory(nbt.getCompound("inventory"));
            this.energy.deserializeNBT(nbt.getCompound("energy"));
            rsState.readSyncNBT(nbt);
        }

        protected void readInventory(CompoundTag nbt){
            NonNullList<ItemStack> list = NonNullList.create();
            ContainerHelper.loadAllItems(nbt, list);

            for(int i = 0;i < this.inventory.size();i++){
                ItemStack stack = ItemStack.EMPTY;
                if(i < list.size()){
                    stack = list.get(i);
                }

                this.inventory.set(i, stack);
            }
        }

        protected CompoundTag writeInventory(NonNullList<ItemStack> list){
            return ContainerHelper.saveAllItems(new CompoundTag(), list);
        }

        public ItemStack getInventory(Inventory inv){
            return this.inventory.get(inv.id());
        }

        public ItemStack setInventory(Inventory inv, ItemStack stack){
            return this.inventory.set(inv.id(), stack);
        }

        public ItemStack copyStack(ItemStack stack, int amount){
            ItemStack copy = stack.copy();
            copy.setCount(amount);
            return copy;
        }
    }

    public static record BufferTanks(FluidTank input, FluidTank output){
        public BufferTanks(){
            this(new FluidTank(16000), new FluidTank(16000));
        }

        public void readNBT(CompoundTag nbt){
            this.input.readFromNBT(nbt.getCompound("input"));
            this.output.readFromNBT(nbt.getCompound("output"));
        }

        public CompoundTag writeNBT(){
            CompoundTag nbt = new CompoundTag();
            nbt.put("input", this.input.writeToNBT(new CompoundTag()));
            nbt.put("output", this.output.writeToNBT(new CompoundTag()));
            return nbt;
        }

        public FluidTank[] asArray ()
        {
            return new FluidTank[]{input, output};
        }
    }

    public static record Chambers(CokingChamber primary, CokingChamber secondary) implements IReadWriteNBT{
        public Chambers(){
            this(new CokingChamber(64, 8000), new CokingChamber(64, 8000));
        }

        protected void tick(){
            this.primary.tick(null, CHAMBER_A);
            this.secondary.tick(null, CHAMBER_B);
        }

        public CokingChamber[] asArray()
        {
            return new CokingChamber[]{primary(), secondary()};
        }

        @Override
        public void readNBT(CompoundTag nbt){
            this.primary.readFromNBT(nbt.getCompound("primary"));
            this.secondary.readFromNBT(nbt.getCompound("secondary"));
        }

        @Override
        public CompoundTag writeNBT(){
            CompoundTag nbt = new CompoundTag();
            nbt.put("primary", this.primary.writeToNBT(new CompoundTag()));
            nbt.put("secondary", this.secondary.writeToNBT(new CompoundTag()));
            return nbt;
        }
    }

    public static enum CokingState{
        /** Wait for Input */
        STANDBY,

        /** Process materials into the result */
        PROCESSING,

        /** Draining residual fluids from processing materials */
        DRAIN_RESIDUE,

        /** Filling up the chamber with fluid, with the amount required by the recipe */
        FLOODING,

        /** Dumping the result below the chamber output and voiding the flushing fluids */
        DUMPING;

        public int id(){
            return ordinal();
        }
    }

    public static class CokingChamber{
        @Nullable
        CokerUnitRecipe recipe = null;
        CokingState state = CokingState.STANDBY;
        FluidTank tank;

        /** Total capacity. inputAmount + outputAmount, should not go above this */
        int capacity;
        /** This has a ratio of X:1 to the input amount. (X amount of items always adds 1) */
        int inputAmount = 0;
        /** This has a ratio of 1:1 to the output amount. */
        int outputAmount = 0;

        int timer = 0;

        public CokingChamber(int itemCapacity, int fluidCapacity){
            this.capacity = itemCapacity;
            this.tank = new FluidTank(fluidCapacity);
        }

        public CokingChamber readFromNBT(CompoundTag nbt){
            this.tank.readFromNBT(nbt.getCompound("tank"));
            this.timer = nbt.getInt("timer");
            this.inputAmount = nbt.getInt("input");
            this.outputAmount = nbt.getInt("output");
            this.state = CokingState.values()[nbt.getInt("state")];

            if(nbt.contains("recipe", Tag.TAG_STRING)){
                try{
                    this.recipe = CokerUnitRecipe.recipes.get(new ResourceLocation(nbt.getString("recipe")));
                }catch(ResourceLocationException e){
                    ImmersivePetroleum.log.error("Tried to load a coking recipe with an invalid name", e);
                }
            }else{
                this.recipe = null;
            }

            return this;
        }

        public CompoundTag writeToNBT(CompoundTag nbt){
            nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));
            nbt.putInt("timer", this.timer);
            nbt.putInt("input", this.inputAmount);
            nbt.putInt("output", this.outputAmount);
            nbt.putInt("state", this.state.id());

            if(this.recipe != null){
                nbt.putString("recipe", this.recipe.getId().toString());
            }

            return nbt;
        }

        /** Returns true when the recipe has been set, false if it already is set and the chamber is working */
        public boolean setRecipe(@Nullable CokerUnitRecipe recipe){
            if(state == CokingState.STANDBY){
                this.recipe = recipe;
                return true;
            }

            return false;
        }

        /** Always returns 0 if the recipe hasnt been set yet, otherwise it pretty much does what you'd expect it to */
        public int addStack(@Nonnull ItemStack stack, boolean simulate){
            if(this.recipe != null && !stack.isEmpty() && this.recipe.inputItem.test(stack)){
                int capacity = getCapacity() * recipe.inputItem.getCount();
                int current = getTotalAmount() * recipe.inputItem.getCount();

                if(simulate){
                    return Math.min(capacity - current, stack.getCount());
                }

                int filled = capacity - current;
                if(stack.getCount() < filled){
                    filled = stack.getCount();
                }
                this.inputAmount++;

                return filled;
            }

            return 0;
        }

        public CokingState getState(){
            return this.state;
        }

        public int getCapacity(){
            return this.capacity;
        }

        public int getInputAmount(){
            return this.inputAmount;
        }

        public int getOutputAmount(){
            return this.outputAmount;
        }

        /** returns the combined I/O Amount */
        public int getTotalAmount(){
            return this.inputAmount + this.outputAmount;
        }

        public int getTimer(){
            return this.timer;
        }

        private boolean setStage(CokingState state){
            if(this.state != state){
                this.state = state;
                return true;
            }
            return false;
        }

        @Nullable
        public CokerUnitRecipe getRecipe(){
            return this.recipe;
        }

        /** Expected input. */
        public ItemStack getInputItem(){
            if(this.recipe == null){
                return ItemStack.EMPTY;
            }
            return this.recipe.inputItem.getMatchingStacks()[0];
        }

        /** Expected output. */
        public ItemStack getOutputItem(){
            if(this.recipe == null){
                return ItemStack.EMPTY;
            }

            return this.recipe.outputItem.copy();
        }

        public FluidTank getTank(){
            return this.tank;
        }

        /** returns true when the coker should update, false otherwise */
        public boolean tick(IMultiblockContext<State> context , int chamberId){
            if(this.recipe == null)
            {
                return setStage(CokingState.STANDBY);
            }

            CokerUnitLogic.State logicState = context.getState();

            switch(this.state){
                case STANDBY -> {
                    if(this.recipe != null){
                        return setStage(CokingState.PROCESSING);
                    }
                }
                case PROCESSING -> {
                    if(this.inputAmount > 0 && !getInputItem().isEmpty() && (this.tank.getCapacity() - this.tank.getFluidAmount()) >= this.recipe.outputFluid.getAmount()){
                        if(logicState.energy.getEnergyStored() >= this.recipe.getTotalProcessEnergy() / this.recipe.getTotalProcessTime()){
                            logicState.energy.extractEnergy(this.recipe.getTotalProcessEnergy() / this.recipe.getTotalProcessTime(), false);

                            this.timer++;
                            if(this.timer >= (this.recipe.getTotalProcessTime() * this.recipe.inputItem.getCount())){
                                this.timer = 0;

                                this.tank.fill(Utils.copyFluidStackWithAmount(this.recipe.outputFluid, this.recipe.outputFluid.getAmount(), false), IFluidHandler.FluidAction.EXECUTE);
                                this.inputAmount--;
                                this.outputAmount++;

                                if(this.inputAmount <= 0){
                                    setStage(CokingState.DRAIN_RESIDUE);
                                }
                            }

                            return true;
                        }
                    }
                }
                case DRAIN_RESIDUE -> {
                    if(this.tank.getFluidAmount() > 0){
                        FluidTank buffer = logicState.bufferTanks.output();
                        FluidStack drained = this.tank.drain(25, IFluidHandler.FluidAction.SIMULATE);

                        int accepted = buffer.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                        if(accepted > 0){
                            int amount = Math.min(drained.getAmount(), accepted);

                            this.tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                            buffer.fill(Utils.copyFluidStackWithAmount(drained, amount, false), IFluidHandler.FluidAction.EXECUTE);

                            return true;
                        }
                    }else{
                        return setStage(CokingState.FLOODING);
                    }
                }
                case FLOODING -> {
                    this.timer++;
                    if(this.timer >= 2){
                        this.timer = 0;

                        int max = getTotalAmount() * this.recipe.inputFluid.getAmount();
                        if(this.tank.getFluidAmount() < max){
                            FluidStack accepted = logicState.bufferTanks.input().drain(this.recipe.inputFluid.getAmount(), IFluidHandler.FluidAction.SIMULATE);
                            if(accepted.getAmount() >= this.recipe.inputFluid.getAmount()){
                                logicState.bufferTanks.input().drain(this.recipe.inputFluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                                this.tank.fill(accepted, IFluidHandler.FluidAction.EXECUTE);
                            }
                        }else if(this.tank.getFluidAmount() >= max){
                            return setStage(CokingState.DUMPING);
                        }
                    }
                }
                case DUMPING -> {
                    boolean update = false;

                    this.timer++;
                    if(this.timer >= 5){ // Output speed will always be fixed
                        this.timer = 0;

                        if(this.outputAmount > 0){
                            IMultiblockLevel multiLevel = context.getLevel();
                            Level world = multiLevel.getRawLevel();
                            int amount = Math.min(this.outputAmount, 1);
                            ItemStack copy = this.recipe.outputItem.copy();
                            copy.setCount(amount);

                            // Drop item(s) at the designated chamber output location
                            BlockPos itemOutPos = multiLevel.toAbsolute(chamberId == 0 ? Chamber_A_OUT.posInMultiblock() : Chamber_B_OUT.posInMultiblock());
                            Vec3 center = new Vec3(itemOutPos.getX() + 0.5, itemOutPos.getY() - 0.5, itemOutPos.getZ() + 0.5);
                            ItemEntity ent = new ItemEntity(world, center.x, center.y, center.z, copy);
                            ent.setDeltaMovement(0.0, 0.0, 0.0); // Any movement has the potential to end with the stack bouncing all over the place
                            world.addFreshEntity(ent);
                            this.outputAmount -= amount;

                            update = true;
                        }
                    }

                    // Void washing fluid
                    if(this.tank.getFluidAmount() > 0){
                        this.tank.drain(25, IFluidHandler.FluidAction.EXECUTE);

                        update = true;
                    }

                    if(this.outputAmount <= 0 && this.tank.isEmpty()){
                        this.recipe = null;
                        setStage(CokingState.STANDBY);

                        update = true;
                    }

                    if(update){
                        return true;
                    }
                }
            }

            return false;
        }
    }
}