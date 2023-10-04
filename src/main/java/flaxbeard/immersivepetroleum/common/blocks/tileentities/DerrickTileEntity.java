package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.client.gui.elements.PipeConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.stone.WellPipeBlock;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPCommonTickableTile;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.gui.IPMenuProvider;
import flaxbeard.immersivepetroleum.common.multiblocks.DerrickMultiblock;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author TwistedGate
 */
public class DerrickTileEntity extends PoweredMultiblockBlockEntity<DerrickTileEntity, MultiblockRecipe> implements IPCommonTickableTile, IPMenuProvider<DerrickTileEntity>, IEBlockInterfaces.IBlockBounds{
	
	@Deprecated public static FluidStack WATER = FluidStack.EMPTY;
	@Deprecated public static FluidStack CONCRETE = FluidStack.EMPTY;
	
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
	public static final BlockPos Fluid_IN = new BlockPos(2, 0, 4);
	
	/** Template-Location of the Fluid Output Port. (4 0 2)<br> */
	public static final BlockPos Fluid_OUT = new BlockPos(4, 0, 2);
	
	/** Template-Location of the Energy Input Ports.<br><pre>2 1 0</pre><br> */
	public static final Set<MultiblockFace> Energy_IN = ImmutableSet.of(new MultiblockFace(2, 1, 0, RelativeBlockFace.UP));
	
	/** Template-Location of the Redstone Input Port. (0 1 1)<br> */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 1));
	
	public final FluidTank tank = new FluidTank(8000, this::acceptsFluid);
	
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	public boolean drilling, spilling;
	private int clientFlow;
	public int timer = 0;
	
	private Fluid fluidSpilled = Fluids.EMPTY;
	
	/** Stores the current derrick configuration. */
	@Nullable
	public PipeConfig.Grid gridStorage;
	
	public DerrickTileEntity(BlockEntityType<DerrickTileEntity> type, BlockPos pWorldPosition, BlockState pBlockState){
		super(DerrickMultiblock.INSTANCE, 16000, true, type, pWorldPosition, pBlockState);
	}
	
	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		
		this.drilling = nbt.getBoolean("drilling");
		this.spilling = nbt.getBoolean("spilling");
		this.clientFlow = nbt.getInt("flow");
		this.timer = nbt.getInt("timer");
		
		try{
			this.fluidSpilled = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("spillingfluid")));
		}catch(ResourceLocationException rle){
			this.fluidSpilled = Fluids.EMPTY;
		}
		
		this.tank.readFromNBT(nbt.getCompound("tank"));
		
		if(nbt.contains("grid", Tag.TAG_COMPOUND)){
			this.gridStorage = PipeConfig.Grid.fromCompound(nbt.getCompound("grid"));
		}
		
		if(!descPacket && !this.isDummy()){
			ContainerHelper.loadAllItems(nbt, this.inventory);
			this.setChanged();
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		
		nbt.putBoolean("drilling", this.drilling);
		nbt.putBoolean("spilling", this.spilling);
		nbt.putInt("timer", this.timer);
		
		nbt.putString("spillingfluid", this.fluidSpilled.getRegistryName().toString());
		nbt.putInt("flow", getReservoirFlow());
		
		nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));
		
		if(this.gridStorage != null){
			nbt.put("grid", this.gridStorage.toCompound());
		}
		
		if(!descPacket && !this.isDummy()){
			ContainerHelper.saveAllItems(nbt, this.inventory);
		}
	}
	
	// Only accept as much Concrete and Water as needed
	@SuppressWarnings("deprecation")
	private boolean acceptsFluid(FluidStack fs){
		WellTileEntity well = getOrCreateWell(false);
		if(well == null){
			return false;
		}
		
		int realPipeLength = (getBlockPos().getY() - 1) - well.getBlockPos().getY();
		int concreteNeeded = (REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
		if(concreteNeeded > 0 && fs.getFluid().is(IPTags.Fluids.concrete)){
			FluidStack tankFluidStack = this.tank.getFluid();
			
			if(fs.getFluid() == tankFluidStack.getFluid() && tankFluidStack.getAmount() >= concreteNeeded){
				return false;
			}
			
			return concreteNeeded >= fs.getAmount();
		}
		
		if(concreteNeeded <= 0){
			int waterNeeded = REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
			if(waterNeeded > 0 && fs.getFluid().is(IPTags.Fluids.water)){
				FluidStack tankFluidStack = this.tank.getFluid();
				
				if(fs.getFluid() == tankFluidStack.getFluid() && tankFluidStack.getAmount() >= waterNeeded){
					return false;
				}
				
				return waterNeeded >= fs.getAmount();
			}
		}
		
		return false;
	}
	
	public int rotation = 0;
	
	@Override
	public void tickClient(){
		// Drilling Particles
		if(this.drilling){
			this.rotation += 9;
			this.rotation %= 360;
			
			for(int i = 0;i < 10;i++){
				float rx = (this.level.random.nextFloat() - .5F) * 1.5F;
				float rz = (this.level.random.nextFloat() - .5F) * 1.5F;
				
				if(!(rx > -0.625 && rx < 0.625) || !(rz > -0.625 && rz < 0.625)){
					float xa = (this.level.random.nextFloat() - .5F) / 16;
					float ya = 0.01F * this.level.random.nextFloat();
					float za = (this.level.random.nextFloat() - .5F) / 16;
					
					double x = (this.worldPosition.getX() + 0.5) + rx;
					double y = (this.worldPosition.getY() + 1.625) + this.level.random.nextFloat();
					double z = (this.worldPosition.getZ() + 0.5) + rz;
					
					this.level.addParticle(this.level.random.nextFloat() < 0.5F ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE, x, y, z, xa, ya, za);
				}
			}
		}
		
		if(this.spilling){
			ClientProxy.spawnSpillParticles(level, this.worldPosition, this.fluidSpilled, 5, 15.75F, this.clientFlow);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tickServer(){
		if(isDummy())
			return;
		
		if(this.level.isAreaLoaded(this.getBlockPos(), 2)){
			boolean forceUpdate = false;
			boolean lastDrilling = this.drilling;
			boolean lastSpilling = this.spilling;
			this.drilling = this.spilling = false;
			
			if(this.worldPosition.getY() < this.level.getSeaLevel()){
				// Refuse to work below this height to avoid cheesing the system.
				// Also displays in the GUI as to "why"
				
				if(this.fluidSpilled == Fluids.EMPTY){
					this.fluidSpilled = Fluids.WATER;
				}
				this.spilling = true;
			}else{
				WellTileEntity well = getOrCreateWell(getInventory(Inventory.INPUT) != ItemStack.EMPTY);
				if(!isRSDisabled()){
					if(this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), true) >= IPServerConfig.EXTRACTION.derrick_consumption.get()){
						
						if(well != null){
							if(well.wellPipeLength < well.getMaxPipeLength()){
								if(well.pipes <= 0 && getInventory(Inventory.INPUT) != ItemStack.EMPTY){
									ItemStack stack = getInventory(Inventory.INPUT);
									if(stack.getCount() > 0){
										stack.shrink(1);
										well.pipes = WellTileEntity.PIPE_WORTH;
										
										if(stack.getCount() <= 0){
											setInventory(Inventory.INPUT, ItemStack.EMPTY);
										}
										
										well.setChanged();
										forceUpdate = true;
									}
								}
								
								if(well.pipes > 0){
									final BlockPos dPos = getBlockPos();
									final BlockPos wPos = well.getBlockPos();
									int realPipeLength = ((dPos.getY() - 1) - wPos.getY());
									
									if(well.phyiscalPipesList.size() < realPipeLength && well.wellPipeLength < realPipeLength){
										if(this.tank.drain(REQUIRED_CONCRETE_AMOUNT, FluidAction.SIMULATE).getAmount() >= REQUIRED_CONCRETE_AMOUNT){
											this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);
											
											if(advanceTimer()){
												Level world = getLevelNonnull();
												int y = dPos.getY() - 1;
												for(;y > wPos.getY();y--){
													BlockPos current = new BlockPos(dPos.getX(), y, dPos.getZ());
													BlockState state = world.getBlockState(current);
													
													if(state.getBlock() == Blocks.BEDROCK || state.getBlock() == IPContent.Blocks.WELL.get()){
														break;
													}else if(!(state.getBlock() == IPContent.Blocks.WELL_PIPE.get() && !state.getValue(WellPipeBlock.BROKEN))){
														world.destroyBlock(current, false);
														world.setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
														
														well.phyiscalPipesList.add(y);
														
														this.tank.drain(REQUIRED_CONCRETE_AMOUNT, FluidAction.EXECUTE);
														
														well.usePipe();
														break;
													}
												}
												
												if(well.phyiscalPipesList.size() >= realPipeLength && well.wellPipeLength >= realPipeLength){
													well.pastPhysicalPart = true;
													well.setChanged();
												}
											}
											
											forceUpdate = true;
											this.drilling = true;
										}
									}else{
										if(!this.tank.getFluid().isEmpty() && this.tank.getFluid().getFluid() == CONCRETE.getFluid()){
											// FIXME ! This happens every now and then, and i have not yet nailed down HOW this happens.
											// Void excess concrete.
											this.tank.drain(this.tank.getFluidAmount(), FluidAction.EXECUTE);
											forceUpdate = true;
										}
										if(this.tank.drain(REQUIRED_WATER_AMOUNT, FluidAction.SIMULATE).getAmount() >= REQUIRED_WATER_AMOUNT){
											this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);
											
											if(advanceTimer()){
												restorePhysicalPipeProgress(dPos, realPipeLength);
												
												this.tank.drain(REQUIRED_WATER_AMOUNT, FluidAction.EXECUTE);
												well.usePipe();
											}
											
											forceUpdate = true;
											this.drilling = true;
										}
									}
								}
							}
						}
					}
				}
				if(well != null && well.wellPipeLength == well.getMaxPipeLength()) outputReservoirFluid();
			}
			
			if(forceUpdate || (lastDrilling != this.drilling) || (lastSpilling != this.spilling) || (Math.abs(getReservoirFlow() - this.clientFlow) > 0.075 * this.clientFlow)){
				updateMasterBlock(null, true);
				setChanged();
			}
		}
	}
	
	/** Only returns true if the timer reached zero */
	private boolean advanceTimer(){
		if(this.timer-- <= 0){
			this.timer = 10;
			return true;
		}
		return false;
	}
	
	private int getReservoirFlow() {
		return ReservoirHandler.getIsland(getLevelNonnull(), getBlockPos()) == null || this.worldPosition.getY() < getLevelNonnull().getSeaLevel() ? 10 :
			   ReservoirIsland.getFlow(ReservoirHandler.getIsland(getLevelNonnull(), getBlockPos()).getPressure(getLevelNonnull(), getBlockPos().getX(), getBlockPos().getZ()));
	}
	
	/** May end up being removed */
	public void restorePhysicalPipeProgress(BlockPos dPos, int realPipeLength){
		if(this.wellCache == null){
			return;
		}
		
		int min = Math.min(this.wellCache.wellPipeLength, realPipeLength);
		for(int i = 1;i < min;i++){
			BlockPos current = new BlockPos(dPos.getX(), dPos.getY() - i, dPos.getZ());
			BlockState state = getLevelNonnull().getBlockState(current);
			if(state.getBlock() != IPContent.Blocks.WELL_PIPE.get()){
				getLevelNonnull().destroyBlock(current, false);
				getLevelNonnull().setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
			}
		}
	}
	
	private void outputReservoirFluid(){
		WellTileEntity well = getOrCreateWell(true);
		if(well == null){
			return;
		}
		
		FluidStack extracted = getExtractedFluidStack(well);
		if(!extracted.isEmpty()){
			Direction facing = getIsMirrored() ? getFacing().getCounterClockWise() : getFacing().getClockWise();
			BlockPos outPos = getBlockPosForPos(Fluid_OUT).relative(facing, 1);
			IFluidHandler output = FluidUtil.getFluidHandler(this.level, outPos, facing.getOpposite()).orElse(null);
			if(output != null){
				FluidStack fluid = FluidHelper.copyFluid(extracted, extracted.getAmount());
				
				int accepted = output.fill(fluid, FluidAction.SIMULATE);
				if(accepted > 0){
					boolean iePipe = this.level.getBlockEntity(outPos) instanceof IFluidPipe;
					
					int drained = output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted), iePipe), FluidAction.EXECUTE);
					if(fluid.getAmount() - drained > 0){
						this.spilling = true;
					}
				}else{
					this.spilling = true;
				}
			}else{
				this.spilling = true;
			}
		}
		
		if(this.spilling && !extracted.isEmpty() && this.fluidSpilled != extracted.getFluid()){
			this.fluidSpilled = extracted.getFluid();
		}
		if(!this.spilling && this.fluidSpilled != Fluids.EMPTY){
			this.fluidSpilled = Fluids.EMPTY;
		}
	}
	
	private FluidStack getExtractedFluidStack(@Nonnull WellTileEntity well){
		Fluid extractedFluid = Fluids.EMPTY;
		int extractedAmount = 0;
		for(ColumnPos cPos:well.tappedIslands){
			ReservoirIsland island = ReservoirHandler.getIsland(this.level, cPos);
			if(island != null){
				if(extractedFluid == Fluids.EMPTY){
					extractedFluid = island.getFluid();
				}else if(island.getFluid() != extractedFluid){
					continue;
				}
				
				extractedAmount += island.extractWithPressure(getLevelNonnull(), cPos.x, cPos.z);
			}
		}
		
		return new FluidStack(extractedFluid, extractedAmount);
	}
	
	private WellTileEntity wellCache = null;
	/**
	 * Create or Get the {@link WellTileEntity}.
	 * 
	 * @param popList Set to true, to try and populate the {@link WellTileEntity#tappedIslands} list.
	 * @return WellTileEntity or possibly null
	 */
	public WellTileEntity getOrCreateWell(boolean popList){
		if(this.wellCache != null && this.wellCache.isRemoved()){
			this.wellCache = null;
		}
		
		if(this.wellCache == null){
			Level world = this.getLevelNonnull();
			WellTileEntity well = null;
			
			for(int y = getBlockPos().getY() - 1;y >= world.getMinBuildHeight();y--){
				BlockPos current = new BlockPos(this.getBlockPos().getX(), y, this.getBlockPos().getZ());
				BlockState state = world.getBlockState(current);
				
				if(state.getBlock() == IPContent.Blocks.WELL.get()){
					well = (WellTileEntity) world.getBlockEntity(current);
					break;
				}else if(state.getBlock() == Blocks.BEDROCK){
					world.setBlockAndUpdate(current, IPContent.Blocks.WELL.get().defaultBlockState());
					well = (WellTileEntity) world.getBlockEntity(current);
					break;
				}
			}
			
			this.wellCache = well;
		}
		
		if(popList && this.wellCache != null && this.wellCache.tappedIslands.isEmpty()){
			if(this.gridStorage != null){
				transferGridDataToWell(this.wellCache);
			}else{
				this.wellCache.tappedIslands.add(new ColumnPos(this.worldPosition.getX(), this.worldPosition.getZ()));
				this.wellCache.setChanged();
			}
		}
		
		if(this.wellCache != null){
			this.wellCache.abortSelfDestructSequence();
		}
		
		return this.wellCache;
	}
	
	/** Only gets the well if it exists, does not attempt to create it. May return null. */
	@Nullable
	public WellTileEntity getWell(){
		if(this.wellCache != null && this.wellCache.isRemoved()){
			this.wellCache = null;
		}
		
		if(this.wellCache == null){
			Level world = this.getLevelNonnull();
			WellTileEntity well = null;
			
			for(int y = getBlockPos().getY() - 1;y >= world.getMinBuildHeight();y--){
				BlockPos current = new BlockPos(this.getBlockPos().getX(), y, this.getBlockPos().getZ());
				BlockState state = world.getBlockState(current);
				
				if(state.getBlock() == IPContent.Blocks.WELL.get()){
					well = (WellTileEntity) world.getBlockEntity(current);
					break;
				}
			}
			
			this.wellCache = well;
		}
		
		return this.wellCache;
	}
	
	public void transferGridDataToWell(@Nullable WellTileEntity well){
		if(well != null){
			int additionalPipes = 0;
			List<ColumnPos> list = new ArrayList<>();
			PipeConfig.Grid grid = this.gridStorage;
			for(int j = 0;j < grid.getHeight();j++){
				for(int i = 0;i < grid.getWidth();i++){
					int type = grid.get(i, j);
					
					if(type > 0){
						switch(type){
							case PipeConfig.PIPE_PERFORATED:
							case PipeConfig.PIPE_PERFORATED_FIXED:{
								int x = i - (grid.getWidth() / 2);
								int z = j - (grid.getHeight() / 2);
								ColumnPos pos = new ColumnPos(this.worldPosition.getX() + x, this.worldPosition.getZ() + z);
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
	
	@Override
	public void disassemble(){
		if(this.formed && !this.level.isClientSide){
			// Do this even if this is the master itself
			DerrickTileEntity master = master();
			
			Level world = master.getLevelNonnull();
			BlockPos dPos = master.getBlockPos();
			for(int y = getBlockPos().getY() - 1;y >= world.getMinBuildHeight();y--){
				BlockPos current = new BlockPos(dPos.getX(), y, dPos.getZ());
				BlockEntity teLow = world.getBlockEntity(current);
				
				if(teLow instanceof WellTileEntity well){
					
					if(!well.drillingCompleted){
						if(well.wellPipeLength > 0){
							well.startSelfDestructSequence();
						}else{
							world.setBlockAndUpdate(current, Blocks.BEDROCK.defaultBlockState());
						}
						break;
					}
				}
			}
		}
		
		// Calling it after just to be on the safe side
		super.disassemble();
	}
	
	public ItemStack getInventory(Inventory inv){
		return this.inventory.get(inv.id());
	}
	
	public ItemStack setInventory(Inventory inv, ItemStack stack){
		return this.inventory.set(inv.id(), stack);
	}
	
	@Override
	public DerrickTileEntity getGuiMaster(){
		return master();
	}
	
	@Nonnull
	@Override
	public BEContainerIP<? super DerrickTileEntity, ?> getContainerTypeIP(){
		return IPMenuTypes.DERRICK;
	}
	
	@Override
	public boolean canUseGui(@Nonnull Player player){
		return this.formed;
	}
	
	/** Locations that don't require sneaking to avoid the GUI */
	public boolean skipGui(BlockHitResult hit){
		Direction facing = getFacing();
		
		// Power input
		if(DistillationTowerTileEntity.Energy_IN.stream().anyMatch((t) -> t.posInMultiblock() == this.posInMultiblock) && hit.getDirection() == Direction.UP){
			return true;
		}
		
		// Redstone controller input
		if(DerrickTileEntity.Redstone_IN.contains(posInMultiblock) && (getIsMirrored() ? hit.getDirection() == facing.getClockWise() : hit.getDirection() == facing.getCounterClockWise())){
			return true;
		}
		
		return false;
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return this.inventory;
	}
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack){
		return true;
	}
	
	@Override
	public int getSlotLimit(int slot){
		return 64;
	}
	
	@Override
	public void doGraphicalUpdates(){
		updateMasterBlock(null, true);
	}
	
	@Override
	protected MultiblockRecipe getRecipeForId(Level level, ResourceLocation id){
		return null;
	}
	
	@Override
	public Set<MultiblockFace> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return new IFluidTank[]{this.tank};
	}
	
	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	public int[] getOutputSlots(){
		return new int[0];
	}
	
	@Override
	public int[] getOutputTanks(){
		return new int[0];
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process){
		return false;
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process){
	}
	
	@Override
	public int getMaxProcessPerTick(){
		return 1;
	}
	
	@Override
	public int getProcessQueueMaxLength(){
		return 1;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process){
		return 0;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	private final MultiblockCapability<IFluidHandler> fluidInputHandler = MultiblockCapability.make(
			this, be -> be.fluidInputHandler, DerrickTileEntity::master, registerFluidInput(tank)
	);
	private final ResettableCapability<IFluidHandler> dummyTank = registerFluidOutput(DUMMY_TANK);
	
	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction side){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			if(posInMultiblock.equals(Fluid_IN) && (side == null || side == getFacing().getOpposite())){
				return fluidInputHandler.getAndCast();
			}
			if(this.posInMultiblock.equals(Fluid_OUT)){
				if(side == null || (getIsMirrored() ? side == getFacing().getCounterClockWise() : side == getFacing().getClockWise())){
					return dummyTank.cast();
				}
			}
		}
		return super.getCapability(capability, side);
	}
	
	public boolean isLadder(){
		int x = posInMultiblock.getX();
		int y = posInMultiblock.getY();
		int z = posInMultiblock.getZ();
		
		// Primary Ladder
		if((x == 0 && z == 2) && (y >= 0 && y <= 8)){
			return true;
		}
		
		// Secondary Ladder
		if((x == 1 && z == 2) && (y >= 9 && y <= 14)){
			return true;
		}
		
		return false;
	}
	
	private static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(DerrickTileEntity::getShape);
	@Override
	@Nonnull
	public VoxelShape getBlockBounds(CollisionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AABB> getShape(BlockPos posInMultiblock){
		int x = posInMultiblock.getX();
		int y = posInMultiblock.getY();
		int z = posInMultiblock.getZ();
		
		List<AABB> main = new ArrayList<>();
		
		// Base
		if(y == 0){
			if(!(x == 2 && z == 4 || x == 4 && z == 2 || x == 2 && z == 0 || x == 2 && z == 2)){
				main.add(box(0, 0, 0, 16, 8, 16));
			}
		}
		
		// Platform 1 & 2
		if((y == 8 || y == 14) && !(x == 2 && z == 2 || x == 0 && z == 2 || x == 1 && z == 2)){
			main.add(box(0, 8, 0, 16, 16, 16));
		}
		
		if(y == 0 || y == 1){
			// Power Box
			if(z == 0){
				if(x == 1){
					main.add(box(4, 0, 0, 16, 16, 16));
				}else if(x == 3){
					main.add(box(0, 0, 0, 12, 16, 16));
				}
			}
			
			// Fluid Input Box
			if(x == 1 && z == 4){
				main.add(box(8, 8, 0, 16, 24, 16));
			}else if(x == 3 && z == 4){
				main.add(box(0, 8, 0, 8, 24, 16));
			}
			if(y == 1 && x == 2 && z == 4){
				main.add(box(-8, 0, 0, 24, 8, 16));
			}
		}
		
		// Center Pipe and Stuff
		if(x == 2 && z == 2 && y >= 0 && y <= 13){
			if(y == 0){
				main.add(box(-4, 8, -4, 20, 16, 20));
				main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
			}else if(y > 1){
				// Pipe
				main.add(box(4, 0, 4, 12, 16, 12));
			}
			
			if(y == 1){
				main.add(box(-4, 0, -4, 20, 8, 20));
				main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
			}
			if(y == 2){
				main.add(box(0, 0, 0, 16, 8, 16));
			}
		}
		
		// Primary Ladder
		if(x == 0 && z == 2 && (y >= 1 && y <= 8)){
			if(y == 1){
				main.add(box(15, 8, 2, 16, 16, 14));
			}else{
				main.add(box(15, 0, 2, 16, 16, 14));
			}
			
			if(y > 2){
				main.add(box(0, 0, 0, 1, 16, 16));
				main.add(box(0, 0, 0, 16, 16, 1));
				main.add(box(0, 0, 15, 16, 16, 16));
			}
		}
		// Secondary Ladder
		if((x == 1 && z == 2) && (y >= 9 && y <= 14)){
			if(y == 9){
				main.add(box(15, 8, 2, 16, 16, 14));
			}else{
				main.add(box(15, 0, 2, 16, 16, 14));
			}
			
			if(y > 10){
				main.add(box(0, 0, 0, 1, 16, 16));
				main.add(box(0, 0, 0, 16, 16, 1));
				main.add(box(0, 0, 15, 16, 16, 16));
			}
		}
		
		// Supports: from Base to Platform 1
		if(y >= 0 && y <= 8){
			// Corners
			if(x == 0 && z == 0){
				main.add(box(9 + y, 0, 9 + y, 15 + y, 16, 15 + y));
			}else if(x == 4 && z == 0){
				main.add(box(1 - y, 0, 9 + y, 7 - y, 16, 15 + y));
			}else if(x == 0 && z == 4){
				main.add(box(9 + y, 0, 1 - y, 15 + y, 16, 7 - y));
			}else if(x == 4 && z == 4){
				main.add(box(1 - y, 0, 1 - y, 7 - y, 16, 7 - y));
			}
			
			// Centers
			if(y >= 3){
				if(x == 2 && z == 0){
					main.add(box(6, 0, 9 + y, 10, 16, 14 + y));
				}else if(x == 2 && z == 4){
					main.add(box(6, 0, 2 - y, 10, 16, 7 - y));
				}else if(x == 4 && z == 2){
					main.add(box(2 - y, 0, 6, 7 - y, 16, 10));
				}
			}
			
			// First horizontal bars, above base
			if(y == 3){
				if(x >= 1 && x <= 3){
					if(z == 0){
						main.add(box(0, -4, 12, 16, 0, 16));
					}else if(z == 4){
						main.add(box(0, -4, 0, 16, 0, 4));
					}
				}
				if(z >= 1 && z <= 3){
					if(x == 0 && z != 2){
						main.add(box(12, -4, 0, 16, 0, 16));
					}else if(x == 4){
						main.add(box(0, -4, 0, 4, 0, 16));
					}
				}
			}
			
			// Second horizontal bars, below Platform 1
			if(y == 6){
				if(x >= 1 && x <= 3){
					if(z == 0){
						main.add(box(0, 4, 16, 16, 8, 20));
					}else if(z == 4){
						main.add(box(0, 4, -4, 16, 8, 0));
					}
				}
				if(z >= 1 && z <= 3){
					if(x == 0){
						main.add(box(16, 4, 0, 20, 8, 16));
					}else if(x == 4){
						main.add(box(-4, 4, 0, 0, 8, 16));
					}
				}
			}
		}
		
		// Supports: From Platform 1 to Platform 2, up to the very top
		if(y >= 9 && y <= 16){
			double off = y - 9;
			
			// Corners
			if(x == 1 && z == 1){
				main.add(box(2 + off, 0, 2 + off, 7 + off, 16, 7 + off));
			}else if(x == 3 && z == 1){
				main.add(box(9 - off, 0, 2 + off, 14 - off, 16, 7 + off));
			}else if(x == 1 && z == 3){
				main.add(box(2 + off, 0, 9 - off, 7 + off, 16, 14 - off));
			}else if(x == 3 && z == 3){
				main.add(box(9 - off, 0, 9 - off, 14 - off, 16, 14 - off));
			}
			
			// Centers
			if(x == 2 && z == 1){
				main.add(box(6, 0, 2 + off, 10, 16, 7 + off));
			}else if(x == 2 && z == 3){
				main.add(box(6, 0, 9 - off, 10, 16, 14 - off));
			}else if(x == 3 && z == 2){
				main.add(box(10 - off, 0, 6, 14, 16 - off, 10));
			}
			
			// Third horizontal bars, above platform 1
			if(y == 9){
				if(x == 3){
					if(z == 1){
						main.add(box(9, 12, 7, 13, 16, 16));
					}else if(z == 2){
						main.add(box(9, 12, 0, 13, 16, 16));
					}else if(z == 3){
						main.add(box(9, 12, 0, 13, 16, 9));
					}
				}
				
				if(z == 1){
					if(x == 1){
						main.add(box(7, 12, 3, 16, 16, 7));
					}else if(x == 2){
						main.add(box(0, 12, 3, 16, 16, 7));
					}else if(x == 3){
						main.add(box(0, 12, 3, 9, 16, 7));
					}
				}else if(z == 3){
					if(x == 1){
						main.add(box(7, 12, 9, 16, 16, 13));
					}else if(x == 2){
						main.add(box(0, 12, 9, 16, 16, 13));
					}else if(x == 3){
						main.add(box(0, 12, 9, 9, 16, 13));
					}
				}
			}
			
			// Fourth horizontal bars, below platform 2
			if(y == 13){
				if(x == 3){
					if(z == 1){
						main.add(box(5, 4, 11, 9, 8, 16));
					}else if(z == 2){
						main.add(box(5, 4, 0, 9, 8, 16));
					}else if(z == 3){
						main.add(box(5, 4, 0, 9, 8, 5));
					}
				}
				
				if(z == 1){
					if(x == 1){
						main.add(box(11, 4, 7, 16, 8, 11));
						main.add(box(7, 4, 11, 11, 8, 16));
					}else if(x == 2){
						main.add(box(0, 4, 7, 16, 8, 11));
					}else if(x == 3){
						main.add(box(0, 4, 7, 5, 8, 11));
					}
				}else if(z == 3){
					if(x == 1){
						main.add(box(11, 4, 5, 16, 8, 9));
						main.add(box(7, 4, 0, 11, 8, 5));
					}else if(x == 2){
						main.add(box(0, 4, 5, 16, 8, 9));
					}else if(x == 3){
						main.add(box(0, 4, 5, 5, 8, 9));
					}
				}
			}
			
			// Horizontal bars at the very top
			if(y == 16){
				if(z == 1){
					if(x == 1){
						main.add(box(9, 12, 9, 16, 16, 16));
					}else if(x == 2){
						main.add(box(0, 12, 9, 16, 16, 15));
					}else if(x == 3){
						main.add(box(0, 12, 9, 7, 16, 16));
					}
				}
				
				if(z == 2){
					if(x == 1){
						main.add(box(9, 12, 0, 15, 16, 16));
					}else if(x == 3){
						main.add(box(1, 12, 0, 7, 16, 16));
					}
				}
				
				if(z == 3){
					if(x == 1){
						main.add(box(9, 12, 0, 16, 16, 7));
					}else if(x == 2){
						main.add(box(0, 12, 1, 16, 16, 7));
					}else if(x == 3){
						main.add(box(0, 12, 0, 7, 16, 7));
					}
				}
			}
		}
		
		// Use default cube shape if nessesary
		if(main.isEmpty()){
			main.add(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
		}
		return main;
	}
	
	/** Makes a box using texture pixel space (Assuming 16x16 p texture) */
	private static AABB box(double x0, double y0, double z0, double x1, double y1, double z1){
		return new AABB(x0 / 16D, y0 / 16D, z0 / 16D, x1 / 16D, y1 / 16D, z1 / 16D);
	}
}
