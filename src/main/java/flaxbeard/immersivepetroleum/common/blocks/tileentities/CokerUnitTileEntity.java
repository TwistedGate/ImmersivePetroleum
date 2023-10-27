package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.IPMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPCommonTickableTile;
import flaxbeard.immersivepetroleum.common.gui.IPMenuProvider;
import flaxbeard.immersivepetroleum.common.multiblocks.CokerUnitMultiblock;
import flaxbeard.immersivepetroleum.common.util.AABBUtils;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class CokerUnitTileEntity extends PoweredMultiblockBlockEntity<CokerUnitTileEntity, CokerUnitRecipe> implements IPCommonTickableTile, IPMenuProvider<CokerUnitTileEntity>, IEBlockInterfaces.IBlockBounds{
	
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
	public static final BlockPos Chamber_A_OUT = new BlockPos(2, 0, 2);
	
	/** Template-Location of the Chamber B Item Output */
	public static final BlockPos Chamber_B_OUT = new BlockPos(6, 0, 2);
	
	/** Template-Location of the Fluid Input Port. (2 0 4)<br> */
	public static final BlockPos Fluid_IN = new BlockPos(2, 0, 4);
	
	/** Template-Location of the Fluid Output Port. (5 0 4)<br> */
	public static final BlockPos Fluid_OUT = new BlockPos(5, 0, 4);
	
	/** Template-Location of the Item Input Port. (3 0 4)<br> */
	public static final BlockPos Item_IN = new BlockPos(3, 0, 4);
	
	/** Template-Location of the Energy Input Ports.<br><pre>1 1 0<br>2 1 0<br>3 1 0</pre><br> */
	public static final Set<MultiblockFace> Energy_IN = ImmutableSet.of(
			new MultiblockFace(6, 1, 4, RelativeBlockFace.FRONT),
			new MultiblockFace(7, 1, 4, RelativeBlockFace.FRONT)
	);
	
	/** Template-Location of the Redstone Input Port. (6 1 4)<br> */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(1, 1, 4));
	
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(Inventory.values().length, ItemStack.EMPTY);
	public final FluidTank[] bufferTanks = {new FluidTank(16000), new FluidTank(16000)};
	public final CokingChamber[] chambers = {new CokingChamber(64, 8000), new CokingChamber(64, 8000)};
	public CokerUnitTileEntity(BlockEntityType<CokerUnitTileEntity> type, BlockPos pWorldPosition, BlockState pBlockState){
		super(CokerUnitMultiblock.INSTANCE, 24000, true, type, pWorldPosition, pBlockState);
		this.bufferTanks[TANK_INPUT].setValidator(fs -> CokerUnitRecipe.hasRecipeWithInput(fs, true));
	}
	
	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		
		this.bufferTanks[TANK_INPUT].readFromNBT(nbt.getCompound("tank0"));
		this.bufferTanks[TANK_OUTPUT].readFromNBT(nbt.getCompound("tank1"));
		
		this.chambers[CHAMBER_A].readFromNBT(nbt.getCompound("chamber0"));
		this.chambers[CHAMBER_B].readFromNBT(nbt.getCompound("chamber1"));
		
		if(!descPacket){
			readInventory(nbt.getCompound("inventory"));
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		
		nbt.put("tank0", this.bufferTanks[TANK_INPUT].writeToNBT(new CompoundTag()));
		nbt.put("tank1", this.bufferTanks[TANK_OUTPUT].writeToNBT(new CompoundTag()));
		
		nbt.put("chamber0", this.chambers[CHAMBER_A].writeToNBT(new CompoundTag()));
		nbt.put("chamber1", this.chambers[CHAMBER_B].writeToNBT(new CompoundTag()));
		
		if(!descPacket){
			nbt.put("inventory", writeInventory(this.inventory));
		}
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
	
	@Override
	public void doGraphicalUpdates(){
		updateMasterBlock(null, true);
	}
	
	@Override
	public CokerUnitRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<CokerUnitRecipe> process){
		return false;
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
	}
	
	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, CokerUnitTileEntity::master,
			new ResettableCapability<>(new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[8]))
	);
	private final MultiblockCapability<IFluidHandler> fluidOutHandler = MultiblockCapability.make(
			this, be -> be.fluidOutHandler, CokerUnitTileEntity::master,
			registerFluidOutput(this.bufferTanks[TANK_OUTPUT])
	);
	private final MultiblockCapability<IFluidHandler> fluidInHandler = MultiblockCapability.make(
			this, be -> be.fluidInHandler, CokerUnitTileEntity::master,
			registerFluidInput(this.bufferTanks[TANK_INPUT])
	);
	
	@Override
	@Nonnull
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction facing){
		if((facing == null || this.posInMultiblock.equals(Item_IN)) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return this.insertionHandler.getAndCast();
		}else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			if(this.posInMultiblock.equals(Fluid_OUT) && (facing == null || facing == getFacing().getOpposite())){
				return this.fluidOutHandler.getAndCast();
			}else if(this.posInMultiblock.equals(Fluid_IN) && (facing == null || facing == getFacing().getOpposite())){
				return this.fluidInHandler.getAndCast();
			}
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<CokerUnitRecipe> process){
	}
	
	@Override
	public void tickClient(){
		if(isDummy() || isRSDisabled()){
			return;
		}
		
		boolean debug = false;
		for(int i = 0;i < this.chambers.length;i++){
			if(debug || this.chambers[i].getState() == CokingState.DUMPING){
				BlockPos cOutPos = getBlockPosForPos(i == 0 ? Chamber_A_OUT : Chamber_B_OUT);
				Vec3 origin = new Vec3(cOutPos.getX() + 0.5, cOutPos.getY() + 2.125, cOutPos.getZ() + 0.5);
				for(int j = 0;j < 10;j++){
					double rX = (Math.random() - 0.5) * 0.4;
					double rY = (Math.random() - 0.5) * 0.5;
					double rdx = (Math.random() - 0.5) * 0.10;
					double rdy = (Math.random() - 0.5) * 0.10;
					
					this.level.addParticle(ParticleTypes.SMOKE,
							origin.x + rX, origin.y, origin.z + rY,
							rdx, -(Math.random() * 0.06 + 0.11), rdy);
				}
			}
		}
	}
	
	@Override
	public void tickServer(){
		if(isDummy()){
			return;
		}
		
		super.tickServer();
		
		boolean update = false;
		
		if(!isRSDisabled()){
			ItemStack inputStack = getInventory(Inventory.INPUT);
			FluidStack inputFluid = this.bufferTanks[TANK_INPUT].getFluid();
			
			if(!inputStack.isEmpty() && inputFluid.getAmount() > 0 && CokerUnitRecipe.hasRecipeWithInput(inputStack, inputFluid)){
				CokerUnitRecipe recipe = CokerUnitRecipe.findRecipe(inputStack, inputFluid);
				
				if(recipe != null && inputStack.getCount() >= recipe.inputItem.getCount() && inputFluid.getAmount() >= recipe.inputFluid.getAmount()){
					for(CokingChamber chamber:this.chambers){
						boolean skipNext = false;
						
						switch(chamber.getState()){
							case STANDBY -> {
								if(chamber.setRecipe(recipe)){
									update = true;
									skipNext = true;
								}
							}
							case PROCESSING -> {
								int acceptedStack = chamber.addStack(copyStack(inputStack, recipe.inputItem.getCount()), true);
								if(acceptedStack >= recipe.inputItem.getCount()){
									acceptedStack = Math.min(acceptedStack, inputStack.getCount());
									
									chamber.addStack(copyStack(inputStack, acceptedStack), false);
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
			
			for(int i = 0;i < this.chambers.length;i++){
				update |= this.chambers[i].tick(this, i);
			}
		}
		
		if(!getInventory(Inventory.INPUT_FILLED).isEmpty() && this.bufferTanks[TANK_INPUT].getFluidAmount() < this.bufferTanks[TANK_INPUT].getCapacity()){
			ItemStack container = Utils.drainFluidContainer(this.bufferTanks[TANK_INPUT], getInventory(Inventory.INPUT_FILLED), getInventory(Inventory.INPUT_EMPTY));
			if(!container.isEmpty()){
				if(!getInventory(Inventory.INPUT_EMPTY).isEmpty() && ItemHandlerHelper.canItemStacksStack(getInventory(Inventory.INPUT_EMPTY), container)){
					getInventory(Inventory.INPUT_EMPTY).grow(container.getCount());
				}else if(getInventory(Inventory.INPUT_EMPTY).isEmpty()){
					setInventory(Inventory.INPUT_EMPTY, container.copy());
				}
				
				getInventory(Inventory.INPUT_FILLED).shrink(1);
				if(getInventory(Inventory.INPUT_FILLED).getCount() <= 0){
					setInventory(Inventory.INPUT_FILLED, ItemStack.EMPTY);
				}
				
				update = true;
			}
		}
		
		if(this.bufferTanks[TANK_OUTPUT].getFluidAmount() > 0){
			if(!getInventory(Inventory.OUTPUT_EMPTY).isEmpty()){
				ItemStack filledContainer = FluidHelper.fillFluidContainer(this.bufferTanks[TANK_OUTPUT], getInventory(Inventory.OUTPUT_EMPTY), getInventory(Inventory.OUTPUT_FILLED), null);
				if(!filledContainer.isEmpty()){
					
					if(getInventory(Inventory.OUTPUT_FILLED).getCount() == 1 && !FluidHelper.isFluidContainerFull(filledContainer)){
						setInventory(Inventory.OUTPUT_FILLED, filledContainer.copy());
					}else{
						if(!getInventory(Inventory.OUTPUT_FILLED).isEmpty() && ItemHandlerHelper.canItemStacksStack(getInventory(Inventory.OUTPUT_FILLED), filledContainer)){
							getInventory(Inventory.OUTPUT_FILLED).grow(filledContainer.getCount());
						}else if(getInventory(Inventory.OUTPUT_FILLED).isEmpty()){
							setInventory(Inventory.OUTPUT_FILLED, filledContainer.copy());
						}
						
						getInventory(Inventory.OUTPUT_EMPTY).shrink(1);
						if(getInventory(Inventory.OUTPUT_EMPTY).getCount() <= 0){
							setInventory(Inventory.OUTPUT_EMPTY, ItemStack.EMPTY);
						}
					}
					
					update = true;
				}
			}
			
			BlockPos outPos = getBlockPosForPos(Fluid_OUT).relative(getFacing().getOpposite());
			update |= FluidUtil.getFluidHandler(this.level, outPos, getFacing()).map(out -> {
				if(this.bufferTanks[TANK_OUTPUT].getFluidAmount() > 0){
					FluidStack fs = this.bufferTanks[TANK_OUTPUT].getFluid();
					fs = FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), 250));
					int accepted = out.fill(fs, FluidAction.SIMULATE);
					if(accepted > 0){
						boolean iePipe = this.level.getBlockEntity(outPos) instanceof IFluidPipe;
						int drained = out.fill(FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), accepted), iePipe), FluidAction.EXECUTE);
						this.bufferTanks[TANK_OUTPUT].drain(FluidHelper.copyFluid(fs, drained), FluidAction.EXECUTE);
						return true;
					}
				}
				return false;
			}).orElse(false);
		}
		
		if(update){
			updateMasterBlock(null, true);
		}
		
		updateComparatorOutput();
	}
	
	int updateDelay = 0;
	int lastCompared = 0;
	private void updateComparatorOutput(){
		boolean update = false;
		
		ItemStack stack = getInventory(Inventory.INPUT);
		if(!stack.isEmpty()){
			int compared = Mth.clamp(Mth.floor(stack.getCount() / (float) Math.min(getSlotLimit(Inventory.INPUT.id()), stack.getMaxStackSize()) * 15), 0, 15);
			if(compared != this.lastCompared){
				this.lastCompared = compared;
				update = true;
			}
		}else if(lastCompared != 0){
			this.lastCompared = 0;
			update = true;
		}
		
		if(update){
			getRedstonePos().forEach(pos -> {
				BlockPos p = getBlockPosForPos(pos);
				this.level.updateNeighborsAt(p, this.level.getBlockState(p).getBlock());
			});
		}
	}
	
	@Override
	public int getComparatorInputOverride(){
		if(this.isRedstonePos()){
			CokerUnitTileEntity master = master();
			if(master != null){
				return master.lastCompared;
			}
		}
		return 0;
	}
	
	private ItemStack copyStack(ItemStack stack, int amount){
		ItemStack copy = stack.copy();
		copy.setCount(amount);
		return copy;
	}
	
	public ItemStack getInventory(Inventory inv){
		return this.inventory.get(inv.id());
	}
	
	public ItemStack setInventory(Inventory inv, ItemStack stack){
		return this.inventory.set(inv.id(), stack);
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return this.inventory;
	}
	
	@Override
	public int getSlotLimit(int slot){
		return 64;
	}
	
	@Override
	protected CokerUnitRecipe getRecipeForId(Level level, ResourceLocation id){
		return CokerUnitRecipe.recipes.get(id);
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
		return this.bufferTanks;
	}
	
	@Override
	public int[] getOutputSlots(){
		return new int[0];
	}
	
	@Override
	public int[] getOutputTanks(){
		return new int[]{TANK_OUTPUT};
	}
	
	@Override
	public int getMaxProcessPerTick(){
		return 1;
	}
	
	@Override
	public int getProcessQueueMaxLength(){
		return 2;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<CokerUnitRecipe> process){
		return 1.0F;
	}
	
	@Override
	public int getComparatedSize(){
		return 1;
	}
	
	@Override
	public CokerUnitTileEntity getGuiMaster(){
		return master();
	}
	
	@Override
	public boolean canUseGui(@Nonnull Player player){
		return this.formed;
	}
	
	/** Locations that don't require sneaking to avoid the GUI */
	public boolean skipGui(BlockHitResult hit){
		Direction facing = getFacing();
		
		// Conveyor locations
		if(this.posInMultiblock.getY() == 0 && this.posInMultiblock.getZ() == 2 && hit.getDirection() == Direction.UP){
			return true;
		}
		
		// All power input sockets
		if(this.getEnergyPos().stream().anyMatch((t) -> t.posInMultiblock() == this.posInMultiblock) && hit.getDirection() == facing){
			return true;
		}
		
		// Redstone controller input
		if(this.getRedstonePos().contains(this.posInMultiblock) && hit.getDirection() == facing.getOpposite()){
			return true;
		}
		
		// Fluid I/O Ports
		if(this.posInMultiblock.equals(CokerUnitTileEntity.Fluid_IN) || this.posInMultiblock.equals(CokerUnitTileEntity.Fluid_OUT)){
			return true;
		}
		
		// Item input port
		if(this.posInMultiblock.equals(CokerUnitTileEntity.Item_IN)){
			return true;
		}
		
		return false;
	}
	
	@Nonnull
	@Override
	public BEContainerIP<? super CokerUnitTileEntity, ?> getContainerTypeIP(){
		return IPMenuTypes.COKER;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack){
		return true;
	}
	
	public boolean isLadder(){
		int bX = posInMultiblock.getX();
		int bY = posInMultiblock.getY();
		int bZ = posInMultiblock.getZ();
		
		// Primary Ladder
		if((bX == 8 && bZ == 2) && (bY >= 5 && bY <= 13)){
			return true;
		}
		
		// Secondary Ladder
		if((bX == 7 && bZ == 2) && (bY >= 15 && bY <= 21)){
			return true;
		}
		
		return false;
	}
	
	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(CokerUnitTileEntity::getShape);
	
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
		
		if((y >= 15 && y <= 22) && z == 2 && (x == 2 || x == 6)){
			// The two vertical pipes at the very top
			AABBUtils.box16(main, 4, 0, 4, 12, 16, 12);
		}
		
		{ // Catwalk Railings
			if(y == 22 || y == 18){
				if(x == 1){
					AABBUtils.box16(main, 0, 0, 0, 1, 16, 16); // West Plate
				}
				if(x == 7){
					AABBUtils.box16(main, 15, 0, 0, 16, 16, 16); // East Plate
				}
				if(z == 1){
					AABBUtils.box16(main, 0, 0, 0, 16, 16, 1); // South Plate
				}
				if(z == 3){
					AABBUtils.box16(main, 0, 0, 15, 16, 16, 16); // North Plate
				}
			}
			if(y == 14 || y == 9){
				if(x >= 0 && x <= 8){
					if(z == 0){
						AABBUtils.box16(main, 0, 0, 0, 16, 16, 1); // South Plate
					}
					if(z == 4){
						AABBUtils.box16(main, 0, 0, 15, 16, 16, 16); // North Plate
					}
					
					if(x == 8 && (z == 0 || z == 4)){
						AABBUtils.box16(main, 15, 0, 0, 16, 16, 16); // East Plate
					}
					
					if(x == 0 && (z == 0 || z == 4)){
						AABBUtils.box16(main, 0, 0, 0, 1, 16, 16); // West Plate
					}
				}
			}
		}
		
		// Catwalk
		{
			// Lower 2
			{
				if(y == 8 || y == 13){
					if(!(x >= 1 && x <= 3 && z >= 1 && z <= 3) && !(x >= 5 && x <= 7 && z >= 1 && z <= 3)){
						if(!(x == 8 && z == 2)){
							AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
						}
					}
				}
			}
			
			// Upper 2
			{
				if(y == 17 || y == 21){
					if(x >= 1 && x <= 7 && z >= 1 && z <= 3){
						if(!((x == 2 || x == 6 || x == 7) && z == 2)){
							AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
						}
					}
				}
			}
		}
		
		// Support Beams
		{
			// Vertical Beams
			boolean lower = y >= 4 && y <= 13;
			boolean upper = y >= 14 && y <= 21;
			if(lower || upper){
				// Corners
				if((lower && x == 0 && z == 0) || (upper && ((x == 1 && z == 1) || (x == 5 && z == 1)))){
					AABBUtils.box16(main, 2, 0, 2, 6, 16, 6);
				}
				if((lower && x == 0 && z == 4) || (upper && ((x == 1 && z == 3) || (x == 5 && z == 3)))){
					AABBUtils.box16(main, 2, 0, 10, 6, 16, 14);
				}
				if((lower && x == 8 && z == 0) || (upper && ((x == 3 && z == 1) || (x == 7 && z == 1)))){
					AABBUtils.box16(main, 10, 0, 2, 14, 16, 6);
				}
				if((lower && x == 8 && z == 4) || (upper && ((x == 3 && z == 3) || (x == 7 && z == 3)))){
					AABBUtils.box16(main, 10, 0, 10, 14, 16, 14);
				}
				
				// Middle
				if(lower && x == 4 && z == 0){
					AABBUtils.box16(main, 6, 0, 2, 10, 16, 6);
				}
				if(lower && x == 4 && z == 4){
					AABBUtils.box16(main, 6, 0, 10, 10, 16, 14);
				}
			}
			
			// Horizontal Beams
			if(y == 6 || y == 11){
				//AABBUtils.box16(main, 0, 2, 0, 16, 6, 16);
				if(x >= 0 && x <= 8){
					if(z == 0){
						double xa = (x == 0) ? 2 : 0;
						double xb = (x == 8) ? 14 : 16;
						AABBUtils.box16(main, xa, 2, 2, xb, 6, 6);
					}
					if(z == 4){
						double xa = (x == 0) ? 2 : 0;
						double xb = (x == 8) ? 14 : 16;
						AABBUtils.box16(main, xa, 2, 10, xb, 6, 14);
					}
				}
				if(z >= 0 && z <= 4){
					if(x == 0){
						double za = (z == 0) ? 2 : 0;
						double zb = (z == 4) ? 14 : 16;
						AABBUtils.box16(main, 2, 2, za, 6, 6, zb);
					}
					if(x == 8 && z != 2){
						double za = (z == 0) ? 2 : 0;
						double zb = (z == 4) ? 14 : 16;
						AABBUtils.box16(main, 10, 2, za, 14, 6, zb);
					}
				}
			}
		}
		
		{ // Pipes
			
			// Up, Down, North, South, East, West Connection
			boolean u = false, d = false, n = false, s = false, e = false, w = false;
			
			// Straight & T Pipes
			{
				// Vertical, Horizontal-X, Horizontal-Z
				boolean v = false, hX = false, hZ = false;
				
				if(((y >= 1 && y <= 7 && y != 3) && x == 2 && z == 4) || ((y >= 9 && y <= 13) && x == 0 && z == 2) || (y == 1 && x == 5 && z == 4)){
					v = true;
					
					if(y == 2 || y == 7 || (x == 5 && z == 4)){
						u = true;
					}
					if(y == 1 || y == 4 || y == 9){
						d = true;
					}
				}
				
				if(y == 0){
					if(x == 3 && z == 3){
						hZ = n = s = true;
					}
				}
				
				if(y == 1){
					if(x == 4 && z == 2){
						v = u = d = true;
					}
				}
				if(y == 2){
					if(x == 3 && z == 3){
						hX = w = true;
					}
					if(x == 4 && z == 3){
						hX = e = true;
					}
					if((x == 3 || x == 5) && z == 2){
						hX = e = w = true;
					}
					if(x == 4 && z == 2){
						hX = e = w = d = true;
						AABBUtils.box16(main, 4, 2, 4, 12, 4, 12);
					}
					if(x == 5 && z == 3){
						hX = e = w = s = true;
						AABBUtils.box16(main, 4, 4, 12, 12, 12, 14);
					}
				}
				if(y == 8){
					if(x == 1 && z == 4){
						hX = e = w = true;
					}
					if(x == 0 && z == 3){
						hZ = n = s = true;
					}
				}
				
				if(v) AABBUtils.box16(main, 4, 0, 4, 12, 16, 12); // Vertical Pipe
				if(hX) AABBUtils.box16(main, 0, 4, 4, 16, 12, 12); // Horizontal-X Pipe
				if(hZ) AABBUtils.box16(main, 4, 4, 0, 12, 12, 16); // Horizontal-Z Pipe
			}
			
			// 90° Bends
			{
				if(y == 0){
					if(x == 4 && z == 2){
						u = w = true;
						AABBUtils.box16(main, 2, 4, 4, 12, 14, 12);
					}
					if(x == 3 && z == 2){
						e = s = true;
						AABBUtils.box16(main, 4, 4, 4, 14, 12, 14);
					}
				}
				if(y == 2){
					if(x == 2 && z == 3){
						n = e = true;
						AABBUtils.box16(main, 4, 4, 2, 14, 12, 12);
					}
					if(x == 5 && z == 4){
						d = n = true;
						AABBUtils.box16(main, 4, 2, 2, 12, 12, 12);
					}
					if(x == 6 && z == 3){
						w = n = true;
						AABBUtils.box16(main, 2, 4, 2, 12, 12, 12);
					}
				}
				if(y == 8){
					if(x == 0 && z == 2){
						u = s = true;
						AABBUtils.box16(main, 4, 4, 4, 12, 14, 14);
					}
					if(x == 0 && z == 4){
						n = e = true;
						AABBUtils.box16(main, 4, 4, 2, 14, 12, 12);
					}
					if(x == 2 && z == 4){
						d = w = true;
						AABBUtils.box16(main, 2, 2, 4, 12, 12, 12);
					}
				}
			}
			
			if(u) AABBUtils.box16(main, 2, 14, 2, 14, 16, 14); // Top Connection
			if(d) AABBUtils.box16(main, 2, 0, 2, 14, 2, 14); // Bottom Connection
			if(n) AABBUtils.box16(main, 2, 2, 0, 14, 14, 2); // North Connection
			if(s) AABBUtils.box16(main, 2, 2, 14, 14, 14, 16); // South Connection
			if(e) AABBUtils.box16(main, 14, 2, 2, 16, 14, 14); // East Connection
			if(w) AABBUtils.box16(main, 0, 2, 2, 2, 14, 14); // West Connection
		}
		
		// Redstone Controller
		if(x == 1 && z == 4){
			if(y == 0){ // Bottom
				AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
				AABBUtils.box16(main, 12, 0, 10, 14, 16, 14);
				AABBUtils.box16(main, 2, 0, 10, 4, 16, 14);
			}
			if(y == 1){ // Top
				AABBUtils.box16(main, 0, 0, 8, 16, 16, 16);
			}
		}
		
		// Power Sockets
		if(y == 1 && z == 4 && (x == 6 || x == 7)){
			AABBUtils.box16(main, 0, 0, 0, 16, 16, 14);
			AABBUtils.box16(main, 4, 4, 14, 12, 12, 16);
		}
		
		// Power Cables
		if((x == 6 && y == 2 && z == 4) || (y >= 4 && y <= 13 && x == 4 && z == 2)){
			AABBUtils.box16(main, 5, 0, 5, 11, 16, 11);
		}
		
		// Bitumen input Sign
		if(y == 1 && x == 3 && z == 4){
			AABBUtils.box16(main, 0, 15, 14, 16, 17, 16);
			AABBUtils.box16(main, 1, 1, 14, 15, 15, 15);
		}
		
		// Primary Ladder
		if((x == 8 && z == 2) && (y >= 5 && y <= 13)){
			AABBUtils.box16(main, 0, 0, 2, 1, 16, 14); // West Plate
		}
		
		// Secondary Ladder
		if((x == 7 && z == 2) && (y >= 15 && y <= 21)){
			AABBUtils.box16(main, 0, 0, 2, 1, 16, 14); // West Plate
		}
		
		// Baseplate
		if(y == 0){
			if(!((z == 0 || z == 4) && (x == 0 || x == 4 || x == 8)) && !(z == 4 && (x == 2 || x == 3 || (x >= 5 && x <= 7)))){
				AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
			}
		}
		
		// Chambers
		{
			if(y >= 4 && y <= 13){
				if(x == 1 || x == 5){
					if(z == 1) List.of(AABBUtils.box16(1, 0, 1, 16, 16, 16));
					if(z == 2) List.of(AABBUtils.box16(1, 0, 0, 16, 16, 16));
					if(z == 3) List.of(AABBUtils.box16(1, 0, 0, 16, 16, 15));
				}
				if(x == 2 || x == 6){
					if(z == 1) List.of(AABBUtils.box16(0, 0, 1, 16, 16, 16));
					if(z == 3) List.of(AABBUtils.box16(0, 0, 0, 16, 16, 15));
				}
				if(x == 3 || x == 7){
					if(z == 1) List.of(AABBUtils.box16(0, 0, 1, 15, 16, 16));
					if(z == 2) List.of(AABBUtils.box16(0, 0, 0, 15, 16, 16));
					if(z == 3) List.of(AABBUtils.box16(0, 0, 0, 15, 16, 15));
				}
			}
		}
		
		// Fences
		{
			if(y == 0 || y == 1){
				if(z >= 1 && z <= 3){
					if(x == 0){
						AABBUtils.box16(main, 6, 0, 0, 7, 16, 16);
					}
					if(x == 8){
						AABBUtils.box16(main, 9, 0, 0, 10, 16, 16);
					}
				}
				if(x >= 1 && x <= 3 || x >= 5 && x <= 7){
					if(z == 0){
						AABBUtils.box16(main, 0, 0, 6, 16, 16, 7);
					}
				}
			}
		}
		
		// Use default cube shape if nessesary
		if(main.isEmpty()){
			main.add(AABBUtils.FULL);
		}
		return main;
	}
	
	// STATIC CLASSES
	
	public enum CokingState{
		/** Wait for Input */
		STANDBY,
		
		/** Process materials into the result */
		PROCESSING,
		
		/** Draining residual fluids from processing materials */
		DRAIN_RESIDUE,
		
		/** Filling up the chamber with fluid, with the amount required by the recipe */
		FLOODING,
		
		/** Dumping the result below the chamber outout and voiding the flushing fluids */
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
			nbt.putInt("state", state.id());
			
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
			
			return this.recipe.outputItem.get().copy();
		}
		
		public FluidTank getTank(){
			return this.tank;
		}
		
		/** returns true when the coker should update, false otherwise */
		public boolean tick(CokerUnitTileEntity cokerunit, int chamberId){
			if(this.recipe == null){
				return setStage(CokingState.STANDBY);
			}
			
			switch(this.state){
				case STANDBY -> {
					if(this.recipe != null){
						return setStage(CokingState.PROCESSING);
					}
				}
				case PROCESSING -> {
					if(this.inputAmount > 0 && !getInputItem().isEmpty() && (this.tank.getCapacity() - this.tank.getFluidAmount()) >= this.recipe.outputFluid.getAmount()){
						if(cokerunit.energyStorage.getEnergyStored() >= this.recipe.getTotalProcessEnergy()/this.recipe.getTotalProcessTime()){
							cokerunit.energyStorage.extractEnergy(this.recipe.getTotalProcessEnergy()/this.recipe.getTotalProcessTime(), false);
							
							this.timer++;
							if(this.timer >= (this.recipe.getTotalProcessTime() * this.recipe.inputItem.getCount())){
								this.timer = 0;
								
								this.tank.fill(Utils.copyFluidStackWithAmount(this.recipe.outputFluid, this.recipe.outputFluid.getAmount(), false), FluidAction.EXECUTE);
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
						FluidTank buffer = cokerunit.bufferTanks[TANK_OUTPUT];
						FluidStack drained = this.tank.drain(25, FluidAction.SIMULATE);
						
						int accepted = buffer.fill(drained, FluidAction.SIMULATE);
						if(accepted > 0){
							int amount = Math.min(drained.getAmount(), accepted);
							
							this.tank.drain(amount, FluidAction.EXECUTE);
							buffer.fill(Utils.copyFluidStackWithAmount(drained, amount, false), FluidAction.EXECUTE);
							
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
							FluidStack accepted = cokerunit.bufferTanks[TANK_INPUT].drain(this.recipe.inputFluid.getAmount(), FluidAction.SIMULATE);
							if(accepted.getAmount() >= this.recipe.inputFluid.getAmount()){
								cokerunit.bufferTanks[TANK_INPUT].drain(this.recipe.inputFluid.getAmount(), FluidAction.EXECUTE);
								this.tank.fill(accepted, FluidAction.EXECUTE);
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
							Level world = cokerunit.getLevelNonnull();
							int amount = Math.min(this.outputAmount, 1);
							ItemStack copy = this.recipe.outputItem.get().copy();
							copy.setCount(amount);
							
							// Drop item(s) at the designated chamber output location
							BlockPos itemOutPos = cokerunit.getBlockPosForPos(chamberId == 0 ? Chamber_A_OUT : Chamber_B_OUT);
							Vec3 center = new Vec3(itemOutPos.getX() + 0.5, itemOutPos.getY() - 0.5, itemOutPos.getZ() + 0.5);
							ItemEntity ent = new ItemEntity(cokerunit.getLevelNonnull(), center.x, center.y, center.z, copy);
							ent.setDeltaMovement(0.0, 0.0, 0.0); // Any movement has the potential to end with the stack bouncing all over the place
							world.addFreshEntity(ent);
							this.outputAmount -= amount;
							
							update = true;
						}
					}
					
					// Void washing fluid
					if(this.tank.getFluidAmount() > 0){
						this.tank.drain(25, FluidAction.EXECUTE);
						
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
